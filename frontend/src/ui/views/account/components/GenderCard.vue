<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-venus-mars"
        dialog-type="modal"
        :label="$t('domain.user.gender')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.gender" class="truncate">
                {{ gender.getName(props.modelValue.gender) }}
            </p>
            <p v-else class="truncate italic">
                {{ $t('generic.no-information') }}
            </p>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">{{ $t('views.account.personal.gender-description') }}</p>
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
