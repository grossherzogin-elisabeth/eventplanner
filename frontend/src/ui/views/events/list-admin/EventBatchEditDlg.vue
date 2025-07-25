<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Alle bearbeiten</h1>
        </template>
        <template #default>
            <div class="px-4 pt-4 xs:px-8 lg:px-10">
                <section>
                    <p class="mb-8">
                        Du kannst mehrere Reisen auf einmal bearbeiten. Dabei werden die hier eingegebenen Werte für alle Reisen übernommen.
                    </p>
                    <div class="mb-4">
                        <VInputLabel>Status</VInputLabel>
                        <VInputSelect
                            v-model="patch.state"
                            :options="eventStates.options.value"
                            :errors="validation.errors.value['state']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="nicht geändert"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Name</VInputLabel>
                        <VInputText
                            v-model="patch.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="nicht geändert"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Kategorie</VInputLabel>
                        <VInputSelect
                            v-model="patch.type"
                            :options="eventTypes.options.value"
                            :errors="validation.errors.value['type']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="nicht geändert"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Anmeldetyp</VInputLabel>
                        <VInputSelect
                            v-model="patch.signupType"
                            :options="eventSignupTypes.options.value"
                            :errors="validation.errors.value['signupType']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="nicht geändert"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Beschreibung</VInputLabel>
                        <VInputTextArea
                            v-model="patch.description"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="nicht geändert"
                        />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <AsyncButton class="btn-primary" name="save" :action="submit" :disabled="validation.disableSubmit.value">
                <template #label>Speichern</template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import type { Event } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VDialog, VInputLabel, VInputSelect, VInputText, VInputTextArea } from '@/ui/components/common';
import { useEventAdministrationUseCase } from '@/ui/composables/Application.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
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
