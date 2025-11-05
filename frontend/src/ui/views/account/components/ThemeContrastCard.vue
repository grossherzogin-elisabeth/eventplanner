<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-circle-half-stroke"
        :label="$t('views.account.app-settings.contrast')"
        direct
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.contrast">
                {{ $t(`generic.contrast.${props.modelValue.contrast}`) }}
            </span>
            <span v-else> {{ $t('generic.contrast.0') }} </span>
        </template>
        <template #edit="{ value }">
            <VInputSelectionList v-model="value.contrast" :options="options" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { InputSelectOption, UserSettings } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserSettings;
}

type Emits = (e: 'update:modelValue', value: UserSettings) => void;

const { t } = useI18n();

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const options: InputSelectOption<number>[] = [
    { value: 0, label: t('generic.contrast.0') },
    { value: 1, label: t('generic.contrast.1') },
    { value: 2, label: t('generic.contrast.2') },
];
</script>
