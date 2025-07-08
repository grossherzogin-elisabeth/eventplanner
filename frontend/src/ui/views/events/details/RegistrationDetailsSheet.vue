<template>
    <VSheet ref="dlg" min-height="20rem">
        <template #title>
            <h1 v-if="registration.key">Anmeldung bearbeiten</h1>
            <h1 v-else>Anmeldung erstellen</h1>
        </template>
        <template #default>
            <div class="px-8 pt-4 lg:px-10">
                <section class="sm:w-[25rem]">
                    <p class="mb-8">
                        Möchtest du weitere Informationen wie einen abweichenden Anreisetag oder eine Notiz fürs Büro hinterlegen?
                    </p>
                    <div v-if="!registration.userKey" class="mb-4">
                        <VInputLabel>Name</VInputLabel>
                        <VInputText
                            v-model.trim="registration.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Auf welcher Position möchtest du mitfahren?</VInputLabel>
                        <VInputSelect
                            v-model="registration.positionKey"
                            :options="availablePositionsForSignedInUser"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                            :disabled="availablePositionsForSignedInUser.length <= 1"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Wann möchtest du anreisen?</VInputLabel>
                        <VInputDate
                            v-model="registration.arrival"
                            :errors="validation.errors.value['arrival']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Hast du eine Notiz fürs Büro?</VInputLabel>
                        <VInputTextArea
                            v-model.trim="registration.note"
                            :errors="validation.errors.value['note']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Zusätzliche Informationen zu deiner Anmeldung, die für das Büro zur Planung wichtig sein könnten"
                        />
                    </div>
                    <VInfo v-if="!registration.confirmed" class="mb-8">
                        Du hast deine Teilnahme bereits bestätigt. Solltest du doch nicht wie geplant an der Reise teilnehmen können, melde
                        dich bitte im Büro.
                    </VInfo>
                </section>
            </div>
        </template>
        <template #bottom>
            <div class="lg:px-10-lg flex justify-end gap-2 bg-surface px-8 py-4">
                <button class="btn-ghost" name="save" @click="cancel">
                    <span>Abbrechen</span>
                </button>
                <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                    <span v-if="registration.key">Speichern</span>
                    <span v-else>Anmelden</span>
                </button>
            </div>
        </template>
    </VSheet>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { deepCopy } from '@/common';
import type { InputSelectOption, PositionKey, Registration, UserDetails, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VSheet } from '@/ui/components/common';
import { VInputSelect } from '@/ui/components/common';
import { VInputDate } from '@/ui/components/common';
import { VInfo } from '@/ui/components/common';
import { VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { v4 as uuid } from 'uuid';

const usersUseCase = useUsersUseCase();
const positions = usePositions();

const dlg = ref<Dialog<Registration, Registration | undefined> | null>(null);
const signedInUserDetails = ref<UserDetails | null>(null);
const registration = ref<Registration>({ key: uuid(), positionKey: '' });
const validation = useValidation(registration, (value) => {
    // TODO extract to service
    const errors: Record<string, ValidationHint[]> = {};
    if (!value.positionKey) {
        errors.positionKey = errors.positionKey || [];
        errors.positionKey.push({ key: 'Bitte wähle eine Position', params: {} });
    }
    if (!value.name && !value.userKey) {
        errors.userKey = errors.userKey || [];
        errors.userKey.push({ key: 'Bitte wähle eine Stammcrew Mitglied', params: {} });
    }
    return errors;
});

const availablePositionsForSignedInUser = computed<InputSelectOption<string | undefined>[]>(() => {
    const validPositionKeys: (PositionKey | undefined)[] = signedInUserDetails.value?.positionKeys || [];
    if (!validPositionKeys.includes(registration.value.positionKey)) {
        validPositionKeys.push(registration.value.positionKey);
    }
    return positions.options.value.filter((it) => validPositionKeys.includes(it.value));
});

async function init(): Promise<void> {
    await fetchSignedInUserDetails();
}

async function fetchSignedInUserDetails(): Promise<void> {
    signedInUserDetails.value = await usersUseCase.getUserDetailsForSignedInUser();
}

async function open(value: Registration): Promise<Registration | undefined> {
    validation.reset();
    registration.value = deepCopy(value);
    // wait until user submits
    const result = await dlg.value?.open().catch(() => undefined);
    if (!result) {
        return undefined;
    }
    if (!registration.value.userKey) {
        value.name = result.name;
    }
    value.positionKey = result.positionKey;
    value.note = result.note;
    return value;
}

function submit(): void {
    if (validation.isValid.value) {
        dlg.value?.submit(registration.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Registration, Registration | undefined>>({
    open: (eventRegistration: Registration) => open(eventRegistration),
    close: () => dlg.value?.reject(),
    submit: (result: Registration) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});

init();
</script>
