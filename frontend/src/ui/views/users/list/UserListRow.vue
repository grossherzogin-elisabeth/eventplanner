<template>
    <td class="w-full font-semibold md:w-1/2">
        <div class="mb-1 flex items-center justify-between gap-2">
            <p class="w-0 grow truncate whitespace-nowrap">
                {{ props.user?.nickName || props.user?.firstName }} {{ props.user?.lastName }}
            </p>
            <div class="sm:hidden">
                <UserQualificationsSummaryBadge :user="props.user" class="text-xs" />
            </div>
        </div>
        <p class="text-sm opacity-50">
            <template v-if="props.user?.lastLoginAt">
                {{ $t('domain.user.last-login-at', { date: $d(props.user?.lastLoginAt, DateTimeFormat.DD_MM_YYYY) }) }}
            </template>
            <template v-else-if="props.user"> {{ $t('domain.user.no-login-recorded') }} </template>
        </p>
        <div class="mt-1 flex flex-wrap gap-1 opacity-75 lg:hidden xl:block 2xl:hidden">
            <template v-if="!props.user">
                <span></span>
                <span></span>
            </template>
            <template v-else>
                <span v-if="userPositions.length === 0" data-test-id="user-no-position" class="text-sm italic">
                    {{ $t('views.user-list.no-position-assigned') }}
                </span>
                <span
                    v-if="userPositions.length >= 1"
                    data-test-id="user-position"
                    class="tag custom"
                    :style="{ '--color': userPositions[0].color }"
                >
                    {{ userPositions[0].name }}
                </span>
                <span
                    v-if="userPositions.length >= 2"
                    data-test-id="user-position"
                    class="tag custom"
                    :style="{ '--color': userPositions[1].color }"
                >
                    {{ userPositions[1].name }}
                </span>
                <template v-if="userPositions.length >= 3">
                    <span
                        v-for="position in userPositions.slice(2)"
                        :key="position.key"
                        data-test-id="user-position-additional"
                        class="tag custom xs:inline hidden"
                        :style="{ '--color': position.color }"
                    >
                        {{ position.name }}
                    </span>
                    <span data-test-id="user-position-overflow" class="tag custom xs:hidden"> + {{ userPositions.length - 2 }} </span>
                </template>
            </template>
        </div>
    </td>
    <td class="hidden w-1/3 lg:table-cell xl:hidden 2xl:table-cell">
        <div class="flex flex-wrap gap-1 opacity-75">
            <template v-if="!props.user">
                <span></span>
                <span></span>
            </template>
            <template v-else>
                <span v-for="position in userPositions" :key="position.key" class="tag custom" :style="{ '--color': position.color }">
                    {{ position.name }}
                </span>
            </template>
        </div>
    </td>
    <td class="hidden w-1/3 md:table-cell">
        <div class="flex space-x-8">
            <div :class="{ 'opacity-25': !userSingleDayEventsCount }">
                <p data-test-id="user-single-day-events-count" class="mb-1 font-semibold">{{ userSingleDayEventsCount || '-' }}</p>
                <p class="text-sm" :title="$t('domain.event-type.single-day-event')">TF</p>
            </div>
            <div :class="{ 'opacity-25': !userWeekendEventsCount }">
                <p data-test-id="user-weekend-events-count" class="mb-1 font-semibold">{{ userWeekendEventsCount || '-' }}</p>
                <p class="text-sm" :title="$t('domain.event-type.weekend-event')">WE</p>
            </div>
            <div :class="{ 'opacity-25': !userMultiDayEventsCount }">
                <p data-test-id="user-multi-day-events-count" class="mb-1 font-semibold">{{ userMultiDayEventsCount || '-' }}</p>
                <p class="text-sm" :title="$t('domain.event-type.multi-day-event')">SR</p>
            </div>
            <div :class="{ 'opacity-25': !userWaitingListCount }">
                <p data-test-id="user-waiting-list-count" class="mb-1 font-semibold">{{ userWaitingListCount || '-' }}</p>
                <p class="text-sm" :title="$t('domain.event.waiting-list')">WL</p>
            </div>
        </div>
    </td>
    <td class="hidden sm:table-cell">
        <div class="flex items-center justify-end">
            <UserQualificationsSummaryBadge :user="props.user" />
        </div>
    </td>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { Event, Position, User } from '@/domain';
import { EventSignupType, EventType, useEventService } from '@/domain';
import { filterUndefined } from '@/common';
import { usePositions } from '@/ui/composables/Positions.ts';
import UserQualificationsSummaryBadge from './UserQualificationsSummaryBadge.vue';
import { DateTimeFormat } from '@/common/date';

interface EventSummary {
    type: EventType;
    hasRegistration: boolean;
    isAssigned: boolean;
}

interface Props {
    user?: User;
    events?: Event[];
}

const props = defineProps<Props>();

const positions = usePositions();
const eventService = useEventService();

const userPositions = computed<Position[]>(() => {
    return (
        props.user?.positionKeys
            ?.map((key) => positions.get(key))
            .filter(filterUndefined)
            .sort((a, b) => b.prio - a.prio) ?? []
    );
});

const userRegistrations = computed<EventSummary[]>(() => {
    if (!props.user || !props.events) {
        return [];
    }
    return props.events?.map((event) => getUserEventSummary(event, props.user)).filter((aggregate) => aggregate.hasRegistration) ?? [];
});

const userSingleDayEventsCount = computed<number>(() => {
    return userRegistrations.value.filter((it) => it.isAssigned && it.type === EventType.SingleDayEvent).length;
});

const userWaitingListCount = computed<number>(() => {
    return userRegistrations.value.filter((it) => !it.isAssigned).length;
});

const userMultiDayEventsCount = computed<number>(() => {
    return userRegistrations.value.filter((it) => it.isAssigned && it.type === EventType.MultiDayEvent).length;
});

const userWeekendEventsCount = computed<number>(() => {
    return userRegistrations.value.filter((it) => it.isAssigned && it.type === EventType.WeekendEvent).length;
});

function getUserEventSummary(event: Event, user?: User): EventSummary {
    if (!user) {
        return { type: event.type, hasRegistration: false, isAssigned: false };
    }
    const userRegistration = eventService.findRegistration(event, user.key);
    if (!userRegistration) {
        return { type: event.type, hasRegistration: false, isAssigned: false };
    }
    if (event.signupType === EventSignupType.Open) {
        return { type: event.type, hasRegistration: true, isAssigned: true };
    }
    const assignedRegistrationKeys = event.slots.map((it) => it.assignedRegistrationKey).filter(filterUndefined);
    if (assignedRegistrationKeys.includes(userRegistration.key)) {
        return { type: event.type, hasRegistration: true, isAssigned: true };
    }
    return { type: event.type, hasRegistration: true, isAssigned: false };
}
</script>
