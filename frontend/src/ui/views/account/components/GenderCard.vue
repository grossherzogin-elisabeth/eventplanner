<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-venus-mars"
        :label="$t('views.account.personal.gender.label')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.gender">
                {{ gender.getName(props.modelValue.gender) }}
            </span>
            <span v-else> {{ $t('generic.no-information') }} </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">{{ $t('views.account.personal.gender.hint') }}</p>
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
