<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-note-sticky"
        :label="$t('components.event-registration-details-card.note')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.note" class="line-clamp-3 italic">
                {{ props.modelValue.note }}
            </p>
            <p v-else class="italic">-</p>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('components.event-registration-details-card.note-description') }}
            </p>
            <div class="mb-4">
                <VInputTextArea
                    v-model="value.note"
                    data-test-id="input-note"
                    :errors="errors['note']"
                    :errors-visible="true"
                    required
                    :label="$t('components.event-registration-details-card.note')"
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { Registration } from '@/domain';
import { VInputTextArea, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: Registration;
}

type Emits = (e: 'update:modelValue', user: Registration) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
