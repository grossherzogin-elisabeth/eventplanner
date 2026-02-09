<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-calendar"
        dialog-type="modal"
        :label="$t('components.event-registration-details-card.arrival')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.arrival"> {{ $t('generic.yes') }} </span>
            <span v-else> {{ $t('generic.no') }} </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('components.event-registration-details-card.arrival-description') }}
            </p>
            <VInputSelectionList
                v-model="value.arrival"
                :options="[
                    { label: 'Ja, ich möchte wenn möglich am Vortag anreisen', value: previousDay },
                    { label: 'Nein, ich reise am Tag der Veranstaltung an', value: undefined },
                ]"
            />
            <VInfo class="mt-4">
                {{ $t('components.event-registration-details-card.arrival-note') }}
            </VInfo>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { cropToPrecision, subtractFromDate } from '@/common';
import type { Event, Registration } from '@/domain';
import { VInfo, VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    event: Event;
    modelValue: Registration;
}

type Emits = (e: 'update:modelValue', user: Registration) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const previousDay = computed<Date>(() => cropToPrecision(subtractFromDate(props.event.start, { days: 1 }), 'days'));
</script>
