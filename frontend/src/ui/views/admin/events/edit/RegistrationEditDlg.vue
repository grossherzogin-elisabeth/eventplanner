<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung bearbeiten</h1>
        </template>
        <template #default>
            <div v-if="registration" class="p-8 lg:px-16">
                <p class="mb-8 max-w-lg">
                    Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut
                    labore et dolore magna aliquyam erat, sed diam voluptua.
                </p>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Nutzer</VInputLabel>
                    <VInputCombobox v-model="registration.userKey" :options="userOptions" disabled />
                </div>
                <div v-if="!registration.userKey" class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText v-model="registration.name" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Position</VInputLabel>
                    <VInputCombobox v-model="registration.positionKey" :options="positionOptions" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Notiz</VInputLabel>
                    <VInputTextArea v-model="registration.note" />
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="!isValid" @click="submit">
                <span>Speichern</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { ObjectUtils } from '@/common';
import type { Registration, User } from '@/domain';
import { type InputSelectOption, type Position, type PositionKey } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';

const usersUseCase = useUsersUseCase();

const dlg = ref<Dialog<Registration, Registration> | null>(null);
const users = ref<User[]>([]);
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const registration = ref<Registration | null>(null);

const userOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = users.value.map((it) => ({
        label: `${it.firstName} ${it.lastName}`,
        value: it.key,
    }));
    options.unshift({ label: 'Gastcrew', value: undefined });
    return options;
});

const positionOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = [...positions.value.values()].map((it) => ({
        label: it.name,
        value: it.key,
    }));
    return options;
});

const isValid = computed<boolean>(() => {
    return (
        registration.value?.positionKey !== undefined &&
        (registration.value?.name !== undefined || registration.value?.userKey !== undefined)
    );
});

async function init(): Promise<void> {
    await fetchUsers();
    await fetchPositions();
}

async function fetchUsers(): Promise<void> {
    users.value = await usersUseCase.getUsers();
}

async function fetchPositions(): Promise<void> {
    positions.value = await usersUseCase.resolvePositionNames();
}

async function open(eventRegistration: Registration): Promise<Registration> {
    registration.value = ObjectUtils.deepCopy(eventRegistration);
    // wait until user submits
    await dlg.value?.open();
    eventRegistration.positionKey = registration.value.positionKey;
    return eventRegistration;
}

defineExpose<Dialog<Registration, Registration>>({
    open: (eventRegistration: Registration) => open(eventRegistration),
    close: () => dlg.value?.reject(),
    submit: (result: Registration) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
