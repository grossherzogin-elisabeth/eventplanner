<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Alle bearbeiten</h1>
        </template>
        <template #default>
            <div class="px-8 pt-4 lg:px-10">
                <section>
                    <p class="mb-8">
                        Du kannst mehrere Reisen auf einmal bearbeiten. Dabei werden die hier eingegebenen Werte für
                        alle Reisen übernommen.
                    </p>
                    <div class="mb-4">
                        <VInputLabel>Status</VInputLabel>
                        <VInputSelect
                            v-model="patch.state"
                            :options="[
                                { value: undefined, label: '' },
                                { value: EventState.Draft, label: 'Entwurf' },
                                { value: EventState.OpenForSignup, label: 'Crew Anmeldung' },
                                { value: EventState.Planned, label: 'Crewplanung verlöffentlicht' },
                                { value: EventState.Canceled, label: 'Abgesagt' },
                            ]"
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
                            :errors="validation.errors.value['type']"
                            :errors-visible="validation.showErrors.value"
                            :options="[
                                { value: undefined, label: '' },
                                { value: EventType.WorkEvent, label: 'Arbeitsdienst' },
                                { value: EventType.SingleDayEvent, label: 'Tagesfahrt' },
                                { value: EventType.WeekendEvent, label: 'Wochenendreise' },
                                { value: EventType.MultiDayEvent, label: 'Mehrtagesfahrt' },
                            ]"
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
            <AsyncButton class="btn-primary" :action="submit" :disabled="validation.disableSubmit.value">
                <template #label>Speichern</template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import type { Event } from '@/domain';
import { EventState, EventType } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VDialog, VInputLabel, VInputSelect, VInputText, VInputTextArea } from '@/ui/components/common';
import { useEventAdministrationUseCase } from '@/ui/composables/Application.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

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
