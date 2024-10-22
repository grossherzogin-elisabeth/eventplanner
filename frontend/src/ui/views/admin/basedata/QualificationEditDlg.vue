<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Qualifikation hinzufügen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="qualification.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="true"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Icon</VInputLabel>
                    <VInputText
                        v-model="qualification.icon"
                        placeholder="fa-id-card"
                        :errors="validation.errors.value['icon']"
                        :errors-visible="true"
                        required
                    >
                        <template #after>
                            <span :key="qualification.icon">
                                <i class="fa-solid mr-4" :class="qualification.icon"></i>
                            </span>
                        </template>
                    </VInputText>
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Beschreibung</VInputLabel>
                    <VInputTextArea
                        v-model="qualification.description"
                        :errors="validation.errors.value['description']"
                        :errors-visible="true"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Verknüpfte Position</VInputLabel>
                    <VInputCombobox
                        v-model="qualification.grantsPosition"
                        :options="positions.optionsIncludingNone.value"
                    />
                </div>
                <div class="mb-4">
                    <VInputCheckBox
                        v-model="qualification.expires"
                        label="Gültigkeit der Qualifikation ist zeitlich begrenzt"
                    />
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="!validation.isValid.value" @click="submit">
                <span>Qualifikation speichern</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { ObjectUtils } from '@/common';
import type { Qualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useQualificationService } from '@/ui/composables/Domain';
import { usePositions } from '@/ui/composables/Positions';
import { useValidation } from '@/ui/composables/Validation';

const qualificationService = useQualificationService();

const positions = usePositions();

const dlg = ref<Dialog<Qualification, Qualification> | null>(null);
const qualification = ref<Qualification>({
    key: '',
    icon: 'fa-id-card',
    expires: false,
    name: '',
    description: '',
    grantsPosition: undefined,
});
const validation = useValidation(qualification, (qualification) => qualificationService.validate(qualification));

function init(): void {}

async function open(value: Qualification): Promise<Qualification> {
    qualification.value = ObjectUtils.deepCopy(value);
    // wait until user submits
    await dlg.value?.open();

    return qualification.value;
}

defineExpose<Dialog<Qualification, Qualification>>({
    open: (value: Qualification) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result: Qualification) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
