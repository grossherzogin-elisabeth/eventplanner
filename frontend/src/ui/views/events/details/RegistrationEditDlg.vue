<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung bearbeiten</h1>
        </template>
        <template #default>
            <div class="px-8 pt-4 lg:px-10">
                <section>
                    <div v-if="!registration.userKey" class="mb-4">
                        <VInputLabel>Name</VInputLabel>
                        <VInputText
                            v-model.trim="registration.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Position</VInputLabel>
                        <VInputCombobox
                            v-model="registration.positionKey"
                            :options="availablePositionsForSignedInUser"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                            :disabled="availablePositionsForSignedInUser.length <= 1"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Notiz fürs Büro</VInputLabel>
                        <VInputTextArea
                            v-model.trim="registration.note"
                            :errors="validation.errors.value['note']"
                            :errors-visible="validation.showErrors.value"
                            placeholder=""
                        />
                    </div>
                    <VInfo>
                        <p class="mb-2">
                            Hier kannst du zusätzliche Informationen für deine Anmeldung hinterlegen, die für das Büro bei der Planung der
                            Reise relevant sein könnten. Du kannst hier z.B. angeben, das du dein Kind mitnehmen oder am Vortag anreisen
                            möchtest.
                        </p>
                        <p>
                            <b>
                                Bitte beachte: Je wichtiger diese Info ist, desto eher solltest du zusätzlich noch einmal per Mail oder
                                Telefon im Büro Bescheid geben, damit sie nicht unter geht.
                            </b>
                        </p>
                    </VInfo>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" name="save" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Speichern</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { deepCopy } from '@/common';
import type { InputSelectOption, PositionKey, Registration, UserDetails, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInfo } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
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
