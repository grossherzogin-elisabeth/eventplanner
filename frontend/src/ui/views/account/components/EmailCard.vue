<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-envelope"
        label="Email"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            {{ props.modelValue.email }}
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                Deine Email Adresse wird sowohl als Kontakt Email für Benachrichtigungen, als auch für deinen Login verwendet. Wenn du sie
                ändern möchtest, schreib uns bitte eine Email, damit wir auch den Login für dich anpassen können.
            </p>
            <div class="mb-4">
                <VInputLabel>Email</VInputLabel>
                <VInputText v-model="value.email" disabled />
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
