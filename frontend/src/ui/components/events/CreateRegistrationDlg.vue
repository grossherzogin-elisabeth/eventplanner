<template>
    <VDialog ref="dlg" data-test-id="add-registration-dialog">
        <template #title>Anmeldung hinzufügen</template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <p class="mb-8 max-w-lg">
                        Du kannst eine Anmeldung für ein Stammcrew Mitglied oder Gastcrew anlegen. Die Position kannst du nachträglich noch
                        auf der Warteliste ändern.
                    </p>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="registration.userKey"
                            label="Nutzer"
                            required
                            :options="userOptions"
                            :errors="validation.errors.value['userKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div v-if="!registration.userKey" class="mb-4">
                        <VInputText
                            v-model="registration.name"
                            required
                            label="Name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="registration.positionKey"
                            label="Position"
                            required
                            :options="positions.options.value"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                        >
                            <template #item="{ item }">
                                <span class="grow">{{ item.label }}</span>
                                <i
                                    v-if="selectedUser && item.value && !selectedUser.positionKeys?.includes(item.value)"
                                    class="fa-solid fa-warning text-warning mr-4"
                                />
                            </template>
                        </VInputCombobox>
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model="registration.note"
                            label="Notiz"
                            :errors="validation.errors.value['note']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputCheckBox
                            v-model="registration.overnightStay"
                            :errors="validation.errors.value['overnightStay']"
                            :errors-visible="validation.showErrors.value"
                            label="Übernachtung an Bord"
                        />
                    </div>
                    <div v-if="registration.overnightStay" class="mb-4">
                        <VInputDate
                            v-model="registration.arrival"
                            label="Anreise am"
                            :errors="validation.errors.value['arrival']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Am ersten Tag der Reise"
                        />
                    </div>
                    <template v-if="selectedUser !== undefined">
                        <VWarning
                            v-if="registration.positionKey && !selectedUser?.positionKeys?.includes(registration.positionKey)"
                            class="my-4"
                        >
                            {{ selectedUser?.nickName || selectedUser?.firstName }} hat keine Qualifikation für die Position
                            <i>{{ selectedPosition?.name }}</i>
                        </VWarning>
                        <VWarning v-if="expiredQualifications.length > 0" class="my-4">
                            Die folgenden Qualificationen sind abgelaufen oder laufen vor Ende der Reise ab:
                            <ul class="mt-2 ml-4 list-disc">
                                <li v-for="quali in expiredQualifications" :key="quali">{{ quali }}</li>
                            </ul>
                        </VWarning>
                    </template>
                    <VWarning v-else-if="registration.name" class="my-4">
                        {{ registration.name }} ist Gastcrew. Die Gültigkeit der Qualifikationen kann daher nicht automatisiert geprüft
                        werden.
                    </VWarning>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-ghost" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ props.submitText || 'Übernehmen' }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useUsersUseCase } from '@/application';
import { filterUndefined } from '@/common';
import { Validator, notEmpty } from '@/common/validation';
import type { Event, InputSelectOption, Position, Registration, User } from '@/domain';
import { useUserService } from '@/domain/services.ts';
import type { Dialog } from '@/ui/components/common';
import { VInputDate } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VWarning } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputText, VInputTextArea } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQualifications } from '@/ui/composables/Qualifications.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { v4 as uuid } from 'uuid';

interface Props {
    submitText?: string;
}

const props = defineProps<Props>();

const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const positions = usePositions();
const qualifications = useQualifications();

const dlg = ref<Dialog<Event[], Registration | undefined> | null>(null);
const users = ref<User[]>([]);
const registration = ref<Registration>({
    key: uuid(),
    positionKey: '',
    userKey: undefined,
    name: undefined,
});
const hiddenUsers = ref<string[]>([]);
let date: Date = new Date();

const validation = useValidation(registration, (value) => {
    const errors = Validator.validate('positionKey', value.positionKey, notEmpty()).getErrors();
    if (!value.name && !value.userKey) {
        errors.userKey = errors.userKey || [];
        errors.userKey.push('Bitte wähle eine Stammcrew Mitglied');
    }
    return errors;
});

const userOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = users.value
        .filter((it) => !hiddenUsers.value.includes(it.key))
        .map((it) => ({
            label: `${it.firstName} ${it.lastName}`,
            value: it.key,
        }));
    options.unshift({ label: 'Gastcrew', value: undefined });
    return options;
});

const selectedUser = computed<User | undefined>(() => {
    if (!registration.value?.userKey) {
        return undefined;
    }
    return users.value.find((u) => u.key === registration.value?.userKey);
});

const selectedPosition = computed<Position | undefined>(() => {
    return positions.get(registration.value?.positionKey);
});

const expiredQualifications = computed<string[]>(() => {
    if (!selectedUser.value) {
        return [];
    }
    return usersService
        .getExpiredQualifications(selectedUser.value, date)
        .map((key) => qualifications.get(key))
        .filter(filterUndefined)
        .map((qualification) => qualification.name);
});

async function init(): Promise<void> {
    await fetchUsers();
    watch(selectedUser, () => {
        if (selectedUser.value && !registration.value.positionKey) {
            registration.value.positionKey = selectedUser.value.positionKeys?.[0] ?? '';
        }
    });
}

async function fetchUsers(): Promise<void> {
    users.value = await usersUseCase.getUsers();
}

async function open(value: Event[]): Promise<Registration | undefined> {
    validation.reset();
    registration.value = {
        key: uuid(),
        positionKey: '',
        userKey: undefined,
        name: undefined,
        note: '',
        overnightStay: true,
        arrival: undefined,
    };
    date = value.map((it) => it.end).sort((a, b) => b.getTime() - a.getTime())[0] || new Date();
    if (value.length === 1) {
        hiddenUsers.value = value[0].registrations.map((it) => it.userKey).filter(filterUndefined);
    } else {
        hiddenUsers.value = [];
    }

    // wait until user submits
    return await dlg.value?.open().catch(() => undefined);
}

function submit(): void {
    if (validation.isValid.value) {
        if (registration.value.userKey) {
            registration.value.name = undefined;
        }
        if (!registration.value.overnightStay) {
            registration.value.arrival = undefined;
        }
        dlg.value?.submit(registration.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Event[], Registration | undefined>>({
    open: (value: Event[]) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result: Registration) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});

init();
</script>
