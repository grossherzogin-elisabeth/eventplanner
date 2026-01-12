<template>
    <VInputCombobox
        :model-value="props.modelValue"
        :label="props.label"
        :hint="props.hint"
        :placeholder="props.placeholder"
        :required="props.required"
        :disabled="props.disabled"
        :errors="props.errors"
        :errors-visible="props.errorsVisible"
        :options="templates.map((it) => ({ label: it?.name ?? '', value: it }))"
        :loading="loading"
        @update:model-value="emit('update:modelValue', $event ?? undefined)"
    >
        <template #item="{ item }">
            <template v-if="item.value">
                <span class="w-0 flex-grow truncate">{{ item.value?.name }}</span>
                <span class="opacity-50">{{ formatDateRange(item.value?.start, item.value?.end, true) }}</span>
            </template>
            <template v-else>
                <slot name="select-none">
                    {{ $t('generic.no-information') }}
                </slot>
            </template>
        </template>
    </VInputCombobox>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useEventUseCase } from '@/application';
import type { Event } from '@/domain';
import { VInputCombobox } from '@/ui/components/common';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: Event;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
    placeholder?: string;
}

type Emits = (e: 'update:modelValue', value: Event | undefined) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const eventUseCase = useEventUseCase();
const templates = ref<(Event | null)[]>([]);
const loading = ref<boolean>(false);

function init(): void {
    fetchTemplates();
}

async function fetchTemplates(): Promise<void> {
    if (loading.value) {
        return;
    }
    loading.value = true;
    const year = new Date().getFullYear();
    const eventsNextYear = await eventUseCase.getEvents(year + 1);
    const eventsCurrentYear = await eventUseCase.getEvents(year);
    const eventsPreviousYear = await eventUseCase.getEvents(year - 1);
    templates.value = [null, ...eventsPreviousYear, ...eventsCurrentYear, ...eventsNextYear];
    loading.value = false;
}

init();
</script>
