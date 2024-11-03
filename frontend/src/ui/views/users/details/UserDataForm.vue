<template>
    <section v-if="user" class="-mx-4">
        <div class="mb-4">
            <VInputLabel>OpenID Connect Subject ID</VInputLabel>
            <VInputText v-model="user.authKey" />
        </div>
        <div class="mb-4">
            <VInputLabel>Geschlecht</VInputLabel>
            <VInputSelect v-model="user.gender" :options="genderOptions" />
        </div>
        <div class="mb-4">
            <VInputLabel>Vorname</VInputLabel>
            <VInputText v-model="user.firstName" required />
        </div>
        <div class="mb-4">
            <VInputLabel>Anzeigename</VInputLabel>
            <VInputText v-model="user.nickName" :placeholder="user.firstName" />
        </div>
        <div class="mb-4">
            <VInputLabel>Zweiter Vorname</VInputLabel>
            <VInputText v-model="user.secondName" />
        </div>
        <div class="mb-4">
            <VInputLabel>Nachname</VInputLabel>
            <VInputText v-model="user.lastName" required />
        </div>
        <div class="mb-4">
            <VInputLabel>Geboren am</VInputLabel>
            <VInputDate v-model="user.dateOfBirth" required />
        </div>
        <div class="mb-4">
            <VInputLabel>Geburtsort</VInputLabel>
            <VInputText v-model="user.placeOfBirth" required />
        </div>
        <div class="mb-4">
            <VInputLabel>Pass Nummer</VInputLabel>
            <VInputText v-model="user.passNr" required />
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { InputSelectOption, UserDetails } from '@/domain';
import { VInputDate, VInputLabel, VInputSelect, VInputText } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

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

function emitUpdate() {
    emit('update:modelValue', user.value);
}
</script>
