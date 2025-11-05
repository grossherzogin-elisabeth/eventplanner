<template>
    <div class="bg-surface-container grid h-96 w-96 grid-cols-7 rounded-2xl p-4">
        <span class="text-center opacity-50">{{ $t('views.home.month-overview.weekdays.mo') }}</span>
        <span class="text-center opacity-50">{{ $t('views.home.month-overview.weekdays.di') }}</span>
        <span class="text-center opacity-50">{{ $t('views.home.month-overview.weekdays.mi') }}</span>
        <span class="text-center opacity-50">{{ $t('views.home.month-overview.weekdays.do') }}</span>
        <span class="text-center opacity-50">{{ $t('views.home.month-overview.weekdays.fr') }}</span>
        <span class="text-center opacity-50">{{ $t('views.home.month-overview.weekdays.sa') }}</span>
        <span class="text-center opacity-50">{{ $t('views.home.month-overview.weekdays.so') }}</span>
        <span v-for="day in days" :key="day.toISOString()" class="text-center">
            {{ day.getDate() }}
        </span>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { addToDate, cropToPrecision, subtractFromDate } from '@/common';
import type { Event } from '@/domain';
import { useEventUseCase } from '@/ui/composables/Application.ts';

const eventUseCase = useEventUseCase();
const events = ref<Event[]>([]);

const days = computed<Date[]>(() => {
    let day = cropToPrecision(new Date(), 'months');
    const month = day.getMonth();
    const daysOfMonth = [];
    while (day.getDay() > 1) {
        day = subtractFromDate(day, { days: 1 });
    }
    while (day.getMonth() !== month) {
        // days of previous month
        daysOfMonth.push(day);
        day = addToDate(day, { days: 1 });
    }
    while (day.getMonth() === month) {
        // days of current month
        daysOfMonth.push(day);
        day = addToDate(day, { days: 1 });
    }
    while (day.getDay() !== 1) {
        // days of next month
        daysOfMonth.push(day);
        day = addToDate(day, { days: 1 });
    }
    return daysOfMonth;
});

function init(): void {
    fetchEvents();
}

async function fetchEvents(): Promise<void> {
    events.value = await eventUseCase.getEvents(new Date().getFullYear());
}

init();
</script>
