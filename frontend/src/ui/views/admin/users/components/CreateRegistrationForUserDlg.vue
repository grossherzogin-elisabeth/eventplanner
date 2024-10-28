<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung für {{ selectedUser?.firstName }} hinzufügen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <p class="mb-8 max-w-lg">
                    {{ selectedUser?.firstName }} wird zur Warteliste der ausgewählten Reise hinzugefügt. Wenn
                    {{ selectedUser?.firstName }} auch direkt zur Crew hinzugefügt werden soll, musst du die Reise noch
                    manuell bearbeiten.
                </p>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Reise</VInputLabel>
                    <VInputCombobox v-model="selectedEventKey" :options="eventOptions" />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Position</VInputLabel>
                    <VInputCombobox v-model="selectedPositionKey" :options="positionOptions" />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Notiz</VInputLabel>
                    <VInputTextArea />
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
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import type { Event, User } from '@/domain';
import { type InputSelectOption, type Position, type PositionKey } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputTextArea } from '@/ui/components/common';
import {
    useErrorHandling,
    useEventAdministrationUseCase,
    useEventUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application';

const usersUseCase = useUsersUseCase();
const eventsUseCase = useEventUseCase();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const errorHandling = useErrorHandling();
const i18n = useI18n();

const dlg = ref<Dialog<User> | null>(null);
const events = ref<Event[]>([]);
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const hiddenEvents = ref<string[]>([]);

const selectedUser = ref<User | null>(null);
const selectedEventKey = ref<string | null>(null);
const selectedPositionKey = ref<string | null>(null);

const eventOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = events.value
        .filter((it) => !hiddenEvents.value.includes(it.key))
        .map((it) => ({
            label: `${i18n.d(it.start, DateTimeFormat.DD_MM_YYYY)} - ${it.name}`,
            value: it.key,
        }));
    return options;
});

const positionOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = [...positions.value.values()]
        .filter((it) => selectedUser.value?.positionKeys.includes(it.key))
        .sort((a, b) => b.prio - a.prio)
        .map((it) => ({
            label: it.name,
            value: it.key,
        }));
    return options;
});

const isValid = computed<boolean>(() => {
    return selectedEventKey.value !== null && selectedPositionKey.value !== null;
});

async function init(): Promise<void> {
    await fetchEvents();
    await fetchPositions();
}

async function fetchEvents(): Promise<void> {
    events.value = await eventsUseCase.getFutureEvents();
}

async function fetchPositions(): Promise<void> {
    positions.value = await usersUseCase.resolvePositionNames();
}

async function open(user: User): Promise<void> {
    selectedUser.value = user;
    selectedPositionKey.value = user.positionKeys[0];

    const eventsByUser = await eventsUseCase.getFutureEventsByUser(user.key);
    hiddenEvents.value = eventsByUser.map((it) => it.key);

    dlg.value?.open().then(async () => {
        if (selectedEventKey.value && selectedPositionKey.value) {
            try {
                await eventAdministrationUseCase.addRegistration(selectedEventKey.value, {
                    key: '',
                    userKey: user.key,
                    positionKey: selectedPositionKey.value,
                });
            } catch (e) {
                errorHandling.handleRawError(e);
            }
        }
    });
}

defineExpose<Dialog<User, void>>({
    open: (user: User) => open(user),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(),
    reject: () => dlg.value?.reject(),
});

init();
</script>
