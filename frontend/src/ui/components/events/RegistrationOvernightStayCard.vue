<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-bed"
        dialog-type="modal"
        :label="$t('components.event-registration-details-card.overnight-stay')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.overnightStay"> {{ $t('generic.yes') }} </span>
            <span v-else> {{ $t('generic.no') }} </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('components.event-registration-details-card.overnight-stay-description') }}
            </p>
            <VInputSelectionList
                v-model="value.overnightStay"
                :options="[
                    { label: 'Ja', value: true },
                    { label: 'Nein', value: false },
                ]"
            />
            <VInfo class="mt-4">
                {{ $t('components.event-registration-details-card.overnight-stay-note') }}
            </VInfo>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { Registration } from '@/domain';
import { VInfo, VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: Registration;
}

type Emits = (e: 'update:modelValue', user: Registration) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
