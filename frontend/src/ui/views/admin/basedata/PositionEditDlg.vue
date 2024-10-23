<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Position bearbeiten</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Id</VInputLabel>
                    <VInputText v-model="position.key" required disabled />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="position.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="true"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Farbe</VInputLabel>
                    <VInputText
                        v-model="position.color"
                        :errors="validation.errors.value['color']"
                        :errors-visible="true"
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
                        :errors-visible="true"
                        required
                    />
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="!validation.isValid.value" @click="submit">
                <span>Position speichern</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { ObjectUtils } from '@/common';
import type { Position } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputNumber } from '@/ui/components/common';
import { VDialog, VInputLabel, VInputText } from '@/ui/components/common';
import { usePositionService } from '@/ui/composables/Domain';
import { useValidation } from '@/ui/composables/Validation';

const positionService = usePositionService();

const dlg = ref<Dialog<Position, Position> | null>(null);
const position = ref<Position>({
    key: '',
    name: '',
    color: '',
    prio: 0,
});
const validation = useValidation(position, (position) => positionService.validate(position));

function init(): void {}

async function open(value: Position): Promise<Position> {
    position.value = ObjectUtils.deepCopy(value);
    // wait until user submits
    await dlg.value?.open();

    return position.value;
}

defineExpose<Dialog<Position, Position>>({
    open: (value: Position) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result: Position) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
