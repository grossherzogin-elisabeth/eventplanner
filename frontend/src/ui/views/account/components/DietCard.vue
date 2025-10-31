<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-utensils"
        :label="$t('views.account.diet.label')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.diet">
                {{ diet.getName(props.modelValue.diet) }}
            </span>
            <span v-else>{{ $t('generic.no-information') }}</span>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.diet.hint') }}
            </p>
            <VInputSelectionList v-model="value.diet" :options="diet.options" required />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';
import { useDiet } from '@/ui/composables/Diet';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const diet = useDiet();
</script>
