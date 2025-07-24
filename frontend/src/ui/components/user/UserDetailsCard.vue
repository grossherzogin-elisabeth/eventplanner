<template>
    <section>
        <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">Persönliche Daten</h2>
        <div class="grid gap-3 rounded-2xl bg-surface-container bg-opacity-50 p-4 shadow xs:-mx-4 md:bg-transparent md:shadow-none">
            <!-- id -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-fingerprint"
                label="OpenID Connect Subject ID"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.authKey">
                        {{ props.modelValue.authKey }}
                    </span>
                    <span v-else> Kein Login verküpft </span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>OpenID Connect Subject ID</VInputLabel>
                        <VInputText v-model="value.authKey" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Letzter Login am</VInputLabel>
                        <VInputDate v-model="value.lastLoginAt" disabled />
                    </div>
                </template>
            </CardFact>
            <!-- name -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-user-tag"
                label="Name"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    {{ props.modelValue.title }}
                    {{ props.modelValue.firstName }}
                    {{ props.modelValue.secondName }}
                    {{ props.modelValue.lastName }}
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Anzeigename</VInputLabel>
                        <VInputText v-model="value.nickName" :placeholder="value.firstName" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Titel</VInputLabel>
                        <VInputText v-model="value.title" placeholder="keine Angabe" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Vorname</VInputLabel>
                        <VInputText v-model="value.firstName" required />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Zweiter Vorname</VInputLabel>
                        <VInputText v-model="value.secondName" placeholder="keine Angabe" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Nachname</VInputLabel>
                        <VInputText v-model="value.lastName" required placeholder="keine Angabe" />
                    </div>
                </template>
            </CardFact>
            <!-- gender -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-venus-mars"
                label="Geschlecht"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.gender">
                        {{ gender.getName(props.modelValue.gender) }}
                    </span>
                    <span v-else> Keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <VInputSelectionList v-model="value.gender" :options="gender.options" />
                </template>
            </CardFact>
            <!-- date and place of birth -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-birthday-cake"
                label="Geburtstag und Ort"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.dateOfBirth"> {{ $d(props.modelValue.dateOfBirth, DateTimeFormat.DD_MM_YYYY) }} </span>
                    <span v-else> Keine Angabe </span>
                    <span v-if="props.modelValue.placeOfBirth"> in {{ props.modelValue.placeOfBirth }} </span>
                    <span v-else> in Keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Geburtstag</VInputLabel>
                        <VInputDate v-model="value.dateOfBirth" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Geburtsort</VInputLabel>
                        <VInputText v-model="value.placeOfBirth" />
                    </div>
                </template>
            </CardFact>
            <!-- nationality -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-flag"
                label="Nationalität"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.nationality"> {{ nationalities.getName(props.modelValue.nationality) }} </span>
                    <span v-else> Keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <VInputSelectionList v-model="value.nationality" :options="nationalities.options" />
                </template>
            </CardFact>
            <!-- pass number -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-passport"
                label="Passnummer"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.passNr">{{ props.modelValue.passNr }}</span>
                    <span v-else> Keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Passnummer</VInputLabel>
                        <VInputText v-model="value.passNr" />
                    </div>
                </template>
            </CardFact>
            <!-- note -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-note-sticky"
                label="Notiz"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.comment" class="line-clamp-3">
                        {{ props.modelValue.comment }}
                    </span>
                    <span v-else>Keine Angabe</span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Notiz</VInputLabel>
                        <VInputTextArea v-model="value.comment" />
                    </div>
                </template>
            </CardFact>
            <!-- verified -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-check"
                label="Daten verifiziert am"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.verifiedAt">
                        {{ $d(props.modelValue.verifiedAt, DateTimeFormat.DD_MM_YYYY) }}
                    </span>
                    <span v-else> Keine Angabe </span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Daten verifiziert am</VInputLabel>
                        <VInputDate v-model="value.verifiedAt" />
                    </div>
                </template>
            </CardFact>
        </div>
    </section>
</template>

<script lang="ts" setup>
import { DateTimeFormat } from '@/common/date';
import type { UserDetails } from '@/domain';
import { VInputDate, VInputLabel, VInputSelectionList, VInputText, VInputTextArea } from '@/ui/components/common';
import CardFact from '@/ui/components/user/CardFact.vue';
import { useGender } from '@/ui/composables/Gender.ts';
import { useNationalities } from '@/ui/composables/Nationalities.ts';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const gender = useGender();
const nationalities = useNationalities();
</script>
