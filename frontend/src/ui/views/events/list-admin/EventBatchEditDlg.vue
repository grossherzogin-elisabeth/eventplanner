<template>
    <VDialog ref="dlg" data-test-id="event-batch-edit-dialog">
        <template #title>{{ $t('views.event-admin-list.batch-edit.title') }}</template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <p class="mb-8">
                        {{ $t('views.event-admin-list.batch-edit.info') }}
                    </p>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="patch.state"
                            data-test-id="input-event-status"
                            :label="$t('domain.event.status')"
                            :options="eventStates.options.value"
                            :errors="validation.errors.value['state']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.event-admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="patch.name"
                            data-test-id="input-event-name"
                            :label="$t('domain.event.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.event-admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="patch.type"
                            data-test-id="input-event-type"
                            :label="$t('domain.event.category')"
                            :options="eventTypes.options.value"
                            :errors="validation.errors.value['type']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.event-admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="patch.signupType"
                            data-test-id="input-event-signup-type"
                            :label="$t('domain.event.signup-type')"
                            :options="eventSignupTypes.options.value"
                            :errors="validation.errors.value['signupType']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.event-admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <EventTemplateSelect
                            v-model="copySlotsFrom"
                            data-test-id="input-event-slots"
                            :label="$t('views.event-admin-list.batch-edit.copy-slots-from')"
                            :placeholder="$t('views.event-admin-list.batch-edit.not-changed')"
                            :errors="validation.errors.value['copySlotsFrom']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <VWarning v-if="copySlotsFrom" class="mb-4" data-test-id="warning-slots-overwrite">
                        {{ $t('views.event-admin-list.batch-edit.copy-slots-warning') }}
                    </VWarning>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model="patch.description"
                            data-test-id="input-event-description"
                            :label="$t('domain.event.description')"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.event-admin-list.batch-edit.not-changed')"
                        />
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
                name="save"
                :action="submit"
                :disabled="validation.disableSubmit.value"
                data-test-id="button-submit"
            >
                <template #label>{{ $t('generic.save') }}</template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useEventAdministrationUseCase } from '@/application';
import type { Event } from '@/domain';
import { useEventService } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VDialog, VInputSelect, VInputText, VInputTextArea, VWarning } from '@/ui/components/common';
import EventTemplateSelect from '@/ui/components/events/EventTemplateSelect.vue';
import { useEventSignupTypes } from '@/ui/composables/EventSignupTypes.ts';
import { useEventStates } from '@/ui/composables/EventStates.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

const eventStates = useEventStates();
const eventTypes = useEventTypes();
const eventSignupTypes = useEventSignupTypes();
const eventService = useEventService();
const eventAdminUseCase = useEventAdministrationUseCase();

const dlg = ref<Dialog<Event[], boolean> | null>(null);
const patch = ref<Partial<Event>>({});
const copySlotsFrom = ref<Event | undefined>(undefined);
const validation = useValidation(patch, eventService.validatePartial);
let eventsToEdit: Event[] = [];

async function submit(): Promise<void> {
    if (!copySlotsFrom.value && Object.keys(patch.value).length === 0) {
        cancel();
        return;
    }
    if (validation.isValid.value) {
        const keys = eventsToEdit.map((it) => it.key);
        if (copySlotsFrom.value) {
            patch.value.slots = copySlotsFrom.value?.slots.map((it) => ({
                ...it,
                assignedRegistrationKey: undefined,
            }));
        }
        await eventAdminUseCase.updateEvents(keys, patch.value);
        dlg.value?.submit(true);
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

function cancel(): void {
    dlg.value?.submit(false);
}

async function open(events: Event[]): Promise<boolean> {
    eventsToEdit = events;
    validation.reset();
    patch.value = {};
    copySlotsFrom.value = undefined;
    return (await dlg.value?.open().catch(() => false)) || false;
}

defineExpose<Dialog<Event[], boolean>>({
    open: (events: Event[]) => open(events),
    close: () => dlg.value?.reject(),
    submit: (changed: boolean) => dlg.value?.submit(changed),
    reject: () => dlg.value?.reject(),
});
</script>
