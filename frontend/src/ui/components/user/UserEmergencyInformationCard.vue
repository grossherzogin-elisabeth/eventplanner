<template>
    <section>
        <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">Notfallinformationen</h2>
        <div class="grid gap-3 rounded-2xl bg-surface-container bg-opacity-50 p-4 shadow xs:-mx-4 md:bg-transparent md:shadow-none">
            <!-- Emergency Contact -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-address-book"
                label="Notfallkontakt"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="!props.modelValue.emergencyContact.name && !props.modelValue.emergencyContact.phone"> Keine Angabe </span>
                    <template v-else>
                        <span v-if="props.modelValue.emergencyContact.name"> {{ props.modelValue.emergencyContact?.name }}, </span>
                        <span v-else> Unbekannte Person, </span>
                        <a
                            v-if="props.modelValue.emergencyContact?.phone"
                            :href="`tel:${props.modelValue.emergencyContact.phone}`"
                            class="mr-2 text-primary hover:underline"
                            @click.stop=""
                        >
                            {{ props.modelValue.emergencyContact?.phone }}
                        </a>
                    </template>
                </template>
                <template #edit="{ value }">
                    <p class="mb-8 text-sm">
                        Im Notfall zählt jede Sekunde. Hier kannst du eine Person hinterlegen, die wir im Falle eines medizinischen Notfalls
                        oder einer anderen dringenden Situation kontaktieren können. Bitte gib den Namen und die Telefonnummer der Person
                        an.
                    </p>
                    <div class="mb-4">
                        <VInputLabel>Name des Notfallkontaktes</VInputLabel>
                        <VInputText v-model="value.emergencyContact.name" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Telefonnummer des Notfallkontakt</VInputLabel>
                        <VInputText v-model="value.emergencyContact.phone" />
                    </div>
                </template>
            </CardFact>
            <!-- medication -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-pills"
                label="Medikamente"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.medication"> [zum Ansehen klicken] </span>
                    <span v-else>Keine Angabe</span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Medikamente</VInputLabel>
                        <VInputTextArea v-model="value.medication" />
                    </div>
                    <VWarning>
                        Diese Daten sind vertraulich zu behandlen und nur für den Kapitän im Rahmen der Reiseunterlagen bestimmt! Alle
                        Angaben werden von den Nutzern freiwillig gemacht.
                    </VWarning>
                </template>
            </CardFact>
            <!-- diseases -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-file-medical-alt"
                label="Krankheiten"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.diseases"> [zum Ansehen klicken] </span>
                    <span v-else>Keine Angabe</span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Krankheiten</VInputLabel>
                        <VInputTextArea v-model="value.diseases" />
                    </div>
                    <VWarning>
                        Diese Daten sind vertraulich zu behandlen und nur für den Kapitän im Rahmen der Reiseunterlagen bestimmt! Alle
                        Angaben werden von den Nutzern freiwillig gemacht.
                    </VWarning>
                </template>
            </CardFact>
        </div>
    </section>
</template>
<script lang="ts" setup>
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputText, VInputTextArea, VWarning } from '@/ui/components/common';
import CardFact from '@/ui/components/user/CardFact.vue';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
