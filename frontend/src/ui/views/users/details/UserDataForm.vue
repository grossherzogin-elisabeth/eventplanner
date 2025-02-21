<template>
    <section v-if="user" class="">
        <h2 class="mb-4 font-bold text-secondary">App</h2>
        <div class="mb-4">
            <VInputLabel>OpenID Connect Subject ID</VInputLabel>
            <VInputText
                v-model.trim="user.authKey"
                placeholder="Nicht verknüpft"
                :errors="props.errors['authKey']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Anzeigename</VInputLabel>
            <VInputText
                v-model.trim="user.nickName"
                :placeholder="user.firstName"
                :errors="props.errors['nickName']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Erstellt am</VInputLabel>
            <VInputDate v-model.trim="user.createdAt" disabled :errors="props.errors['createdAt']" :errors-visible="true" />
        </div>
        <div class="mb-4">
            <VInputLabel>Letzte Änderung am</VInputLabel>
            <VInputDate v-model.trim="user.updatedAt" disabled :errors="props.errors['updatedAt']" :errors-visible="true" />
        </div>
        <div class="mb-4">
            <VInputLabel>Letzter Login am</VInputLabel>
            <VInputDate v-model.trim="user.lastLoginAt" disabled :errors="props.errors['lastLoginAt']" :errors-visible="true" />
        </div>
        <div class="mb-4">
            <VInputLabel>Daten verifiziert am</VInputLabel>
            <VInputDate v-model.trim="user.verifiedAt" :errors="props.errors['verifiedAt']" :errors-visible="true" />
        </div>

        <h2 class="mb-4 mt-8 font-bold text-secondary">Persönliche Daten</h2>
        <div class="mb-4 sm:w-64">
            <VInputLabel>Geschlecht</VInputLabel>
            <VInputSelect
                v-model="user.gender"
                :options="genderOptions"
                placeholder="keine Angabe"
                :errors="props.errors['gender']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4 sm:w-64">
            <VInputLabel>Titel</VInputLabel>
            <VInputText v-model.trim="user.title" placeholder="keine Angabe" :errors="props.errors['title']" :errors-visible="true" />
        </div>
        <div class="mb-4">
            <VInputLabel>Vorname</VInputLabel>
            <VInputText v-model.trim="user.firstName" required :errors="props.errors['firstName']" :errors-visible="true" />
        </div>
        <div class="mb-4">
            <VInputLabel>Zweiter Vorname</VInputLabel>
            <VInputText
                v-model.trim="user.secondName"
                placeholder="keine Angabe"
                :errors="props.errors['secondName']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Nachname</VInputLabel>
            <VInputText
                v-model.trim="user.lastName"
                required
                placeholder="keine Angabe"
                :errors="props.errors['lastName']"
                :errors-visible="true"
            />
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-64">
                <VInputLabel>Geboren am</VInputLabel>
                <VInputDate
                    v-model="user.dateOfBirth"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['dateOfBirth']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputLabel>Geburtsort</VInputLabel>
                <VInputText
                    v-model.trim="user.placeOfBirth"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['placeOfBirth']"
                    :errors-visible="true"
                />
            </div>
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-64">
                <VInputLabel>Pass Nummer</VInputLabel>
                <VInputText
                    v-model.trim="user.passNr"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['passNr']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputLabel>Nationalität</VInputLabel>
                <VInputCombobox
                    v-model="user.nationality"
                    :options="nationalities.options"
                    required
                    :errors="props.errors['nationality']"
                    :errors-visible="true"
                />
            </div>
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { InputSelectOption, UserDetails, ValidationHint } from '@/domain';
import { VInputCombobox, VInputDate, VInputLabel, VInputSelect, VInputText } from '@/ui/components/common';
import { useNationalities } from '@/ui/composables/Nationalities.ts';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, ValidationHint[]>;
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
