<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-bell"
        :label="$t('views.account.app-settings.notifications')"
        direct
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default> Aktiviert </template>
        <template #edit="{ value }"> TODO </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserSettings } from '@/domain';
import { VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserSettings;
}

type Emits = (e: 'update:modelValue', value: UserSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
