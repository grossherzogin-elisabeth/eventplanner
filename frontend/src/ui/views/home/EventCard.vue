<template>
    <RouterLink
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
                        <span class="">{{ event.name }}</span>
                    </h3>
                    <p class="mb-4 flex text-sm">
                        <span class="flex-grow">
                            {{ formatDateRange(props.event.start, props.event.end) }}
                        </span>
                        <span>{{ props.event.assignedUserCount }} Crew</span>
                    </p>
                    <div class="flex flex-col flex-wrap justify-between">
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
                        </div>
                    </div>

                    <div
                        v-if="props.event.signedInUserWaitingListPosition"
                        class="-mx-4 mt-6 flex rounded-xl bg-blue-500 bg-opacity-15 px-4 py-2"
                    >
                        <p class="flex items-center space-x-2 text-blue-700">
                            <i class="fa-solid fa-circle-info w-4" />
                            <span class="text-sm font-bold">{{ $t('app.event-details.note-waitinglist') }}</span>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </RouterLink>
</template>
<script lang="ts" setup>
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import CountryFlag from '@/ui/components/utils/CountryFlag.vue';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { Routes } from '@/ui/views/Routes';

interface Props {
    event: Event;
}

const props = defineProps<Props>();
</script>
