<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung bearbeiten</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Nutzer</VInputLabel>
                    <VInputCombobox
                        v-model="registration.userKey"
                        :options="userOptions"
                        disabled
                        :errors="validation.errors.value['userKey']"
                        :errors-visible="validation.showErrors.value"
                    />
                </div>
                <div v-if="!registration.userKey" class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="registration.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="validation.showErrors.value"
                    />
                </div>
                <div class="-mx-4 mb-4">
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
                                v-if="selectedUser && item.value && !selectedUser.positionKeys.includes(item.value)"
                                class="fa-solid fa-warning mr-4 text-yellow-500"
                            />
                        </template>
                    </VInputCombobox>
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Notiz</VInputLabel>
                    <VInputTextArea
                        v-model="registration.note"
                        :errors="validation.errors.value['note']"
                        :errors-visible="validation.showErrors.value"
                    />
                </div>
                <template v-if="selectedUser !== undefined">
                    <VWarning v-if="!selectedUser?.positionKeys.includes(registration.positionKey)" class="-mx-4 my-4">
                        {{ selectedUser?.nickName || selectedUser?.firstName }} hat keine Qualifikation für die Position
                        <i>{{ selectedPosition?.name }}</i>
                    </VWarning>
                    <VWarning v-if="expiredQualifications.length > 0" class="-mx-4 my-4">
                        Die folgenden Qualificationen sind abgelaufen oder laufen vor Ende der Reise ab:
                        <ul class="ml-4 mt-2 list-disc">
                            <li v-for="quali in expiredQualifications" :key="quali">{{ quali }}</li>
                        </ul>
                    </VWarning>
                </template>
                <VWarning v-else-if="registration.name" class="-mx-4 my-4">
                    {{ registration.name }} ist Gastcrew. Die Gültigkeit der Qualifikationen kann daher nicht
                    automatisiert geprüft werden.
                </VWarning>
            </div>
        </template>
        <template #buttons>
            <button class="btn-secondary" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Speichern</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { deepCopy, filterUndefined } from '@/common';
import type { Event, InputSelectOption, Position, Registration, User, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VWarning } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application.ts';
import { useUserService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQualifications } from '@/ui/composables/Qualifications.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { v4 as uuid } from 'uuid';

interface Props {
    event: Event;
}

const props = defineProps<Props>();

const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const positions = usePositions();
const qualifications = useQualifications();

const dlg = ref<Dialog<Registration, Registration | undefined> | null>(null);
const users = ref<User[]>([]);
const registration = ref<Registration>({
    key: uuid(),
    positionKey: '',
});
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
    const options: InputSelectOption<string | undefined>[] = users.value.map((it) => ({
        label: `${it.nickName || it.firstName} ${it.lastName}`,
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
    if (!registration.value?.positionKey) {
        return undefined;
    }
    return positions.get(registration.value.positionKey);
});

const expiredQualifications = computed<string[]>(() => {
    if (!selectedUser.value) {
        return [];
    }
    return usersService
        .getExpiredQualifications(selectedUser.value, props.event.end)
        .map((key) => qualifications.get(key))
        .filter(filterUndefined)
        .map((qualification) => qualification.name);
});

async function init(): Promise<void> {
    await fetchUsers();
}

async function fetchUsers(): Promise<void> {
    users.value = await usersUseCase.getUsers();
}

async function open(value: Registration): Promise<Registration | undefined> {
    validation.reset();
    registration.value = deepCopy(value);
    // wait until user submits
    const result = await dlg.value?.open().catch(() => undefined);
    if (!result) {
        return undefined;
    }
    if (!registration.value.userKey) {
        value.name = result.name;
    }
    value.positionKey = result.positionKey;
    value.note = result.note;
    return value;
}

function submit() {
    if (validation.isValid.value) {
        dlg.value?.submit(registration.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Registration, Registration | undefined>>({
    open: (eventRegistration: Registration) => open(eventRegistration),
    close: () => dlg.value?.reject(),
    submit: (result: Registration) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});

init();
</script>
