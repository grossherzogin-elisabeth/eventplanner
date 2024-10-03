<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung hinzufügen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <p class="mb-8 max-w-lg">
                    Du kannst eine Anmeldung für ein Stammcrew Mitglied oder Gastcrew anlegen. Die Position kannst du
                    nachträglich noch auf der Warteliste ändern.
                </p>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Nutzer</VInputLabel>
                    <VInputCombobox v-model="registration.userKey" :options="userOptions" />
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
                <span>Anmeldung hinzufügen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { ArrayUtils } from '@/common';
import type { Event, Registration, User } from '@/domain';
import { type InputSelectOption, type Position, type PositionKey } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';

const usersUseCase = useUsersUseCase();

const dlg = ref<Dialog<Event, Event> | null>(null);
const users = ref<User[]>([]);
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const registration = ref<Registration>({
    key: '',
    positionKey: '',
    userKey: undefined,
    name: undefined,
});
const hiddenUsers = ref<string[]>([]);

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

const positionOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = [...positions.value.values()]
        .sort((a, b) => b.prio - a.prio)
        .map((it) => ({
            label: it.name,
            value: it.key,
        }));
    return options;
});

const isValid = computed<boolean>(() => {
    return (
        (registration.value.name !== undefined || registration.value.userKey !== undefined) &&
        registration.value.positionKey !== undefined
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

async function open(event: Event): Promise<Event> {
    registration.value = {
        key: '',
        positionKey: '',
        userKey: undefined,
        name: undefined,
    };
    hiddenUsers.value = event.registrations.map((it) => it.userKey).filter(ArrayUtils.filterUndefined);

    // wait until user submits
    await dlg.value?.open();

    if (registration.value.userKey) {
        registration.value.name = undefined;
    }
    event.registrations.push(registration.value);
    return event;
}

defineExpose<Dialog<Event, Event>>({
    open: (event: Event) => open(event),
    close: () => dlg.value?.reject(),
    submit: (result: Event) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
