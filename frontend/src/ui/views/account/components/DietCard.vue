<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-utensils"
        label="Ernährungsweise"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.diet">
                {{ diet.getName(props.modelValue.diet) }}
            </span>
            <span v-else>keine Angabe</span>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                Bitte gib an, falls du dich vegetarisch oder vegan ernährst. Diese Information ist wichtig, damit die Kombüse ausreichend
                planen kann.
            </p>
            <VInputSelectionList v-model="value.diet" :options="diet.options" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';
import { useDiet } from '@/ui/composables/Diet.ts';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const diet = useDiet();
</script>
