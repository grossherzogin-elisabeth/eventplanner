<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-briefcase"
        label="Telefon Dienstlich"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.phoneWork">{{ props.modelValue.phoneWork }}</span>
            <span v-else class="italic">keine Angabe</span>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">Wie können wir dich erreichen?</p>
            <div class="mb-4">
                <VInputLabel>Telefon Dienstlich</VInputLabel>
                <VInputText v-model="value.phoneWork" />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
