<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-envelope"
        :label="$t('views.account.contact.email')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            {{ props.modelValue.email }}
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.contact.hint-mail') }}
            </p>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.contact.email') }}</VInputLabel>
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
