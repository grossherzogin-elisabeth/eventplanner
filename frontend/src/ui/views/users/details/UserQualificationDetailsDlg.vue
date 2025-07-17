<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1 v-if="editing">Qualifikation bearbeiten</h1>
            <h1 v-else>Qualifikation hinzufügen</h1>
        </template>
        <template #default>
            <div v-if="userQualification" class="flex flex-1 flex-col px-4 pt-4 xs:px-8 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputLabel>Qualifikation</VInputLabel>
                        <VInputCombobox
                            v-model="userQualification.qualificationKey"
                            :options="qualificationOptions"
                            :disabled="editing"
                            :errors="validation.errors.value['qualificationKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div v-if="selectedQualification?.expires" class="mb-4">
                        <VInputLabel>Gültig bis</VInputLabel>
                        <VInputDate
                            v-model="userQualification.expiresAt"
                            :errors="validation.errors.value['expiresAt']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Bemerkung</VInputLabel>
                        <VInputTextArea
                            v-model="userQualification.note"
                            :errors="validation.errors.value['note']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Übernehmen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { deepCopy } from '@/common';
import type { InputSelectOption, Qualification, QualificationKey, UserQualification, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputDate, VInputLabel, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

const usersUseCase = useUsersUseCase();

const dlg = ref<Dialog<UserQualification, UserQualification | undefined> | null>(null);
const editing = ref<boolean>(false);
const qualifications = ref<Map<QualificationKey, Qualification>>(new Map<QualificationKey, Qualification>());
const userQualification = ref<UserQualification>({ qualificationKey: '', expires: false });

const validation = useValidation(userQualification, (value) => {
    const errors: Record<string, ValidationHint[]> = {};
    if (!value.qualificationKey) {
        errors.qualificationKey = errors.qualificationKey || [];
        errors.qualificationKey.push({ key: 'Bitte wähle eine Qualifikation aus', params: {} });
    }
    return errors;
});

const qualificationOptions = computed<InputSelectOption<QualificationKey>[]>(() => {
    return [...qualifications.value.values()]
        .map((it) => ({ label: it.name, value: it.key }))
        .sort((a, b) => a.label.localeCompare(b.label));
});

const selectedQualification = computed<Qualification | undefined>(() => {
    return qualifications.value.get(userQualification.value.qualificationKey);
});

function init(): void {
    fetchQualifications();
}

async function fetchQualifications(): Promise<void> {
    qualifications.value = await usersUseCase.resolveQualifications();
}

async function open(value?: UserQualification): Promise<UserQualification | undefined> {
    if (value) {
        editing.value = true;
        userQualification.value = deepCopy(value);
    } else {
        editing.value = false;
        userQualification.value = {
            qualificationKey: '',
            expiresAt: undefined,
            note: undefined,
            expires: false,
        };
    }
    return await dlg.value?.open().catch(() => undefined);
}

function submit(): void {
    if (validation.isValid.value) {
        userQualification.value.expires = selectedQualification.value?.expires ?? false;
        dlg.value?.submit(userQualification.value);
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<UserQualification, UserQualification | undefined>>({
    open: (eventSlot: UserQualification) => open(eventSlot),
    close: () => dlg.value?.reject(),
    submit: (result?: UserQualification) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});

init();
</script>
