<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1 v-if="editing">Qualifikation bearbeiten</h1>
            <h1 v-else>Qualifikation hinzufügen</h1>
        </template>
        <template #default>
            <div v-if="userQualification" class="flex flex-1 flex-col p-8 lg:px-16">
                <div class="-mx-4 mb-2">
                    <VInputLabel>Qualifikation</VInputLabel>
                    <VInputCombobox
                        v-model="userQualification.qualificationKey"
                        :options="qualificationOptions"
                        :disabled="editing"
                    />
                </div>
                <!--                <div class="-mx-0 mb-4 mt-2">-->
                <!--                    <VInputCheckBox label="Qualifikation muss nicht erneuert werden" />-->
                <!--                </div>-->
                <div v-if="selectedQualification?.expires" class="-mx-4 mb-2">
                    <VInputLabel>Gültig bis</VInputLabel>
                    <VInputDate v-model="userQualification.expiresAt" />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Bemerkung</VInputLabel>
                    <VInputTextArea v-model="userQualification.note" />
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">Abbrechen</button>
            <button class="btn-primary" @click="submit">Übernehmen</button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { ObjectUtils } from '@/common';
import type { InputSelectOption, Qualification, QualificationKey, UserQualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputDate, VInputLabel, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';

const usersUseCase = useUsersUseCase();

const dlg = ref<Dialog<UserQualification, UserQualification> | null>(null);
const editing = ref<boolean>(false);
const qualifications = ref<Map<QualificationKey, Qualification>>(new Map<QualificationKey, Qualification>());
const userQualification = ref<UserQualification>({ qualificationKey: '' });

const qualificationOptions = computed<InputSelectOption<QualificationKey>[]>(() => {
    return [...qualifications.value.values()].map((it) => ({ label: it.name, value: it.key }));
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

async function open(value?: UserQualification): Promise<UserQualification> {
    if (value) {
        editing.value = true;
        userQualification.value = ObjectUtils.deepCopy(value);
    } else {
        editing.value = false;
        userQualification.value = {
            qualificationKey: '',
            expiresAt: undefined,
            note: undefined,
        };
    }
    await dlg.value?.open();
    return userQualification.value;
}

defineExpose<Dialog<UserQualification, UserQualification>>({
    open: (eventSlot: UserQualification) => open(eventSlot),
    close: () => dlg.value?.reject(),
    submit: (result: UserQualification) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
