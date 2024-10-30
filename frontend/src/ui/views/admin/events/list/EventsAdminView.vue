<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" placeholder="Events durchsuchen" />
            </div>
        </teleport>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="flex items-stretch gap-2 pb-2">
                    <VSearchButton v-model="filter" placeholder="Reisen filtern" />
                    <button
                        v-if="user.permissions.includes(Permission.WRITE_EVENTS)"
                        class="btn-ghost"
                        @click="importEvents()"
                    >
                        <i class="fa-solid fa-upload"></i>
                        <span class="text-base">Importieren</span>
                    </button>
                    <div class="hidden 2xl:block">
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
                :page-size="20"
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="editEvent($event)"
                @click-ctrl="selectEvent($event)"
            >
                <template #row="{ item }">
                    <td @click.stop="item.selected = !item.selected">
                        <span v-if="item.selected">
                            <i class="fa-solid fa-check-square text-2xl text-primary-600"></i>
                        </span>
                        <span v-else>
                            <i class="fa-solid fa-square text-2xl text-primary-200"></i>
                        </span>
                        <!--                        <VInputCheckBox v-model="item.selected" class="-ml-2" />-->
                    </td>
                    <td class="w-1/2 whitespace-nowrap font-semibold" style="max-width: min(65vw, 20rem)">
                        <p
                            class="mb-1 truncate"
                            :class="{ 'text-red-700 line-through': item.state === EventState.Canceled }"
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
                    <td class="">
                        <div class="flex items-center justify-end">
                            <div
                                v-if="item.state === EventState.Draft"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-gray-200 py-1 pl-3 pr-4 text-gray-700"
                            >
                                <i class="fa-solid fa-compass-drafting w-4"></i>
                                <span class="whitespace-nowrap font-semibold">Entwurf</span>
                            </div>
                            <div
                                v-else-if="item.state === EventState.Canceled"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-red-200 py-1 pl-3 pr-4 text-red-700"
                            >
                                <i class="fa-solid fa-xmark w-4"></i>
                                <span class="whitespace-nowrap font-semibold">Abgesagt</span>
                            </div>
                            <div
                                v-else-if="item.state === EventState.OpenForSignup"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-gray-200 py-1 pl-3 pr-4 text-gray-700"
                            >
                                <i class="fa-solid fa-unlock w-4"></i>
                                <span class="whitespace-nowrap font-semibold">Anmeldung</span>
                            </div>
                            <div
                                v-else-if="item.hasOpenRequiredSlots"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                            >
                                <i class="fa-solid fa-warning w-4"></i>
                                <span class="whitespace-nowrap font-semibold">Fehlende Crew</span>
                            </div>
                            <div
                                v-else-if="item.hasOpenSlots"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-blue-200 py-1 pl-3 pr-4 text-blue-700"
                            >
                                <i class="fa-solid fa-info-circle w-4"></i>
                                <span class="whitespace-nowrap font-semibold">Freie Plätze</span>
                            </div>
                            <div
                                v-else
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-200 py-1 pl-3 pr-4 text-green-700"
                            >
                                <i class="fa-solid fa-check-circle w-4"></i>
                                <span class="whitespace-nowrap font-semibold">Voll belegt</span>
                            </div>
                        </div>
                    </td>
                    <td class="w-1/6 whitespace-nowrap text-center">
                        <p class="mb-1 font-semibold">
                            {{ item.assignedUserCount }}
                            <span v-if="item.waitingListCount" class="opacity-40"> +{{ item.waitingListCount }} </span>
                        </p>
                        <p class="text-sm">Crew</p>
                    </td>
                    <td class="hidden w-2/6 whitespace-nowrap md:table-cell">
                        <p class="mb-1 font-semibold lg:hidden">{{ $d(item.start, DateTimeFormat.DDD_DD_MM) }}</p>
                        <p class="mb-1 hidden font-semibold lg:block">{{ formatDateRange(item.start, item.end) }}</p>
                        <p class="text-sm">{{ item.duration }} Tage</p>
                    </td>

                    <td class="w-0">
                        <ContextMenuButton class="px-4 py-2">
                            <ul>
                                <li>
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
                                <template v-if="user.permissions.includes(Permission.WRITE_EVENTS)">
                                    <li>
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
                                        v-if="item.state === EventState.Draft"
                                        class="context-menu-item"
                                        @click="openEventsForSignup([item])"
                                    >
                                        <i class="fa-solid fa-unlock-alt" />
                                        <span>Anmeldungen freischalten</span>
                                    </li>
                                    <li
                                        v-else-if="item.state === EventState.OpenForSignup"
                                        class="context-menu-item"
                                        @click="publishCrewPlanning([item])"
                                    >
                                        <i class="fa-solid fa-earth-europe" />
                                        <span>Crewplanung veröffentlichen</span>
                                    </li>
                                </template>
                                <li class="context-menu-item disabled">
                                    <i class="fa-solid fa-users" />
                                    <span>Fehlende Crew anfragen</span>
                                </li>
                                <li class="context-menu-item disabled">
                                    <i class="fa-solid fa-envelope" />
                                    <span>Crew kontaktieren</span>
                                </li>
                                <li
                                    v-if="item.state === EventState.Canceled"
                                    class="context-menu-item text-red-700"
                                    @click="deleteEvent(item)"
                                >
                                    <i class="fa-solid fa-trash-alt" />
                                    <span>Reise löschen</span>
                                </li>
                                <li v-else class="context-menu-item text-red-700" @click="cancelEvent(item)">
                                    <i class="fa-solid fa-ban" />
                                    <span>Reise absagen</span>
                                </li>
                            </ul>
                        </ContextMenuButton>
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
            </VTable>
        </div>

        <EventCreateDlg ref="createEventDialog" />
        <EventCancelDlg ref="cancelEventDialog" />
        <ImportEventsDlg ref="importEventsDialog" />
        <VConfirmationDialog ref="confirmationDialog" />
        <EventBatchEditDlg ref="eventBatchEditDialog" />

        <div class="flex-1"></div>

        <div v-if="selectedEvents && selectedEvents.length > 0" class="sticky bottom-0 z-20">
            <div
                class="h-full border-t border-primary-200 bg-primary-50 px-4 md:px-12 xl:rounded-bl-3xl xl:pb-4 xl:pl-16 xl:pr-20"
            >
                <div class="flex h-full items-stretch gap-2 whitespace-nowrap py-2">
                    <button class="btn-ghost" @click="selectNone()">
                        <i class="fa-solid fa-xmark w-6 text-base" />
                    </button>
                    <span class="self-center text-base font-bold">{{ selectedEvents.length }} ausgewählt</span>
                    <div class="flex-grow"></div>
                    <div class="hidden sm:block">
                        <button
                            v-if="showBatchOpenEventForSignup"
                            class="btn-ghost"
                            @click="openEventsForSignup(selectedEvents)"
                        >
                            <i class="fa-solid fa-lock-open"></i>
                            <span class="truncate text-base">Anmeldungen freischalten</span>
                        </button>
                        <button
                            v-else-if="showBatchPublishPlannedCrew"
                            class="btn-ghost"
                            @click="publishCrewPlanning(selectedEvents)"
                        >
                            <i class="fa-solid fa-earth-europe"></i>
                            <span class="truncate text-base">Crewplanung veröffentlichen</span>
                        </button>
                        <button v-else class="btn-ghost" @click="editBatch(selectedEvents)">
                            <i class="fa-solid fa-edit"></i>
                            <span class="truncate text-base">Ausgewählte bearbeiten</span>
                        </button>
                    </div>
                    <ContextMenuButton class="btn-ghost">
                        <template #default>
                            <li class="context-menu-item" @click="selectAll">
                                <i class="fa-solid fa-list-check" />
                                <span>Alle auswählen</span>
                            </li>
                            <template v-if="user.permissions.includes(Permission.WRITE_EVENTS)">
                                <li class="context-menu-item" @click="editBatch(selectedEvents)">
                                    <i class="fa-solid fa-edit" />
                                    <span>Ausgewählte bearbeiten</span>
                                </li>
                                <li
                                    v-if="showBatchOpenEventForSignup"
                                    class="context-menu-item"
                                    @click="openEventsForSignup(selectedEvents)"
                                >
                                    <i class="fa-solid fa-unlock-alt" />
                                    <span>Anmeldungen freischalten</span>
                                </li>
                                <li
                                    v-if="showBatchPublishPlannedCrew"
                                    class="context-menu-item"
                                    @click="publishCrewPlanning(selectedEvents)"
                                >
                                    <i class="fa-solid fa-earth-europe" />
                                    <span>Crewplanung veröffentlichen</span>
                                </li>
                            </template>
                            <li class="context-menu-item disabled">
                                <i class="fa-solid fa-users" />
                                <span>Fehlende Crew anfragen</span>
                            </li>
                            <li class="context-menu-item disabled">
                                <i class="fa-solid fa-envelope" />
                                <span>Crews kontaktieren</span>
                            </li>
                            <li class="context-menu-item text-red-700">
                                <i class="fa-solid fa-ban" />
                                <span>Reisen absagen</span>
                            </li>
                        </template>
                    </ContextMenuButton>
                </div>
            </div>
        </div>
        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div v-else class="sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-7 xl:pr-12 2xl:hidden">
            <button class="btn-primary btn-floating" @click="createEvent()">
                <i class="fa-solid fa-calendar-plus"></i>
                <span>Event erstellen</span>
            </button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Event, EventState, Permission } from '@/domain';
import {
    ConfirmationDialog,
    ContextMenuButton,
    Dialog,
    VConfirmationDialog,
    VTable,
    VTabs,
} from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import EventCancelDlg from '@/ui/components/events/EventCancelDlg.vue';
import EventCreateDlg from '@/ui/components/events/EventCreateDlg.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { useEventService } from '@/ui/composables/Domain';
import { Routes } from '@/ui/views/Routes';
import EventBatchEditDlg from '@/ui/views/admin/events/list/EventBatchEditDlg.vue';
import ImportEventsDlg from '@/ui/views/admin/events/list/ImportEventsDlg.vue';
import { DateTimeFormat } from '../../../../../common/date';

interface EventTableViewItem extends Event {
    selected: boolean;
    duration: number;
    isPastEvent: boolean;
    waitingListCount: number;
    hasOpenSlots: boolean;
    hasOpenRequiredSlots: boolean;
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const eventAdminUseCase = useEventAdministrationUseCase();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const route = useRoute();
const router = useRouter();
const user = authUseCase.getSignedInUser();

const events = ref<EventTableViewItem[] | null>(null);
const tab = ref<string>('Zukünftige');
const filter = ref<string>('');
const createEventDialog = ref<Dialog<Event> | null>(null);
const cancelEventDialog = ref<Dialog<Event, string> | null>(null);
const importEventsDialog = ref<Dialog<Event> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);
const eventBatchEditDialog = ref<Dialog<void, Partial<Event> | undefined> | null>(null);

const filteredEvents = computed<EventTableViewItem[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value?.filter((it) => it.name.toLowerCase().includes(f));
});

const selectedEvents = computed<EventTableViewItem[] | undefined>(() => {
    return filteredEvents.value?.filter((it) => it.selected);
});

const showBatchOpenEventForSignup = computed<boolean>(() => {
    return (
        selectedEvents.value != undefined &&
        selectedEvents.value.filter((it) => it.state === EventState.Draft).length > 0
    );
});

const showBatchPublishPlannedCrew = computed<boolean>(() => {
    return (
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
    emit('update:title', 'Events verwalten');
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
        return {
            ...evt,
            selected: false,
            duration: new Date(evt.end.getTime() - evt.start.getTime()).getDate(),
            isPastEvent: evt.start.getTime() < new Date().getTime(),
            waitingListCount: evt.registrations.length - evt.assignedUserCount,
            hasOpenSlots: hasOpenSlots(evt),
            hasOpenRequiredSlots: eventService.hasOpenRequiredSlots(evt),
        };
    });
}

function hasOpenSlots(event: Event): boolean {
    return event.slots.filter((slt) => !slt.assignedRegistrationKey).length > 0;
}

async function selectEvent(evt: EventTableViewItem): Promise<void> {
    evt.selected = !evt.selected;
}

async function editEvent(evt: EventTableViewItem): Promise<void> {
    if (selectedEvents.value && selectedEvents.value.length > 0) {
        evt.selected = !evt.selected;
    } else if (user.permissions.includes(Permission.WRITE_EVENTS)) {
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
    const patch = await eventBatchEditDialog.value?.open();
    if (patch === undefined) {
        return;
    }
    const keys = events.map((it) => it.key);
    await eventAdminUseCase.updateEvents(keys, patch);
    await fetchEvents();
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
