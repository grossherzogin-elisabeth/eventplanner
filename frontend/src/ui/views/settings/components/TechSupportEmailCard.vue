<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-computer"
        :label="$t('views.settings.ui.tech-support-email')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            {{ props.modelValue.ui.technicalSupportEmail }}
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.settings.ui.tech-support-email-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.ui.technicalSupportEmail"
                    :errors="errors['technicalSupportEmail']"
                    :errors-visible="true"
                    required
                    :label="$t('views.settings.ui.tech-support-email')"
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
