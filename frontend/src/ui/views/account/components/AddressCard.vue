<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-map-location-dot"
        label="Adresse"
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
                keine Angabe
            </p>
        </template>
        <template #edit="{ value }">
            <p class="mb-8 text-sm">Bitte gib deine primäre Wohnanschrift an.</p>
            <div class="mb-4">
                <VInputLabel>Straße und Hausnummer</VInputLabel>
                <VInputText v-model.trim="value.address.addressLine1" required placeholder="keine Angabe" />
            </div>
            <div class="mb-4">
                <VInputLabel>Adresszusatz</VInputLabel>
                <VInputText v-model.trim="value.address.addressLine2" placeholder="optional" />
            </div>
            <div class="flex flex-col sm:flex-row sm:space-x-4">
                <div class="mb-4 sm:w-36">
                    <VInputLabel>PLZ</VInputLabel>
                    <VInputText v-model.trim="value.address.zipcode" required placeholder="keine Angabe" />
                </div>
                <div class="mb-4 sm:flex-grow">
                    <VInputLabel>Ort</VInputLabel>
                    <VInputText v-model.trim="value.address.town" required placeholder="keine Angabe" />
                </div>
            </div>
            <div class="mb-4">
                <VInputLabel>Land</VInputLabel>
                <VInputCombobox v-model="value.address.country" :options="countries.options" required placeholder="keine Angabe" />
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
