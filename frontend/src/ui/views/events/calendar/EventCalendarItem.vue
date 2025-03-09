<template>
    <div
        ref="root"
        :style="{
            height: `calc(var(--row-height) * ${props.durationInMonth} - 3px)`,
            top: `calc(var(--row-height) * ${props.start})`,
        }"
        class="calendar-eventDetails-wrapper"
    >
        <div :class="`${$attrs.class}`" class="calendar-eventDetails-entry" @click="showDetails()">
            <div class="calendar-eventDetails-entry-bar"></div>
            <div class="calendar-eventDetails-entry-bg">
                <div class="w-full truncate" :title="props.eventDetails.name">
                    <span v-if="eventDetails.state === EventState.Draft" class="opacity-50"> Entwurf: </span>
                    <span class="">
                        {{ props.eventDetails.name }}
                    </span>
                </div>
                <template v-if="props.durationInMonth > 1">
                    <span class="block w-full truncate text-xs font-normal"> {{ props.duration }} Tage </span>
                    <span v-if="props.eventDetails.description" class="block w-full truncate text-xs font-normal">
                        {{ props.eventDetails.description }}
                    </span>
                </template>
            </div>
        </div>
        <VDropdownWrapper
            v-if="showDropdown"
            :anchor="$refs.root as HTMLElement"
            anchor-align-x="right"
            anchor-align-y="top"
            max-width="min(30rem, 95vw)"
            min-width="min(30rem, 95vw)"
            @close="showDropdown = false"
        >
            <div class="w-full px-2">
                <div class="rounded-2xl bg-surface-container-low p-4 px-8 shadow-xl">
                    <div class="-mr-4 mb-4 flex items-center justify-end">
                        <!-- title -->
                        <h2 class="w-0 flex-grow text-lg">
                            <span>{{ props.eventDetails.name }}</span>
                        </h2>

                        <button class="icon-button" title="Schließen" @click="showDropdown = false">
                            <i class="fa-solid fa-close"></i>
                        </button>
                    </div>

                    <!-- state -->
                    <div
                        v-if="props.eventDetails.signedInUserAssignedPosition"
                        class="-mx-4 mb-4 flex items-center space-x-4 rounded-xl bg-green-container px-4 py-3 text-ongreen-container"
                    >
                        <i class="fa-solid fa-check w-4" />
                        <p class="text-sm font-bold">
                            Du bist für diese Reise als
                            {{ positions.get(props.eventDetails.signedInUserAssignedPosition).name }}
                            eingeplant
                        </p>
                    </div>
                    <div
                        v-else-if="props.eventDetails.signedInUserWaitingListPosition"
                        class="-mx-4 mb-4 flex items-center space-x-4 rounded-xl bg-blue-container px-4 py-3 text-onblue-container"
                    >
                        <i class="fa-solid fa-hourglass-half w-4" />
                        <p class="text-sm font-bold">
                            Du stehst für diese Reise als
                            {{ positions.get(props.eventDetails.signedInUserWaitingListPosition).name }}
                            auf der Warteliste
                        </p>
                    </div>

                    <!-- info -->
                    <div class="mb-4">
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-calendar-day w-4"></i>
                            <span>{{ formatDateRange(props.eventDetails.start, props.eventDetails.end) }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell w-4" />
                            <span>Crew an Board: {{ $d(eventDetails.start, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell-slash w-4" />
                            <span>Crew von Board: {{ $d(eventDetails.end, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p v-if="props.eventDetails.assignedUserCount" class="items-center space-x-4">
                            <i class="fa-solid fa-users w-4"></i>
                            <span>{{ props.eventDetails.assignedUserCount }} Crew</span>
                        </p>
                        <p v-if="props.eventDetails.description" class="flex items-baseline space-x-4">
                            <i class="fa-solid fa-info-circle mt-0.5 w-4"></i>
                            <span class="line-clamp-3">{{ props.eventDetails.description }}</span>
                        </p>
                    </div>

                    <!-- route -->
                    <div class="">
                        <p v-for="(location, index) in props.eventDetails.locations" :key="index" class="flex items-center space-x-4">
                            <i :class="location.icon" class="fa-solid w-4" />
                            <span class="flex-grow">{{ location.name }}</span>
                        </p>
                    </div>

                    <VInfo v-if="signedInUser.positions.length === 0" class="-mx-4 my-4 text-sm" clamp>
                        Deinem Benutzerkonto wurde noch keine Position zugewiesen. Du kannst dich deshalb nicht selber für Reisen anmelden.
                    </VInfo>

                    <VInfo
                        v-if="
                            signedInUser.positions.length > 1 &&
                            !props.eventDetails.signedInUserAssignedPosition &&
                            !props.eventDetails.signedInUserWaitingListPosition
                        "
                        class="-mx-4 my-4 text-sm"
                    >
                        <p class="mb-2">
                            Anmeldungen werden mit deiner aktuellen Standardposition
                            <i>{{ positions.get(signedInUser.positions[0]).name }}</i> angelegt.
                        </p>
                        <button class="" @click="choosePositionAndJoinEvent(props.eventDetails)">
                            <span class="underline"> Mit einer andere Position anmelden? </span>
                        </button>
                    </VInfo>

                    <!-- primary button -->
                    <div class="-mx-8 mt-8 flex flex-wrap justify-end px-4">
                        <div class="flex-grow"></div>
                        <button
                            v-if="props.eventDetails.signedInUserAssignedPosition"
                            class="btn-ghost-danger text-sm"
                            title="Event absagen"
                            @click="leaveEvent()"
                        >
                            <i class="fa-solid fa-ban"></i>
                            <span class="">Absagen</span>
                        </button>
                        <button
                            v-else-if="props.eventDetails.signedInUserWaitingListPosition && props.eventDetails.canSignedInUserLeave"
                            class="btn-ghost-danger text-sm"
                            title="Warteliste verlassen"
                            @click="leaveEvent()"
                        >
                            <i class="fa-solid fa-user-minus"></i>
                            <span class="">Warteliste verlassen</span>
                        </button>
                        <button
                            v-else-if="props.eventDetails.canSignedInUserJoin && signedInUser.positions.length >= 1"
                            class="btn-ghost max-w-80 text-sm"
                            title="Anmelden"
                            @click="joinEvent(props.eventDetails)"
                        >
                            <i class="fa-solid fa-user-plus"></i>
                            <span class="truncate"> Anmelden als {{ positions.get(signedInUser.positions[0]).name }}</span>
                        </button>
                        <RouterLink
                            v-if="eventDetails.state === EventState.Draft && signedInUser.permissions.includes(Permission.WRITE_EVENTS)"
                            :to="{ name: Routes.EventEdit, params: { key: props.eventDetails.key } }"
                            class="btn-ghost text-sm"
                            title="Detailansicht"
                        >
                            <i class="fa-solid fa-edit"></i>
                            <span class="">Bearbeiten</span>
                        </RouterLink>
                        <RouterLink
                            v-else
                            :to="{ name: Routes.EventDetails, params: { key: props.eventDetails.key } }"
                            class="btn-ghost text-sm"
                            title="Detailansicht"
                        >
                            <i class="fa-solid fa-search"></i>
                            <span class="">Details</span>
                        </RouterLink>
                    </div>
                </div>
            </div>
        </VDropdownWrapper>

        <PositionSelectDlg ref="positionSelectDialog" />
    </div>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DateTimeFormat } from '@/common/date';
import type { Event, PositionKey, SignedInUser } from '@/domain';
import { EventState, Permission } from '@/domain';
import { type Dialog, VDropdownWrapper, VInfo } from '@/ui/components/common';
import PositionSelectDlg from '@/ui/components/events/PositionSelectDlg.vue';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { usePositions } from '@/ui/composables/Positions';
import { Routes } from '@/ui/views/Routes';

interface Props {
    eventDetails: Event;
    duration: number;
    durationInMonth: number;
    start: number;
}

type Emits = (e: 'update:eventDetails', value: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const route = useRoute();
const router = useRouter();
const positions = usePositions();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const showDropdown = ref<boolean>(false);

const positionSelectDialog = ref<Dialog<void, PositionKey | undefined> | null>(null);

function init(): void {
    watch(
        () => route.fullPath,
        () => (showDropdown.value = false)
    );
}

function showDetails(): void {
    if (window.innerWidth < 640) {
        router.push({
            name: Routes.EventDetails,
            params: { year: props.eventDetails.start.getFullYear(), key: props.eventDetails.key },
        });
    } else {
        showDropdown.value = true;
    }
}

async function choosePositionAndJoinEvent(evt: Event): Promise<void> {
    showDropdown.value = false;
    const position = await positionSelectDialog.value?.open();
    if (position) {
        // default position might have changed
        signedInUser.value = authUseCase.getSignedInUser();
        const updatedEvent = await eventUseCase.joinEvent(evt, position);
        emit('update:eventDetails', updatedEvent);
    }
}

async function joinEvent(evt: Event): Promise<void> {
    const updatedEvent = await eventUseCase.joinEvent(evt, signedInUser.value.positions[0]);
    emit('update:eventDetails', updatedEvent);
    showDropdown.value = false;
}

async function leaveEvent(): Promise<void> {
    const updatedEvent = await eventUseCase.leaveEvent(props.eventDetails);
    emit('update:eventDetails', updatedEvent);
    showDropdown.value = false;
}

init();
</script>

<style scoped>
.calendar-eventDetails-wrapper {
    @apply absolute left-0 right-0 top-px z-10;
    @apply rounded-md bg-surface-container-lowest hover:shadow;
    @apply overflow-hidden;
}

.calendar-eventDetails-entry-bar {
    @apply w-2 bg-current opacity-10;
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
}

.calendar-eventDetails-entry-bg {
    @apply h-full w-0 flex-grow py-1 pl-2 pr-4 sm:px-2;
    @apply text-sm font-semibold;
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
}

.calendar-eventDetails-entry {
    @apply flex h-full w-full items-stretch;
    @apply cursor-pointer;
    @apply bg-surface-container-high;
    @apply font-bold text-onsurface;
}

.calendar-eventDetails-entry.assigned {
    @apply bg-primary-container;
    @apply text-onprimary-container;
}

.calendar-eventDetails-entry.waiting-list {
    @apply bg-primary-container bg-opacity-50;
    @apply text-onsecondary-container;
}

.calendar-eventDetails-entry.waiting-list .calendar-eventDetails-entry-bar {
    @apply bg-primary-container bg-opacity-50;
    @apply opacity-100;
    background-image: var(--pattern);
}

.calendar-eventDetails-entry.draft {
    @apply bg-surface-container-low;
}

.calendar-eventDetails-entry.draft .calendar-eventDetails-entry-bar {
    @apply bg-surface-container-low bg-opacity-50;
    @apply opacity-100;
    background-image: var(--pattern);
}

.calendar-eventDetails-entry.small .calendar-eventDetails-entry-bg {
    @apply flex items-center py-0;
    font-size: 0.6rem;
}

.calendar-eventDetails-entry.in-past {
    @apply border-opacity-10 opacity-50 hover:opacity-100;
}
</style>
