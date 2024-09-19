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
            <div class="flex items-center space-x-2">
                <span :title="props.event.name" class="block w-full truncate">
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
                <div class="rounded-2xl border border-primary-100 bg-primary-50 p-4 px-8 shadow-xl">
                    <div class="-mr-4 mb-4 flex items-center justify-end">
                        <!-- title -->
                        <h2 class="flex-grow text-lg">
                            <span>{{ props.event.name }}</span>
                        </h2>

                        <button class="dialog-close-button" title="Schließen" @click="showDropdown = false">
                            <i class="fa-solid fa-close"></i>
                        </button>
                    </div>

                    <!-- state -->
                    <div
                        v-if="props.event.signedInUserAssignedPosition"
                        class="-mx-4 mb-4 flex items-center space-x-4 rounded-xl bg-green-100 px-4 py-3 text-green-800"
                    >
                        <i class="fa-solid fa-check" />
                        <p class="text-sm font-bold">{{ $t('app.event-details.note-assigned') }}</p>
                    </div>
                    <div
                        v-else-if="props.event.signedInUserWaitingListPosition"
                        class="-mx-4 mb-4 flex items-center space-x-4 rounded-xl bg-blue-100 px-4 py-3 text-blue-800"
                    >
                        <i class="fa-solid fa-clock" />
                        <p class="text-sm font-bold">{{ $t('app.event-details.note-waitinglist') }}</p>
                    </div>

                    <!-- info -->
                    <div class="mb-4">
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-calendar-day w-4 text-gray-700"></i>
                            <span>{{ formatDateRange(props.event.start, props.event.end) }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell w-4 text-gray-700" />
                            <span>Crew an Board: {{ $d(event.start, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell-slash w-4 text-gray-700" />
                            <span>Crew von Board: {{ $d(event.end, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p v-if="props.event.assignedUserCount" class="items-center space-x-4">
                            <i class="fa-solid fa-users w-4 text-gray-700"></i>
                            <span>{{ props.event.assignedUserCount }} Crew</span>
                        </p>
                        <p v-if="props.event.description" class="flex items-baseline space-x-4">
                            <i class="fa-solid fa-info-circle mt-0.5 w-4 text-gray-700"></i>
                            <span class="line-clamp-3">{{ props.event.description }}</span>
                        </p>
                    </div>

                    <!-- route -->
                    <div class="">
                        <p
                            v-for="(location, index) in props.event.locations"
                            :key="index"
                            class="flex items-center space-x-4"
                        >
                            <i :class="location.icon" class="fa-solid w-4" />
                            <span class="flex-grow">{{ location.name }}</span>
                            <CountryFlag
                                v-if="location.country"
                                :country="location.country"
                                class="border border-gray-200"
                            />
                        </p>
                    </div>

                    <!-- primary button -->
                    <div class="mt-4 flex justify-end space-x-2 xl:-mr-4">
                        <button
                            v-if="props.event.signedInUserAssignedPosition"
                            class="btn-ghost-danger"
                            title="Event absagen"
                            :disabled="!props.event.canSignedInUserLeave"
                            @click="leaveEvent()"
                        >
                            <i class="fa-solid fa-ban"></i>
                            <span class="ml-2">Absagen</span>
                        </button>
                        <button
                            v-else-if="props.event.signedInUserWaitingListPosition && props.event.canSignedInUserLeave"
                            class="btn-ghost-danger"
                            title="Warteliste verlassen"
                            @click="leaveEvent()"
                        >
                            <i class="fa-solid fa-user-minus"></i>
                            <span class="ml-2">Warteliste verlassen</span>
                        </button>
                        <button
                            v-else-if="props.event.canSignedInUserJoin"
                            class="btn-ghost"
                            title="Anmelden"
                            @click="joinEvent()"
                        >
                            <i class="fa-solid fa-user-plus"></i>
                            <span class="ml-2">Anmelden</span>
                        </button>
                        <RouterLink
                            :to="{ name: Routes.EventDetails, params: { key: props.event.key } }"
                            class="btn-ghost"
                            title="Detailansicht"
                        >
                            <i class="fa-solid fa-up-right-from-square"></i>
                            <span class="ml-2">Details</span>
                        </RouterLink>
                    </div>
                </div>
            </div>
        </VDropdownWrapper>
    </div>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import { VDropdownWrapper } from '@/ui/components/common';
import CountryFlag from '@/ui/components/utils/CountryFlag.vue';
import { useEventUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { Routes } from '@/ui/views/Routes';

interface Props {
    event: Event;
    duration: number;
    durationInMonth: number;
    start: number;
}

interface Emits {
    (e: 'update:event', value: Event): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const route = useRoute();
const router = useRouter();
const eventUseCase = useEventUseCase();

const showDropdown = ref<boolean>(false);

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

async function joinEvent(): Promise<void> {
    const updatedEvent = await eventUseCase.joinEvent(props.event);
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
    @apply rounded-lg bg-white hover:shadow-lg;
    @apply overflow-hidden;
}

.calendar-event-entry {
    @apply block h-full w-full py-1 pl-2 pr-4 sm:px-2;
    @apply cursor-pointer;
    @apply text-sm font-semibold;
    --color-1: rgb(255 255 255 / 0.6);
    --color-2: rgb(255 255 255 / 0.2);
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
    @apply border-l-8 border-blue-400 bg-blue-200 bg-opacity-75 hover:bg-opacity-100;
    @apply text-blue-800;
}

.calendar-event-entry:not(.in-past).full {
    background-image: var(--pattern);
}

.calendar-event-entry.assigned {
    @apply border-l-8 border-green-700 bg-green-200 bg-opacity-75 hover:bg-opacity-100;
    @apply font-bold;
}

.calendar-event-entry.waiting-list {
    @apply border-l-8 border-green-200 bg-green-200 bg-opacity-75 hover:bg-opacity-100;
    @apply text-green-800;
    background-image: var(--pattern);
}

.calendar-event-entry.small {
    @apply flex items-center py-0;
    font-size: 0.6rem;
}

.calendar-event-entry.in-past {
    @apply border-l-8 border-gray-400 bg-gray-200 bg-opacity-75 hover:bg-opacity-100;
    @apply text-gray-800;
}
</style>
