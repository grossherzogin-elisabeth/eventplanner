<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Alle bearbeiten</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <p class="mb-8">
                    Du kannst mehrere Reisen auf einmal bearbeiten. Dabei werden die hier eingegebenen Werte für alle
                    Reisen übernommen.
                </p>
                <div class="-mx-4 mb-4">
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
                <div class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="patch.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="validation.showErrors.value"
                        placeholder="nicht geändert"
                    />
                </div>
                <div class="-mx-4 mb-4">
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
                <div class="-mx-4 mb-4">
                    <VInputLabel>Beschreibung</VInputLabel>
                    <VInputTextArea
                        v-model="patch.description"
                        :errors="validation.errors.value['description']"
                        :errors-visible="validation.showErrors.value"
                        placeholder="nicht geändert"
                    />
                </div>
            </div>
        </template>
        <template #buttons>
            <button class="btn-secondary" @click="cancel">
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
                <template #label> Alle aktualisieren </template>
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
import { useEventService } from '@/ui/composables/Domain';
import { useValidation } from '@/ui/composables/Validation';

const eventService = useEventService();

const dlg = ref<Dialog<void, Partial<Event> | undefined> | null>(null);
const patch = ref<Partial<Event>>({});
const validation = useValidation(patch, eventService.validatePartial);

async function submitIfValid(submitFun: () => void) {
    if (validation.isValid.value) {
        submitFun();
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

function submit() {
    dlg.value?.submit(patch.value);
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

async function open(): Promise<Partial<Event> | undefined> {
    validation.reset();
    patch.value = {};
    return await dlg.value?.open().catch(() => undefined);
}

defineExpose<Dialog<void, Partial<Event> | undefined>>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: (patch: Partial<Event>) => dlg.value?.submit(patch),
    reject: () => dlg.value?.reject(),
});
</script>
