<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 v-if="!qualification.key">Qualifikation hinzufügen</h1>
            <h1 v-else>Qualifikation bearbeiten</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="qualification.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="validation.showErrors.value"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Icon</VInputLabel>
                    <VInputText
                        v-model="qualification.icon"
                        placeholder="fa-id-card"
                        :errors="validation.errors.value['icon']"
                        :errors-visible="validation.showErrors.value"
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
                        :errors-visible="validation.showErrors.value"
                        required
                    />
                </div>
                <div class="mb-4">
                    <VInputCheckBox
                        v-model="qualification.expires"
                        label="Gültigkeit der Qualifikation ist zeitlich begrenzt"
                    />
                </div>
                <div class="-mx-4 mt-8 rounded-xl bg-primary-100 p-4 pr-8 text-sm">
                    <h2 class="mb-4 text-xs font-bold text-primary-700 text-opacity-50">Positionen</h2>
                    <div class="grid grid-cols-2 gap-x-8 gap-y-2">
                        <div v-for="position in positions.all.value" :key="position.key">
                            <VInputCheckBox
                                :model-value="qualification.grantsPositions?.includes(position.key)"
                                :label="position.name"
                                @update:model-value="togglePosition(position.key, $event)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </template>
        <template #buttons>
            <button class="btn-secondary" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Speichern</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy } from '@/common';
import type { PositionKey, Qualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VDialog, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useQualificationService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

const qualificationService = useQualificationService();
const positions = usePositions();

const dlg = ref<Dialog<Qualification | undefined, Qualification | undefined> | null>(null);
const qualification = ref<Qualification>({
    key: '',
    icon: 'fa-id-card',
    expires: false,
    name: '',
    description: '',
    grantsPositions: [],
});
const validation = useValidation(qualification, (qualification) => qualificationService.validate(qualification));

async function open(value?: Qualification): Promise<Qualification | undefined> {
    validation.reset();
    qualification.value = value
        ? deepCopy(value)
        : {
              key: '',
              icon: 'fa-id-card',
              expires: false,
              name: '',
              description: '',
              grantsPositions: [],
          };
    // wait until user submits
    return await dlg.value?.open().catch(() => undefined);
}

function togglePosition(position: PositionKey, enabled: boolean): void {
    if (!enabled) {
        qualification.value.grantsPositions = qualification.value.grantsPositions.filter((it) => it !== position);
    } else if (!qualification.value.grantsPositions.includes(position)) {
        qualification.value.grantsPositions.push(position);
    }
}

function submit() {
    if (validation.isValid.value) {
        dlg.value?.submit(qualification.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Qualification | undefined, Qualification | undefined>>({
    open: (value?: Qualification) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result?: Qualification) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
