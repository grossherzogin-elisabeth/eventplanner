<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-palette"
        :label="$t('views.settings.ui.theme-color')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default> #188edc </template>
        <template #edit="{ errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.settings.ui.theme-color-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    model-value="#188edc"
                    data-test-id="input-theme-color"
                    :errors="errors['themeColor']"
                    :errors-visible="true"
                    required
                    :label="$t('views.settings.ui.theme-color')"
                    disabled
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { AppSettings } from '@/domain';
import { VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: AppSettings;
}

type Emits = (e: 'update:modelValue', user: AppSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
