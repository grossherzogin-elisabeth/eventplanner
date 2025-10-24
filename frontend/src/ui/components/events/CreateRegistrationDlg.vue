<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung hinzufügen</h1>
        </template>
        <template #default>
            <div class="px-4 pt-4 xs:px-8 lg:px-10">
                <section>
                    <p class="mb-8 max-w-lg">
                        Du kannst eine Anmeldung für ein Stammcrew Mitglied oder Gastcrew anlegen. Die Position kannst du nachträglich noch
                        auf der Warteliste ändern.
                    </p>
                    <div class="mb-4">
                        <VInputLabel>Nutzer</VInputLabel>
                        <VInputCombobox
                            v-model="registration.userKey"
                            :options="userOptions"
                            :errors="validation.errors.value['userKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div v-if="!registration.userKey" class="mb-4">
                        <VInputLabel>Name</VInputLabel>
                        <VInputText
                            v-model="registration.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Position</VInputLabel>
                        <VInputCombobox
                            v-model="registration.positionKey"
                            :options="positions.options.value"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                        >
                            <template #item="{ item }">
                                <span class="flex-grow">{{ item.label }}</span>
                                <i
                                    v-if="selectedUser && item.value && !selectedUser.positionKeys?.includes(item.value)"
                                    class="fa-solid fa-warning mr-4 text-warning"
                                />
                            </template>
                        </VInputCombobox>
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Notiz</VInputLabel>
                        <VInputTextArea
                            v-model="registration.note"
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
                        <VInputLabel>Anreise am</VInputLabel>
                        <VInputDate
                            v-model="registration.arrival"
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
                            <ul class="ml-4 mt-2 list-disc">
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
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ props.submitText || 'Übernehmen' }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { filterUndefined } from '@/common';
import type { Event, InputSelectOption, Position, Registration, User, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputDate } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VWarning } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application.ts';
import { useUserService } from '@/ui/composables/Domain.ts';
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
    // TODO extract to service
    const errors: Record<string, ValidationHint[]> = {};
    if (!value.positionKey) {
        errors.positionKey = errors.positionKey || [];
        errors.positionKey.push({ key: 'Bitte wähle eine Position', params: {} });
    }
    if (!value.name && !value.userKey) {
        errors.userKey = errors.userKey || [];
        errors.userKey.push({ key: 'Bitte wähle eine Stammcrew Mitglied', params: {} });
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
