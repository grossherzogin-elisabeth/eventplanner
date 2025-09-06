<template>
    <VInteractiveListItem
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
            <p class="mb-4 text-sm">
                Alle Daten, die du hier angibst, müssen genau so auch auf deinem Ausweisdokument stehen, da diese zum Erstellen der
                offiziellen IMO Liste verwendet werden.
            </p>
            <p class="mb-8 text-sm font-bold">Bitte beachte, dass du dein Ausweisdokument auf Reisen mitführen musst!</p>
            <div class="mb-4">
                <VInputLabel>Geburtstag</VInputLabel>
                <VInputDate v-model="value.dateOfBirth" disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>Geburtsort</VInputLabel>
                <VInputText v-model="value.placeOfBirth" disabled />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { DateTimeFormat } from '@/common/date';
import type { UserDetails } from '@/domain';
import { VInputDate, VInputLabel, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
