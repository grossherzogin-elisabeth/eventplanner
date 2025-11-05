<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <template v-if="editing">Qualifikation bearbeiten</template>
            <template v-else>Qualifikation hinzufügen</template>
        </template>
        <template #default>
            <div v-if="userQualification" class="xs:px-8 flex flex-1 flex-col px-4 pt-4 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="userQualification.qualificationKey"
                            label="Qualifikation"
                            :options="qualificationOptions"
                            :disabled="editing"
                            :errors="validation.errors.value['qualificationKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div v-if="selectedQualification?.expires" class="mb-4">
                        <VInputDate
                            v-model="userQualification.expiresAt"
                            label="Gültig bis"
                            :errors="validation.errors.value['expiresAt']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model="userQualification.note"
                            label="Bemerkung"
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
            <button class="btn-ghost" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Übernehmen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { deepCopy } from '@/common';
import { Validator, notEmpty } from '@/common/validation';
import type { InputSelectOption, Qualification, QualificationKey, UserQualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputDate, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

const usersUseCase = useUsersUseCase();

const dlg = ref<Dialog<UserQualification, UserQualification | undefined> | null>(null);
const editing = ref<boolean>(false);
const qualifications = ref<Map<QualificationKey, Qualification>>(new Map<QualificationKey, Qualification>());
const userQualification = ref<UserQualification>({ qualificationKey: '', expires: false });

const validation = useValidation(userQualification, (value) => {
    return Validator.validate('qualificationKey', value.qualificationKey, notEmpty()).getErrors();
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
