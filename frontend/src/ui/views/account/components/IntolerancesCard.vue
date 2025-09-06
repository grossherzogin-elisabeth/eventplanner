<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-wheat-awn-circle-exclamation"
        label="Unverträglichkeiten"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.intolerances" class="line-clamp-3">
                {{ props.modelValue.intolerances }}
            </span>
            <span v-else>keine Angabe</span>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">Hier kannst du Unverträglichkeiten und Allergien angeben, die der Kombüse bekannt sein sollten.</p>
            <div class="mb-4">
                <VInputLabel>Unverträglichkeiten</VInputLabel>
                <VInputTextArea v-model="value.intolerances" />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputTextArea, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
