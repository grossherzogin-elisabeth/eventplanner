<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-phone"
        label="Telefon Festnetz"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.phone">{{ props.modelValue.phone }}</span>
            <span v-else class="italic">keine Angabe</span>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                Wie können wir dich erreichen? Bitte gib mindestens eine Telefonnummer an: Festnetz, mobil oder dienstlich.
            </p>
            <div class="mb-4">
                <VInputLabel>Telefon Festnetz</VInputLabel>
                <VInputText v-model="value.phone" />
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
