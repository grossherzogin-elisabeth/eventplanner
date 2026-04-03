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
                :model-value="value.arrival !== undefined"
                :options="[
                    { label: 'Ja, ich möchte wenn möglich am Vortag anreisen', value: true },
                    { label: 'Nein, ich reise am Tag der Veranstaltung an', value: false },
                ]"
                @update:model-value="update(value, $event)"
            />
            <VInfo class="mt-4">
                {{ $t('components.event-registration-details-card.arrival-note') }}
            </VInfo>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { cropToPrecision, subtractFromDate } from '@/common';
import type { Event, Registration } from '@/domain';
import { VInfo, VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    event: Event;
    modelValue: Registration;
}

type Emits = (e: 'update:modelValue', registration: Registration) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

function update(registration: Registration, dayBeforeArrival: boolean): void {
    if (dayBeforeArrival) {
        registration.arrival = cropToPrecision(subtractFromDate(props.event.start, { days: 1 }), 'days');
    } else {
        registration.arrival = undefined;
    }
}
</script>
