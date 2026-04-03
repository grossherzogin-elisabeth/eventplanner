<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-brands fa-microsoft"
        :label="$t('views.settings.notifications.teams-webhook-url')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.notifications.teamsWebhookUrl" class="truncate">
                {{ $t('views.settings.notifications.teams-webhook-set-up') }}
            </p>
            <p v-else class="truncate">
                {{ $t('views.settings.notifications.teams-webhook-not-set-up') }}
            </p>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.settings.notifications.teams-webhook-url-description') }}
            </p>
            <div class="mb-4">
                <VInputTextArea
                    v-model="value.notifications.teamsWebhookUrl"
                    data-test-id="input-teams-webhook"
                    :errors="errors['teamsWebhookUrl']"
                    :errors-visible="true"
                    required
                    :label="$t('views.settings.notifications.teams-webhook-url')"
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { AppSettings } from '@/domain';
import { VInputTextArea, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: AppSettings;
}

type Emits = (e: 'update:modelValue', user: AppSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
