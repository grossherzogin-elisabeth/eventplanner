<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Neue Reise erstellen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="event.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="validation.showErrors.value"
                        placeholder="Titel der Reise"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Kategorie</VInputLabel>
                    <VInputSelect
                        v-model="event.type"
                        :errors="validation.errors.value['type']"
                        :errors-visible="validation.showErrors.value"
                        :options="[
                            { value: EventType.WorkEvent, label: 'Arbeitsdienst' },
                            { value: EventType.SingleDayEvent, label: 'Tagesfahrt' },
                            { value: EventType.WeekendEvent, label: 'Wochenendreise' },
                            { value: EventType.MultiDayEvent, label: 'Mehrtagesfahrt' },
                        ]"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Vorlage</VInputLabel>
                    <VInputCombobox
                        v-model="template"
                        :errors="validation.errors.value['template']"
                        :errors-visible="validation.showErrors.value"
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
                <div class="-mx-4 mb-4">
                    <VInputLabel>Beschreibung</VInputLabel>
                    <VInputTextArea
                        v-model="event.description"
                        :errors="validation.errors.value['description']"
                        :errors-visible="validation.showErrors.value"
                        placeholder="Kurze Bschreibung oder Zusatzinformationen"
                    />
                </div>
                <div class="-mx-4 mb-4 flex space-x-4">
                    <div class="w-3/5">
                        <VInputLabel>Startdatum</VInputLabel>
                        <VInputDate
                            v-model="event.start"
                            :errors="validation.errors.value['start']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="w-2/5">
                        <VInputLabel>Crew an Board</VInputLabel>
                        <VInputTime
                            v-model="event.start"
                            :errors="validation.errors.value['start']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                </div>

                <div class="-mx-4 mb-4 flex space-x-4">
                    <div class="w-3/5">
                        <VInputLabel>Enddatum</VInputLabel>
                        <VInputDate
                            v-model="event.end"
                            :errors="validation.errors.value['end']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="w-2/5">
                        <VInputLabel>Crew von Board</VInputLabel>
                        <VInputTime
                            v-model="event.end"
                            :errors="validation.errors.value['end']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <i class="fa-solid fa-xmark"></i>
                <span>Abbrechen</span>
            </button>
            <AsyncButton
                class="btn-primary"
                :action="() => submitIfValid(submit)"
                :disabled="validation.disableSubmit.value"
            >
                <template #icon>
                    <i class="fa-solid fa-save"></i>
                </template>
                <template #label> Reise erstellen </template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy } from '@/common';
import { DateTimeFormat, cropToPrecision } from '@/common/date';
import type { Event } from '@/domain';
import { EventState, EventType } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputTime } from '@/ui/components/common';
import {
    VDialog,
    VInputCombobox,
    VInputDate,
    VInputLabel,
    VInputSelect,
    VInputText,
    VInputTextArea,
} from '@/ui/components/common';
import { useEventUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { useValidation } from '@/ui/composables/Validation';
import AsyncButton from '../common/buttons/AsyncButton.vue';

const eventUseCase = useEventUseCase();
const eventService = useEventService();

const dlg = ref<Dialog<Event, Event> | null>(null);
const templates = ref<Event[]>([]);
const template = ref<Event | null>(null);
const event = ref<Event>({
    key: '',
    name: '',
    description: '',
    type: EventType.WeekendEvent,
    state: EventState.Draft,
    start: new Date(),
    end: new Date(),
    locations: [],
    slots: [],
    registrations: [],
    assignedUserCount: 0,
    canSignedInUserJoin: false,
    canSignedInUserLeave: false,
});
const validation = useValidation(event, eventService.validate);

async function init(): Promise<void> {
    await fetchTemplates(new Date().getFullYear());
}

async function fetchTemplates(year: number): Promise<void> {
    templates.value = await eventUseCase.getEvents(year);
    if (templates.value.length === 0) {
        templates.value = await eventUseCase.getEvents(year - 1);
    }
}

async function submitIfValid(submitFun: () => void) {
    if (validation.isValid.value) {
        submitFun();
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

async function open(partialEvent?: Partial<Event>): Promise<Event> {
    validation.reset();
    event.value.name = partialEvent?.name || '';
    event.value.description = partialEvent?.description || '';
    event.value.locations = partialEvent?.locations || [];
    event.value.state = partialEvent?.state || EventState.Draft;
    event.value.type = partialEvent?.type || event.value.type || EventType.WeekendEvent;
    const start = cropToPrecision(new Date(partialEvent?.start?.getTime() || new Date().getTime()), 'days');
    start.setHours(16);
    event.value.start = start;
    const end = cropToPrecision(new Date(partialEvent?.end?.getTime() || new Date().getTime()), 'days');
    end.setHours(18);
    event.value.end = end;

    await dlg.value?.open();
    event.value.slots =
        template.value?.slots.map((slot) => ({
            key: slot.key,
            criticality: slot.criticality,
            positionKeys: slot.positionKeys,
            positionName: slot.positionName,
            order: slot.order,
        })) || [];
    event.value.locations = template.value?.locations.map(deepCopy) || [];
    return event.value;
}

defineExpose<Dialog<Partial<Event>, Event>>({
    open: (event?: Partial<Event>) => open(event),
    close: () => dlg.value?.reject(),
    submit: (result: Event) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});

init();
</script>
