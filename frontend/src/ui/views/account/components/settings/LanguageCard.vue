<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-language"
        :label="$t('views.account.app-settings.language')"
        direct
        @update:model-value="selectLanguage($event)"
    >
        <template #default>
            <p v-if="i18n.locale.value" class="truncate">
                {{ $t(`generic.language.${i18n.locale.value}`) }}
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
import { computed } from 'vue';

interface Props {
    modelValue: UserSettings;
}

type Emits = (e: 'update:modelValue', value: UserSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const i18n = useI18n();

const options = computed<InputSelectOption<string>[]>(() => {
    return i18n.availableLocales.map((locale) => ({
        value: locale,
        label: i18n.t(`generic.language.${locale}`),
    }));
});

function selectLanguage(updated: UserSettings): void {
    if (updated.language) {
        i18n.locale.value = updated.language;
        emit('update:modelValue', updated);
    }
}
</script>
