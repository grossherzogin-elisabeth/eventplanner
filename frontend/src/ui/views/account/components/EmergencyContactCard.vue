<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-address-book"
        :label="$t('views.account.emergency.contact.title')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="!props.modelValue.emergencyContact.name && !props.modelValue.emergencyContact.phone">{{ $t('generic.no-information') }}</span>
            <template v-else>
                {{ props.modelValue.emergencyContact?.name ?? $t('generic.no-information') }},
                {{ props.modelValue.emergencyContact?.phone ?? $t('generic.no-information') }}
            </template>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">
                {{$t('views.account.emergency.contact.hint')}}

            </p>
            <div class="mb-4">
                <VInputLabel>{{$t('views.account.emergency.contact.name')}}</VInputLabel>
                <VInputText v-model="value.emergencyContact.name" />
            </div>
            <div class="mb-4">
                <VInputLabel>{{$t('views.account.emergency.contact.phone')}}</VInputLabel>
                <VInputText v-model="value.emergencyContact.phone" />
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
