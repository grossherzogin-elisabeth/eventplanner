<template>
    <section v-if="user">
        <h2 class="mb-4 font-bold text-secondary">App</h2>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.authKey"
                label="OpenID Connect Subject ID"
                placeholder="Nicht verknüpft"
                :errors="props.errors['authKey']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.nickName"
                label="Anzeigename"
                :placeholder="user.firstName"
                :errors="props.errors['nickName']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputDate
                v-model.trim="user.createdAt"
                label="Erstellt am"
                disabled
                :errors="props.errors['createdAt']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputDate
                v-model.trim="user.updatedAt"
                label="Letzte Änderung am"
                disabled
                :errors="props.errors['updatedAt']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputDate
                v-model.trim="user.lastLoginAt"
                label="Letzter Login am"
                disabled
                :errors="props.errors['lastLoginAt']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputDate
                v-model.trim="user.verifiedAt"
                label="Daten verifiziert am"
                :errors="props.errors['verifiedAt']"
                :errors-visible="true"
            />
        </div>

        <h2 class="mb-4 mt-8 font-bold text-secondary">Persönliche Daten</h2>
        <div class="mb-4 sm:w-64">
            <VInputSelect
                v-model="user.gender"
                label="Geschlecht"
                :options="genderOptions"
                placeholder="keine Angabe"
                :errors="props.errors['gender']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4 sm:w-64">
            <VInputText
                v-model.trim="user.title"
                label="Titel"
                placeholder="keine Angabe"
                :errors="props.errors['title']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputText v-model.trim="user.firstName" label="Vorname" required :errors="props.errors['firstName']" :errors-visible="true" />
        </div>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.secondName"
                label="Zweiter Vorname"
                placeholder="keine Angabe"
                :errors="props.errors['secondName']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.lastName"
                label="Nachname"
                required
                placeholder="keine Angabe"
                :errors="props.errors['lastName']"
                :errors-visible="true"
            />
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-64">
                <VInputDate
                    v-model="user.dateOfBirth"
                    label="Geboren am"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['dateOfBirth']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputText
                    v-model.trim="user.placeOfBirth"
                    label="Geburtsort"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['placeOfBirth']"
                    :errors-visible="true"
                />
            </div>
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-64">
                <VInputText
                    v-model.trim="user.passNr"
                    label="Pass Nummer"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['passNr']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputCombobox
                    v-model="user.nationality"
                    label="Nationalität"
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
import { VInputCombobox, VInputDate, VInputSelect, VInputText } from '@/ui/components/common';
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
