<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-address-book"
        :label="$t('domain.user.emergency-contact')"
        :validate="UserService.validateEmergencyContact"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="!props.modelValue.emergencyContact.name && !props.modelValue.emergencyContact.phone">
                {{ $t('generic.no-information') }}
            </span>
            <template v-else>
                {{ props.modelValue.emergencyContact?.name ?? $t('generic.no-information') }},
                {{ props.modelValue.emergencyContact?.phone ?? $t('generic.no-information') }}
            </template>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.emergency.emergency-contact-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.emergencyContact.name"
                    :errors="errors['emergencyContact.name']"
                    :errors-visible="true"
                    :label="$t('domain.emergency-contact.name')"
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.emergencyContact.phone"
                    :errors="errors['emergencyContact.phone']"
                    :errors-visible="true"
                    :label="$t('domain.emergency-contact.phone')"
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
