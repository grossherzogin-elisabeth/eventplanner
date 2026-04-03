<template>
    <div class="grid gap-x-32 gap-y-8 md:grid-cols-1">
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">{{ $t('views.settings.notifications.title') }}</h2>
            <VInteractiveList>
                <EmailCard data-test-id="email" :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <TeamsWebhookCard
                    data-test-id="teams-webhook"
                    :model-value="props.modelValue"
                    @update:model-value="emit('update:modelValue', $event)"
                />
            </VInteractiveList>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { AppSettings } from '@/domain';
import { VInteractiveList } from '@/ui/components/common';
import EmailCard from '../components/EmailCard.vue';
import TeamsWebhookCard from '../components/TeamsWebhookCard.vue';

interface Props {
    modelValue: AppSettings;
}

type Emits = (e: 'update:modelValue', user: AppSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
