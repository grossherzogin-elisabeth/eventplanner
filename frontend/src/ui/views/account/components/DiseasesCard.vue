<template>
    <VInteractiveListItem
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
            <p class="mb-4 text-sm">
                Wenn auf einer Reise mal etwas passiert, ist es gegebenenfalls wichtig, dass jemand an Bord weiß, ob du Krankheiten hast,
                die bestimmte Behandlungen erfordern.
            </p>
            <p class="mb-8 text-sm font-bold">
                Alle Angaben sind freiwillig! Deine Daten werden verschlüsselt gespeichert und sind nur für autorisierte Personen einsehbar!
            </p>
            <div class="mb-4">
                <VInputLabel>Krankheiten</VInputLabel>
                <VInputTextArea v-model="value.diseases" />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputTextArea, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
