<template>
    <VDialog ref="dlg">
        <template #title>{{ $t('views.events.admin-list.batch-edit.title') }}</template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <p class="mb-8">
                        {{ $t('views.events.admin-list.batch-edit.info') }}
                    </p>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="patch.state"
                            :label="$t('domain.event.status')"
                            :options="eventStates.options.value"
                            :errors="validation.errors.value['state']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.events.admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="patch.name"
                            :label="$t('domain.event.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.events.admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="patch.type"
                            :label="$t('domain.event.category')"
                            :options="eventTypes.options.value"
                            :errors="validation.errors.value['type']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.events.admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="patch.signupType"
                            :label="$t('domain.event.signup-type')"
                            :options="eventSignupTypes.options.value"
                            :errors="validation.errors.value['signupType']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.events.admin-list.batch-edit.not-changed')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model="patch.description"
                            :label="$t('domain.event.description')"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            :placeholder="$t('views.events.admin-list.batch-edit.not-changed')"
                        />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <AsyncButton class="btn-ghost" name="save" :action="submit" :disabled="validation.disableSubmit.value">
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
import { AsyncButton, VDialog, VInputSelect, VInputText, VInputTextArea } from '@/ui/components/common';
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
const validation = useValidation(patch, eventService.validatePartial);
let eventsToEdit: Event[] = [];

async function submit(): Promise<void> {
    if (validation.isValid.value) {
        const keys = eventsToEdit.map((it) => it.key);
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
    return (await dlg.value?.open().catch(() => false)) || false;
}

defineExpose<Dialog<Event[], boolean>>({
    open: (events: Event[]) => open(events),
    close: () => dlg.value?.reject(),
    submit: (changed: boolean) => dlg.value?.submit(changed),
    reject: () => dlg.value?.reject(),
});
</script>
