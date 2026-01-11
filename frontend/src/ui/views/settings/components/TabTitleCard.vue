<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-window-maximize"
        :label="$t('views.settings.ui.tab-title')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            {{ props.modelValue.ui.tabTitle }}
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.settings.ui.tab-title-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.ui.tabTitle"
                    data-test-id="input-tab-title"
                    :errors="errors['tabTitle']"
                    :errors-visible="true"
                    required
                    :label="$t('views.settings.ui.tab-title')"
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
