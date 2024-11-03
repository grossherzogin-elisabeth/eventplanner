<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1 v-if="!slot.key">Slot hinzufügen</h1>
            <h1 v-else>Slot bearbeiten</h1>
        </template>
        <template #default>
            <div class="flex flex-1 flex-col p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Position</VInputLabel>
                    <VInputCombobox v-model="primaryPositionKey" :options="positions.options.value" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Anzeigename</VInputLabel>
                    <VInputText v-model="slot.positionName" :placeholder="positions.get(primaryPositionKey).name" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Kritikaliät</VInputLabel>
                    <VInputSelect
                        v-model="slot.criticality"
                        :options="[
                            { value: SlotCriticality.Security, label: 'Notwendig' },
                            { value: SlotCriticality.Required, label: 'Wichtig' },
                            { value: SlotCriticality.Optional, label: 'Optional' },
                        ]"
                    />
                </div>
                <div class="-mx-4 mt-8 rounded-xl bg-primary-100 p-4 pr-8 text-sm">
                    <h2 class="mb-4 text-xs font-bold text-primary-700 text-opacity-50">Alternative Positionen</h2>
                    <div class="grid grid-cols-2 gap-x-8 gap-y-2">
                        <div v-for="position in positions.all.value" :key="position.key">
                            <VInputCheckBox
                                :model-value="
                                    slot.positionKeys.includes(position.key) || position.key === primaryPositionKey
                                "
                                :label="position.name"
                                :disabled="position.key === primaryPositionKey"
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
            <button class="btn-primary" @click="submit">
                <span>Übernehmen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy, filterDuplicates } from '@/common';
import type { PositionKey, Slot } from '@/domain';
import { SlotCriticality } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCheckBox, VInputCombobox, VInputLabel, VInputSelect, VInputText } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';

const positions = usePositions();

const dlg = ref<Dialog<Slot | undefined, Slot | undefined> | null>(null);
const slot = ref<Slot>({
    key: '',
    order: -1,
    criticality: SlotCriticality.Optional,
    positionKeys: [],
});
const primaryPositionKey = ref<PositionKey>('');

async function open(value?: Slot): Promise<Slot | undefined> {
    slot.value = value
        ? deepCopy(value)
        : {
              key: '',
              order: -1,
              criticality: SlotCriticality.Optional,
              positionKeys: [],
          };
    primaryPositionKey.value = slot.value?.positionKeys[0] || '';
    const result = await dlg.value?.open().catch(() => undefined);
    if (!result) {
        return undefined;
    }
    if (primaryPositionKey.value) {
        result.positionKeys.unshift(primaryPositionKey.value);
    }
    result.positionKeys = result.positionKeys.filter(filterDuplicates);
    return result;
}

function togglePosition(position: PositionKey, enabled: boolean): void {
    if (!slot.value) {
        return;
    }
    if (!enabled) {
        slot.value.positionKeys = slot.value.positionKeys.filter((it) => it !== position);
    } else if (!slot.value.positionKeys.includes(position)) {
        slot.value.positionKeys.push(position);
    }
}

function submit() {
    dlg.value?.submit(slot.value);
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Slot, Slot | undefined>>({
    open: (value?: Slot) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result?: Slot) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
