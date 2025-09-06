<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-venus-mars"
        label="Geschlecht"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.gender">
                {{ gender.getName(props.modelValue.gender) }}
            </span>
            <span v-else> Keine Angabe </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">Mit welchem Geschlecht identifizierst du dich?</p>
            <VInputSelectionList v-model="value.gender" :options="gender.options" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';
import { useGender } from '@/ui/composables/Gender';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const gender = useGender();
</script>
