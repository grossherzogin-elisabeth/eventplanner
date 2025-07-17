<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Neue Reise erstellen</h1>
        </template>
        <template #default>
            <div class="px-8 pt-4 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputLabel>Name</VInputLabel>
                        <VInputText
                            v-model.trim="event.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Titel der Reise"
                            required
                        />
                    </div>
                    <div class="mb-4">
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
                    <div class="mb-4">
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
                    <div class="mb-4">
                        <VInputLabel>Beschreibung</VInputLabel>
                        <VInputTextArea
                            v-model.trim="event.description"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Kurze Beschreibung oder Zusatzinformationen"
                        />
                    </div>
                    <div class="mb-4 flex space-x-4">
                        <div class="w-3/5">
                            <VInputLabel>Startdatum</VInputLabel>
                            <VInputDate
                                :model-value="event.start"
                                :highlight-from="event.start"
                                :highlight-to="event.end"
                                :errors="validation.errors.value['start']"
                                :errors-visible="validation.showErrors.value"
                                required
                                @update:model-value="event.start = updateDate(event.start, $event)"
                            />
                        </div>
                        <div class="w-2/5">
                            <VInputLabel>Crew an Bord</VInputLabel>
                            <VInputTime
                                :model-value="event.start"
                                :errors="validation.errors.value['start']"
                                :errors-visible="validation.showErrors.value"
                                required
                                @update:model-value="event.start = updateTime(event.start, $event, 'minutes')"
                            />
                        </div>
                    </div>

                    <div class="mb-4 flex space-x-4">
                        <div class="w-3/5">
                            <VInputLabel>Enddatum</VInputLabel>
                            <VInputDate
                                :model-value="event.end"
                                :highlight-from="event.start"
                                :highlight-to="event.end"
                                :errors="validation.errors.value['end']"
                                :errors-visible="validation.showErrors.value"
                                required
                                @update:model-value="event.end = updateDate(event.end, $event)"
                            />
                        </div>
                        <div class="w-2/5">
                            <VInputLabel>Crew von Bord</VInputLabel>
                            <VInputTime
                                :model-value="event.end"
                                :errors="validation.errors.value['end']"
                                :errors-visible="validation.showErrors.value"
                                required
                                @update:model-value="event.end = updateTime(event.end, $event, 'minutes')"
                            />
                        </div>
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" name="save" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Speichern</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { deepCopy, updateDate, updateTime } from '@/common';
import { DateTimeFormat, cropToPrecision } from '@/common/date';
import type { Event } from '@/domain';
import { EventState, EventType } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputTime } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputDate, VInputLabel, VInputSelect, VInputText, VInputTextArea } from '@/ui/components/common';
import { useEventUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { useValidation } from '@/ui/composables/Validation';

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
    days: 0,
    end: new Date(),
    locations: [],
    slots: [],
    registrations: [],
    assignedUserCount: 0,
    canSignedInUserJoin: false,
    canSignedInUserLeave: false,
    canSignedInUserUpdateRegistration: false,
});
const validation = useValidation(event, eventService.validate);

async function init(): Promise<void> {
    await fetchTemplates(new Date().getFullYear());
    watch(
        () => event.value.start,
        () => {
            if (event.value.end.getTime() < event.value.start.getTime()) {
                event.value.end = updateDate(event.value.end, event.value.start);
            }
        }
    );
}

async function fetchTemplates(year: number): Promise<void> {
    templates.value = await eventUseCase.getEvents(year);
    if (templates.value.length === 0) {
        templates.value = await eventUseCase.getEvents(year - 1);
    }
}

async function open(partialEvent?: Partial<Event>): Promise<Event | undefined> {
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

    const result = await dlg.value?.open().catch(() => undefined);
    if (!result) {
        return undefined;
    }
    result.slots =
        template.value?.slots.map((slot) => ({
            key: slot.key,
            criticality: slot.criticality,
            positionKeys: slot.positionKeys,
            positionName: slot.positionName,
            order: slot.order,
        })) || [];
    result.locations = template.value?.locations.map(deepCopy) || [];
    return result;
}

function submit(): void {
    if (validation.isValid.value) {
        dlg.value?.submit(event.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Partial<Event>, Event | undefined>>({
    open: (event?: Partial<Event>) => open(event),
    close: () => dlg.value?.reject(),
    submit: (result?: Event) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});

init();
</script>
