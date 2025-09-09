<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-mobile"
        :label="$t('views.account.contact.mobile')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.mobile">{{ props.modelValue.mobile }}</span>
            <span v-else class="italic">{{ $t('generic.no-information') }}</span>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.contact.hint-phone') }}
            </p>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.contact.mobile') }}</VInputLabel>
                <VInputText v-model="value.mobile" />
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
