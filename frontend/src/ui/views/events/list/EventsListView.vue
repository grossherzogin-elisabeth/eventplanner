<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" :placeholder="$t('views.events.list.filter.search')" />
            </div>
        </teleport>

        <div v-if="signedInUser.positions.length === 0" class="px-4 md:px-12 xl:px-16">
            <VInfo class="mt-4 xl:mt-8" clamp>
                {{ $t('views.events.list.note-no-position') }}
            </VInfo>
        </div>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <VSearchButton v-model="filter" :placeholder="$t('views.events.list.filter.search')" />
                </div>
            </template>
        </VTabs>

        <div class="scrollbar-invisible mt-4 flex items-center gap-2 overflow-x-auto px-4 md:px-16 xl:min-h-8 xl:px-20">
            <FilterMultiselect
                v-model="filterEventType"
                :placeholder="$t('views.events.list.filter.all-types')"
                :options="eventTypes.options.value"
            />
            <FilterToggle v-model="filterAssigned" :label="$t('views.events.list.filter.assigned')" />
            <FilterToggle v-model="filterWaitingList" :label="$t('views.events.list.filter.waitinglist')" />
            <FilterToggle v-model="filterFreeSlots" :label="$t('views.events.list.filter.free-slots')" />
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
                    <!-- date -->
                    <td class="hidden w-1/6 whitespace-nowrap lg:table-cell" :class="{ 'opacity-50': item.isPastEvent }">
                        <p class="mb-1 font-semibold 2xl:hidden">{{ $d(item.start, DateTimeFormat.DDD_DD_MM) }}</p>
                        <p class="mb-1 hidden font-semibold 2xl:block">{{ formatDateRange(item.start, item.end) }}</p>
                        <p class="text-sm">
                            {{ $t('views.events.list.table.day-count', { count: item.days }) }}
                        </p>
                    </td>
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
                            <span v-if="item.state === EventState.Draft" class="opacity-50"> {{ $t('generic.event-state.draft') }}: </span>
                            <span v-else-if="item.state === EventState.Canceled" class="opacity-50">
                                {{ $t('generic.event-state.canceled') }}:
                            </span>
                            {{ item.name }}
                        </p>
                        <i18n-t
                            v-if="item.isSignedInUserAssigned"
                            tag="p"
                            class="hidden truncate text-sm font-light lg:block"
                            keypath="views.events.list.table.assigned-as"
                        >
                            <i>{{ positions.get(item.signedInUserRegistration?.positionKey).name }}</i>
                        </i18n-t>
                        <i18n-t
                            v-else-if="item.signedInUserRegistration"
                            tag="p"
                            class="hidden truncate text-sm font-light lg:block"
                            keypath="views.events.list.table.on-waiting-list-as"
                        >
                            <i>{{ positions.get(item.signedInUserRegistration.positionKey).name }}</i>
                        </i18n-t>
                        <p v-else-if="item.description" class="hidden truncate text-sm font-light lg:block">
                            {{ item.description }}
                        </p>
                        <p v-else class="hidden truncate text-sm font-light lg:block">
                            {{ item.locations.map((it) => it.name).join(' - ') }}
                        </p>
                        <p class="truncate text-sm font-light lg:hidden">
                            {{ formatDateRange(item.start, item.end) }} |
                            {{ $t('views.events.list.table.day-count', { count: item.days }) }}
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
                        <template
                            v-if="
                                [EventState.Draft, EventState.OpenForSignup].includes(item.state) ||
                                item.signupType === EventSignupType.Open
                            "
                        >
                            <p class="mb-1 pl-4 font-semibold">
                                <span> {{ item.registrations.length }} </span>
                            </p>
                            <p class="pl-4 text-sm">
                                {{ $t('views.events.list.table.registration-count', { count: item.registrations.length }) }}
                            </p>
                        </template>
                        <template v-else>
                            <p class="mb-1 pl-4 font-semibold">
                                {{ item.assignedUserCount }}
                                <span v-if="item.waitingListCount" class="opacity-40"> +{{ item.waitingListCount }} </span>
                            </p>
                            <p class="pl-4 text-sm">
                                {{ $t('views.events.list.table.team-count', { count: item.assignedUserCount }) }}
                            </p>
                        </template>
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
                            <span>{{ $t('views.events.list.action.link-event-details') }}</span>
                        </RouterLink>
                    </li>
                    <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntry(item)">
                        <i class="fa-solid fa-calendar-alt" />
                        <span>{{ $t('views.events.list.action.create-calendar-entry') }}</span>
                    </li>
                    <template v-if="!item.signedInUserRegistration">
                        <li
                            class="permission-write-own-registrations context-menu-item"
                            :class="{ disabled: !item.canSignedInUserJoin }"
                            @click="joinEvents([item])"
                        >
                            <i class="fa-solid fa-user-plus" />
                            <span>{{ $t('views.events.list.action.signup') }}</span>
                        </li>
                    </template>
                    <li
                        v-if="item.isSignedInUserAssigned"
                        class="permission-write-own-registrations context-menu-item text-error"
                        :class="{ disabled: item.isPastEvent }"
                        @click="leaveEvents([item])"
                    >
                        <i class="fa-solid fa-ban" />
                        <span>{{ $t('views.events.list.action.cancel') }}</span>
                    </li>
                    <li
                        v-else-if="item.signedInUserRegistration"
                        class="permission-write-own-registrations context-menu-item"
                        :class="{ disabled: item.isPastEvent }"
                        @click="leaveEvents([item])"
                    >
                        <i class="fa-solid fa-user-minus" />
                        <span>{{ $t('views.events.list.action.leave-waitinglist') }}</span>
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
                        <span>{{ $t('views.events.list.action.signup') }}</span>
                    </button>
                </div>
            </template>
            <template #menu>
                <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntries(selectedEvents)">
                    <i class="fa-solid fa-calendar-alt" />
                    <span>{{ $t('views.events.list.action.create-calendar-entry') }}</span>
                </li>
                <li
                    v-if="hasAnySelectedEventWhichSignedInUserCanJoin"
                    class="permission-write-own-registrations context-menu-item"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="joinEvents(selectedEvents)"
                >
                    <i class="fa-solid fa-user-plus" />
                    <span>{{ $t('views.events.list.action.signup') }}</span>
                </li>
                <li
                    v-if="hasAnySelectedEventWithSignedInUserOnWaitingList"
                    class="permission-write-own-registrations context-menu-item"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="leaveEventsWaitingListOnly(selectedEvents)"
                >
                    <i class="fa-solid fa-user-minus" />
                    <span>{{ $t('views.events.list.action.leave-waitinglist') }}</span>
                </li>
                <li
                    v-if="hasAnySelectedEventWithSignedInUserInTeam"
                    class="permission-write-own-registrations context-menu-item text-error"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="leaveEvents(selectedEvents)"
                >
                    <i class="fa-solid fa-ban" />
                    <span>{{ $t('views.events.list.action.cancel') }}</span>
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
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import type { Event, EventType, InputSelectOption, Registration, SignedInUser } from '@/domain';
import { EventSignupType } from '@/domain';
import { EventState } from '@/domain';
import type { ConfirmationDialog, Sheet } from '@/ui/components/common';
import { VConfirmationDialog, VInfo, VMultiSelectActions, VTable, VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import { FilterMultiselect, FilterToggle } from '@/ui/components/filters';
import RegistrationDetailsSheet from '@/ui/components/sheets/RegistrationDetailsSheet.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQuery } from '@/ui/composables/QueryState.ts';
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

const { t } = useI18n();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const router = useRouter();
const positions = usePositions();
const eventTypes = useEventTypes();

const filter = useQuery<string>('filter', '').parameter;
const filterAssigned = useQuery<boolean>('assigned', false).parameter;
const filterWaitingList = useQuery<boolean>('waitinglist', false).parameter;
const filterFreeSlots = useQuery<boolean>('has-free-slots', false).parameter;
const filterEventType = useQuery<EventType[]>('types', []).parameter;

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const events = ref<EventTableViewItem[] | null>(null);
const tab = ref<string>('future');

const confirmationDialog = ref<ConfirmationDialog | null>(null);
const createRegistrationSheet = ref<Sheet<
    {
        registration?: Registration;
        event: Event | Event[];
    },
    Registration | undefined
> | null>(null);

const hasAnySelectedEventInFuture = computed<boolean>(() => {
    const now = new Date().getTime();
    return selectedEvents.value?.find((it) => it.start.getTime() > now) !== undefined;
});

const hasAnySelectedEventWhichSignedInUserCanJoin = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => !it.signedInUserRegistration) !== undefined;
});

const hasAnySelectedEventWithSignedInUserOnWaitingList = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserRegistration && !it.isSignedInUserAssigned) !== undefined;
});

const hasAnySelectedEventWithSignedInUserInTeam = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserRegistration && it.isSignedInUserAssigned) !== undefined;
});

const filteredEvents = computed<EventTableViewItem[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value
        ?.filter((it) => eventService.doesEventMatchFilter(it, f))
        .filter((it) => filterEventType.value.length === 0 || filterEventType.value.includes(it.type))
        .filter((it) => {
            if (filterAssigned.value || filterWaitingList.value || filterFreeSlots.value) {
                let state = 0;
                if (it.isSignedInUserAssigned) {
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

const tabs = computed<InputSelectOption[]>(() => {
    const currentYear = new Date().getFullYear();
    return [
        { value: 'future', label: t('views.events.list.tab.future') },
        { value: String(currentYear + 1), label: String(currentYear + 1) },
        { value: String(currentYear), label: String(currentYear) },
        { value: String(currentYear - 1), label: String(currentYear - 1) },
    ];
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
    if (tab.value === tabs.value[0].value) {
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
    if (event.state === EventState.Canceled) {
        return {
            name: t('generic.event-state.canceled'),
            icon: 'fa-ban',
            color: 'bg-red-container text-onred-container',
        };
    }
    if (event.isSignedInUserAssigned) {
        return {
            name: t('views.events.list.state.assigned'),
            icon: 'fa-check-circle',
            color: 'bg-green-container text-ongreen-container',
        };
    }
    if (event.signedInUserRegistration) {
        return {
            name: t('views.events.list.state.waitinglist'),
            icon: 'fa-hourglass-half',
            color: 'bg-surface-container-highest text-onsurface',
        };
    }
    if (event.state === EventState.Draft) {
        return {
            name: t('generic.event-state.draft'),
            icon: 'fa-compass-drafting',
            color: 'bg-surface-container-highest text-onsurface',
        };
    }
    if (event.state === EventState.OpenForSignup) {
        return {
            name: t('generic.event-state.open-for-signup'),
            icon: 'fa-people-group',
            color: 'bg-blue-container text-onblue-container',
        };
    }
    if (event.hasOpenImportantSlots) {
        return {
            name: t('generic.event-state.crew-wanted'),
            icon: 'fa-info-circle',
            color: 'bg-yellow-container text-onyellow-container',
        };
    }
    if (event.hasOpenSlots) {
        return {
            name: t('generic.event-state.open-slots'),
            icon: 'fa-info-circle',
            color: 'bg-blue-container text-onblue-container',
        };
    }
    return {
        name: t('generic.event-state.full'),
        icon: 'fa-info-circle',
        color: 'bg-surface-container-highest text-onsurface',
    };
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
