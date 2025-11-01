<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-map-location-dot"
        :label="$t('domain.user.address')"
        :validate="UserService.validateAddress"
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
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">{{ $t('views.account.contact.address-description') }}</p>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.address.addressLine1"
                    :label="$t('domain.address.address-line-1')"
                    required
                    :errors="errors['address.addressLine1']"
                    :errors-visible="true"
                    :placeholder="$t('generic.no-information')"
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.address.addressLine2"
                    :label="$t('domain.address.address-line-2')"
                    :errors="errors['address.addressLine2']"
                    :errors-visible="true"
                    :placeholder="$t('generic.optional')"
                />
            </div>
            <div class="flex flex-col sm:flex-row sm:space-x-4">
                <div class="mb-4 sm:w-36">
                    <VInputText
                        v-model.trim="value.address.zipcode"
                        :label="$t('domain.address.zipcode')"
                        required
                        :errors="errors['address.zipcode']"
                        :errors-visible="true"
                        :placeholder="$t('generic.no-information')"
                    />
                </div>
                <div class="mb-4 sm:flex-grow">
                    <VInputText
                        v-model.trim="value.address.town"
                        :label="$t('domain.address.town')"
                        required
                        :errors="errors['address.town']"
                        :errors-visible="true"
                        :placeholder="$t('generic.no-information')"
                    />
                </div>
            </div>
            <div class="mb-4">
                <VInputCombobox
                    v-model="value.address.country"
                    :label="$t('domain.address.country')"
                    :options="countries.options"
                    required
                    :errors="errors['address.country']"
                    :errors-visible="true"
                    :placeholder="$t('generic.no-information')"
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { UserService } from '@/domain';
import { VInputCombobox, VInputText, VInteractiveListItem } from '@/ui/components/common';
import { useCountries } from '@/ui/composables/Countries';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const countries = useCountries();
</script>
