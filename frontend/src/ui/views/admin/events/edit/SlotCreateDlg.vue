<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1>Slot hinzufügen</h1>
        </template>
        <template #default>
            <div v-if="eventSlot" class="flex flex-1 flex-col p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Position</VInputLabel>
                    <VInputCombobox v-model="primaryPositionKey" :options="positions.options.value" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Anzeigename</VInputLabel>
                    <VInputText
                        v-model="eventSlot.positionName"
                        :placeholder="positions.get(primaryPositionKey).name"
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Kritikaliät</VInputLabel>
                    <VInputSelect
                        v-model="eventSlot.criticality"
                        :options="[
                            { value: SlotCriticality.Security, label: 'Sichere Mindestbesatzung' },
                            { value: SlotCriticality.Required, label: 'Erforderlich' },
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
                                    eventSlot.positionKeys.includes(position.key) || position.key === primaryPositionKey
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
import { filterDuplicates } from '@/common';
import type { PositionKey, Slot } from '@/domain';
import { SlotCriticality } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCheckBox, VInputCombobox, VInputLabel, VInputSelect, VInputText } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';

const positions = usePositions();

const dlg = ref<Dialog<void, Slot | undefined> | null>(null);
const eventSlot = ref<Slot | undefined>(undefined);
const primaryPositionKey = ref<PositionKey>('');

async function open(): Promise<Slot | undefined> {
    eventSlot.value = {
        key: '',
        order: -1,
        positionKeys: [],
        criticality: SlotCriticality.Optional,
    };
    primaryPositionKey.value = eventSlot.value?.positionKeys[0] || '';
    return await dlg.value
        ?.open()
        .then((slot) => {
            if (slot) {
                if (primaryPositionKey.value) {
                    slot.positionKeys.unshift(primaryPositionKey.value);
                }
                slot.positionKeys = slot.positionKeys.filter(filterDuplicates);
            }
            return slot;
        })
        .catch(() => undefined);
}

function togglePosition(position: PositionKey, enabled: boolean): void {
    if (!eventSlot.value) {
        return;
    }
    if (!enabled) {
        eventSlot.value.positionKeys = eventSlot.value.positionKeys.filter((it) => it !== position);
    } else if (!eventSlot.value.positionKeys.includes(position)) {
        eventSlot.value.positionKeys.push(position);
    }
}

function submit() {
    dlg.value?.submit(eventSlot.value);
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<void, Slot | undefined>>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: (result?: Slot) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
