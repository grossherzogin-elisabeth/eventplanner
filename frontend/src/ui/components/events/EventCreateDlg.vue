<template>
    <VDialog ref="dlg" data-test-id="event-create-dialog">
        <template #title>Neue Veranstaltung erstellen</template>
        <template #default>
            <div class="px-4 pt-4 sm:px-8 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputText
                            v-model.trim="event.name"
                            data-test-id="input-event-name"
                            :label="$t('domain.event.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Titel der Veranstaltung"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="event.type"
                            :label="$t('domain.event.category')"
                            :errors="validation.errors.value['type']"
                            :errors-visible="validation.showErrors.value"
                            :options="eventTypes.options.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="template"
                            data-test-id="input-event-template"
                            :label="$t('domain.event.template')"
                            :errors="validation.errors.value['template']"
                            :errors-visible="validation.showErrors.value"
                            :options="templates.map((it) => ({ label: it.name, value: it }))"
                        >
                            <template #item="{ item }">
                                <template v-if="item.value">
                                    <span class="w-0 flex-grow truncate">{{ item.value?.name }}</span>
                                    <span class="opacity-50">{{ formatDateRange(item.value?.start, item.value?.end, true) }}</span>
                                </template>
                                <template v-else>-</template>
                            </template>
                        </VInputCombobox>
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="event.signupType"
                            :label="$t('domain.event.signup-type')"
                            :options="eventSignupTypes.options.value"
                            :errors="validation.errors.value['signupType']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model.trim="event.description"
                            :label="$t('domain.event.description')"
                            :hint="$t('generic.markdown-supported')"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Kurze Beschreibung oder Zusatzinformationen"
                        />
                    </div>
                    <div class="mb-4 flex space-x-4">
                        <div class="w-3/5">
                            <VInputDate
                                :label="$t('domain.event.start-date')"
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
                                :label="$t('domain.event.start-time')"
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
                                :label="$t('domain.event.end-date')"
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
                                :label="$t('domain.event.end-time')"
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
            <button class="btn-ghost" data-test-id="button-cancel" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <AsyncButton
                class="btn-ghost"
                data-test-id="button-submit"
                name="save"
                :disabled="validation.disableSubmit.value"
                :action="submit"
            >
                <template #label>{{ $t('generic.save') }}</template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useEventAdministrationUseCase, useEventUseCase } from '@/application';
import { cropToPrecision, deepCopy, updateDate, updateTime } from '@/common';
import type { Event } from '@/domain';
import { EventSignupType, EventState, EventType, useEventService } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import {
    AsyncButton,
    VDialog,
    VInputCombobox,
    VInputDate,
    VInputSelect,
    VInputText,
    VInputTextArea,
    VInputTime,
} from '@/ui/components/common';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter.ts';
import { useEventSignupTypes } from '@/ui/composables/EventSignupTypes.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { useValidation } from '@/ui/composables/Validation';

const eventTypes = useEventTypes();
const eventSignupTypes = useEventSignupTypes();
const eventAdminUseCase = useEventAdministrationUseCase();
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
    fetchTemplates();
}

async function fetchTemplates(): Promise<void> {
    const year = new Date().getFullYear();
    const eventsNextYear = await eventUseCase.getEvents(year + 1);
    const eventsCurrentYear = await eventUseCase.getEvents(year);
    const eventsPreviousYear = await eventUseCase.getEvents(year - 1);
    templates.value = [...eventsPreviousYear, ...eventsCurrentYear, ...eventsNextYear];
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

    return await dlg.value?.open().catch(() => undefined);
}

async function submit(): Promise<void> {
    if (validation.isValid.value) {
        event.value.slots =
            template.value?.slots.map((slot) => ({
                key: slot.key,
                criticality: slot.criticality,
                positionKeys: slot.positionKeys,
                positionName: slot.positionName,
                order: slot.order,
            })) || [];
        event.value.locations = template.value?.locations.map(deepCopy) || [];
        event.value = await eventAdminUseCase.createEvent(event.value);
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
