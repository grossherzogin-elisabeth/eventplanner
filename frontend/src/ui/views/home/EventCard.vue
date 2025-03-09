<template>
    <RouterLink
        v-if="props.eventDetails"
        :to="{ name: Routes.EventDetails, params: { year: props.eventDetails.start.getFullYear(), key: props.eventDetails.key } }"
        class="block hover:no-underline"
    >
        <div
            class="eventDetails-card"
            :class="{
                'assigned': props.eventDetails.signedInUserAssignedPosition,
                'waiting-list': props.eventDetails.signedInUserWaitingListPosition,
            }"
        >
            <div class="flex">
                <div
                    class="border-eventDetails-card -my-2 -ml-2 hidden w-32 flex-col items-center justify-center border-r sm:flex">
                    <span class="text-3xl">{{ props.eventDetails.start.getDate() }}</span>
                    <span class="text-lg">{{ $d(props.eventDetails.start, DateTimeFormat.MMM) }}</span>
                </div>
                <div class="flex w-0 flex-grow flex-col py-3 sm:px-8">
                    <h3 class="mb-2 flex items-center space-x-2">
                        <span class="">{{ props.eventDetails.name }}</span>
                    </h3>
                    <p class="mb-4 flex text-sm opacity-50">
                        <span class="flex-grow">
                            {{ formatDateRange(props.eventDetails.start, props.eventDetails.end) }}
                        </span>
                        <span>{{ props.eventDetails.assignedUserCount }} Crew</span>
                    </p>
                    <div class="flex flex-col items-stretch gap-6">
                        <div class="flex flex-grow flex-col flex-wrap gap-2 sm:flex-row">
                            <div v-for="(location, index) in props.eventDetails.locations" :key="index"
                                 class="flex items-center gap-2">
                                <i :class="location.icon" class="fa-solid w-4" />
                                <span class="flex-grow">{{ location.name }}</span>
                            </div>
                        </div>

                        <div
                            v-if="props.eventDetails.signedInUserWaitingListPosition"
                            class="flex rounded-xl bg-surface-container-highest px-4 py-2 sm:-mx-4"
                        >
                            <p class="flex items-center gap-2">
                                <i class="fa-solid fa-hourglass-half w-4" />
                                <span
                                    class="text-sm font-bold">{{ $t('app.eventDetails-details.note-waitinglist') }}</span>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </RouterLink>
    <div v-else class="eventDetails-card loading animate-pulse">
        <div class="flex">
            <div
                class="border-eventDetails-card -my-2 -ml-2 hidden w-32 flex-col items-center justify-center border-r sm:flex">
                <span class="mb-2 inline-block h-8 w-12 rounded-lg bg-surface-container-highest"></span>
                <span class="mb-2 inline-block h-4 w-16 rounded-lg bg-surface-container-highest"></span>
            </div>
            <div class="flex flex-grow flex-col px-4 py-3 sm:px-8">
                <h3 class="mb-2 flex items-center space-x-2">
                    <span class="mb-2 inline-block h-4 w-64 rounded-lg bg-surface-container-highest"></span>
                </h3>
                <p class="mb-4 flex text-sm">
                    <span class="mb-2 inline-block h-3 w-40 rounded-lg bg-surface-container-highest"></span>
                    <span class="flex-grow"></span>
                    <span class="mb-2 inline-block h-3 w-12 rounded-lg bg-surface-container-highest"></span>
                </p>
                <div class="flex flex-col flex-wrap justify-between">
                    <div v-for="i in 3" :key="i" class="flex items-center space-x-2">
                        <span class="my-1 inline-block h-4 w-4 rounded-full bg-surface-container-highest"></span>
                        <span class="my-1 inline-block h-3 w-32 rounded-lg bg-surface-container-highest"></span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { Routes } from '@/ui/views/Routes';

interface Props {
    eventDetails?: Event;
}

const props = defineProps<Props>();
</script>
