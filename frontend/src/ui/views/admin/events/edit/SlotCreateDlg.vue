<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1>Slot hinzufügen</h1>
        </template>
        <template #default>
            <div v-if="eventSlot" class="flex flex-1 flex-col p-8 lg:px-16">
                <div class="-mx-4 mb-2">
                    <VInputLabel>Position</VInputLabel>
                    <VInputCombobox
                        v-model="primaryPosition"
                        :options="(positions || []).map((it) => ({ label: it.name, value: it }))"
                    />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Anzeigename</VInputLabel>
                    <VInputText v-model="eventSlot.positionName" :placeholder="primaryPosition?.name" />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Kritikaliät</VInputLabel>
                    <VInputSelect
                        v-model="eventSlot.required"
                        :options="[
                            { value: true, label: 'Erforderliche Slots' },
                            { value: false, label: 'Optionale Slots' },
                        ]"
                    />
                </div>
                <div class="-mx-4 mt-8 rounded-xl bg-primary-100 p-4 pr-8 text-sm">
                    <h2 class="mb-4">Alternative Positionen</h2>
                    <div class="grid grid-cols-2 gap-x-8 gap-y-2">
                        <div v-for="position in positions" :key="position.key">
                            <VInputCheckBox
                                :model-value="
                                    eventSlot.positionKeys.includes(position.key) ||
                                    position.key === primaryPosition?.key
                                "
                                :label="position.name"
                                :disabled="position.key === primaryPosition?.key"
                                @update:model-value="togglePosition(position.key, $event)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" @click="submit">
                <span>Erstellen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { ArrayUtils } from '@/common';
import type { Event, Position, PositionKey, Slot } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCheckBox, VInputCombobox, VInputLabel, VInputSelect, VInputText } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';

const usersUseCase = useUsersUseCase();

const dlg = ref<Dialog<Event> | null>(null);
const eventSlot = ref<Slot>();
const positions = ref<Position[]>([]);
const primaryPosition = ref<Position | null>(null);

async function init(): Promise<void> {
    await fetchPositions();
}

async function fetchPositions(): Promise<void> {
    const positionsMap = await usersUseCase.resolvePositionNames();
    positions.value = [...positionsMap.values()].sort((a, b) => a.prio - b.prio);
}

async function open(event: Event): Promise<Event> {
    eventSlot.value = {
        key: '',
        order: -1,
        positionKeys: [],
        required: false,
    };
    primaryPosition.value = positions.value.find((it) => it.key === eventSlot.value?.positionKeys[0]) || null;
    await dlg.value?.open();
    if (eventSlot.value && primaryPosition.value) {
        eventSlot.value.positionKeys.unshift(primaryPosition.value.key);
        eventSlot.value.positionKeys = eventSlot.value.positionKeys.filter(ArrayUtils.filterDuplicates);
    }
    event.slots.push(eventSlot.value);
    return event;
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

defineExpose<Dialog<Event>>({
    open: (event: Event) => open(event),
    close: () => dlg.value?.reject(),
    submit: (result: Event) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
