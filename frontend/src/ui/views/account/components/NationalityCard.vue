<template>
    <VInteractiveListItem
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
            <p class="mb-4 text-sm">
                Deine Nationalität wird für das Erstellen der offiziellen IMO Liste benötigt. Solltest du mehrere Nationalitäten haben, gib
                hier bitte diejenige an, die auch auf deinem Ausweisdokumentes steht.
            </p>
            <p class="mb-8 text-sm font-bold">Bitte beachte, dass du dein Ausweisdokument auf Reisen mitführen musst!</p>
            <VInputSelectionList v-model="value.nationality" :options="nationalities.options" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';
import { useNationalities } from '@/ui/composables/Nationalities';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const nationalities = useNationalities();
</script>
