<template>
    <div
        ref="root"
        :style="{
            height: `calc(var(--row-height) * ${props.durationInMonth} - 3px)`,
            top: `calc(var(--row-height) * ${props.start})`,
        }"
        class="calendar-event-wrapper"
    >
        <div :class="`${$attrs.class}`" class="calendar-event-entry" @click="showDetails()">
            <div class="calendar-event-entry-bar"></div>
            <div class="calendar-event-entry-bg">
                <div class="w-full truncate" :title="props.event.name">
                    <span v-if="event.state === EventState.Draft" class="opacity-50"> Entwurf: </span>
                    <span>
                        {{ props.event.name }}
                    </span>
                </div>
                <template v-if="props.durationInMonth > 1">
                    <span class="block w-full truncate text-xs font-normal"> {{ props.duration }} Tage </span>
                    <span v-if="props.event.description" class="block w-full truncate text-xs font-normal">
                        {{ props.event.description }}
                    </span>
                </template>
            </div>
        </div>
        <EventDetailsSheet ref="eventPreviewSheet" :event="event" />
    </div>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import type { Event } from '@/domain';
import { EventState } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import EventDetailsSheet from '@/ui/components/sheets/EventDetailsSheet.vue';

interface Props {
    event: Event;
    duration: number;
    durationInMonth: number;
    start: number;
}

type Emits = (e: 'update:event', value: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const route = useRoute();

const eventPreviewSheet = ref<Dialog<Event, Event> | null>(null);

function init(): void {
    watch(
        () => route.fullPath,
        () => eventPreviewSheet.value?.close()
    );
}

async function showDetails(): Promise<void> {
    const evt = await eventPreviewSheet.value?.open(props.event);
    if (evt) {
        emit('update:event', evt);
    }
}

init();
</script>

<style scoped>
.calendar-event-wrapper {
    @apply absolute left-0 right-0 top-px z-10;
    @apply rounded-md bg-surface-container-lowest hover:shadow;
    @apply overflow-hidden;
}

.calendar-event-entry-bar {
    @apply w-2 bg-current opacity-10;
    --color-1: rgb(0 0 0 / 0.25);
    --color-2: rgb(255 255 255 / 0.25);
    --pattern: linear-gradient(
        135deg,
        var(--color-1) 25%,
        var(--color-2) 25%,
        var(--color-2) 50%,
        var(--color-1) 50%,
        var(--color-1) 75%,
        var(--color-2) 75%,
        var(--color-2) 100%
    );
    background-size: 10px 10px;
}

.calendar-event-entry-bg {
    @apply h-full w-0 flex-grow py-1 pl-2 pr-4 sm:px-2;
    @apply text-sm font-semibold;
    --color-1: rgb(0 0 0 / 0.5);
    --color-2: rgb(255 255 255 / 0.5);
    --pattern: linear-gradient(
        135deg,
        var(--color-1) 25%,
        var(--color-2) 25%,
        var(--color-2) 50%,
        var(--color-1) 50%,
        var(--color-1) 75%,
        var(--color-2) 75%,
        var(--color-2) 100%
    );
    background-size: 10px 10px;
}

.calendar-event-entry {
    @apply flex h-full w-full items-stretch;
    @apply cursor-pointer;
    @apply bg-surface-container-high;
    @apply font-bold text-onsurface;
}

.calendar-event-entry.assigned {
    @apply bg-primary-container;
    @apply text-onprimary-container;
}

.calendar-event-entry.waiting-list {
    @apply bg-primary-container bg-opacity-50;
    @apply text-onsecondary-container;
}

.calendar-event-entry.waiting-list .calendar-event-entry-bar {
    @apply bg-primary-container bg-opacity-50;
    @apply opacity-100;
    background-image: var(--pattern);
}

.calendar-event-entry.draft {
    @apply bg-surface-container-low;
}

.calendar-event-entry.draft .calendar-event-entry-bar {
    @apply bg-surface-container-low bg-opacity-50;
    @apply opacity-100;
    background-image: var(--pattern);
}

.calendar-event-entry.small .calendar-event-entry-bg {
    @apply flex items-center py-0;
    font-size: 0.6rem;
}

.calendar-event-entry.in-past {
    @apply border-opacity-10 opacity-50 hover:opacity-100;
}
</style>
