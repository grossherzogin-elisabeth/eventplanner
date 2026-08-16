<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-bed"
        dialog-type="modal"
        :label="$t('domain.registration.overnight-stay')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.overnightStay"> {{ $t('generic.yes') }} </span>
            <span v-else> {{ $t('generic.no') }} </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('components.registration-overnight-stay-card.description') }}
            </p>
            <VInputSelectionList
                v-model="value.overnightStay"
                :options="[
                    { label: $t('generic.yes'), value: true },
                    { label: $t('generic.no'), value: false },
                ]"
            />
            <VInfo class="mt-4">
                {{ $t('components.registration-overnight-stay-card.note') }}
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
