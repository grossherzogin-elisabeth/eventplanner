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
                    <span v-if="event.state === EventState.Draft" class="opacity-50"> {{ $t('domain.event-state.draft') }}: </span>
                    <span>
                        {{ props.event.name }}
                    </span>
                </div>
                <template v-if="props.durationInMonth > 1">
                    <span class="block w-full truncate text-xs font-normal">
                        {{ $t('views.calendar.days', { count: props.duration }) }}
                    </span>
                    <span v-if="props.event.description" class="block w-full truncate text-xs font-normal">
                        {{ props.event.description }}
                    </span>
                </template>
            </div>
        </div>
        <EventDetailsSheet ref="eventPreviewSheet" />
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
@reference "tailwindcss";

.calendar-event-wrapper {
    position: absolute;
    left: 0;
    right: 0;
    top: 1px;
    z-index: 10;
    overflow: hidden;
    border-radius: var(--radius-md);
    background-color: var(--color-surface-container);
    @apply shadow;
}

.calendar-event-entry-bar {
    height: 100%;
    background-color: currentColor;
    opacity: 0.1;
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
    @apply w-2;
}

.calendar-event-entry-bg {
    height: 100%;
    width: 0;
    flex-grow: 1;
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
    @apply py-1;
    @apply pl-2;
    @apply pr-4;
    font-size: var(--text-sm);
    font-weight: var(--font-weight-semibold);
    @apply sm:px-2;
}

.calendar-event-entry {
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    cursor: pointer;
    font-weight: var(--font-weight-bold);
    background-color: var(--color-surface-container);
    color: var(--color-onsurface);
}

.calendar-event-entry.assigned {
    background-color: var(--color-primary-container);
    color: var(--color-onprimary-container);
}

.calendar-event-entry.waiting-list {
    background-color: --alpha(var(--color-primary-container) / 50%);
    color: var(--color-onprimary-container);
}

.calendar-event-entry.waiting-list .calendar-event-entry-bar {
    background-color: --alpha(var(--color-primary-container) / 50%);
    opacity: 1;
    background-image: var(--pattern);
}

.calendar-event-entry.draft {
    background-color: var(--color-surface-container-low);
}

.calendar-event-entry.draft .calendar-event-entry-bar {
    background-color: --alpha(var(--color-surface-container) / 50%);
    opacity: 1;
    background-image: var(--pattern);
}

.calendar-event-entry.small .calendar-event-entry-bg {
    display: flex;
    align-items: center;
    padding-top: 0;
    padding-bottom: 0;
    font-size: 0.6rem;
}

.calendar-event-entry.in-past {
    opacity: 0.5;
}

.calendar-event-entry.in-past:hover {
    opacity: 1;
}
</style>
