<template>
    <!-- date -->
    <td class="hidden w-1/6 whitespace-nowrap lg:table-cell" :class="{ 'opacity-50': isPastEvent }">
        <p class="mb-1 font-semibold 2xl:hidden">
            {{ $d(props.event?.start ?? new Date(), DateTimeFormat.DDD_DD_MM) }}
        </p>
        <p class="mb-1 hidden font-semibold 2xl:block">
            {{ formatDateRange(props.event?.start, props.event?.end) }}
        </p>
        <p class="text-sm">
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
            <div :key="stateDetails.icon" class="status-badge -mt-1 text-xs md:hidden" :class="stateDetails.color">
                <span>{{ stateDetails.name }}</span>
            </div>
        </div>
        <p class="hidden truncate text-sm font-light lg:block">
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
        <div class="flex justify-between gap-x-2 text-sm font-light lg:hidden">
            <!-- date -->
            <p class="grow truncate">
                {{ formatDateRange(props.event?.start, props.event?.end) }}
                <span class="opacity-40"> ({{ $t('views.event-list.table.day-count', { count: props.event?.days }) }}) </span>
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
    </td>
    <!-- crew -->
    <td class="hidden w-1/6 min-w-16 text-right whitespace-nowrap md:table-cell" :class="{ 'opacity-50': isPastEvent }">
        <template v-if="showWaitingList">
            <p class="mb-1 pl-4 font-semibold">
                {{ props.event?.assignedUserCount }}
                <span v-if="props.event?.waitingListCount" class="opacity-40"> +{{ props.event?.waitingListCount }} </span>
            </p>
            <p class="pl-4 text-sm">
                {{ $t('domain.event.crew', { count: props.event?.assignedUserCount }) }}
            </p>
        </template>
        <template v-else>
            <p class="mb-1 pl-4 font-semibold">
                {{ props.event?.registrations.length }}
            </p>
            <p class="pl-4 text-sm">
                <template v-if="props.event">
                    {{ $t('domain.event.registrations', { count: props.event?.registrations.length }) }}
                </template>
            </p>
        </template>
    </td>
    <!-- status -->
    <td class="hidden w-1/6 md:table-cell" :class="{ 'opacity-50': isPastEvent }">
        <div class="flex items-center lg:justify-end">
            <div :key="stateDetails.icon" class="status-badge text-sm" :class="stateDetails.color">
                <i class="fa-solid w-4" :class="stateDetails.icon"></i>
                <span>{{ stateDetails.name }}</span>
            </div>
        </div>
    </td>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import { useEventService, EventState } from '@/domain';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';

interface StateDetails {
    name: string;
    color: string;
    icon: string;
}

interface Props {
    event?: Event;
}

const props = defineProps<Props>();

const { t } = useI18n();
const eventService = useEventService();

const showWaitingList = computed<boolean>(() => {
    return props.event !== undefined && eventService.showWaitingList(props.event);
});

const isPastEvent = computed<boolean>(() => {
    return props.event !== undefined && props.event.start.getTime() < Date.now();
});

const hasOpenSlots = computed<boolean>(() => {
    return props.event !== undefined && eventService.hasOpenSlots(props.event);
});

const hasOpenImportantSlots = computed<boolean>(() => {
    return props.event !== undefined && eventService.hasOpenImportantSlots(props.event);
});

const stateDetails = computed<StateDetails>(() => {
    if (props.event === undefined) {
        return { name: '', icon: 'fa-circle text-surface-container-high', color: 'neutral' };
    }
    if (props.event.state === EventState.Canceled) {
        return { name: t('domain.event-state.canceled'), icon: 'fa-ban', color: 'error' };
    }
    if (props.event.isSignedInUserAssigned) {
        return { name: t('views.event-list.state.assigned'), icon: 'fa-check-circle', color: 'success' };
    }
    if (props.event.signedInUserRegistration) {
        return { name: t('views.event-list.state.waitinglist'), icon: 'fa-hourglass-half', color: 'neutral' };
    }
    if (props.event.state === EventState.Draft) {
        return { name: t('domain.event-state.draft'), icon: 'fa-compass-drafting', color: 'neutral' };
    }
    if (props.event.state === EventState.OpenForSignup) {
        return { name: t('domain.event-state.open-for-signup'), icon: 'fa-people-group', color: 'info' };
    }
    if (hasOpenImportantSlots.value) {
        return { name: t('domain.event-state.crew-wanted'), icon: 'fa-info-circle', color: 'warning' };
    }
    if (hasOpenSlots.value) {
        return { name: t('domain.event-state.open-slots'), icon: 'fa-info-circle', color: 'info' };
    }
    return { name: t('domain.event-state.full'), icon: 'fa-info-circle', color: 'neutral' };
});
</script>
