<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" placeholder="Reisen durchsuchen" />
            </div>
        </teleport>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <div class="permission-create-events hidden 2xl:block">
                        <button class="btn-primary ml-2" name="create" @click="createEvent()">
                            <i class="fa-solid fa-calendar-plus"></i>
                            <span>Hinzufügen</span>
                        </button>
                    </div>
                    <div v-if="!Number.isNaN(parseInt(tab))" class="permission-read-events hidden lg:block">
                        <button class="btn-ghost ml-2" name="export" @click="eventUseCase.exportEvents(parseInt(tab))">
                            <i class="fa-solid fa-download"></i>
                            <span>Export</span>
                        </button>
                    </div>
                    <VSearchButton v-model="filter" placeholder="Einträge filtern" />
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
                    <span v-if="filterEventType.length === 0">Alle Veranstaltungen</span>
                    <span v-else class="block max-w-64 truncate">
                        {{ filterEventType.map(eventTypes.getName).join(', ') }}
                    </span>
                </template>
                <template #default>
                    <ul>
                        <li v-if="filterEventType.length === 0" class="context-menu-item">
                            <i class="fa-solid fa-check"></i>
                            <span>Alle Veranstaltungen</span>
                        </li>
                        <li v-else class="context-menu-item" @click="filterEventType = []">
                            <i class="w-4"></i>
                            <span>Alle Veranstaltungen</span>
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
            <ContextMenuButton
                anchor-align-x="left"
                dropdown-position-x="right"
                class="btn-tag"
                :class="{ active: filterEventStates.length > 0 }"
            >
                <template #icon>
                    <span v-if="filterEventStates.length === 0">Alle Status</span>
                    <span v-else-if="filterEventStates.length > 4"> {{ filterEventStates.length }} Status </span>
                    <span v-else class="block max-w-64 truncate">
                        {{ filterEventStates.map(eventStates.getName).join(', ') }}
                    </span>
                </template>
                <template #default>
                    <ul>
                        <li v-if="filterEventStates.length === 0" class="context-menu-item">
                            <i class="fa-solid fa-check"></i>
                            <span>Alle Status</span>
                        </li>
                        <li v-else class="context-menu-item" @click="filterEventStates = []">
                            <i class="w-4"></i>
                            <span>Alle Status</span>
                        </li>
                        <template v-for="eventStatus in eventStates.options.value" :key="eventStatus.value">
                            <li
                                v-if="filterEventStates.includes(eventStatus.value)"
                                class="context-menu-item"
                                @click="filterEventStates = filterEventStates.filter((it) => it !== eventStatus.value)"
                            >
                                <i class="fa-solid fa-check w-4"></i>
                                <span>{{ eventStatus.label }}</span>
                            </li>
                            <li v-else class="context-menu-item" @click="filterEventStates.push(eventStatus.value)">
                                <i class="w-4"></i>
                                <span>{{ eventStatus.label }}</span>
                            </li>
                        </template>
                    </ul>
                </template>
            </ContextMenuButton>
            <button class="btn-tag" :class="{ active: filterFreeSlots }" @click="filterFreeSlots = !filterFreeSlots">
                <span>Freie Plätze</span>
            </button>
            <button class="btn-tag" :class="{ active: filterWaitinglist }" @click="filterWaitinglist = !filterWaitinglist">
                <span>Warteliste</span>
            </button>
        </div>

        <div class="w-full">
            <VTable
                :items="filteredEvents"
                multiselection
                query
                :page-size="20"
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="editEvent($event.item, $event.event)"
            >
                <template #row="{ item }">
                    <!-- date -->
                    <td class="hidden w-1/6 whitespace-nowrap lg:table-cell">
                        <p class="mb-1 font-semibold 2xl:hidden">{{ $d(item.start, DateTimeFormat.DDD_DD_MM) }}</p>
                        <p class="mb-1 hidden font-semibold 2xl:block">{{ formatDateRange(item.start, item.end) }}</p>
                        <p class="text-sm">{{ item.days }} Tage</p>
                    </td>
                    <!-- name -->
                    <td class="w-2/3 max-w-[80vw] whitespace-nowrap font-semibold" style="max-width: min(65vw, 20rem)">
                        <p class="mb-1 truncate" :class="{ 'text-error line-through': item.state === EventState.Canceled }">
                            <span v-if="item.state === EventState.Draft" class="opacity-50">Entwurf: </span>
                            <span v-else-if="item.state === EventState.Canceled">Abgesagt: </span>
                            {{ item.name }}
                        </p>

                        <p class="hidden truncate text-sm font-light lg:block">
                            <template v-if="item.description">{{ item.description }}</template>
                            <template v-else-if="item.locations.length === 0">keine Reiseroute angegeben</template>
                            <template v-else>{{ item.locations.map((it) => it.name).join(' - ') }}</template>
                        </p>
                        <p class="truncate text-sm font-light lg:hidden">
                            {{ formatDateRange(item.start, item.end) }} | {{ item.days }} Tage
                        </p>
                        <div class="flex w-full items-center gap-px pt-2">
                            <template v-for="(position, index) in item.assignedPositions" :key="`${position.key}-${index}`">
                                <div :data-index="index" class="w-1 flex-grow">
                                    <VTooltip :delay="50">
                                        <template #tooltip>
                                            <div class="position text-sm shadow-xl" :style="{ backgroundColor: position.color }">
                                                {{ position.name }}
                                            </div>
                                        </template>
                                        <template #default>
                                            <div class="h-2 rounded" :style="{ backgroundColor: position.color }" />
                                        </template>
                                    </VTooltip>
                                </div>
                            </template>
                        </div>
                    </td>
                    <!-- status -->
                    <td class="w-1/6">
                        <EventStateBadge :event="item" />
                    </td>
                    <!-- crew -->
                    <td class="w-1/6 min-w-24 whitespace-nowrap">
                        <p class="mb-1 pl-4 font-semibold">
                            {{ item.assignedUserCount }}
                            <span v-if="item.waitingListCount" class="opacity-40"> +{{ item.waitingListCount }} </span>
                        </p>
                        <p class="pl-4 text-sm">Crew</p>
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
                    <li class="permission-write-event-details">
                        <RouterLink
                            :to="{
                                name: Routes.EventEdit,
                                params: { year: item.start.getFullYear(), key: item.key },
                            }"
                            class="context-menu-item"
                        >
                            <i class="fa-solid fa-drafting-compass" />
                            <span>Reise bearbeiten</span>
                        </RouterLink>
                    </li>
                    <li class="permission-read-user-details context-menu-item" @click="eventUseCase.downloadImoList(item)">
                        <i class="fa-solid fa-clipboard-user" />
                        <span>IMO Liste generieren</span>
                    </li>
                    <li class="permission-read-users context-menu-item" @click="eventUseCase.downloadConsumptionList(item)">
                        <i class="fa-solid fa-beer-mug-empty" />
                        <span>Verzehrliste generieren</span>
                    </li>
                    <li class="permission-read-user-details context-menu-item" @click="eventUseCase.downloadCaptainList(item)">
                        <i class="fa-solid fa-file-medical" />
                        <span>Kapitänsliste generieren</span>
                    </li>
                    <li class="permission-write-registrations context-menu-item" @click="addRegistration([item])">
                        <i class="fa-solid fa-user-plus" />
                        <span>Anmeldung hinzufügen</span>
                    </li>
                    <li
                        v-if="item.state === EventState.Draft"
                        class="permission-write-event-details context-menu-item"
                        @click="openEventsForSignup([item])"
                    >
                        <i class="fa-solid fa-people-group" />
                        <span>Anmeldungen freischalten</span>
                    </li>
                    <li
                        v-else-if="item.state === EventState.OpenForSignup"
                        class="permission-write-event-details context-menu-item"
                        @click="publishCrewPlanning([item])"
                    >
                        <i class="fa-solid fa-earth-europe" />
                        <span>Crew veröffentlichen</span>
                    </li>
                    <li class="permission-read-user-details context-menu-item disabled">
                        <i class="fa-solid fa-users" />
                        <span>Weitere Crew anfragen*</span>
                    </li>
                    <li
                        class="permission-read-user-details context-menu-item"
                        :class="{ disabled: item.assignedUserCount === 0 }"
                        @click="contactCrew([item])"
                    >
                        <i class="fa-solid fa-envelope" />
                        <span>Crew kontaktieren ({{ item.assignedUserCount }} Pers.)</span>
                    </li>
                    <li
                        v-if="item.state === EventState.Canceled"
                        class="permission-delete-events context-menu-item text-error"
                        @click="deleteEvent(item)"
                    >
                        <i class="fa-solid fa-trash-alt" />
                        <span>Reise löschen</span>
                    </li>
                    <li v-else class="permission-delete-events context-menu-item text-error" @click="cancelEvent(item)">
                        <i class="fa-solid fa-ban" />
                        <span>Reise absagen</span>
                    </li>
                </template>
            </VTable>
        </div>

        <EventCreateDlg ref="createEventDialog" />
        <EventCancelDlg ref="cancelEventDialog" />
        <VConfirmationDialog ref="confirmationDialog" />
        <EventBatchEditDlg ref="eventBatchEditDialog" />
        <CreateRegistrationDlg ref="createRegistrationDialog" submit-text="Speichern" />

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
                        v-if="showBatchOpenEventForSignup"
                        class="permission-write-events btn-ghost"
                        @click="openEventsForSignup(selectedEvents)"
                    >
                        <i class="fa-solid fa-lock-open"></i>
                        <span class="truncate">Anmeldungen freischalten</span>
                    </button>
                    <button
                        v-else-if="showBatchPublishPlannedCrew"
                        class="permission-write-events btn-ghost"
                        @click="publishCrewPlanning(selectedEvents)"
                    >
                        <i class="fa-solid fa-earth-europe"></i>
                        <span class="truncate">Crew veröffentlichen</span>
                    </button>
                    <button v-else class="permission-write-events btn-ghost" @click="editBatch(selectedEvents)">
                        <i class="fa-solid fa-edit"></i>
                        <span class="truncate">Ausgewählte bearbeiten</span>
                    </button>
                </div>
            </template>
            <template #menu>
                <li class="permission-write-registrations context-menu-item" @click="addRegistration(selectedEvents)">
                    <i class="fa-solid fa-user-plus" />
                    <span>Anmeldung hinzufügen</span>
                </li>
                <li class="permission-write-event-details context-menu-item" @click="editBatch(selectedEvents)">
                    <i class="fa-solid fa-edit" />
                    <span>Ausgewählte bearbeiten</span>
                </li>
                <li
                    v-if="showBatchOpenEventForSignup"
                    class="permission-write-event-details context-menu-item"
                    @click="openEventsForSignup(selectedEvents)"
                >
                    <i class="fa-solid fa-people-group" />
                    <span>Anmeldungen freischalten</span>
                </li>
                <li
                    v-if="showBatchPublishPlannedCrew"
                    class="permission-write-event-details context-menu-item"
                    @click="publishCrewPlanning(selectedEvents)"
                >
                    <i class="fa-solid fa-earth-europe" />
                    <span>Crew veröffentlichen</span>
                </li>
                <li class="permission-read-user-details permission-write-events context-menu-item disabled">
                    <i class="fa-solid fa-users" />
                    <span>Weitere Crew anfragen*</span>
                </li>
                <li class="permission-read-user-details context-menu-item disabled">
                    <i class="fa-solid fa-envelope" />
                    <span>Crew kontaktieren*</span>
                </li>
                <li class="permission-delete-events context-menu-item disabled text-error">
                    <i class="fa-solid fa-ban" />
                    <span>Reisen absagen*</span>
                </li>
            </template>
        </VMultiSelectActions>
        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-else
            class="permission-create-events pointer-events-none sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" @click="createEvent()">
                <i class="fa-solid fa-calendar-plus"></i>
                <span>Reise erstellen</span>
            </button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { filterUndefined } from '@/common';
import { DateTimeFormat } from '@/common/date';
import type { Event, EventType, InputSelectOption, Position, Registration } from '@/domain';
import { EventState, Permission, SlotCriticality } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { ContextMenuButton, VConfirmationDialog, VMultiSelectActions, VTable, VTabs, VTooltip } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import CreateRegistrationDlg from '@/ui/components/events/CreateRegistrationDlg.vue';
import EventCancelDlg from '@/ui/components/events/EventCancelDlg.vue';
import EventCreateDlg from '@/ui/components/events/EventCreateDlg.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import {
    useAuthUseCase,
    useEventAdministrationUseCase,
    useEventUseCase,
    useUserAdministrationUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application.ts';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { useEventStates } from '@/ui/composables/EventStates.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQuery } from '@/ui/composables/QueryState.ts';
import { restoreScrollPosition } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes.ts';
import EventBatchEditDlg from '@/ui/views/events/list-admin/EventBatchEditDlg.vue';
import EventStateBadge from '@/ui/views/events/list-admin/EventStateBadge.vue';

interface EventTableViewItem extends Event {
    selected: boolean;
    isPastEvent: boolean;
    waitingListCount: number;
    hasOpenSlots: boolean;
    hasOpenRequiredSlots: boolean;
    assignedPositions: Position[];
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const eventAdminUseCase = useEventAdministrationUseCase();
const userAdminUseCase = useUserAdministrationUseCase();
const usersUseCase = useUsersUseCase();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const positions = usePositions();
const router = useRouter();
const signedInUser = authUseCase.getSignedInUser();
const eventTypes = useEventTypes();
const eventStates = useEventStates();

const filter = useQuery<string>('filter', '').parameter;
const filterWaitinglist = useQuery<boolean>('has-waitinglist', false).parameter;
const filterFreeSlots = useQuery<boolean>('has-free-slots', false).parameter;
const filterEventStates = useQuery<EventState[]>('states', []).parameter;
const filterEventType = useQuery<EventType[]>('types', []).parameter;

const events = ref<EventTableViewItem[] | null>(null);
const tab = ref<string>('future');

const createEventDialog = ref<Dialog<Event> | null>(null);
const cancelEventDialog = ref<Dialog<Event, string> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);
const eventBatchEditDialog = ref<Dialog<Event[], boolean> | null>(null);
const createRegistrationDialog = ref<Dialog<Event[], Registration | undefined> | null>(null);

const filteredEvents = computed<EventTableViewItem[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value
        ?.filter((it) => eventService.doesEventMatchFilter(it, f))
        .filter((it) => filterEventType.value.length === 0 || filterEventType.value.includes(it.type))
        .filter((it) => !filterFreeSlots.value || it.hasOpenSlots)
        .filter((it) => filterEventStates.value.length === 0 || filterEventStates.value.includes(it.state))
        .filter((it) => !filterWaitinglist.value || it.waitingListCount > 0);
});

const selectedEvents = computed<EventTableViewItem[] | undefined>(() => {
    return filteredEvents.value?.filter((it) => it.selected);
});

const showBatchOpenEventForSignup = computed<boolean>(() => {
    return (
        signedInUser.permissions.includes(Permission.WRITE_EVENTS) &&
        selectedEvents.value != undefined &&
        selectedEvents.value.filter((it) => it.state === EventState.Draft).length > 0
    );
});

const showBatchPublishPlannedCrew = computed<boolean>(() => {
    return (
        signedInUser.permissions.includes(Permission.WRITE_EVENTS) &&
        selectedEvents.value != undefined &&
        selectedEvents.value.filter((it) => it.state === EventState.OpenForSignup).length > 0
    );
});

const tabs = computed<InputSelectOption[]>(() => {
    const currentYear = new Date().getFullYear();
    return [
        { value: 'future', label: t('views.event-admin-list.tab.future') },
        { value: String(currentYear + 1), label: String(currentYear + 1) },
        { value: String(currentYear), label: String(currentYear) },
        { value: String(currentYear - 1), label: String(currentYear - 1) },
    ];
});

async function init(): Promise<void> {
    emit('update:tab-title', 'Reisen verwalten');
    watch(tab, () => fetchEvents());
    await positions.loading;
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
        const openSlots = eventService.getOpenSlots(evt);
        const openRequiredSlots = openSlots.filter((slot) => slot.criticality !== SlotCriticality.Optional);
        const openOptionalSlots = openSlots.filter((slot) => slot.criticality === SlotCriticality.Optional);

        const tableItem: EventTableViewItem = {
            ...evt,
            selected: false,
            isPastEvent: evt.start.getTime() < new Date().getTime(),
            waitingListCount: evt.registrations.length - evt.assignedUserCount,
            hasOpenSlots: openOptionalSlots.length > 0,
            hasOpenRequiredSlots: openRequiredSlots.length > 0,
            assignedPositions: eventService
                .getAssignedRegistrations(evt)
                .map((reg) => positions.get(reg.positionKey))
                .filter(filterUndefined)
                .sort((a, b) => b.prio - a.prio),
        };
        return tableItem;
    });
}

async function editEvent(item: EventTableViewItem, evt: MouseEvent): Promise<void> {
    let to: RouteLocationRaw = {
        name: Routes.EventDetails,
        params: { year: item.start.getFullYear(), key: item.key },
    };
    if (signedInUser.permissions.includes(Permission.WRITE_EVENTS)) {
        to = {
            name: Routes.EventEdit,
            params: { year: item.start.getFullYear(), key: item.key },
        };
    }
    if (evt.ctrlKey || evt.metaKey) {
        window.open(router.resolve(to).href, '_blank');
    } else {
        await router.push(to);
    }
}

async function createEvent(): Promise<void> {
    const event = await createEventDialog.value?.open().catch();
    if (event) {
        await eventAdminUseCase.createEvent(event);
        await fetchEvents();
    }
}

async function deleteEvent(evt: Event): Promise<void> {
    const confirmed = await confirmationDialog.value?.open({
        title: 'Reise löschen',
        message: `Bist du sicher, dass du die Reise ${evt.name} löschen möchtest? Diese Aktion kann später nicht
            rückgängig gemacht werden.`,
        submit: 'Unwiederruflich löschen',
        danger: true,
    });
    if (confirmed) {
        await eventAdminUseCase.deleteEvent(evt);
        await fetchEvents();
    }
}

async function cancelEvent(evt: Event): Promise<void> {
    const message = await cancelEventDialog.value?.open(evt);
    if (message !== undefined) {
        await eventAdminUseCase.cancelEvent(evt, message);
        await fetchEvents();
    }
}

async function editBatch(events: Event[]): Promise<void> {
    const changed = await eventBatchEditDialog.value?.open(events);
    if (changed) {
        await fetchEvents();
    }
}

async function contactCrew(events: Event[]): Promise<void> {
    const userKeys = events
        .flatMap((event) => eventService.getAssignedRegistrations(event))
        .map((it) => it.userKey)
        .filter(filterUndefined);
    const users = await usersUseCase.getUsers(userKeys);
    await userAdminUseCase.contactUsers(users);
}

async function addRegistration(events: Event[]): Promise<void> {
    const result = await createRegistrationDialog.value?.open(events);
    if (result) {
        await eventAdminUseCase.addRegistrations(events, result);
        await fetchEvents();
    }
}

async function openEventsForSignup(events: Event[]): Promise<void> {
    // filter out those events that already have the desired state
    let eventsToEdit = events.filter((it) => it.state !== EventState.OpenForSignup);
    if (eventsToEdit.find((event) => event.state !== EventState.Draft)) {
        const confirmed = await confirmationDialog.value?.open({
            title: 'Anmeldungen freigeben',
            message: `Unter den ausgewählten Reisen ist mindestens eine, die nicht im Status 'Entwurf' ist. Möchtest du
                trotzdem alle ausgewählten Reisen in den Status 'Crew Anmeldung' ändern?`,
            cancel: 'Nein, nur die Entwürfe',
            submit: 'Ja, alle ändern',
        });
        if (confirmed === undefined) {
            return;
        }
        if (confirmed === false) {
            eventsToEdit = events.filter((it) => it.state === EventState.Draft);
        }
    }
    const keys = eventsToEdit.map((it) => it.key);
    await eventAdminUseCase.updateEvents(keys, { state: EventState.OpenForSignup });
    await fetchEvents();
}

async function publishCrewPlanning(events: Event[]): Promise<void> {
    // filter out those events that already have the desired state
    let eventsToEdit = events.filter((it) => it.state !== EventState.Planned);
    if (eventsToEdit.find((event) => event.state !== EventState.OpenForSignup)) {
        const confirmed = await confirmationDialog.value?.open({
            title: 'Crew veröffentlichen',
            message: `Unter den ausgewählten Reisen ist mindestens eine, die nicht im Status 'Crew Anmeldung' ist.
                Möchtest du trotzdem alle ausgewählten Reisen in den Status 'Crewplanung veröffentlicht' ändern?`,
            cancel: 'Nein, nur die in Anmeldung',
            submit: 'Ja, alle ändern',
        });
        if (confirmed === undefined) {
            return;
        }
        if (confirmed === false) {
            eventsToEdit = events.filter((it) => it.state === EventState.OpenForSignup);
        }
    }
    const keys = eventsToEdit.map((it) => it.key);
    await eventAdminUseCase.updateEvents(keys, { state: EventState.Planned });
    await fetchEvents();
}

init();
</script>
