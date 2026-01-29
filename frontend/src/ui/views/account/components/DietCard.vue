<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-utensils"
        :label="$t('domain.user.diet')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.diet" class="truncate">
                {{ diet.getName(props.modelValue.diet) }}
            </p>
            <p v-else class="truncate italic">
                {{ $t('generic.no-information') }}
            </p>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.diet.diet-description') }}
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
