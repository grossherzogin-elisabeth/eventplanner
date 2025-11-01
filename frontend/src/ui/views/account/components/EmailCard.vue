<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-envelope"
        :label="$t('domain.user.email')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            {{ props.modelValue.email }}
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.contact.email-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.email"
                    :errors="errors['email']"
                    :errors-visible="true"
                    required
                    :label="$t('domain.user.email')"
                    disabled
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
