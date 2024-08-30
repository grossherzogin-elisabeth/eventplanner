<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Event erstellen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div class="-mx-4 mb-2">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText v-model="event.name" />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Kategorie</VInputLabel>
                    <VInputSelect
                        v-model="event.type"
                        :options="[
                            { value: EventType.WorkEvent, label: 'Arbeitsdienst' },
                            { value: EventType.SINGLE_DAY_VOYAGE, label: 'Tagesfahrt' },
                            { value: EventType.VOYAGE, label: 'Mehrtagesfahrt' },
                        ]"
                        required
                    />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Vorlage</VInputLabel>
                    <VInputCombobox
                        v-model="template"
                        :options="
                            templates
                                .filter((it) => it.type === event.type)
                                .map((it) => ({
                                    label: `${$d(it.start, DateTimeFormat.DD_MM_YYYY)} - ${it.name} `,
                                    value: it,
                                }))
                        "
                    />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Beschreibung</VInputLabel>
                    <VInputTextArea v-model="event.description" />
                </div>
                <div class="-mx-4 mb-2 flex space-x-4">
                    <div class="flex-grow">
                        <VInputLabel>Startdatum</VInputLabel>
                        <VInputDate v-model="event.start" required />
                    </div>
                    <div class="flex-grow">
                        <VInputLabel>Crew an Board</VInputLabel>
                        <VInputText model-value="16:00" required />
                    </div>
                </div>

                <div class="-mx-4 mb-2 flex space-x-4">
                    <div class="flex-grow">
                        <VInputLabel>Enddatum</VInputLabel>
                        <VInputDate v-model="event.end" required />
                    </div>
                    <div class="flex-grow">
                        <VInputLabel>Crew von Board</VInputLabel>
                        <VInputText model-value="16:00" required />
                    </div>
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" @click="submit">
                <span>Event erstellen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import { EventState, EventType } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import {
    VDialog,
    VInputCombobox,
    VInputDate,
    VInputLabel,
    VInputSelect,
    VInputText,
    VInputTextArea,
} from '@/ui/components/common';
import { useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application';

const eventAdministrationUseCase = useEventAdministrationUseCase();
const eventUseCase = useEventUseCase();

const dlg = ref<Dialog<Event> | null>(null);
const templates = ref<Event[]>([]);
const template = ref<Event | null>(null);
const event = ref<Event>({
    key: '',
    name: '',
    description: '',
    type: EventType.VOYAGE,
    state: EventState.Draft,
    start: new Date(),
    end: new Date(),
    locations: [],
    slots: [],
    registrations: [],
    assignedUserCount: 0,
});

async function init(): Promise<void> {
    await fetchTemplates(new Date().getFullYear());
}

async function fetchTemplates(year: number): Promise<void> {
    templates.value = await eventUseCase.getEvents(year);
    if (templates.value.length === 0) {
        templates.value = await eventUseCase.getEvents(year - 1);
    }
}

async function open(): Promise<Event> {
    await dlg.value?.open();
    event.value = await eventAdministrationUseCase.createEvent(event.value);
    return event.value;
}

defineExpose<Dialog<Event>>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: (result: Event) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
