<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-map-location-dot"
        :label="$t('views.account.contact.address.title')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.address.addressLine1">{{ props.modelValue.address.addressLine1 }}</p>
            <p v-if="props.modelValue.address.addressLine2">{{ props.modelValue.address.addressLine2 }}</p>
            <p v-if="props.modelValue.address.zipcode || props.modelValue.address.town">
                {{ props.modelValue.address.zipcode }}
                {{ props.modelValue.address.town }}
            </p>
            <p v-if="props.modelValue.address.country">
                {{ countries.getName(props.modelValue.address.country) }}
            </p>
            <p
                v-if="
                    !props.modelValue.address.addressLine1 &&
                    !props.modelValue.address.addressLine2 &&
                    !props.modelValue.address.zipcode &&
                    !props.modelValue.address.town &&
                    !props.modelValue.address.country
                "
                class="italic"
            >
                {{ $t('generic.no-information') }}
            </p>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">{{$t('views.account.contact.address.hint')}}</p>
            <div class="mb-4">
                <VInputLabel>{{$t('views.account.contact.address.address-line1')}}</VInputLabel>
                <VInputText v-model.trim="value.address.addressLine1" required :placeholder="$t('generic.no-information')" />
            </div>
            <div class="mb-4">
                <VInputLabel>{{$t('views.account.contact.address.address-line2')}}</VInputLabel>
                <VInputText v-model.trim="value.address.addressLine2" :placeholder="$t('generic.optional')" />
            </div>
            <div class="flex flex-col sm:flex-row sm:space-x-4">
                <div class="mb-4 sm:w-36">
                    <VInputLabel>{{$t('views.account.contact.address.zip')}}</VInputLabel>
                    <VInputText v-model.trim="value.address.zipcode" required :placeholder="$t('generic.no-information')" />
                </div>
                <div class="mb-4 sm:flex-grow">
                    <VInputLabel>{{$t('views.account.contact.address.city')}}</VInputLabel>
                    <VInputText v-model.trim="value.address.town" required :placeholder="$t('generic.no-information')" />
                </div>
            </div>
            <div class="mb-4">
                <VInputLabel>{{$t('views.account.contact.address.country')}}</VInputLabel>
                <VInputCombobox v-model="value.address.country" :options="countries.options" required :placeholder="$t('generic.no-information')" />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputCombobox, VInputLabel, VInputText, VInteractiveListItem } from '@/ui/components/common';
import { useCountries } from '@/ui/composables/Countries';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const countries = useCountries();
</script>
