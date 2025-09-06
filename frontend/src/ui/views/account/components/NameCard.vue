<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-user-tag"
        label="Name"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.nickName">
                {{ props.modelValue.title }}
                {{ props.modelValue.nickName }}
                {{ props.modelValue.lastName }}
            </p>
            <p v-else>
                {{ props.modelValue.title }}
                {{ props.modelValue.firstName }}
                {{ props.modelValue.secondName }}
                {{ props.modelValue.lastName }}
            </p>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                Dein Name, wie hier angegeben, muss genau so auf deinem Ausweisdokument stehen, da dieser auch zum Erstellen der offiziellen
                IMO Liste verwendet wird.
            </p>
            <p class="mb-4 text-sm">
                Solltest du mit einem anderen Namen angesprochen werden wollen, kannst du einen Anzeigenamen angeben. Dieser wird in der App
                angezeigt und beim Kammerplan und der Getränkeliste verwendet.
            </p>
            <p class="mb-8 text-sm font-bold">Bitte beachte, dass du dein Ausweisdokument auf Reisen mitführen musst!</p>
            <div class="mb-4">
                <VInputLabel>Anzeigename</VInputLabel>
                <VInputText v-model="value.nickName" :placeholder="value.firstName" />
            </div>
            <div class="mb-4">
                <VInputLabel>Titel</VInputLabel>
                <VInputText v-model="value.title" placeholder="keine Angabe" disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>Vorname</VInputLabel>
                <VInputText v-model="value.firstName" required disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>Zweiter Vorname</VInputLabel>
                <VInputText v-model="value.secondName" placeholder="keine Angabe" disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>Nachname</VInputLabel>
                <VInputText v-model="value.lastName" required placeholder="keine Angabe" disabled />
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
