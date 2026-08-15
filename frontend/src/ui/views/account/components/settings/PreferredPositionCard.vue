<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-anchor"
        dialog-type="modal"
        :label="$t('views.account.app-settings.preferred-position')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.preferredPosition" class="truncate">
                {{ positions.get(props.modelValue.preferredPosition).name }}
            </p>
            <p v-else-if="availablePositions.length > 0" class="truncate">
                {{ positions.get(availablePositions[0]).name }}
            </p>
            <p v-else class="truncate italic">
                {{ $t('generic.no-information') }}
            </p>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.app-settings.preferred-position-description') }}
            </p>
            <VInputSelectionList v-model="value.preferredPosition" :options="filteredPositions" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { InputSelectOption, PositionKey, UserSettings } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';

interface Props {
    modelValue: UserSettings;
    availablePositions: PositionKey[];
}

type Emits = (e: 'update:modelValue', value: UserSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const positions = usePositions();

const filteredPositions = computed<InputSelectOption<PositionKey | undefined>[]>(() => {
    return positions.options.value
        .filter((it) => isAvailablePosition(it.value, true))
        .map((it) => ({
            value: it.value,
            label: it.label,
            disabled: !isAvailablePosition(it.value),
        }));
});

function isAvailablePosition(position: PositionKey | undefined, includeCurrent: boolean = false): boolean {
    if (!position) {
        return false;
    }
    if (props.availablePositions.includes(position)) {
        return true;
    }
    return includeCurrent && position === props.modelValue.preferredPosition;
}
</script>
