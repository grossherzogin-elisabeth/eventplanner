<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Position hinzufügen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
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
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submitIfValid(submit)">
                <span>Position hinzufügen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import type { Position } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputNumber } from '@/ui/components/common';
import { VDialog, VInputLabel, VInputText } from '@/ui/components/common';
import { usePositionService } from '@/ui/composables/Domain';
import { useValidation } from '@/ui/composables/Validation';

const positionService = usePositionService();

const dlg = ref<Dialog<void, Position> | null>(null);
const position = ref<Position>({
    key: '',
    name: '',
    color: '',
    prio: 0,
});
const validation = useValidation(position, (position) => positionService.validate(position));

function init(): void {}

async function open(): Promise<Position> {
    position.value = {
        key: '',
        name: '',
        color: '',
        prio: 0,
    };
    // wait until user submits
    await dlg.value?.open();

    return position.value;
}

async function submitIfValid(submitFun: () => void) {
    if (validation.isValid.value) {
        submitFun();
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

defineExpose<Dialog<void, Position>>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: (result: Position) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
