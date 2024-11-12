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
                    <VSearchButton v-model="filter" placeholder="Reisen filtern" />
                    <button
                        v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)"
                        class="permission-write-events btn-ghost"
                        @click="importEvents()"
                    >
                        <i class="fa-solid fa-upload"></i>
                        <span class="">Importieren</span>
                    </button>
                    <div class="permission-write-events hidden 2xl:block">
                        <button class="btn-primary ml-2" @click="createEvent()">
                            <i class="fa-solid fa-calendar-plus"></i>
                            <span class="">Hinzufügen</span>
                        </button>
                    </div>
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
                @click="editEvent($event)"
            >
                <template #row="{ item }">
                    <!-- name -->
                    <td class="w-1/2 whitespace-nowrap font-semibold" style="max-width: min(65vw, 20rem)">
                        <p
                            class="mb-1 truncate"
                            :class="{ 'text-error line-through': item.state === EventState.Canceled }"
                        >
                            <span v-if="item.state === EventState.Draft" class="opacity-50">Entwurf: </span>
                            <span v-else-if="item.state === EventState.Canceled" class="">Abgesagt: </span>
                            {{ item.name }}
                        </p>
                        <p class="hidden truncate text-sm font-light md:block">
                            <template v-if="item.locations.length === 0">keine Reiseroute angegeben</template>
                            <template v-else>{{ item.locations.map((it) => it.name).join(' - ') }}</template>
                        </p>
                        <p class="truncate text-sm font-light md:hidden">
                            {{ formatDateRange(item.start, item.end) }}
                        </p>
                    </td>
                    <!-- status -->
                    <td class="w-1/6">
                        <div class="flex items-center justify-end">
                            <div class="status-panel" :class="item.stateDetails.color">
                                <i class="fa-solid w-4" :class="item.stateDetails.icon"></i>
                                <span class="whitespace-nowrap font-semibold">{{ item.stateDetails.name }}</span>
                            </div>
                        </div>
                    </td>
                    <!-- crew -->
                    <td class="w-1/6 whitespace-nowrap text-center">
                        <p class="mb-1 font-semibold">
                            {{ item.assignedUserCount }}
                            <span v-if="item.waitingListCount" class="opacity-40"> +{{ item.waitingListCount }} </span>
                        </p>
                        <p class="text-sm">Crew</p>
                    </td>
                    <!-- date -->
                    <td class="hidden w-2/6 whitespace-nowrap md:table-cell">
                        <p class="mb-1 font-semibold lg:hidden">{{ $d(item.start, DateTimeFormat.DDD_DD_MM) }}</p>
                        <p class="mb-1 hidden font-semibold lg:block">{{ formatDateRange(item.start, item.end) }}</p>
                        <p class="text-sm">{{ item.duration }} Tage</p>
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

                        <td class="">
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
                    <li class="permission-write-events">
                        <RouterLink
                            :to="{
                                name: Routes.EventEdit,
                                params: { year: item.start.getFullYear(), key: item.key },
                            }"
                            class="context-menu-item"
                        >
                            <i class="fa-solid fa-edit" />
                            <span>Reise bearbeiten</span>
                        </RouterLink>
                    </li>
                    <li
                        class="permission-read-user-details context-menu-item"
                        @click="eventUseCase.downloadImoList(item)"
                    >
                        <i class="fa-solid fa-clipboard-user" />
                        <span>IMO Liste generieren</span>
                    </li>
                    <li
                        class="permission-read-user-details context-menu-item"
                        @click="eventUseCase.downloadConsumptionList(item)"
                    >
                        <i class="fa-solid fa-beer-mug-empty" />
                        <span>Verzehrliste generieren</span>
                    </li>
                    <li class="permission-write-registrations context-menu-item" @click="addRegistration([item])">
                        <i class="fa-solid fa-user-plus" />
                        <span>Anmeldung hinzufügen</span>
                    </li>
                    <li
                        v-if="item.state === EventState.Draft"
                        class="permission-write-events context-menu-item"
                        @click="openEventsForSignup([item])"
                    >
                        <i class="fa-solid fa-unlock-alt" />
                        <span>Anmeldungen freischalten</span>
                    </li>
                    <li
                        v-else-if="item.state === EventState.OpenForSignup"
                        class="permission-write-events context-menu-item"
                        @click="publishCrewPlanning([item])"
                    >
                        <i class="fa-solid fa-earth-europe" />
                        <span>Crewplanung veröffentlichen</span>
                    </li>
                    <li class="permission-write-events context-menu-item disabled">
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
                        class="permission-write-events context-menu-item text-error"
                        @click="deleteEvent(item)"
                    >
                        <i class="fa-solid fa-trash-alt" />
                        <span>Reise löschen</span>
                    </li>
                    <li v-else class="permission-write-events context-menu-item text-error" @click="cancelEvent(item)">
                        <i class="fa-solid fa-ban" />
                        <span>Reise absagen</span>
                    </li>
                </template>
            </VTable>
        </div>

        <EventCreateDlg ref="createEventDialog" />
        <EventCancelDlg ref="cancelEventDialog" />
        <ImportEventsDlg ref="importEventsDialog" />
        <VConfirmationDialog ref="confirmationDialog" />
        <EventBatchEditDlg ref="eventBatchEditDialog" />
        <CreateRegistrationDlg ref="createRegistrationDialog" submit-text="Speichern" />

        <div class="flex-1"></div>

        <div v-if="selectedEvents && selectedEvents.length > 0" class="sticky bottom-0 z-20">
            <div
                class="h-full border-t border-outline-variant bg-surface px-2 md:px-12 xl:rounded-bl-3xl xl:pb-4 xl:pl-16 xl:pr-20"
            >
                <div class="flex h-full items-stretch gap-2 whitespace-nowrap py-2">
                    <button class="btn-ghost" @click="selectNone()">
                        <i class="fa-solid fa-xmark" />
                    </button>
                    <span class="self-center font-bold">{{ selectedEvents.length }} ausgewählt</span>
                    <div class="flex-grow"></div>
                    <div class="hidden sm:block">
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
                            <span class="truncate">Crewplanung veröffentlichen</span>
                        </button>
                        <button v-else class="permission-write-events btn-ghost" @click="editBatch(selectedEvents)">
                            <i class="fa-solid fa-edit"></i>
                            <span class="truncate">Ausgewählte bearbeiten</span>
                        </button>
                    </div>
                    <ContextMenuButton class="btn-ghost">
                        <template #default>
                            <li class="context-menu-item" @click="selectAll">
                                <i class="fa-solid fa-list-check" />
                                <span>Alle auswählen</span>
                            </li>
                            <li
                                class="permission-write-registrations context-menu-item"
                                @click="addRegistration(selectedEvents)"
                            >
                                <i class="fa-solid fa-user-plus" />
                                <span>Anmeldung hinzufügen</span>
                            </li>
                            <li class="permission-write-events context-menu-item" @click="editBatch(selectedEvents)">
                                <i class="fa-solid fa-edit" />
                                <span>Ausgewählte bearbeiten</span>
                            </li>
                            <li
                                v-if="showBatchOpenEventForSignup"
                                class="permission-write-events context-menu-item"
                                @click="openEventsForSignup(selectedEvents)"
                            >
                                <i class="fa-solid fa-unlock-alt" />
                                <span>Anmeldungen freischalten</span>
                            </li>
                            <li
                                v-if="showBatchPublishPlannedCrew"
                                class="permission-write-events context-menu-item"
                                @click="publishCrewPlanning(selectedEvents)"
                            >
                                <i class="fa-solid fa-earth-europe" />
                                <span>Crewplanung veröffentlichen</span>
                            </li>
                            <li class="permission-write-events context-menu-item disabled">
                                <i class="fa-solid fa-users" />
                                <span>Weitere Crew anfragen*</span>
                            </li>
                            <li class="permission-write-events context-menu-item disabled">
                                <i class="fa-solid fa-envelope" />
                                <span>Crew kontaktieren*</span>
                            </li>
                            <li class="permission-write-events context-menu-item disabled text-error">
                                <i class="fa-solid fa-ban" />
                                <span>Reisen absagen*</span>
                            </li>
                        </template>
                    </ContextMenuButton>
                </div>
            </div>
        </div>
        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-else
            class="permission-write-events pointer-events-none sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" @click="createEvent()">
                <i class="fa-solid fa-calendar-plus"></i>
                <span>Reise erstellen</span>
            </button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { filterUndefined } from '@/common';
import { DateTimeFormat } from '@/common/date';
import type { Event, Registration } from '@/domain';
import { EventState, Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { ContextMenuButton, VConfirmationDialog, VTable, VTabs } from '@/ui/components/common';
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
import { Routes } from '@/ui/views/Routes.ts';
import EventBatchEditDlg from '@/ui/views/events/list-admin/EventBatchEditDlg.vue';
import ImportEventsDlg from '@/ui/views/events/list-admin/ImportEventsDlg.vue';

interface StateDetails {
    name: string;
    color: string;
    icon: string;
    iconMobile?: string;
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

const eventAdminUseCase = useEventAdministrationUseCase();
const userAdminUseCase = useUserAdministrationUseCase();
const usersUseCase = useUsersUseCase();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const route = useRoute();
const router = useRouter();
const signedInUser = authUseCase.getSignedInUser();

const events = ref<EventTableViewItem[] | null>(null);
const tab = ref<string>('Zukünftige');
const filter = ref<string>('');

const createEventDialog = ref<Dialog<Event> | null>(null);
const cancelEventDialog = ref<Dialog<Event, string> | null>(null);
const importEventsDialog = ref<Dialog<Event> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);
const eventBatchEditDialog = ref<Dialog<Event[], boolean> | null>(null);
const createRegistrationDialog = ref<Dialog<Event[], Registration | undefined> | null>(null);

const filteredEvents = computed<EventTableViewItem[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value?.filter((it) => it.name.toLowerCase().includes(f));
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

const tabs = computed<string[]>(() => {
    const currentYear = new Date().getFullYear();
    return [
        'Zukünftige',
        String(currentYear + 1),
        String(currentYear),
        String(currentYear - 1),
        String(currentYear - 2),
    ];
});

function init(): void {
    emit('update:title', 'Reisen verwalten');
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
    switch (event.state) {
        case EventState.Draft:
            return {
                name: 'Entwurf',
                icon: 'fa-compass-drafting',
                color: 'bg-surface-container-highest text-onsurface',
            };
        case EventState.OpenForSignup:
            return { name: 'Crew Anmeldung', icon: 'fa-unlock', color: 'bg-surface-container-highest text-onsurface' };
        case EventState.Canceled:
            return { name: 'Abgesagt', icon: 'fa-ban', color: 'bg-red-container text-onred-container' };
    }
    if (event.hasOpenRequiredSlots) {
        return { name: 'Fehlende Crew', icon: 'fa-warning', color: 'bg-yellow-container text-onyellow-container' };
    }
    if (event.hasOpenSlots) {
        return {
            name: 'Freie Plätze',
            icon: 'fa-info-circle',
            iconMobile: 'fa-info',
            color: 'bg-blue-container text-onblue-container',
        };
    }
    return {
        name: 'Voll belegt',
        icon: 'fa-check-circle',
        iconMobile: 'fa-check',
        color: 'bg-green-container text-ongreen-container',
    };
}

function hasOpenSlots(event: Event): boolean {
    return event.slots.filter((slt) => !slt.assignedRegistrationKey).length > 0;
}

async function editEvent(evt: EventTableViewItem): Promise<void> {
    if (signedInUser.permissions.includes(Permission.WRITE_EVENTS)) {
        await router.push({
            name: Routes.EventEdit,
            params: { year: evt.start.getFullYear(), key: evt.key },
        });
    } else {
        await router.push({
            name: Routes.EventDetails,
            params: { year: evt.start.getFullYear(), key: evt.key },
        });
    }
}

async function createEvent(): Promise<void> {
    const event = await createEventDialog.value?.open().catch();
    if (event) {
        await eventAdminUseCase.createEvent(event);
    }
}

async function deleteEvent(evt: Event): Promise<void> {
    const confirmed = await confirmationDialog.value?.open({
        title: 'Reise löschen',
        message: `Bist du sicher, das du die Reise ${evt.name} löschen möchtest? Diese Aktion kann später nicht
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

async function importEvents(): Promise<void> {
    await importEventsDialog.value?.open().catch();
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
            title: 'Crewplanung veröffentlichen',
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
