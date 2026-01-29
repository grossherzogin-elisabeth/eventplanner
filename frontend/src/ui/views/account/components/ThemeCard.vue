<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-paint-brush"
        :label="$t('views.account.app-settings.theme')"
        direct
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.theme" class="truncate">
                {{ $t(`generic.theme.${props.modelValue.theme}`) }}
            </p>
            <p v-else class="truncate">
                {{ $t('generic.theme.system') }}
            </p>
        </template>
        <template #edit="{ value }">
            <VInputSelectionList v-model="value.theme" :options="options" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { InputSelectOption, UserSettings } from '@/domain';
import { Theme } from '@/domain';
import { VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserSettings;
}

type Emits = (e: 'update:modelValue', value: UserSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { t } = useI18n();

const options: InputSelectOption<Theme>[] = [
    { value: Theme.System, label: t('generic.theme.system') },
    { value: Theme.Dark, label: t('generic.theme.dark') },
    { value: Theme.Light, label: t('generic.theme.light') },
];
</script>
