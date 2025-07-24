<template>
    <section>
        <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">Kontaktdaten</h2>
        <div class="grid gap-3 rounded-2xl bg-surface-container bg-opacity-50 p-4 shadow xs:-mx-4 md:bg-transparent md:shadow-none">
            <!-- email -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-envelope"
                label="Email"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <a
                        v-if="props.modelValue.email"
                        :href="`mailto:${props.modelValue.email}`"
                        class="mr-2 text-primary hover:underline"
                        @click.stop=""
                    >
                        {{ props.modelValue.email }}
                    </a>
                    <span v-else> keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <p class="mb-8 text-sm">
                        Diese Email Adresse wird sowohl als Kontakt Email für automatisch Benachrichtigungen, als auch für den Login
                        verwendet. Wenn sie geändert wird, muss sie in beiden Systemen geändert werden.
                    </p>
                    <div class="mb-4">
                        <VInputLabel>Email</VInputLabel>
                        <VInputText v-model="value.email" />
                    </div>
                </template>
            </CardFact>
            <!-- phone -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-phone"
                label="Telefon Festnetz"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <a
                        v-if="props.modelValue.phone"
                        :href="`tel:${props.modelValue.phone}`"
                        class="mr-2 text-primary hover:underline"
                        @click.stop=""
                    >
                        {{ props.modelValue.phone }}
                    </a>
                    <span v-else> keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Telefon Festnetz</VInputLabel>
                        <VInputText v-model="value.phone" />
                    </div>
                </template>
            </CardFact>
            <!-- phone mobile -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-mobile"
                label="Telefon Mobil"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <a
                        v-if="props.modelValue.mobile"
                        :href="`tel:${props.modelValue.mobile}`"
                        class="mr-2 text-primary hover:underline"
                        @click.stop=""
                    >
                        {{ props.modelValue.mobile }}
                    </a>
                    <span v-else> keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Telefon Mobil</VInputLabel>
                        <VInputText v-model="value.mobile" />
                    </div>
                </template>
            </CardFact>
            <!-- phone work -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-briefcase"
                label="Telefon Dienstlich"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <a
                        v-if="props.modelValue.phoneWork"
                        :href="`tel:${props.modelValue.phoneWork}`"
                        class="mr-2 text-primary hover:underline"
                        @click.stop=""
                    >
                        {{ props.modelValue.phoneWork }}
                    </a>
                    <span v-else> keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Telefon Dienstlich</VInputLabel>
                        <VInputText v-model="value.phoneWork" />
                    </div>
                </template>
            </CardFact>
            <!-- address -->
            <CardFact
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
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Straße, Hausnr</VInputLabel>
                        <VInputText v-model.trim="value.address.addressLine1" required placeholder="keine Angabe" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Adresszusatz</VInputLabel>
                        <VInputText v-model.trim="value.address.addressLine2" placeholder="keine Angabe" />
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
            </CardFact>
        </div>
    </section>
</template>
<script lang="ts" setup>
import type { UserDetails } from '@/domain';
import { VInputCombobox, VInputLabel, VInputText } from '@/ui/components/common';
import CardFact from '@/ui/components/user/CardFact.vue';
import { useCountries } from '@/ui/composables/Countries.ts';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const countries = useCountries();
</script>
