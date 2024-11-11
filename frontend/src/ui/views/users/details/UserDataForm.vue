<template>
    <section v-if="user" class="">
        <h2 class="mb-4 font-bold text-secondary">App</h2>
        <div class="mb-4">
            <VInputLabel>OpenID Connect Subject ID</VInputLabel>
            <VInputText v-model="user.authKey" placeholder="Nicht verknüpft" />
        </div>
        <div class="mb-4">
            <VInputLabel>Anzeigename</VInputLabel>
            <VInputText v-model="user.nickName" :placeholder="user.firstName" />
        </div>

        <h2 class="mb-4 mt-8 font-bold text-secondary">Persönliche Daten</h2>
        <div class="mb-4 sm:w-64">
            <VInputLabel>Geschlecht</VInputLabel>
            <VInputSelect v-model="user.gender" :options="genderOptions" placeholder="keine Angabe" />
        </div>
        <div class="mb-4 sm:w-64">
            <VInputLabel>Titel</VInputLabel>
            <VInputText v-model="user.title" placeholder="keine Angabe" />
        </div>
        <div class="mb-4">
            <VInputLabel>Vorname</VInputLabel>
            <VInputText v-model="user.firstName" required />
        </div>
        <div class="mb-4">
            <VInputLabel>Zweiter Vorname</VInputLabel>
            <VInputText v-model="user.secondName" placeholder="keine Angabe" />
        </div>
        <div class="mb-4">
            <VInputLabel>Nachname</VInputLabel>
            <VInputText v-model="user.lastName" required placeholder="keine Angabe" />
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-64">
                <VInputLabel>Geboren am</VInputLabel>
                <VInputDate v-model="user.dateOfBirth" required placeholder="keine Angabe" />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputLabel>Geburtsort</VInputLabel>
                <VInputText v-model="user.placeOfBirth" required placeholder="keine Angabe" />
            </div>
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-64">
                <VInputLabel>Pass Nummer</VInputLabel>
                <VInputText v-model="user.passNr" required placeholder="keine Angabe" />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputLabel>Nationalität</VInputLabel>
                <VInputCombobox v-model="user.nationality" :options="nationalities.options" required />
            </div>
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { InputSelectOption, UserDetails } from '@/domain';
import { VInputCombobox, VInputDate, VInputLabel, VInputSelect, VInputText } from '@/ui/components/common';
import { useNationalities } from '@/ui/composables/Nationalities.ts';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const nationalities = useNationalities();

const user = ref<UserDetails>(props.modelValue);
const genderOptions: InputSelectOption[] = [
    { value: 'm', label: 'männlich' },
    { value: 'f', label: 'weiblich' },
    { value: 'd', label: 'divers' },
];

watch(props.modelValue, () => (user.value = props.modelValue));
watch(() => user.value.gender, emitUpdate);
watch(() => user.value.firstName, emitUpdate);
watch(() => user.value.secondName, emitUpdate);
watch(() => user.value.nickName, emitUpdate);
watch(() => user.value.lastName, emitUpdate);
watch(() => user.value.dateOfBirth, emitUpdate);
watch(() => user.value.placeOfBirth, emitUpdate);
watch(() => user.value.passNr, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
