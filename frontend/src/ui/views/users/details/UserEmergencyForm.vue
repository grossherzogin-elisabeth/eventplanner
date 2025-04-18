<template>
    <section v-if="user">
        <h2 class="mb-4 font-bold text-secondary">Notfallkontakt</h2>
        <div class="mb-4">
            <VInputLabel>Name des Notfallkontakts</VInputLabel>
            <VInputText
                v-model.trim="user.emergencyContact.name"
                placeholder="Keine Angabe"
                :errors="props.errors['emergencyContact.name']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Telefonnummer des Notfallkontakts</VInputLabel>
            <VInputText
                v-model.trim="user.emergencyContact.phone"
                placeholder="Keine Angabe"
                :errors="props.errors['emergencyContact.phone']"
                :errors-visible="true"
            />
        </div>
        <h2 class="mb-4 mt-8 font-bold text-secondary">Wichtige gesundheitliche Informationen</h2>
        <VInfo class="mb-8">
            Diese Daten sind vertraulich zu behandlen und nur für den Kapitän im Rahmen der Reiseunterlagen bestimmt! Angaben werden von der
            Stammcrew freiwillig gemacht.
        </VInfo>
        <div class="mb-4">
            <VInputLabel>Krankheiten</VInputLabel>
            <VInputTextArea
                v-model.trim="user.diseases"
                placeholder="Keine Angabe"
                :errors="props.errors['diseases']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Medikamente</VInputLabel>
            <VInputTextArea
                v-model.trim="user.medication"
                placeholder="Keine Angabe"
                :errors="props.errors['medication']"
                :errors-visible="true"
            />
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails, ValidationHint } from '@/domain';
import { VInfo, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, ValidationHint[]>;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const user = ref<UserDetails>(props.modelValue);

watch(props.modelValue, () => (user.value = props.modelValue));
watch(() => user.value.emergencyContact.name, emitUpdate);
watch(() => user.value.emergencyContact.phone, emitUpdate);
watch(() => user.value.diseases, emitUpdate);
watch(() => user.value.medication, emitUpdate);
watch(() => user.value.intolerances, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
