<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1 v-if="createMode">{{ $t('views.events.edit.actions.add-slot') }}</h1>
            <h1 v-else>{{ $t('views.events.edit.actions.edit-slot') }}</h1>
        </template>
        <template #default>
            <div class="xs:px-8 flex flex-1 flex-col px-4 pt-4 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="primaryPositionKey"
                            required
                            :label="$t('domain.event-slot.position')"
                            :options="positions.options.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model.trim="slot.positionName"
                            :label="$t('domain.event-slot.display-name')"
                            :placeholder="positions.get(primaryPositionKey).name"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="slot.criticality"
                            required
                            :label="$t('domain.event-slot.criticality')"
                            :options="[
                                {
                                    value: SlotCriticality.Required,
                                    label: $t('domain.event-slot.required'),
                                },
                                {
                                    value: SlotCriticality.Important,
                                    label: $t('domain.event-slot.important'),
                                },
                                {
                                    value: SlotCriticality.Optional,
                                    label: $t('domain.event-slot.optional'),
                                },
                            ]"
                        />
                    </div>
                </section>
                <div class="bg-surface-container-low sm:bg-surface-container-highest mt-8 rounded-xl p-4 pr-8 text-sm">
                    <h2 class="mb-4 text-xs font-bold">{{ $t('domain.event-slot.alternative-positions') }}</h2>
                    <div class="grid gap-x-8 gap-y-2 sm:grid-cols-2">
                        <div v-for="position in positions.all.value" :key="position.key">
                            <VInputCheckBox
                                :model-value="slot.positionKeys.includes(position.key) || position.key === primaryPositionKey"
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
            <button class="btn-ghost" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-primary" @click="submit">
                <span>{{ $t('generic.apply') }}</span>
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
import { VDialog, VInputCheckBox, VInputCombobox, VInputSelect, VInputText } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';
import { v4 as uuid } from 'uuid';

const positions = usePositions();

const dlg = ref<Dialog<Slot | undefined, Slot | undefined> | null>(null);
const createMode = ref<boolean>(false);
const slot = ref<Slot>({
    key: uuid(),
    order: -1,
    criticality: SlotCriticality.Optional,
    positionKeys: [],
});
const primaryPositionKey = ref<PositionKey>('');

async function open(value?: Slot): Promise<Slot | undefined> {
    createMode.value = value === undefined;
    slot.value = value
        ? deepCopy(value)
        : {
              key: uuid(),
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

function submit(): void {
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
