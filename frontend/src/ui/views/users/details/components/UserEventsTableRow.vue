<template>
    <!-- date -->
    <td class="hidden w-1/6 whitespace-nowrap lg:table-cell">
        <p class="mb-1 font-semibold">
            {{ $d(props.event?.start ?? new Date(), DateTimeFormat.DDD_DD_MM) }}
        </p>
        <p class="text-sm opacity-75">
            {{ $t('generic.days', { count: props.event?.days }) }}
        </p>
    </td>
    <td class="w-full border-none font-semibold lg:w-2/3">
        <div class="mb-1 flex">
            <p class="w-0 grow truncate">{{ props.event?.name }}</p>
            <span :style="{ '--color': props.position?.color }" class="tag custom md:hidden">
                {{ props.positionName }}
            </span>
        </div>
        <div class="hidden truncate text-sm font-light opacity-75 lg:flex">
            <!-- locations -->
            <p v-if="props.event?.locations.length" class="w-0 grow truncate">
                {{ props.event.locations.map((it) => it.name).join(' - ') }}
            </p>
            <!-- placeholder -->
            <p v-else>-</p>
        </div>
        <div class="flex justify-between gap-x-2 text-sm font-light opacity-75 lg:hidden">
            <!-- date -->
            <p class="w-0 grow truncate">
                {{ formatDateRange(props.event?.start, props.event?.end) }}
                <span> - {{ $t('generic.days', { count: props.event?.days }) }} </span>
            </p>
            <!-- crew -->
            <p class="truncate text-right whitespace-nowrap md:hidden">
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
    </td>
    <td class="hidden w-64 md:table-cell">
        <span :style="{ '--color': props.position?.color }" class="tag custom">
            {{ props.positionName }}
        </span>
    </td>
    <!-- crew -->
    <td class="hidden w-1/6 min-w-16 text-right whitespace-nowrap md:table-cell">
        <template v-if="showWaitingList">
            <p class="mb-1 pl-4 font-semibold" data-test-id="crew-count">
                {{ props.event?.assignedUserCount }}
                <span v-if="props.event?.waitingListCount" class="opacity-40"> +{{ props.event?.waitingListCount }} </span>
            </p>
            <p class="pl-4 text-sm opacity-75">
                {{ $t('domain.event.crew', { count: props.event?.assignedUserCount }) }}
            </p>
        </template>
        <template v-else>
            <p class="mb-1 pl-4 font-semibold">
                {{ props.event?.registrations.length }}
            </p>
            <p class="pl-4 text-sm opacity-75">
                <template v-if="props.event">
                    {{ $t('domain.event.registrations', { count: props.event?.registrations.length }) }}
                </template>
            </p>
        </template>
    </td>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { Event, Position, Registration } from '@/domain';
import { useEventService } from '@/domain';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';

export interface Props {
    event?: Event;
    registration?: Registration;
    position?: Position;
    positionName?: string;
    isAssigned?: boolean;
}

const props = defineProps<Props>();

const eventService = useEventService();

const showWaitingList = computed<boolean>(() => {
    return props.event !== undefined && eventService.showWaitingList(props.event);
});
</script>
