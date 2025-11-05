<template>
    <VDialog ref="dlg">
        <template #title>Neue Reise erstellen</template>
        <template #default>
            <div class="px-4 pt-4 sm:px-8 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputText
                            v-model.trim="event.name"
                            label="Name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Titel der Reise"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="event.type"
                            label="Kategorie"
                            :errors="validation.errors.value['type']"
                            :errors-visible="validation.showErrors.value"
                            :options="eventTypes.options.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="template"
                            label="Vorlage"
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
                        <VInputSelect
                            v-model="event.signupType"
                            label="Anmeldetyp"
                            :options="eventSignupTypes.options.value"
                            :errors="validation.errors.value['signupType']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model.trim="event.description"
                            label="Beschreibung"
                            hint="Markdown wird unterstützt"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Kurze Beschreibung oder Zusatzinformationen"
                        />
                    </div>
                    <div class="mb-4 flex space-x-4">
                        <div class="w-3/5">
                            <VInputDate
                                label="Startdatum"
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
                            <VInputTime
                                label="Crew an Bord"
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
                            <VInputDate
                                label="Enddatum"
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
                            <VInputTime
                                label="Crew von Bord"
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
            <button class="btn-ghost" name="save" :disabled="validation.disableSubmit.value" @click="submit">
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
import { EventSignupType, EventState, EventType } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputDate, VInputSelect, VInputText, VInputTextArea, VInputTime } from '@/ui/components/common';
import { useEventUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { useEventSignupTypes } from '@/ui/composables/EventSignupTypes.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { useValidation } from '@/ui/composables/Validation';

const eventTypes = useEventTypes();
const eventSignupTypes = useEventSignupTypes();
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
    signupType: EventSignupType.Assignment,
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

function init(): void {
    watch(
        () => event.value.start,
        () => {
            if (event.value.end.getTime() < event.value.start.getTime()) {
                event.value.end = updateDate(event.value.end, event.value.start);
            }
        }
    );
    fetchTemplates(new Date().getFullYear());
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
