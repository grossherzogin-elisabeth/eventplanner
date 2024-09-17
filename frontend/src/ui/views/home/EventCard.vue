<template>
    <RouterLink
        v-if="props.event"
        :to="{ name: Routes.EventDetails, params: { year: props.event.start.getFullYear(), key: props.event.key } }"
        class="block hover:no-underline"
    >
        <div class="event-card" :class="{ assigned: props.event.signedInUserAssignedPosition }">
            <div class="flex" :class="{ 'bg-striped': props.event.signedInUserWaitingListPosition }">
                <div
                    class="border-event-card -my-2 -ml-2 hidden w-32 flex-col items-center justify-center border-r sm:flex"
                >
                    <span class="text-3xl">{{ props.event.start.getDate() }}</span>
                    <span class="text-lg">{{ $d(props.event.start, DateTimeFormat.MMM) }}</span>
                </div>
                <div class="flex flex-grow flex-col px-4 py-3 sm:px-8">
                    <h3 class="mb-2 flex items-center space-x-2">
                        <span class="">{{ props.event.name }}</span>
                    </h3>
                    <p class="mb-4 flex text-sm">
                        <span class="flex-grow">
                            {{ formatDateRange(props.event.start, props.event.end) }}
                        </span>
                        <span>{{ props.event.assignedUserCount }} Crew</span>
                    </p>
                    <div class="flex flex-wrap">
                        <div
                            v-for="(location, index) in props.event.locations"
                            :key="index"
                            class="flex items-center space-x-2"
                        >
                            <i :class="location.icon" class="fa-solid w-4" />
                            <span class="flex-grow">{{ location.name }}</span>
                            <CountryFlag
                                v-if="location.country"
                                :country="location.country"
                                class="border border-gray-200"
                            />
                            <span class="pr-2">↣</span>
                        </div>
                    </div>

                    <div
                        v-if="props.event.signedInUserWaitingListPosition"
                        class="-mx-4 mt-6 flex rounded-xl bg-blue-500 bg-opacity-15 px-4 py-2"
                    >
                        <p class="flex items-center space-x-2 text-blue-700">
                            <i class="fa-solid fa-hourglass-half w-4" />
                            <span class="text-sm font-bold">{{ $t('app.event-details.note-waitinglist') }}</span>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </RouterLink>
    <div v-else class="event-card loading animate-pulse">
        <div class="flex">
            <div
                class="border-event-card -my-2 -ml-2 hidden w-32 flex-col items-center justify-center border-r sm:flex"
            >
                <span class="mb-2 inline-block h-8 w-12 rounded-lg bg-primary-200"></span>
                <span class="mb-2 inline-block h-4 w-16 rounded-lg bg-primary-200"></span>
            </div>
            <div class="flex flex-grow flex-col px-4 py-3 sm:px-8">
                <h3 class="mb-2 flex items-center space-x-2">
                    <span class="mb-2 inline-block h-4 w-64 rounded-lg bg-primary-200"></span>
                </h3>
                <p class="mb-4 flex text-sm">
                    <span class="mb-2 inline-block h-3 w-40 rounded-lg bg-primary-200"></span>
                    <span class="flex-grow"></span>
                    <span class="mb-2 inline-block h-3 w-12 rounded-lg bg-primary-200"></span>
                </p>
                <div class="flex flex-col flex-wrap justify-between">
                    <div v-for="i in 3" :key="i" class="flex items-center space-x-2">
                        <span class="my-1 inline-block h-4 w-4 rounded-full bg-primary-200"></span>
                        <span class="my-1 inline-block h-3 w-32 rounded-lg bg-primary-200"></span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import CountryFlag from '@/ui/components/utils/CountryFlag.vue';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { Routes } from '@/ui/views/Routes';

interface Props {
    event?: Event;
}

const props = defineProps<Props>();
</script>
