<template>
    <div class="grid h-96 w-96 grid-cols-7 rounded-2xl bg-surface-container p-4">
        <span class="text-center opacity-50">Mo</span>
        <span class="text-center opacity-50">Di</span>
        <span class="text-center opacity-50">Mi</span>
        <span class="text-center opacity-50">Do</span>
        <span class="text-center opacity-50">Fr</span>
        <span class="text-center opacity-50">Sa</span>
        <span class="text-center opacity-50">So</span>
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
