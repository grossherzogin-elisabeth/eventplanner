<template>
    <td class="w-0 pr-4 text-xl opacity-50">
        <span v-if="slot || props.event?.signupType === EventSignupType.Open">
            <i class="fa-solid fa-check text-success"></i>
        </span>
        <span v-else>
            <i class="fa-solid fa-hourglass-half"></i>
        </span>
    </td>
    <td class="w-full max-w-[20rem] border-none font-semibold">
        <div class="mb-1 md:flex">
            <p class="grow truncate md:w-0">{{ props.event?.name }}</p>
        </div>
        <p class="truncate text-sm font-light">{{ props.event?.locations.map((it) => it.name).join(' - ') }}</p>
    </td>
    <td class="hidden text-center whitespace-nowrap sm:table-cell">
        <p class="mb-1 w-12 font-semibold">
            <template v-if="showWaitingList">
                <span data-test-id="crew-count">{{ props.event?.assignedUserCount }}</span>
                <span v-if="props.event?.waitingListCount" data-test-id="waiting-list-count" class="opacity-40">
                    +{{ props.event?.waitingListCount }}
                </span>
            </template>
            <template v-else>
                <span data-test-id="crew-count">{{ props.event?.registrations.length }}</span>
            </template>
        </p>
        <p class="text-sm">Crew</p>
    </td>
    <td class="hidden whitespace-nowrap lg:table-cell">
        <div class="mb-1 font-semibold">
            <p class="hidden w-56 lg:block">{{ formatDateRange(props.event?.start, props.event?.end) }}</p>
            <p class="w-20 lg:hidden">{{ $d(props.event?.start ?? new Date(), DateTimeFormat.DDD_DD_MM) }}</p>
        </div>
        <p class="text-sm">{{ props.event?.days }} Tage</p>
    </td>
    <td class="hidden">
        <span :style="{ '--color': position?.color }" class="tag custom">
            {{ position?.name }}
        </span>
    </td>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { Event, Position, Registration, Slot, UserDetails } from '@/domain';
import { useEventService, EventSignupType } from '@/domain';
import { usePositions } from '@/ui/composables/Positions';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';

interface Props {
    event?: Event;
    user?: UserDetails;
}

const props = defineProps<Props>();

const positions = usePositions();
const eventService = useEventService();

const showWaitingList = computed<boolean>(() => {
    return props.event !== undefined && eventService.showWaitingList(props.event);
});

const registration = computed<Registration | undefined>(() => {
    return props.event?.registrations.find((it) => it.userKey === props.user?.key);
});

const position = computed<Position | undefined>(() => {
    return positions.get(registration.value?.positionKey || '');
});

const slot = computed<Slot | undefined>(() => {
    return props.event?.slots.find((it) => it.assignedRegistrationKey === registration.value?.key);
});
</script>
