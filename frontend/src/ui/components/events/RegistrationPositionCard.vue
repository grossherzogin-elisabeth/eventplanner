<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-anchor"
        dialog-type="modal"
        :label="$t('components.event-registration-details-card.position')"
        :disabled="filteredPositions.length < 2"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.positionKey"> {{ positions.get(props.modelValue.positionKey).name }} </span>
            <span v-else> {{ $t('generic.no-information') }} </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('components.event-registration-details-card.position-description') }}
            </p>
            <VInputSelectionList v-model="value.positionKey" :options="filteredPositions" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { InputSelectOption, PositionKey, Registration } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions';
import { useSession } from '@/ui/composables/Session';

interface Props {
    modelValue: Registration;
}

type Emits = (e: 'update:modelValue', value: Registration) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { signedInUser } = useSession();
const positions = usePositions();

const filteredPositions = computed<InputSelectOption<PositionKey | undefined>[]>(() =>
    positions.options.value.filter(
        (it) => it.value && (signedInUser.value?.positions.includes(it.value) || props.modelValue.positionKey === it.value)
    )
);
</script>
