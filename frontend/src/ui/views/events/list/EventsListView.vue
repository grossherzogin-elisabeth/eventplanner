<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" placeholder="Reisen durchsuchen" />
            </div>
        </teleport>

        <div v-if="signedInUser.positions.length === 0" class="px-4 md:px-12 xl:px-16">
            <VInfo class="mt-4 xl:mt-8">
                Deinem Benutzerkonto wurde bisher noch keine Position zugewiesen. Du kannst dich deshalb nicht selber
                für Reisen anmelden. Bitte melde dich im Büro um dir eine Position zuweisen zu lassen.
            </VInfo>
        </div>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="flex items-stretch gap-2 pb-2">
                    <VSearchButton v-model="filter" placeholder="Reisen filtern" />
                </div>
            </template>
        </VTabs>

        <div class="w-full">
            <VTable
                :items="filteredEvents"
                multiselection
                query
                :page-size="20"
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="openEvent($event)"
            >
                <template #row="{ item }">
                    <!-- name -->
                    <td
                        class="w-2/3 max-w-[80vw] font-semibold"
                        style="max-width: min(65vw, 20rem)"
                        :class="{ 'text-primary-900 text-opacity-50': item.isPastEvent }"
                    >
                        <p
                            class="mb-1 truncate whitespace-nowrap"
                            :class="{ 'text-red-700 line-through': item.state === EventState.Canceled }"
                        >
                            <span v-if="item.state === EventState.Draft" class="opacity-50">Entwurf: </span>
                            <span v-else-if="item.state === EventState.Canceled" class="">Abgesagt: </span>
                            {{ item.name }}
                        </p>
                        <p v-if="item.signedInUserAssignedPosition" class="truncate text-sm font-light">
                            Du bist als
                            <i>{{ positions.get(item.signedInUserAssignedPosition).name }}</i>
                            eingeplant.
                        </p>
                        <p v-else-if="item.signedInUserWaitingListPosition" class="truncate text-sm font-light">
                            Du stehst als
                            <i>{{ positions.get(item.signedInUserWaitingListPosition).name }}</i>
                            auf der Warteliste.
                        </p>
                        <p v-else class="truncate text-sm font-light">
                            <template v-if="item.locations.length === 0">keine Reiseroute angegeben</template>
                            <template v-else>{{ item.locations.map((it) => it.name).join(' - ') }}</template>
                        </p>
                        <!--                        <p class="truncate text-sm font-light md:hidden">-->
                        <!--                            {{ formatDateRange(item.start, item.end) }}-->
                        <!--                        </p>-->
                    </td>
                    <!-- status -->
                    <td class="w-1/6">
                        <div class="flex items-center lg:justify-end">
                            <div
                                class="inline-flex w-auto items-center space-x-2 rounded-full py-1 pl-3 pr-4"
                                :class="item.stateDetails.color"
                            >
                                <i class="fa-solid w-4" :class="item.stateDetails.icon"></i>
                                <span class="whitespace-nowrap font-semibold">{{ item.stateDetails.name }}</span>
                            </div>
                        </div>
                    </td>
                    <!-- date -->
                    <td
                        class="hidden w-1/6 whitespace-nowrap md:table-cell"
                        :class="{ 'text-primary-900 text-opacity-50': item.isPastEvent }"
                    >
                        <p class="mb-1 font-semibold lg:hidden">{{ $d(item.start, DateTimeFormat.DDD_DD_MM) }}</p>
                        <p class="mb-1 hidden font-semibold lg:block">{{ formatDateRange(item.start, item.end) }}</p>
                        <p class="text-sm">{{ item.duration }} Tage</p>
                    </td>
                </template>
                <template #loading>
                    <tr v-for="i in 20" :key="i" class="animate-pulse">
                        <td></td>
                        <td class="w-1/2 max-w-[65vw]">
                            <p class="mb-1 h-5 w-64 rounded-lg bg-primary-200"></p>
                            <p class="flex items-center space-x-2 text-sm font-light">
                                <span class="inline-block h-3 w-16 rounded-lg bg-primary-200"></span>
                                <span class="inline-block h-3 w-16 rounded-lg bg-primary-200"></span>
                                <span class="inline-block h-3 w-16 rounded-lg bg-primary-200"></span>
                            </p>
                        </td>
                        <td>
                            <div
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-primary-100 py-1 pl-3 pr-4"
                            >
                                <i class="fa-solid fa-circle text-primary-200"></i>
                                <span class="my-0.5 inline-block h-4 w-12 rounded-lg bg-primary-200"></span>
                            </div>
                        </td>
                        <td class="w-1/6">
                            <p class="mb-1 h-5 w-16 rounded-lg bg-primary-200"></p>
                            <p class="h-3 w-10 rounded-lg bg-primary-200"></p>
                        </td>
                        <td class="w-2/6">
                            <p class="mb-1 h-5 w-56 rounded-lg bg-primary-200"></p>
                            <p class="h-3 w-16 rounded-lg bg-primary-200"></p>
                        </td>

                        <td class="">
                            <div class="px-4 py-2">
                                <i class="fa-solid fa-circle text-primary-200"></i>
                            </div>
                        </td>
                        <td></td>
                    </tr>
                </template>
                <template #context-menu="{ item }">
                    <li class="permission-read-events">
                        <RouterLink
                            :to="{
                                name: Routes.EventDetails,
                                params: { year: item.start.getFullYear(), key: item.key },
                            }"
                            class="context-menu-item"
                        >
                            <i class="fa-solid fa-search" />
                            <span>Reise anzeigen</span>
                        </RouterLink>
                    </li>
                    <template v-if="!item.signedInUserWaitingListPosition && !item.signedInUserAssignedPosition">
                        <li
                            v-if="signedInUser.positions.length === 1"
                            class="permission-write-own-registrations context-menu-item"
                            :class="{ disabled: !item.canSignedInUserJoin }"
                            @click="joinEvents([item])"
                        >
                            <i class="fa-solid fa-user-plus" />
                            <span class="truncate">
                                Anmelden als {{ positions.get(signedInUser.positions[0]).name }}
                            </span>
                        </li>
                        <li
                            v-else
                            class="permission-write-own-registrations context-menu-item"
                            :class="{ disabled: !item.canSignedInUserJoin }"
                            @click="choosePositionAndJoinEvents([item])"
                        >
                            <i class="fa-solid fa-user-plus" />
                            <span>Anmelden als ...</span>
                        </li>
                    </template>
                    <li
                        v-if="item.signedInUserWaitingListPosition"
                        class="permission-write-own-registrations context-menu-item"
                        :class="{ disabled: item.isPastEvent }"
                        @click="leaveEvents([item])"
                    >
                        <i class="fa-solid fa-user-minus" />
                        <span>Warteliste verlassen</span>
                    </li>
                    <template v-else-if="item.signedInUserAssignedPosition">
                        <li
                            class="permission-write-own-registrations context-menu-item text-red-700"
                            :class="{ disabled: item.isPastEvent }"
                            @click="leaveEvents([item])"
                        >
                            <i class="fa-solid fa-ban" />
                            <span>Reise absagen</span>
                        </li>
                    </template>
                </template>
            </VTable>
        </div>

        <VConfirmationDialog ref="confirmationDialog" />
        <PositionSelectDlg ref="positionSelectDialog" />

        <div class="flex-1"></div>

        <div v-if="selectedEvents && selectedEvents.length > 0" class="sticky bottom-0 z-20">
            <div
                class="h-full border-t border-primary-200 bg-primary-50 px-2 md:px-12 xl:rounded-bl-3xl xl:pb-4 xl:pl-16 xl:pr-20"
            >
                <div class="flex h-full items-stretch gap-2 whitespace-nowrap py-2">
                    <button class="btn-ghost" @click="selectNone()">
                        <i class="fa-solid fa-xmark text-base" />
                    </button>
                    <span class="self-center text-base font-bold">{{ selectedEvents.length }} ausgewählt</span>
                    <div class="flex-grow"></div>
                    <div class="hidden sm:block">
                        <button
                            class="btn-ghost"
                            :disabled="!hasAnySelectedEventInFuture"
                            @click="joinEvents(selectedEvents)"
                        >
                            <i class="fa-solid fa-user-plus"></i>
                            <span class="truncate text-base">
                                Anmelden als {{ positions.get(signedInUser.positions[0]).name || '...' }}
                            </span>
                        </button>
                    </div>
                    <ContextMenuButton class="btn-ghost">
                        <template #default>
                            <li class="context-menu-item" @click="selectAll">
                                <i class="fa-solid fa-list-check" />
                                <span>Alle auswählen</span>
                            </li>
                            <li
                                v-if="signedInUser.positions.length === 1"
                                class="permission-write-own-registrations context-menu-item"
                                :class="{ disabled: !hasAnySelectedEventInFuture }"
                                @click="joinEvents(selectedEvents)"
                            >
                                <i class="fa-solid fa-user-plus" />
                                <span class="truncate">
                                    Anmelden als {{ positions.get(signedInUser.positions[0]).name }}
                                </span>
                            </li>
                            <li
                                v-else
                                class="permission-write-own-registrations context-menu-item"
                                :class="{ disabled: !hasAnySelectedEventInFuture }"
                                @click="choosePositionAndJoinEvents(selectedEvents)"
                            >
                                <i class="fa-solid fa-user-plus" />
                                <span>Anmelden als ...</span>
                            </li>
                            <li
                                v-if="hasAnySelectedEventWithSignedInUserOnWaitingList"
                                class="permission-write-own-registrations context-menu-item"
                                :class="{ disabled: !hasAnySelectedEventInFuture }"
                                @click="leaveEventsWaitingListOnly(selectedEvents)"
                            >
                                <i class="fa-solid fa-user-minus" />
                                <span class="truncate"> Warteliste verlassen </span>
                            </li>
                            <li
                                v-if="hasAnySelectedEventWithSignedInUserInTeam"
                                class="permission-write-own-registrations context-menu-item text-red-700"
                                :class="{ disabled: !hasAnySelectedEventInFuture }"
                                @click="leaveEvents(selectedEvents)"
                            >
                                <i class="fa-solid fa-ban" />
                                <span class="truncate"> Reisen absagen </span>
                            </li>
                        </template>
                    </ContextMenuButton>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DateTimeFormat } from '@/common/date';
import type { Event, PositionKey, SignedInUser } from '@/domain';
import { EventState } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VInfo } from '@/ui/components/common';
import { ContextMenuButton, VConfirmationDialog, VTable, VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import PositionSelectDlg from '@/ui/components/events/PositionSelectDlg.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { Routes } from '@/ui/views/Routes.ts';

interface StateDetails {
    name: string;
    color: string;
    icon: string;
}

interface EventTableViewItem extends Event {
    selected: boolean;
    duration: number;
    isPastEvent: boolean;
    waitingListCount: number;
    hasOpenSlots: boolean;
    hasOpenRequiredSlots: boolean;
    stateDetails: StateDetails;
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const route = useRoute();
const router = useRouter();
const positions = usePositions();

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const events = ref<EventTableViewItem[] | null>(null);
const tab = ref<string>('Zukünftige');
const filter = ref<string>('');

const confirmationDialog = ref<ConfirmationDialog | null>(null);
const positionSelectDialog = ref<Dialog<void, PositionKey | undefined> | null>(null);

const hasAnySelectedEventInFuture = computed<boolean>(() => {
    const now = new Date().getTime();
    return selectedEvents.value?.find((it) => it.start.getTime() > now) !== undefined;
});

const hasAnySelectedEventWithSignedInUserOnWaitingList = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserWaitingListPosition) !== undefined;
});

const hasAnySelectedEventWithSignedInUserInTeam = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserAssignedPosition) !== undefined;
});

const filteredEvents = computed<EventTableViewItem[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value?.filter((it) => it.name.toLowerCase().includes(f));
});

const selectedEvents = computed<EventTableViewItem[] | undefined>(() => {
    return filteredEvents.value?.filter((it) => it.selected);
});

const tabs = computed<string[]>(() => {
    const currentYear = new Date().getFullYear();
    return ['Zukünftige', String(currentYear + 1), String(currentYear), String(currentYear - 1)];
});

function init(): void {
    emit('update:title', 'Alle Reisen');
    watch(route, () => fetchEvents());
    watch(tab, () => fetchEvents());
    onMounted(() => {
        if (tab.value === tabs.value[0]) {
            fetchEvents();
        }
    });
}

function selectNone(): void {
    events.value?.forEach((it) => (it.selected = false));
}

function selectAll(): void {
    events.value?.forEach((it) => (it.selected = true));
}

async function fetchEvents(): Promise<void> {
    events.value = null;
    if (tab.value === tabs.value[0]) {
        const now = new Date();
        const currentYear = await fetchEventsByYear(now.getFullYear());
        const nextYear = await fetchEventsByYear(now.getFullYear() + 1);
        events.value = currentYear.concat(nextYear).filter((it) => it.end.getTime() > now.getTime());
    } else {
        const year = parseInt(tab.value);
        if (year) {
            events.value = await fetchEventsByYear(year);
        }
    }
}

async function fetchEventsByYear(year: number): Promise<EventTableViewItem[]> {
    const evts = await eventUseCase.getEvents(year);
    return evts.map((evt) => {
        const tableItem: EventTableViewItem = {
            ...evt,
            selected: false,
            duration: new Date(evt.end.getTime() - evt.start.getTime()).getDate(),
            isPastEvent: evt.start.getTime() < new Date().getTime(),
            waitingListCount: evt.registrations.length - evt.assignedUserCount,
            hasOpenSlots: hasOpenSlots(evt),
            hasOpenRequiredSlots: eventService.hasOpenRequiredSlots(evt),
            stateDetails: {
                name: '',
                icon: '',
                color: '',
            },
        };
        tableItem.stateDetails = getStateDetails(tableItem);
        return tableItem;
    });
}

function getStateDetails(event: EventTableViewItem): StateDetails {
    if (event.isPastEvent) {
        return { name: 'Vergangene', icon: 'fa-check-circle', color: 'bg-gray-200 text-gray-700' };
    }
    if (event.state === EventState.Canceled) {
        return { name: 'Abgesagt', icon: 'fa-ban', color: 'bg-red-200 text-red-700' };
    }
    if (event.signedInUserAssignedPosition) {
        return { name: 'Eingeplant', icon: 'fa-check-circle', color: 'bg-green-200 text-green-700' };
    }
    if (event.signedInUserWaitingListPosition) {
        return { name: 'Warteliste', icon: 'fa-hourglass-half', color: 'bg-gray-200 text-gray-700' };
    }
    if (event.state === EventState.Draft) {
        return { name: 'Entwurf', icon: 'fa-compass-drafting', color: 'bg-gray-200 text-gray-700' };
    }
    if (event.state === EventState.OpenForSignup) {
        return { name: 'Crew Anmeldung', icon: 'fa-unlock', color: 'bg-blue-200 text-blue-700' };
    }
    if (event.hasOpenRequiredSlots) {
        return { name: 'Crew gesucht', icon: 'fa-info-circle', color: 'bg-blue-200 text-blue-700' };
    }
    if (event.hasOpenSlots) {
        return { name: 'Freie Plätze', icon: 'fa-info-circle', color: 'bg-blue-200 text-blue-700' };
    }
    return { name: 'Voll belegt', icon: 'fa-info-circle', color: 'bg-gray-200 text-gray-700' };
}

function hasOpenSlots(event: Event): boolean {
    return event.slots.filter((slt) => !slt.assignedRegistrationKey).length > 0;
}

async function openEvent(evt: EventTableViewItem): Promise<void> {
    await router.push({
        name: Routes.EventDetails,
        params: { year: evt.start.getFullYear(), key: evt.key },
    });
}

async function choosePositionAndJoinEvents(events: EventTableViewItem[]): Promise<void> {
    const position = await positionSelectDialog.value?.open();
    if (position) {
        // default position might have changed
        signedInUser.value = authUseCase.getSignedInUser();
        await eventUseCase.joinEvents(events, signedInUser.value.positions[0]);
        await fetchEvents();
    }
}

async function joinEvents(events: EventTableViewItem[]): Promise<void> {
    await eventUseCase.joinEvents(events, signedInUser.value.positions[0]);
    await fetchEvents();
}

async function leaveEventsWaitingListOnly(events: EventTableViewItem[]): Promise<void> {
    await eventUseCase.leaveEventsWaitingListOnly(events);
    await fetchEvents();
}

async function leaveEvents(events: EventTableViewItem[]): Promise<void> {
    await eventUseCase.leaveEvents(events);
    await fetchEvents();
}

init();
</script>
