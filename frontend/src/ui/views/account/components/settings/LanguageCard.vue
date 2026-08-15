<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-language"
        :label="$t('views.account.app-settings.language')"
        direct
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.language" class="truncate">
                {{ $t(`generic.language.${props.modelValue.language}`) }}
            </p>
            <p v-else class="truncate">
                {{ $t('generic.theme.system') }}
            </p>
        </template>
        <template #edit="{ value }">
            <VInputSelectionList v-model="value.language" :options="options" />
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

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { t } = useI18n();

const options: InputSelectOption<string>[] = [
    { value: 'de', label: t('generic.language.de') },
    { value: 'en', label: t('generic.language.en') },
];
</script>
