<template>
    <!-- date -->
    <td class="hidden w-1/6 whitespace-nowrap lg:table-cell" :class="{ 'opacity-50': isPastEvent }">
        <p class="mb-1 font-semibold 2xl:hidden">
            {{ $d(props.event?.start ?? new Date(), DateTimeFormat.DDD_DD_MM) }}
        </p>
        <p class="mb-1 hidden font-semibold 2xl:block">
            {{ formatDateRange(props.event?.start, props.event?.end) }}
        </p>
        <p class="text-sm opacity-75">
            {{ $t('views.event-list.table.day-count', { count: props.event?.days }) }}
        </p>
    </td>
    <!-- name -->
    <td class="w-full font-semibold md:w-2/3 md:max-w-[80vw]" style="max-width: min(65vw, 20rem)" :class="{ 'opacity-50': isPastEvent }">
        <div class="mb-1 flex items-start justify-between gap-x-2">
            <!-- name -->
            <p class="grow truncate whitespace-nowrap" :class="{ 'text-error line-through': props.event?.state === EventState.Canceled }">
                {{ props.event?.name }}
            </p>
            <!-- mobile state -->
            <EventStateBadge :event="props.event" class="text-xs lg:hidden" />
        </div>
        <p class="hidden truncate text-sm font-light opacity-75 lg:block">
            <!-- description -->
            <span v-if="props.event?.description" class="">
                {{ props.event.description }}
            </span>
            <!-- locations -->
            <span v-else-if="props.event?.locations.length" class="">
                {{ props.event.locations.map((it) => it.name).join(' - ') }}
            </span>
            <!-- placeholder -->
            <span v-else>-</span>
        </p>
        <div class="flex justify-between gap-x-2 text-sm font-light opacity-75 lg:hidden">
            <!-- date -->
            <p class="grow truncate">
                {{ formatDateRange(props.event?.start, props.event?.end) }}
                <span>- {{ $t('views.event-list.table.day-count', { count: props.event?.days }) }} </span>
            </p>
            <!-- crew -->
            <p class="text-right whitespace-nowrap md:hidden">
                <template v-if="showWaitingList">
                    {{ props.event?.assignedUserCount }}
                    <span v-if="props.event?.waitingListCount" class="opacity-40"> +{{ props.event?.waitingListCount }} </span>
                    {{ $t('domain.event.crew', { count: props.event?.assignedUserCount }) }}
                </template>
                <template v-else>
                    {{ props.event?.registrations.length }}
                    <template v-if="props.event">
                        {{ $t('domain.event.registrations', { count: props.event?.registrations.length }) }}
                    </template>
                </template>
            </p>
        </div>
        <div v-if="config.enableEventAdminListPositionsOverview" class="flex w-full items-center gap-px pt-2">
            <template v-for="(position, index) in assignedPositions" :key="`${position.key}-${index}`">
                <div :data-index="index" class="w-1 grow">
                    <VTooltip :delay="50">
                        <template #tooltip>
                            <span class="tag custom" :style="{ '--color': position.color }">
                                {{ position.name }}
                            </span>
                        </template>
                        <template #default>
                            <div class="h-2 rounded-sm" :style="{ backgroundColor: position.color }" />
                        </template>
                    </VTooltip>
                </div>
            </template>
        </div>
    </td>
    <!-- crew -->
    <td class="hidden w-1/6 min-w-16 text-right whitespace-nowrap md:table-cell" :class="{ 'opacity-50': isPastEvent }">
        <template v-if="showWaitingList">
            <p class="mb-1 pl-4 font-semibold">
                {{ props.event?.assignedUserCount }}
                <span v-if="props.event?.waitingListCount" class="opacity-40"> +{{ props.event?.waitingListCount }} </span>
            </p>
            <p class="pl-4 text-sm opacity-75">{{ $t('domain.event.crew') }}</p>
        </template>
        <template v-else>
            <p class="mb-1 pl-4 font-semibold">{{ props.event?.registrations.length }}</p>
            <p class="pl-4 text-sm opacity-75">
                <template v-if="props.event">{{ $t('domain.event.registrations') }}</template>
            </p>
        </template>
    </td>
    <!-- status -->
    <td class="hidden w-1/6 lg:table-cell">
        <EventStateBadge :event="props.event" class="text-sm" />
    </td>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { Event, Position } from '@/domain';
import { useEventService, EventState } from '@/domain';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { usePositions } from '@/ui/composables/Positions';
import { useConfig } from '@/ui/composables/Config';
import EventStateBadge from '@/ui/views/events/list-admin/EventStateBadge.vue';
import { VTooltip } from '@/ui/components/common';
import { filterUndefined } from '@/common';

interface Props {
    event?: Event;
}

const props = defineProps<Props>();

const { config } = useConfig();
const positions = usePositions();
const eventService = useEventService();

const showWaitingList = computed<boolean>(() => {
    return props.event !== undefined && eventService.showWaitingList(props.event);
});

const isPastEvent = computed<boolean>(() => {
    return props.event !== undefined && props.event.start.getTime() < Date.now();
});

const assignedPositions = computed<Position[]>(() => {
    if (props.event === undefined) {
        return [];
    }
    return eventService
        .getAssignedRegistrations(props.event)
        .map((reg) => positions.get(reg.positionKey))
        .filter(filterUndefined)
        .sort((a, b) => b.prio - a.prio);
});
</script>
