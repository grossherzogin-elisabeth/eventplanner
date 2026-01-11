<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-heading"
        :label="$t('views.settings.ui.menu-title')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            {{ props.modelValue.ui.menuTitle }}
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.settings.ui.menu-title-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.ui.menuTitle"
                    data-test-id="input-menu-title"
                    :errors="errors['menuTitle']"
                    :errors-visible="true"
                    required
                    :label="$t('views.settings.ui.menu-title')"
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
