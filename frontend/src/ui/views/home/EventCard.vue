<template>
    <div v-if="props.event" class="" @click="showDetails()">
        <div
            class="event-card"
            :class="{
                'assigned': props.event.isSignedInUserAssigned,
                'waiting-list': props.event.signedInUserRegistration && !props.event.isSignedInUserAssigned,
            }"
        >
            <div class="flex">
                <div class="border-event-card -my-2 -ml-2 hidden w-32 flex-col items-center justify-center border-r sm:flex">
                    <span class="text-3xl">{{ props.event.start.getDate() }}</span>
                    <span class="text-lg">{{ $d(props.event.start, DateTimeFormat.MMM) }}</span>
                </div>
                <div class="flex w-0 grow flex-col py-3 sm:px-8">
                    <h3 class="mb-2 flex items-center space-x-2">
                        <span>{{ props.event.name }}</span>
                    </h3>
                    <p class="mb-4 flex text-sm opacity-75">
                        <span class="grow">
                            {{ formatDateRange(props.event.start, props.event.end) }}
                        </span>
                        <span v-if="props.event.signupType === EventSignupType.Open || props.event.state !== EventState.Planned">
                            {{ props.event.registrations.length }}
                        </span>
                        <span v-else>{{ props.event.assignedUserCount }}</span>
                        <i class="fa-solid fa-users ml-2" />
                    </p>
                    <VMarkdown
                        v-if="props.event.description"
                        class="mb-4 line-clamp-4 text-sm opacity-75"
                        :value="props.event.description"
                    />
                    <p v-else class="mb-4 line-clamp-3 text-sm opacity-75">{{}}</p>
                    <div class="flex flex-col items-stretch gap-6">
                        <div class="flex grow flex-col flex-wrap gap-x-4 gap-y-2 sm:flex-row">
                            <span
                                v-for="(location, index) in props.event.locations.slice(0, 3)"
                                :key="index"
                                class="flex items-center gap-2"
                            >
                                <i :class="location.icon" class="fa-solid w-4" />
                                <span class="grow">{{ location.name }}</span>
                            </span>
                            <span v-if="props.event.locations.length > 3">+ {{ props.event.locations.length - 3 }} weitere</span>
                        </div>

                        <div
                            v-if="!props.event.isSignedInUserAssigned"
                            class="bg-surface-container-highest flex rounded-xl px-4 py-2 sm:-mx-4"
                        >
                            <p class="flex items-center gap-2">
                                <i class="fa-solid fa-hourglass-half w-4" />
                                <span class="text-sm font-bold">{{ $t('views.home.waitinglist') }}</span>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <EventDetailsSheet ref="eventPreviewSheet" />
    </div>
    <div v-else class="event-card loading animate-pulse">
        <div class="flex">
            <div class="border-event-card -my-2 -ml-2 hidden w-32 flex-col items-center justify-center border-r sm:flex">
                <span class="bg-surface-container-highest mb-2 inline-block h-8 w-12 rounded-lg"></span>
                <span class="bg-surface-container-highest mb-2 inline-block h-4 w-16 rounded-lg"></span>
            </div>
            <div class="flex grow flex-col px-4 py-3 sm:px-8">
                <h3 class="mb-2 flex items-center space-x-2">
                    <span class="bg-surface-container-highest mb-2 inline-block h-4 w-64 rounded-lg"></span>
                </h3>
                <p class="mb-4 flex text-sm">
                    <span class="bg-surface-container-highest mb-2 inline-block h-3 w-40 rounded-lg"></span>
                    <span class="grow"></span>
                    <span class="bg-surface-container-highest mb-2 inline-block h-3 w-12 rounded-lg"></span>
                </p>
                <div class="flex flex-col flex-wrap justify-between">
                    <div v-for="i in 3" :key="i" class="flex items-center space-x-2">
                        <span class="bg-surface-container-highest my-1 inline-block h-4 w-4 rounded-full"></span>
                        <span class="bg-surface-container-highest my-1 inline-block h-3 w-32 rounded-lg"></span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import { EventSignupType, EventState } from '@/domain';
import type { Sheet } from '@/ui/components/common';
import { VMarkdown } from '@/ui/components/common';
import EventDetailsSheet from '@/ui/components/sheets/EventDetailsSheet.vue';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';

interface Props {
    event?: Event;
}

const props = defineProps<Props>();
const eventPreviewSheet = ref<Sheet<Event, Event> | null>(null);

async function showDetails(): Promise<void> {
    await eventPreviewSheet.value?.open(props.event);
}
</script>
<style>
@reference "tailwindcss";

.event-card {
    overflow: hidden;
    cursor: pointer;
    border-radius: var(--radius-xl);
    @apply shadow;
    font-size: var(--text-sm);
    font-weight: var(--font-weight-semibold);
    background-color: --alpha(var(--color-surface-container) / 75%);
}

.event-card:hover {
    background-color: var(--color-surface-container);
}

.event-card .border-event-card {
    border-color: var(--color-surface-container);
}

.event-card > * {
    height: 100%;
    width: 100%;
    border-left-width: 0.5rem;
    @apply py-1;
    @apply pl-2;
    @apply pr-4;
    @apply sm:px-2;
    border-color: var(--color-surface-container);
}

.event-card.loading {
    cursor: default;
}

.event-card.loading:hover {
    box-shadow: none;
}

.event-card.assigned {
    background-color: --alpha(var(--color-primary-container) / 50%);
}

.event-card.assigned:hover {
    background-color: --alpha(var(--color-primary-container) / 55%);
}

.event-card.assigned .border-event-card {
    border-color: --alpha(var(--color-onprimary-container) / 5%);
}

.event-card.assigned > * {
    border-left-width: 0.5rem;
    border-color: --alpha(var(--color-onprimary-container) / 20%);
}
</style>
