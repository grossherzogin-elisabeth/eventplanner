<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-passport"
        label="Personalausweis / Passnummer"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.passNr">{{ props.modelValue.passNr }}</span>
            <span v-else> Keine Angabe </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                Bitte gib die Nummer eines offiziellen Ausweisdokuments an. Dies kann entweder dein Personalausweis oder alternativ auch
                dein Reisepass sein.
            </p>
            <p class="mb-8 text-sm font-bold">Bitte beachte, dass du dieses Ausweisdokument auf Reisen mitführen musst!</p>
            <p class="mb-8 text-sm italic">
                <a href="https://commons.wikimedia.org/wiki/File:Personalausweis-nummer.png" target="_blank" class="link">
                    So findest du deine Personalausweis Nummer
                    <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
                </a>
            </p>
            <div class="mb-4">
                <VInputLabel>Personalausweis oder Passnummer</VInputLabel>
                <VInputText v-model="value.passNr" />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
