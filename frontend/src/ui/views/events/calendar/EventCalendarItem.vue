<template>
    <div
        ref="root"
        :style="{
            height: `calc(var(--row-height) * ${props.durationInMonth} - 3px)`,
            top: `calc(var(--row-height) * ${props.start})`,
        }"
        class="calendar-event-wrapper"
    >
        <div :class="`${$attrs.class}`" class="calendar-event-entry" @click="showDetails()">
            <div class="calendar-event-entry-bar"></div>
            <div class="calendar-event-entry-bg">
                <div class="w-full truncate" :title="props.event.name">
                    <span v-if="event.state === EventState.Draft" class="opacity-50"> Entwurf: </span>
                    <span>
                        {{ props.event.name }}
                    </span>
                </div>
                <template v-if="props.durationInMonth > 1">
                    <span class="block w-full truncate text-xs font-normal"> {{ props.duration }} Tage </span>
                    <span v-if="props.event.description" class="block w-full truncate text-xs font-normal">
                        {{ props.event.description }}
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
                            <span>{{ props.event.name }}</span>
                        </h2>

                        <button class="icon-button" title="Schließen" @click="showDropdown = false">
                            <i class="fa-solid fa-close"></i>
                        </button>
                    </div>

                    <!-- user registration state -->
                    <VSuccess
                        v-if="props.event.signedInUserRegistration && props.event.signedInUserAssignedSlot"
                        class="-mx-4 my-4 text-sm"
                        icon="fa-check"
                    >
                        Du bist für diese Reise als
                        <b>{{ positions.get(props.event.signedInUserRegistration.positionKey).name }}</b>
                        eingeplant
                    </VSuccess>
                    <VInfo v-else-if="props.event.signedInUserRegistration" class="-mx-4 my-4 text-sm" icon="fa-hourglass-half">
                        Du stehst für diese Reise als
                        <b>{{ positions.get(props.event.signedInUserRegistration.positionKey).name }}</b>
                        auf der Warteliste
                    </VInfo>

                    <!-- event details -->
                    <div class="mb-4">
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-calendar-day w-4 opacity-75"></i>
                            <span>{{ formatDateRange(props.event.start, props.event.end) }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell w-4 opacity-75" />
                            <span>Crew an Board: {{ $d(event.start, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell-slash w-4 opacity-75" />
                            <span>Crew von Board: {{ $d(event.end, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p v-if="props.event.assignedUserCount" class="items-center space-x-4">
                            <i class="fa-solid fa-users w-4 opacity-75"></i>
                            <span>{{ props.event.assignedUserCount }} Crew</span>
                        </p>
                        <p v-if="props.event.description" class="flex items-baseline space-x-4">
                            <i class="fa-solid fa-info-circle mt-0.5 w-4 opacity-75"></i>
                            <span class="line-clamp-3">{{ props.event.description }}</span>
                        </p>
                    </div>

                    <!-- event route -->
                    <div>
                        <p v-for="(location, index) in props.event.locations" :key="index" class="flex items-center space-x-4">
                            <i :class="location.icon" class="fa-solid w-4 opacity-75" />
                            <span class="flex-grow">{{ location.name }}</span>
                        </p>
                    </div>

                    <VInfo v-if="signedInUser.positions.length === 0" class="-mx-4 my-4 text-sm" clamp>
                        Deinem Benutzerkonto wurde noch keine Position zugewiesen. Du kannst dich deshalb nicht selber für Reisen anmelden.
                    </VInfo>
                    <VInfo v-else-if="props.event.isInPast" class="-mx-4 my-4 text-sm" clamp>
                        Diese Reise liegt in der Vergangenheit. Eine An- oder Abmeldung ist daher nicht mehr möglich.
                    </VInfo>
                    <VInfo
                        v-else-if="signedInUser.positions.length > 1 && !props.event.signedInUserRegistration"
                        class="-mx-4 my-4 text-sm"
                    >
                        <p class="mb-2">
                            Anmeldungen werden mit deiner aktuellen Standardposition
                            <i>{{ positions.get(signedInUser.positions[0]).name }}</i> angelegt.
                        </p>
                        <button @click="choosePositionAndJoinEvent(props.event)">
                            <span class="underline"> Mit einer andere Position anmelden? </span>
                        </button>
                    </VInfo>

                    <!-- primary button -->
                    <div class="-mx-8 mt-8 flex flex-wrap justify-end px-4">
                        <div class="flex-grow"></div>
                        <button
                            v-if="props.event.signedInUserAssignedSlot"
                            class="btn-ghost-danger text-sm"
                            title="Event absagen"
                            :disabled="!props.event.canSignedInUserLeave"
                            @click="leaveEvent()"
                        >
                            <i class="fa-solid fa-ban"></i>
                            <span>Absagen</span>
                        </button>
                        <button
                            v-else-if="props.event.signedInUserRegistration"
                            class="btn-ghost-danger text-sm"
                            title="Warteliste verlassen"
                            :disabled="!props.event.canSignedInUserLeave"
                            @click="leaveEvent()"
                        >
                            <i class="fa-solid fa-user-minus"></i>
                            <span>Warteliste verlassen</span>
                        </button>
                        <button
                            v-else
                            class="btn-ghost max-w-80 text-sm"
                            title="Anmelden"
                            :disabled="!props.event.canSignedInUserJoin"
                            @click="joinEvent(props.event)"
                        >
                            <i class="fa-solid fa-user-plus"></i>
                            <span class="truncate"> Anmelden als {{ positions.get(signedInUser.positions[0]).name }}</span>
                        </button>
                        <RouterLink
                            v-if="event.state === EventState.Draft && signedInUser.permissions.includes(Permission.WRITE_EVENTS)"
                            :to="{ name: Routes.EventEdit, params: { key: props.event.key } }"
                            class="btn-ghost text-sm"
                            title="Detailansicht"
                        >
                            <i class="fa-solid fa-edit"></i>
                            <span>Bearbeiten</span>
                        </RouterLink>
                        <RouterLink
                            v-else
                            :to="{ name: Routes.EventDetails, params: { key: props.event.key } }"
                            class="btn-ghost text-sm"
                            title="Detailansicht"
                        >
                            <i class="fa-solid fa-search"></i>
                            <span>Details</span>
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
import { type Dialog, VDropdownWrapper, VInfo, VSuccess } from '@/ui/components/common';
import PositionSelectDlg from '@/ui/components/events/PositionSelectDlg.vue';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { usePositions } from '@/ui/composables/Positions';
import { Routes } from '@/ui/views/Routes';

interface Props {
    event: Event;
    duration: number;
    durationInMonth: number;
    start: number;
}

type Emits = (e: 'update:event', value: Event) => void;

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
            params: { year: props.event.start.getFullYear(), key: props.event.key },
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
        emit('update:event', updatedEvent);
    }
}

async function joinEvent(evt: Event): Promise<void> {
    const updatedEvent = await eventUseCase.joinEvent(evt, signedInUser.value.positions[0]);
    emit('update:event', updatedEvent);
    showDropdown.value = false;
}

async function leaveEvent(): Promise<void> {
    const updatedEvent = await eventUseCase.leaveEvent(props.event);
    emit('update:event', updatedEvent);
    showDropdown.value = false;
}

init();
</script>

<style scoped>
.calendar-event-wrapper {
    @apply absolute left-0 right-0 top-px z-10;
    @apply rounded-md bg-surface-container-lowest hover:shadow;
    @apply overflow-hidden;
}

.calendar-event-entry-bar {
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

.calendar-event-entry-bg {
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

.calendar-event-entry {
    @apply flex h-full w-full items-stretch;
    @apply cursor-pointer;
    @apply bg-surface-container-high;
    @apply font-bold text-onsurface;
}

.calendar-event-entry.assigned {
    @apply bg-primary-container;
    @apply text-onprimary-container;
}

.calendar-event-entry.waiting-list {
    @apply bg-primary-container bg-opacity-50;
    @apply text-onsecondary-container;
}

.calendar-event-entry.waiting-list .calendar-event-entry-bar {
    @apply bg-primary-container bg-opacity-50;
    @apply opacity-100;
    background-image: var(--pattern);
}

.calendar-event-entry.draft {
    @apply bg-surface-container-low;
}

.calendar-event-entry.draft .calendar-event-entry-bar {
    @apply bg-surface-container-low bg-opacity-50;
    @apply opacity-100;
    background-image: var(--pattern);
}

.calendar-event-entry.small .calendar-event-entry-bg {
    @apply flex items-center py-0;
    font-size: 0.6rem;
}

.calendar-event-entry.in-past {
    @apply border-opacity-10 opacity-50 hover:opacity-100;
}
</style>
