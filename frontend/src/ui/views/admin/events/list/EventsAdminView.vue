<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <NavbarFilter v-model="filter" placeholder="Events durchsuchen" />
        </teleport>
        <div class="top-0 hidden bg-primary-50 px-4 pb-8 pl-16 pr-20 pt-8 xl:block">
            <div class="flex items-center space-x-4">
                <VInputText v-model="filter" class="input-search w-96" placeholder="Reisen filtern">
                    <template #before>
                        <i class="fa-solid fa-magnifying-glass ml-4 text-primary-900 text-opacity-25" />
                    </template>
                </VInputText>
                <div class="hidden flex-grow md:block"></div>
                <button class="btn-primary" @click="createEvent()">
                    <i class="fa-solid fa-calendar-plus"></i>
                    <span>Reise erstellen</span>
                </button>
                <button
                    v-if="user.permissions.includes(Permission.WRITE_EVENTS)"
                    class="btn-secondary"
                    @click="importEvents()"
                >
                    <i class="fa-solid fa-upload"></i>
                    <span>Reisen importieren</span>
                </button>
            </div>
        </div>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8" />
        <div class="w-full">
            <VTable
                :items="filteredEvents"
                :page-size="20"
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="editEvent($event)"
            >
                <template #head>
                    <!--                    <th>-->
                    <!--                        <VInputCheckBox />-->
                    <!--                    </th>-->
                    <th>Name</th>
                    <th>Status</th>
                    <th>Crew</th>
                    <th>Datum</th>
                </template>
                <template #row="{ item }">
                    <!--                    <td class="group" @click.stop="">-->
                    <!--                        <VInputCheckBox v-model="item.selected" />-->
                    <!--                    </td>-->
                    <td class="w-1/2 max-w-[65vw] whitespace-nowrap font-semibold">
                        <p
                            class="mb-1 truncate"
                            :class="{ 'text-red-700 line-through': item.state === EventState.Canceled }"
                        >
                            <span v-if="item.state === EventState.Draft" class="opacity-50">Entwurf: </span>
                            <span v-else-if="item.state === EventState.Canceled" class="">Abgesagt: </span>
                            {{ item.name }}
                        </p>
                        <p v-if="item.locations.length === 0" class="truncate text-sm font-light">
                            keine Reiseroute angegeben
                        </p>
                        <p v-else class="truncate text-sm font-light">
                            {{ item.locations.map((it) => it.name).join(' - ') }}
                        </p>
                    </td>
                    <td>
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
                    </td>
                    <td class="w-1/6 whitespace-nowrap text-center">
                        <p class="mb-1 font-semibold">
                            {{ item.assignedUserCount }}
                            <span v-if="item.waitingListCount" class="opacity-40"> +{{ item.waitingListCount }} </span>
                        </p>
                        <p class="text-sm">Crew</p>
                    </td>
                    <td class="w-2/6 whitespace-nowrap">
                        <p class="mb-1 font-semibold">{{ formatDateRange(item.start, item.end) }}</p>
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
                                    <li v-if="item.state === EventState.Draft" class="context-menu-item disabled">
                                        <i class="fa-solid fa-unlock-alt" />
                                        <span>Anmeldungen freischalten</span>
                                    </li>
                                    <li
                                        v-else-if="item.state === EventState.OpenForSignup"
                                        class="context-menu-item disabled"
                                    >
                                        <i class="fa-solid fa-earth-europe" />
                                        <span>Crew veröffentlichen</span>
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
                                <li class="context-menu-item text-red-700" @click="deleteEvent(item)">
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
        <EventCancelDlg ref="deleteEventDialog" />
        <ImportEventsDlg ref="importEventsDialog" />

        <div class="sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-14 xl:hidden">
            <button class="btn-primary btn-floating" @click="createEvent()">
                <i class="fa-solid fa-calendar-plus"></i>
                <span>Event erstellen</span>
            </button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { Event } from '@/domain';
import { EventState, Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { ContextMenuButton, VInputText, VTable, VTabs } from '@/ui/components/common';
import EventCancelDlg from '@/ui/components/events/EventCancelDlg.vue';
import EventCreateDlg from '@/ui/components/events/EventCreateDlg.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { useEventService } from '@/ui/composables/Domain';
import { Routes } from '@/ui/views/Routes';
import ImportEventsDlg from '@/ui/views/admin/events/list/ImportEventsDlg.vue';

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

const eventAdministrationUseCase = useEventAdministrationUseCase();
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
const deleteEventDialog = ref<Dialog<Event, string> | null>(null);
const importEventsDialog = ref<Dialog<Event> | null>(null);

const filteredEvents = computed<EventTableViewItem[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value?.filter((it) => it.name.toLowerCase().includes(f));
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

async function editEvent(evt: EventTableViewItem): Promise<void> {
    await router.push({
        name: Routes.EventEdit,
        params: { year: evt.start.getFullYear(), key: evt.key },
    });
}

async function createEvent(): Promise<void> {
    const event = await createEventDialog.value?.open().catch();
    if (event) {
        await eventAdministrationUseCase.createEvent(event);
    }
}

async function deleteEvent(evt: Event): Promise<void> {
    if (deleteEventDialog.value) {
        await deleteEventDialog.value
            .open(evt)
            .then((message) => eventAdministrationUseCase.cancelEvent(evt, message))
            .then(() => fetchEvents())
            .catch(() => console.debug('dialog was canceled'));
    }
}

async function importEvents(): Promise<void> {
    await importEventsDialog.value?.open().catch();
}

init();
</script>
