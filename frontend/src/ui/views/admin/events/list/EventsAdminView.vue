<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto">
        <teleport to="#nav-right">
            <NavbarFilter v-model="filter" placeholder="Events durchsuchen" />
        </teleport>
        <div
            class="top-12 z-10 hidden bg-primary-50 px-4 pb-8 pt-4 md:pl-12 md:pr-16 md:pt-8 xl:top-0 xl:block xl:pl-16 xl:pr-20"
        >
            <div class="flex items-center space-x-4">
                <VInputText v-model="filter" class="input-search w-96" placeholder="Reisen filtern">
                    <template #before>
                        <i class="fa-solid fa-magnifying-glass ml-4 text-primary-900 text-opacity-25" />
                    </template>
                </VInputText>
                <div class="hidden flex-grow md:block"></div>
                <button class="btn-primary" @click="createEvent()">
                    <i class="fa-solid fa-calendar-plus"></i>
                    <span>Event erstellen</span>
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

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 bg-primary-50 pt-4" />
        <div class="w-full overflow-x-auto px-8 pt-6 md:px-16 xl:px-20 xl:pt-0">
            <div class="-mx-4 px-4 pt-4">
                <VTable :items="filteredEvents" :page-size="-1" class="interactive-table" @click="editEvent($event)">
                    <template #head>
                        <th class="w-16"></th>
                        <th class="w-1/2" data-sortby="name">Reise</th>
                        <th class="w-1/6" data-sortby="hasOpenRequiredSlots"></th>
                        <th class="w-1/6" data-sortby="registrations">Anmeldungen</th>
                        <th class="hidden w-1/6 md:table-cell" data-sortby="startDate">Datum</th>
                        <th class="hidden w-1/6 md:table-cell" data-sortby="duration">Dauer</th>
                    </template>
                    <template #row="{ item }">
                        <td>
                            <i v-if="item.selected" class="fa-regular fa-check-square"></i>
                            <i v-else class="fa-regular fa-square"></i>
                        </td>
                        <td class="w-full whitespace-nowrap border-none font-semibold">
                            <span class="hover:text-primary-600">
                                {{ item.name }}
                            </span>
                            <p class="text-sm font-light">{{ item.locations }}</p>
                        </td>
                        <td>
                            <div
                                v-if="item.hasOpenRequiredSlots"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                            >
                                <i class="fa-solid fa-warning"></i>
                                <span class="whitespace-nowrap font-semibold">Fehlende Crew</span>
                            </div>
                            <div
                                v-else-if="item.hasOpenSlots"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-100 py-1 pl-3 pr-4 text-green-700"
                            >
                                <i class="fa-solid fa-check-circle"></i>
                                <span class="whitespace-nowrap font-semibold">Freie Plätze</span>
                            </div>
                            <div
                                v-else
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-100 py-1 pl-3 pr-4 text-green-700"
                            >
                                <i class="fa-solid fa-check-circle"></i>
                                <span class="whitespace-nowrap font-semibold">Voll belegt</span>
                            </div>
                        </td>
                        <td>
                            {{ item.registrations }}
                        </td>
                        <td class="hidden md:table-cell">
                            {{ item.start }}
                        </td>
                        <td class="hidden md:table-cell">{{ item.duration }} Tage</td>
                    </template>
                </VTable>
            </div>
        </div>

        <CreateEventDlg ref="createEventDialog" />
        <ImportEventsDlg ref="importEventsDialog" />

        <div class="fixed bottom-0 right-0 flex justify-end pb-4 pr-3 md:pr-14 xl:hidden">
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
import { useI18n } from 'vue-i18n';
import { ArrayUtils } from '@/common';
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import { Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputText, VTable, VTabs } from '@/ui/components/common';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { Routes } from '@/ui/views/Routes';
import CreateEventDlg from '@/ui/views/admin/events/list/CreateEventDlg.vue';
import ImportEventsDlg from '@/ui/views/admin/events/list/ImportEventsDlg.vue';

interface EventTableViewItem {
    selected: boolean;
    eventKey: string;
    name: string;
    locations: string;
    start: string;
    end: string;
    startDate: Date;
    endDate: Date;
    duration: number;
    isPastEvent: boolean;
    registrations: number;
    waitingList: number;
    hasOpenSlots: boolean;
    hasOpenRequiredSlots: boolean;
}

const eventAdministrationUseCase = useEventAdministrationUseCase();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const user = authUseCase.getSignedInUser();

const events = ref<EventTableViewItem[]>([]);
const tab = ref<string>('Zukünftige');
const filter = ref<string>('');
const createEventDialog = ref<Dialog<Event> | null>(null);
const importEventsDialog = ref<Dialog<Event> | null>(null);

const filteredEvents = computed<EventTableViewItem[]>(() => {
    const f = filter.value.toLowerCase();
    return events.value.filter((it) => it.name.toLowerCase().includes(f));
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
    fetchEvents();
    watch(route, () => fetchEvents());
    watch(tab, () => fetchEvents());
}

async function fetchEvents(): Promise<void> {
    if (tab.value === tabs.value[0]) {
        const now = new Date();
        const currentYear = await fetchEventsByYear(now.getFullYear());
        const nextYear = await fetchEventsByYear(now.getFullYear() + 1);
        events.value = currentYear.concat(nextYear).filter((it) => it.endDate.getTime() > now.getTime());
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
            selected: false,
            eventKey: evt.key,
            name: evt.name,
            start: i18n.d(evt.start, DateTimeFormat.DD_MM_YYYY),
            startDate: evt.start,
            end: i18n.d(evt.end, DateTimeFormat.DD_MM_YYYY),
            endDate: evt.end,
            duration: new Date(evt.end.getTime() - evt.start.getTime()).getDate(),
            locations: evt.locations.map((it) => it.name).join(' - '),
            isPastEvent: evt.start.getTime() < new Date().getTime(),
            registrations: evt.registrations.length,
            waitingList: evt.registrations.filter((it) => it.slotKey).length,
            hasOpenSlots: hasOpenSlots(evt),
            hasOpenRequiredSlots: eventService.hasOpenRequiredSlots(evt),
        };
    });
}

function hasOpenSlots(event: Event): boolean {
    const filledSlotKeys = event.registrations.map((it) => it.slotKey).filter(ArrayUtils.filterUndefined);
    return filledSlotKeys.length < 23;
}

async function editEvent(evt: EventTableViewItem): Promise<void> {
    await router.push({
        name: Routes.EventEdit,
        params: { year: evt.startDate.getFullYear(), key: evt.eventKey },
    });
}

async function createEvent(): Promise<void> {
    const event = await createEventDialog.value?.open().catch();
    if (event) {
        await eventAdministrationUseCase.createEvent(event);
    }
}

async function importEvents(): Promise<void> {
    await importEventsDialog.value?.open().catch();
}

init();
</script>
