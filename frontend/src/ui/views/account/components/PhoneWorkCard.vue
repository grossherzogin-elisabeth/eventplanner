<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-briefcase"
        :label="$t('domain.user.phone-work')"
        :validate="UserService.validatePhoneWork"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.phoneWork" class="truncate">
                {{ props.modelValue.phoneWork }}
            </p>
            <p v-else class="truncate italic">
                {{ $t('generic.no-information') }}
            </p>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.contact.phone-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.phoneWork"
                    :errors="errors['phoneWork']"
                    :errors-visible="true"
                    :label="$t('domain.user.phone-work')"
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { UserService } from '@/domain';
import { VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
