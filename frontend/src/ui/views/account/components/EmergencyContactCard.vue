<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-address-book"
        label="Notfallkontakt"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="!props.modelValue.emergencyContact.name && !props.modelValue.emergencyContact.phone"> Keine Angabe </span>
            <template v-else>
                {{ props.modelValue.emergencyContact?.name ?? 'keine Angabe' }},
                {{ props.modelValue.emergencyContact?.phone ?? 'keine Angabe' }}
            </template>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                Im Notfall zählt jede Sekunde. Hier kannst du eine Person hinterlegen, die wir im Falle eines medizinischen Notfalls oder
                einer anderen dringenden Situation kontaktieren können. Bitte gib Namen und Telefonnummer der Person an.
            </p>
            <div class="mb-4">
                <VInputLabel>Name des Notfallkontaktes</VInputLabel>
                <VInputText v-model="value.emergencyContact.name" />
            </div>
            <div class="mb-4">
                <VInputLabel>Telefonnummer des Notfallkontakt</VInputLabel>
                <VInputText v-model="value.emergencyContact.phone" />
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
