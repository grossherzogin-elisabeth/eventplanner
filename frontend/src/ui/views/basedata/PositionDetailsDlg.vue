<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 v-if="position.key">Position bearbeiten</h1>
            <h1 v-else>Position hinzufügen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div v-if="position.key" class="-mx-4 mb-4">
                    <VInputLabel>Id</VInputLabel>
                    <VInputText v-model="position.key" required disabled />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="position.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="validation.showErrors.value"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Anzeigename IMO Liste</VInputLabel>
                    <VInputText
                        v-model="position.imoListRank"
                        :errors="validation.errors.value['imoListRank']"
                        :errors-visible="validation.showErrors.value"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Farbe</VInputLabel>
                    <VInputText
                        v-model="position.color"
                        :errors="validation.errors.value['color']"
                        :errors-visible="validation.showErrors.value"
                        required
                    >
                        <template #after>
                            <div class="mr-4 h-8 w-8 rounded-lg" :style="{ background: position.color }"></div>
                        </template>
                    </VInputText>
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Priorität</VInputLabel>
                    <VInputNumber
                        v-model="position.prio"
                        :errors="validation.errors.value['prio']"
                        :errors-visible="validation.showErrors.value"
                        required
                    />
                </div>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
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
import type { Position } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputNumber } from '@/ui/components/common';
import { VDialog, VInputLabel, VInputText } from '@/ui/components/common';
import { usePositionService } from '@/ui/composables/Domain.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

const positionService = usePositionService();

const dlg = ref<Dialog<Position | undefined, Position | undefined> | null>(null);
const position = ref<Position>({
    key: '',
    name: '',
    imoListRank: '',
    color: '',
    prio: 0,
});
const validation = useValidation(position, (position) => positionService.validate(position));

async function open(value?: Position): Promise<Position | undefined> {
    validation.reset();
    position.value = value
        ? deepCopy(value)
        : {
              key: '',
              name: '',
              imoListRank: '',
              color: '',
              prio: 0,
          };
    return await dlg.value?.open().catch(() => undefined);
}

function submit(): void {
    if (validation.isValid.value) {
        dlg.value?.submit(position.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Position | undefined, Position | undefined>>({
    open: (value?: Position) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result?: Position) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
