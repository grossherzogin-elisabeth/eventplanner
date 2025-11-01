<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-anchor"
        :label="$t('views.account.app-settings.preferred-position')"
        :disabled="availablePositions.length < 2"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.preferredPosition"> {{ positions.get(props.modelValue.preferredPosition).name }} </span>
            <span v-else-if="availablePositions.length > 0"> {{ positions.get(availablePositions[0]).name }} </span>
            <span v-else> {{ $t('generic.no-information') }} </span>
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
import type { PositionKey, UserSettings } from '@/domain';
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

const filteredPositions = computed(() => positions.options.value.filter((it) => props.availablePositions.includes(it.value)));
</script>
