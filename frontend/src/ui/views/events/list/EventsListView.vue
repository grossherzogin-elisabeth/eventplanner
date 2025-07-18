<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" placeholder="Reisen durchsuchen" />
            </div>
        </teleport>

        <div v-if="signedInUser.positions.length === 0" class="px-4 md:px-12 xl:px-16">
            <VInfo class="mt-4 xl:mt-8" clamp>
                Deinem Benutzerkonto wurde bisher noch keine Position zugewiesen. Du kannst dich deshalb nicht selber für Reisen anmelden.
                Bitte melde dich im Büro um dir eine Position zuweisen zu lassen.
            </VInfo>
        </div>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <VSearchButton v-model="filter" placeholder="Reisen filtern" />
                </div>
            </template>
        </VTabs>

        <div class="scrollbar-invisible mt-4 flex items-center gap-2 overflow-x-auto px-4 md:px-16 xl:min-h-8 xl:px-20">
            <ContextMenuButton
                anchor-align-x="left"
                dropdown-position-x="right"
                class="btn-tag"
                :class="{ active: filterEventType.length > 0 }"
            >
                <template #icon>
                    <span v-if="filterEventType.length === 0">Alle Reisearten</span>
                    <span v-else-if="filterEventType.length > 4"> {{ filterEventType.length }} Reisearten </span>
                    <span v-else class="block max-w-64 truncate">
                        {{ filterEventType.map(eventTypes.getName).join(', ') }}
                    </span>
                </template>
                <template #default>
                    <ul>
                        <li v-if="filterEventType.length === 0" class="context-menu-item">
                            <i class="fa-solid fa-check"></i>
                            <span>Alle Reisearten</span>
                        </li>
                        <li v-else class="context-menu-item" @click="filterEventType = []">
                            <i class="w-4"></i>
                            <span>Alle Reisearten</span>
                        </li>
                        <template v-for="eventType in eventTypes.options.value" :key="eventType.value">
                            <li
                                v-if="filterEventType.includes(eventType.value)"
                                class="context-menu-item"
                                @click="filterEventType = filterEventType.filter((it) => it !== eventType.value)"
                            >
                                <i class="fa-solid fa-check w-4"></i>
                                <span>{{ eventType.label }}</span>
                            </li>
                            <li v-else class="context-menu-item" @click="filterEventType.push(eventType.value)">
                                <i class="w-4"></i>
                                <span>{{ eventType.label }}</span>
                            </li>
                        </template>
                    </ul>
                </template>
            </ContextMenuButton>
            <button class="btn-tag" :class="{ active: filterAssigned }" @click="filterAssigned = !filterAssigned">
                <span>Eingeplant</span>
            </button>
            <button class="btn-tag" :class="{ active: filterWaitingList }" @click="filterWaitingList = !filterWaitingList">
                <span>Warteliste</span>
            </button>
            <button class="btn-tag" :class="{ active: filterFreeSlots }" @click="filterFreeSlots = !filterFreeSlots">
                <span>Freie Plätze</span>
            </button>
        </div>

        <div class="w-full">
            <VTable
                :items="filteredEvents"
                multiselection
                query
                :page-size="20"
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="openEvent($event.item, $event.event)"
            >
                <template #row="{ item }">
                    <!-- name -->
                    <td
                        class="w-2/3 max-w-[80vw] font-semibold"
                        style="max-width: min(65vw, 20rem)"
                        :class="{ 'opacity-50': item.isPastEvent }"
                    >
                        <p
                            class="mb-1 truncate whitespace-nowrap"
                            :class="{ 'text-error line-through': item.state === EventState.Canceled }"
                        >
                            <span v-if="item.state === EventState.Draft" class="opacity-50">Entwurf: </span>
                            <span v-else-if="item.state === EventState.Canceled">Abgesagt: </span>
                            {{ item.name }}
                        </p>
                        <p v-if="item.signedInUserAssignedSlot" class="truncate text-sm font-light">
                            Du bist als
                            <i>{{ positions.get(item.signedInUserRegistration?.positionKey).name }}</i>
                            eingeplant.
                        </p>
                        <p v-else-if="item.signedInUserRegistration" class="truncate text-sm font-light">
                            Du stehst als
                            <i>{{ positions.get(item.signedInUserRegistration.positionKey).name }}</i>
                            auf der Warteliste.
                        </p>
                        <p v-else-if="item.description" class="truncate text-sm font-light">
                            {{ item.description }}
                        </p>
                        <p v-else class="truncate text-sm font-light">
                            <template v-if="item.locations.length === 0">keine Reiseroute angegeben</template>
                            <template v-else>{{ item.locations.map((it) => it.name).join(' - ') }}</template>
                        </p>
                    </td>
                    <!-- status -->
                    <td class="w-1/6" :class="{ 'opacity-50': item.isPastEvent }">
                        <div class="flex items-center lg:justify-end">
                            <div :key="item.stateDetails.icon" class="status-panel" :class="item.stateDetails.color">
                                <i class="fa-solid w-4" :class="item.stateDetails.icon"></i>
                                <span class="whitespace-nowrap font-semibold">{{ item.stateDetails.name }}</span>
                            </div>
                        </div>
                    </td>
                    <!-- crew -->
                    <td class="w-1/6 min-w-24 whitespace-nowrap" :class="{ 'opacity-50': item.isPastEvent }">
                        <template v-if="[EventState.Draft, EventState.OpenForSignup].includes(item.state)">
                            <p class="mb-1 pl-4 font-semibold">
                                <span> {{ item.registrations.length }} </span>
                            </p>
                            <p class="pl-4 text-sm">Anmeldungen</p>
                        </template>
                        <template v-else>
                            <p class="mb-1 pl-4 font-semibold">
                                {{ item.assignedUserCount }}
                                <span v-if="item.waitingListCount" class="opacity-40"> +{{ item.waitingListCount }} </span>
                            </p>
                            <p class="pl-4 text-sm">Crew</p>
                        </template>
                    </td>
                    <!-- date -->
                    <td class="w-1/6 whitespace-nowrap" :class="{ 'opacity-50': item.isPastEvent }">
                        <p class="mb-1 font-semibold lg:hidden">{{ $d(item.start, DateTimeFormat.DDD_DD_MM) }}</p>
                        <p class="mb-1 hidden font-semibold lg:block">{{ formatDateRange(item.start, item.end) }}</p>
                        <p class="text-sm">{{ item.days }} Tage</p>
                    </td>
                </template>
                <template #loading>
                    <tr v-for="i in 20" :key="i" class="animate-pulse">
                        <td></td>
                        <td class="w-1/2 max-w-[65vw]">
                            <p class="mb-1 h-5 w-64 rounded-lg bg-surface-container-highest"></p>
                            <p class="flex items-center space-x-2 text-sm font-light">
                                <span class="inline-block h-3 w-16 rounded-lg bg-surface-container-highest"></span>
                                <span class="inline-block h-3 w-16 rounded-lg bg-surface-container-highest"></span>
                                <span class="inline-block h-3 w-16 rounded-lg bg-surface-container-highest"></span>
                            </p>
                        </td>
                        <td>
                            <div class="status-panel bg-surface-container-highest">
                                <i class="fa-solid fa-circle text-surface-container-high"></i>
                                <span class="my-0.5 inline-block h-4 w-12 rounded-lg bg-surface-container-high"></span>
                            </div>
                        </td>
                        <td class="w-1/6">
                            <p class="mb-1 h-5 w-16 rounded-lg bg-surface-container-highest"></p>
                            <p class="h-3 w-10 rounded-lg bg-surface-container-highest"></p>
                        </td>
                        <td class="w-2/6">
                            <p class="mb-1 h-5 w-56 rounded-lg bg-surface-container-highest"></p>
                            <p class="h-3 w-16 rounded-lg bg-surface-container-highest"></p>
                        </td>

                        <td>
                            <div class="px-4 py-2">
                                <i class="fa-solid fa-circle text-surface-container-highest"></i>
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
                    <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntry(item)">
                        <i class="fa-solid fa-calendar-alt" />
                        <span>Kalendereintrag erstellen</span>
                    </li>
                    <template v-if="!item.signedInUserRegistration">
                        <li
                            class="permission-write-own-registrations context-menu-item"
                            :class="{ disabled: !item.canSignedInUserJoin }"
                            @click="joinEvents([item])"
                        >
                            <i class="fa-solid fa-user-plus" />
                            <span class="truncate"> Anmelden </span>
                        </li>
                    </template>
                    <li
                        v-if="item.signedInUserAssignedSlot"
                        class="permission-write-own-registrations context-menu-item text-error"
                        :class="{ disabled: item.isPastEvent }"
                        @click="leaveEvents([item])"
                    >
                        <i class="fa-solid fa-ban" />
                        <span>Reise absagen</span>
                    </li>
                    <li
                        v-else-if="item.signedInUserRegistration"
                        class="permission-write-own-registrations context-menu-item"
                        :class="{ disabled: item.isPastEvent }"
                        @click="leaveEvents([item])"
                    >
                        <i class="fa-solid fa-user-minus" />
                        <span>Warteliste verlassen</span>
                    </li>
                </template>
            </VTable>
        </div>

        <VConfirmationDialog ref="confirmationDialog" />

        <div class="flex-1"></div>

        <VMultiSelectActions
            v-if="selectedEvents && selectedEvents.length > 0"
            :count="selectedEvents.length"
            @select-all="selectAll()"
            @select-none="selectNone()"
        >
            <template #action>
                <div class="hidden sm:inline">
                    <button
                        class="btn-ghost"
                        :disabled="!hasAnySelectedEventInFuture || signedInUser.positions.length === 0"
                        @click="joinEvents(selectedEvents)"
                    >
                        <i class="fa-solid fa-user-plus"></i>
                        <span class="truncate"> Anmelden </span>
                    </button>
                </div>
            </template>
            <template #menu>
                <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntries(selectedEvents)">
                    <i class="fa-solid fa-calendar-alt" />
                    <span>Kalendereintrag erstellen</span>
                </li>
                <li
                    class="permission-write-own-registrations context-menu-item"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="joinEvents(selectedEvents)"
                >
                    <i class="fa-solid fa-user-plus" />
                    <span class="truncate"> Anmelden </span>
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
                    class="permission-write-own-registrations context-menu-item text-error"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="leaveEvents(selectedEvents)"
                >
                    <i class="fa-solid fa-ban" />
                    <span class="truncate"> Reisen absagen </span>
                </li>
            </template>
        </VMultiSelectActions>

        <RegistrationDetailsSheet ref="createRegistrationSheet" />
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRouter } from 'vue-router';
import { DateTimeFormat } from '@/common/date';
import type { Event, Registration, SignedInUser } from '@/domain';
import { EventState, EventType } from '@/domain';
import type { ConfirmationDialog, Sheet } from '@/ui/components/common';
import { ContextMenuButton, VConfirmationDialog, VInfo, VMultiSelectActions, VTable, VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import RegistrationDetailsSheet from '@/ui/components/sheets/RegistrationDetailsSheet.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQueryStateSync } from '@/ui/composables/QueryState.ts';
import { restoreScrollPosition } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes.ts';

interface StateDetails {
    name: string;
    color: string;
    icon: string;
}

interface EventTableViewItem extends Event {
    selected: boolean;
    isPastEvent: boolean;
    waitingListCount: number;
    hasOpenSlots: boolean;
    hasOpenImportantSlots: boolean;
    stateDetails: StateDetails;
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const router = useRouter();
const positions = usePositions();
const eventTypes = useEventTypes();

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const events = ref<EventTableViewItem[] | null>(null);
const tab = ref<string>('Zukünftige');
const filter = ref<string>('');
const filterAssigned = ref<boolean>(false);
const filterWaitingList = ref<boolean>(false);
const filterFreeSlots = ref<boolean>(false);
const filterEventType = ref<EventType[]>([]);

const confirmationDialog = ref<ConfirmationDialog | null>(null);
const createRegistrationSheet = ref<Sheet<{ registration?: Registration; event: Event | Event[] }, Registration | undefined> | null>(null);

useQueryStateSync<boolean>(
    'assigned',
    () => filterAssigned.value,
    (v) => (filterAssigned.value = v)
);
useQueryStateSync<boolean>(
    'waitinglist',
    () => filterWaitingList.value,
    (v) => (filterWaitingList.value = v)
);
useQueryStateSync<boolean>(
    'has-free-slots',
    () => filterFreeSlots.value,
    (v) => (filterFreeSlots.value = v)
);
useQueryStateSync<string>(
    'types',
    () => filterEventType.value.join('_'),
    (v) => (filterEventType.value = v.split('_') as EventType[])
);
useQueryStateSync<string>(
    'filter',
    () => filter.value,
    (v) => (filter.value = v)
);

const hasAnySelectedEventInFuture = computed<boolean>(() => {
    const now = new Date().getTime();
    return selectedEvents.value?.find((it) => it.start.getTime() > now) !== undefined;
});

const hasAnySelectedEventWithSignedInUserOnWaitingList = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserRegistration && !it.signedInUserAssignedSlot) !== undefined;
});

const hasAnySelectedEventWithSignedInUserInTeam = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserRegistration && it.signedInUserAssignedSlot) !== undefined;
});

const filteredEvents = computed<EventTableViewItem[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value
        ?.filter((it) => eventService.doesEventMatchFilter(it, f))
        .filter((it) => filterEventType.value.length === 0 || filterEventType.value.includes(it.type))
        .filter((it) => {
            if (filterAssigned.value || filterWaitingList.value || filterFreeSlots.value) {
                let state = 0;
                if (it.signedInUserAssignedSlot || it.type === EventType.WorkEvent) {
                    state = 1;
                } else if (it.signedInUserRegistration) {
                    state = 2;
                } else if (it.hasOpenSlots) {
                    state = 3;
                }
                return (
                    (filterAssigned.value && state === 1) ||
                    (filterWaitingList.value && state === 2) ||
                    (filterFreeSlots.value && state === 3)
                );
            }
            return true;
        });
});

const selectedEvents = computed<EventTableViewItem[] | undefined>(() => {
    return filteredEvents.value?.filter((it) => it.selected);
});

const tabs = computed<string[]>(() => {
    const currentYear = new Date().getFullYear();
    return ['Zukünftige', String(currentYear + 1), String(currentYear), String(currentYear - 1)];
});

async function init(): Promise<void> {
    emit('update:tab-title', 'Alle Reisen');
    watch(tab, () => fetchEvents());
    await nextTick(); // wait for the tab to have the correct value before fetching
    await fetchEvents();
    restoreScrollPosition();
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
            isPastEvent: evt.start.getTime() < new Date().getTime(),
            waitingListCount: evt.registrations.length - evt.assignedUserCount,
            hasOpenSlots: eventService.hasOpenSlots(evt),
            hasOpenImportantSlots: eventService.hasOpenImportantSlots(evt),
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
    if (event.type === EventType.WorkEvent) {
        return { name: 'Eingeplant', icon: 'fa-check-circle', color: 'bg-green-container text-ongreen-container' };
    }
    if (event.state === EventState.Canceled) {
        return { name: 'Abgesagt', icon: 'fa-ban', color: 'bg-red-container text-onred-container' };
    }
    if (event.signedInUserAssignedSlot) {
        return { name: 'Eingeplant', icon: 'fa-check-circle', color: 'bg-green-container text-ongreen-container' };
    }
    if (event.signedInUserRegistration) {
        return { name: 'Warteliste', icon: 'fa-hourglass-half', color: 'bg-surface-container-highest text-onsurface' };
    }
    if (event.state === EventState.Draft) {
        return { name: 'Entwurf', icon: 'fa-compass-drafting', color: 'bg-surface-container-highest text-onsurface' };
    }
    if (event.state === EventState.OpenForSignup) {
        return { name: 'Crew Anmeldung', icon: 'fa-people-group', color: 'bg-blue-container text-onblue-container' };
    }
    if (event.hasOpenImportantSlots) {
        return { name: 'Crew gesucht', icon: 'fa-info-circle', color: 'bg-yellow-container text-onyellow-container' };
    }
    if (event.hasOpenSlots) {
        return { name: 'Freie Plätze', icon: 'fa-info-circle', color: 'bg-blue-container text-onblue-container' };
    }
    return { name: 'Voll belegt', icon: 'fa-info-circle', color: 'bg-surface-container-highest text-onsurface' };
}

async function openEvent(item: EventTableViewItem, evt: MouseEvent): Promise<void> {
    const to: RouteLocationRaw = {
        name: Routes.EventDetails,
        params: { year: item.start.getFullYear(), key: item.key },
    };
    if (evt.ctrlKey || evt.metaKey) {
        window.open(router.resolve(to).href, '_blank');
    } else {
        await router.push(to);
    }
}

async function joinEvents(events: EventTableViewItem[]): Promise<void> {
    const registration = await createRegistrationSheet.value?.open({
        event: events,
        registration: undefined,
    });
    if (registration) {
        await eventUseCase.joinEvents(events, registration);
        await fetchEvents();
    }
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
