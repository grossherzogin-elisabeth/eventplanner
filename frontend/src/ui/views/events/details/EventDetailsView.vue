<template>
    <DetailsPage :back-to="{ name: Routes.Events }" :class="$attrs.class">
        <template #header>
            <h1 class="mb-2 hidden w-full truncate pt-8 xl:block">
                {{ event?.name }}
            </h1>
            <!--            <p>{{ event?.description }}</p>-->
        </template>
        <template #content>
            <div
                v-if="event"
                class="gap-x-20 gap-y-8 space-y-8 px-8 pb-8 pt-6 md:grid md:grid-cols-2 md:space-y-0 md:px-16 xl:px-20"
            >
                <!-- state info banner -->
                <section
                    v-if="event.state === EventState.Canceled"
                    class="sticky left-4 right-4 top-14 col-start-2 -mx-4 md:static xl:mx-0"
                >
                    <div class="overflow-hidden rounded-2xl bg-red-100 text-red-800">
                        <div class="flex items-center space-x-4 px-4 py-4 lg:px-8">
                            <i class="fa-solid fa-ban" />
                            <p class="text-sm font-bold">Diese Reise wurde abgesagt!</p>
                        </div>
                    </div>
                </section>
                <section
                    v-else-if="event.signedInUserAssignedPosition"
                    class="sticky left-4 right-4 top-14 col-start-2 -mx-4 md:static xl:mx-0"
                >
                    <div class="overflow-hidden rounded-2xl bg-green-100 text-green-800">
                        <div class="flex items-center space-x-4 px-4 py-4 lg:px-8">
                            <i class="fa-solid fa-check" />
                            <p class="text-sm font-bold">
                                Du bist für diese Reise als
                                {{ positions.get(event.signedInUserAssignedPosition).name }}
                                eingeplant
                            </p>
                        </div>
                    </div>
                </section>
                <section
                    v-else-if="event.signedInUserWaitingListPosition"
                    class="sticky left-4 right-4 top-14 col-start-2 -mx-4 md:static xl:mx-0"
                >
                    <div class="overflow-hidden rounded-2xl bg-yellow-100 text-yellow-800">
                        <div class="flex items-center space-x-4 px-4 py-4 lg:px-8">
                            <i class="fa-solid fa-hourglass-half" />
                            <p class="text-sm font-bold">
                                Du stehst für diese Reise als
                                {{ positions.get(event.signedInUserWaitingListPosition).name }}
                                auf der Warteliste
                            </p>
                        </div>
                    </div>
                </section>

                <!-- details -->
                <section class="-mx-4 md:col-start-2 xl:mx-0">
                    <h2 class="mb-2 ml-4 font-bold text-primary-800 text-opacity-50 lg:ml-8">
                        {{ $t('app.event-details.title') }}
                    </h2>
                    <div class="space-y-1 rounded-2xl bg-primary-100 p-4 lg:px-8">
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-route w-4 text-gray-700" />
                            <span>{{ event.name }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-calendar-day w-4 text-gray-700" />
                            <span>{{ formatDateRange(event.start, event.end) }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell w-4 text-gray-700" />
                            <span>Crew an Board: {{ $d(event.start, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell-slash w-4 text-gray-700" />
                            <span>Crew von Board: {{ $d(event.end, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-users w-4 text-gray-700" />
                            <span v-if="event.assignedUserCount && waitingListCount">
                                {{ event.assignedUserCount }} Crew, {{ waitingListCount }} Warteliste
                            </span>
                            <span v-else-if="event.assignedUserCount"> {{ event.assignedUserCount }} Crew </span>
                            <span v-else> {{ event.registrations.length }} Anmeldungen </span>
                        </p>
                    </div>
                </section>

                <!-- route -->
                <section class="-mx-4 md:col-start-2 xl:mx-0">
                    <h2 class="mb-2 ml-4 font-bold text-primary-800 text-opacity-50 lg:ml-8">
                        <template v-if="event.locations.length === 1">Ort</template>
                        <template v-else>Route</template>
                    </h2>
                    <div class="space-y-1 rounded-2xl bg-primary-100 p-4 lg:px-8">
                        <p v-if="event.locations.length === 0" class="text-sm">
                            Für diese Reise wurde noch keine Reiseroute bekannt gegeben. Sobald diese Informationen
                            verfügbar sind, kannst du sie hier sehen.
                        </p>
                        <div
                            v-for="(stop, portIndex) in event.locations"
                            v-else
                            :key="portIndex"
                            class="flex items-center space-x-4"
                        >
                            <i :class="stop.icon" class="fa-solid w-4" />
                            <span class="flex-grow">{{ stop.name }}</span>
                            <CountryFlag v-if="stop.country" :country="stop.country" class="border border-gray-200" />
                        </div>
                    </div>
                </section>

                <!-- crew -->
                <section class="col-start-1 row-span-6 -mx-4 md:row-start-1 md:mx-0">
                    <h2
                        v-if="statesWithHiddenCrew.includes(event.state)"
                        class="mb-2 ml-4 flex space-x-4 font-bold text-primary-800 text-opacity-50 md:mb-6 md:ml-0"
                    >
                        <span>Anmeldungen</span>
                    </h2>
                    <h2
                        v-else
                        class="mb-2 ml-4 flex space-x-4 font-bold text-primary-800 text-opacity-50 md:mb-6 md:ml-0"
                    >
                        <button
                            class="hover:text-primary-600"
                            :class="{ 'text-primary-600 underline': tab === Tab.Team }"
                            @click="tab = Tab.Team"
                        >
                            Crew ({{ event.assignedUserCount }})
                        </button>
                        <button
                            class="hover:text-primary-600"
                            :class="{ 'text-primary-600 underline': tab === Tab.WaitingList }"
                            @click="tab = Tab.WaitingList"
                        >
                            Warteliste ({{ waitingListCount }})
                        </button>
                    </h2>
                    <div
                        v-if="event.assignedUserCount === 0 && waitingListCount === 0"
                        class="rounded-2xl bg-primary-100 px-4 md:-mx-4 md:-mt-4 lg:-mx-8 lg:px-8"
                    >
                        <div class="flex items-center py-8">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-trophy opacity-75"></i>
                                    <span class="ml-4">Du könntest der erste sein!</span>
                                </h3>
                                <p class="text-sm">
                                    Für diese Reise hat sich bisher noch niemand angemeldet. Du kannst den Anfang machen
                                    und dich anmelden. Alle Anmeldungen werden zuerst auf der Warteliste gesammelt und
                                    anschließend wird vom Büro eine Crew zusammengestellt.
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8 opacity-20">
                            <li v-for="i in 10" :key="i" class="flex items-center space-x-4 rounded-xl py-1">
                                <i class="fa-solid fa-circle text-gray-400"></i>
                                <span class="mx-2 inline-block h-4 w-64 rounded-full bg-gray-400"> </span>
                                <span class="flex-grow"></span>
                                <span class="position bg-gray-400">
                                    <span class="mx-2 inline-block h-2 w-16 rounded-full bg-gray-100"> </span>
                                </span>
                            </li>
                        </ul>
                    </div>
                    <div v-else class="rounded-2xl bg-primary-100 p-4 md:rounded-none md:bg-transparent md:p-0">
                        <template v-if="tab === Tab.Team">
                            <ul class="space-y-2">
                                <template v-for="(it, index) in team" :key="index">
                                    <li class="flex items-center space-x-2 md:space-x-4">
                                        <i v-if="it.name" class="fa-solid fa-user-circle text-gray-500" />
                                        <i v-else class="fa-solid fa-user-circle text-red-500" />
                                        <RouterLink
                                            v-if="
                                                it.user &&
                                                signedInUser.permissions.includes(Permission.READ_USER_DETAILS)
                                            "
                                            :to="{ name: Routes.UserDetails, params: { key: it.user.key } }"
                                            class="truncate"
                                        >
                                            {{ it.name }}
                                        </RouterLink>
                                        <span v-else-if="it.name" class="truncate">{{ it.name }}</span>
                                        <span v-else-if="it.user?.key" class="italic text-red-500">
                                            Unbekannter Nutzer
                                        </span>
                                        <span v-else class="truncate italic text-red-500">Noch nicht besetzt</span>
                                        <span class="flex-grow"></span>
                                        <span
                                            :style="{ background: it.position.color }"
                                            class="position ml-auto text-xs"
                                        >
                                            {{ it.position.name }}
                                        </span>
                                    </li>
                                </template>
                            </ul>
                        </template>
                        <template v-else-if="tab === Tab.WaitingList">
                            <ul class="space-y-2">
                                <li
                                    v-for="(it, index) in waitingList"
                                    :key="index"
                                    class="flex items-center justify-between space-x-2 md:space-x-4"
                                >
                                    <i class="fa-solid fa-user-circle text-gray-500" />
                                    <span class="flex-grow">{{ it.name }}</span>
                                    <span :style="{ background: it.position.color }" class="position text-xs">
                                        {{ it.position.name }}
                                    </span>
                                </li>
                            </ul>
                            <div
                                v-if="waitingList.length === 0"
                                class="-mx-4 -mt-4 rounded-xl bg-primary-100 p-4 text-sm lg:-mx-8 lg:px-8"
                            >
                                <p v-if="statesWithHiddenCrew.includes(event.state)">
                                    Für diesen Termin gibt es noch keine Anmeldungen.
                                </p>
                                <p v-else>Für diesen Termin gibt es keine Anmeldungen auf der Warteliste.</p>
                            </div>
                        </template>
                    </div>
                </section>

                <!-- documents -->
                <section
                    v-if="signedInUser.permissions.includes(Permission.BETA_FEATURES)"
                    class="-mx-4 md:col-start-2 xl:mx-0"
                >
                    <h2 class="mb-2 ml-4 font-bold text-primary-800 text-opacity-50 lg:ml-8">Dokumente</h2>
                    <div class="rounded-2xl bg-primary-100 p-4 lg:px-8">
                        <p v-for="doc in documentsMock" :key="doc" class="mb-1 flex items-center space-x-4">
                            <i class="fa-solid fa-file-pdf w-4 text-gray-700" />
                            <span>{{ doc }}</span>
                        </p>
                    </div>
                </section>
            </div>
        </template>
        <template v-if="event && signedInUser.permissions.includes(Permission.EVENT_TEAM_WRITE_SELF)" #primary-button>
            <AsyncButton v-if="event.signedInUserAssignedPosition" class="btn-danger" :action="() => leaveEvent()">
                <template #icon><i class="fa-solid fa-cancel" /></template>
                <template #label>Reise absagen</template>
            </AsyncButton>
            <AsyncButton
                v-else-if="event.signedInUserWaitingListPosition"
                class="btn-danger"
                :disabled="!event.canSignedInUserLeave"
                :action="() => leaveEvent()"
            >
                <template #icon>
                    <i class="fa-solid fa-user-minus" />
                </template>
                <template #label> Warteliste verlassen </template>
            </AsyncButton>
            <div v-else-if="event.canSignedInUserJoin && signedInUserPositions.length > 1" class="btn-split">
                <AsyncButton class="btn-primary max-w-64 sm:max-w-80" :action="() => joinEvent()">
                    <template #icon>
                        <i class="fa-solid fa-user-plus" />
                    </template>
                    <template #label> Anmelden als {{ positions.get(signedInUserPositions[0]).name }} </template>
                </AsyncButton>
                <button
                    v-if="signedInUserPositions.length > 1"
                    class="btn-primary"
                    @click="choosePositionAndJoinEvent(event)"
                >
                    <i class="fa-solid fa-chevron-down" />
                </button>
            </div>
            <AsyncButton
                v-else
                class="btn-primary max-w-80"
                :disabled="!event.canSignedInUserJoin"
                :action="() => joinEvent()"
            >
                <template #icon>
                    <i class="fa-solid fa-user-plus" />
                </template>
                <template #label>
                    <span class="truncate text-left">
                        Anmelden als {{ positions.get(signedInUserPositions[0]).name }}
                    </span>
                </template>
            </AsyncButton>
        </template>
        <template v-if="event" #secondary-buttons>
            <RouterLink
                v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)"
                :to="{ name: Routes.EventEdit }"
                class="btn-secondary"
            >
                <i class="fa-solid fa-edit" />
                <span>Reise bearbeiten</span>
            </RouterLink>
            <button v-else class="btn-secondary" @click="eventUseCase.downloadCalendarEntry(event)">
                <i class="fa-solid fa-calendar-alt" />
                <span>In Kalender speichern</span>
            </button>
        </template>
        <template v-if="event" #actions-menu>
            <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntry(event)">
                <i class="fa-solid fa-calendar-alt" />
                <span>Kalendereintrag erstellen</span>
            </li>
            <li class="context-menu-item disabled">
                <i class="fa-solid fa-note-sticky" />
                <span>Notiz fürs Büro hinzufügen</span>
            </li>
            <li
                v-if="signedInUser.permissions.includes(Permission.READ_USER_DETAILS)"
                class="context-menu-item disabled"
            >
                <i class="fa-solid fa-clipboard-user" />
                <span>IMO Liste generieren</span>
            </li>
            <li class="context-menu-item disabled">
                <i class="fa-solid fa-beer-mug-empty" />
                <span>Getränkeliste generieren</span>
            </li>
            <li v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)">
                <RouterLink :to="{ name: Routes.EventEdit }" class="context-menu-item">
                    <i class="fa-solid fa-edit" />
                    <span>Reise bearbeiten</span>
                </RouterLink>
            </li>
        </template>
    </DetailsPage>
    <PositionSelectDlg ref="positionSelectDialog" />
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { DateTimeFormat } from '@/common/date';
import type { Event, PositionKey } from '@/domain';
import { EventState, Permission } from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton } from '@/ui/components/common';
import PositionSelectDlg from '@/ui/components/events/PositionSelectDlg.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import CountryFlag from '@/ui/components/utils/CountryFlag.vue';
import { useAuthUseCase, useEventUseCase, useUsersUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { usePositions } from '@/ui/composables/Positions';
import { Routes } from '@/ui/views/Routes';

enum Tab {
    Team = 'team',
    WaitingList = 'waitinglist',
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const positions = usePositions();
const authUseCase = useAuthUseCase();
const eventUseCase = useEventUseCase();
const usersUseCase = useUsersUseCase();
const signedInUser = authUseCase.getSignedInUser();

const statesWithHiddenCrew = [EventState.OpenForSignup, EventState.Draft];
const signedInUserPositions = ref<PositionKey[]>([]);
const event = ref<Event | null>(null);
const tab = ref<Tab>(Tab.Team);
const documentsMock = ['Kammerplan', 'Wachplan', 'Getränkeliste Crew'];

const waitingList = ref<ResolvedRegistrationSlot[]>([]);
const team = ref<ResolvedRegistrationSlot[]>([]);

const positionSelectDialog = ref<Dialog<unknown, PositionKey> | null>(null);

const waitingListCount = computed<number>(() => {
    if (!event.value) return 0;
    return event.value.registrations.length - event.value.assignedUserCount;
});

function init(): void {
    fetchEvent();
    fetchSignedInUserPositions();
    watch(event, onEventChanged);
}

async function fetchEvent(): Promise<void> {
    const key = route.params.key as string;
    const year = parseInt(route.params.year as string, 10) || new Date().getFullYear();
    event.value = await eventUseCase.getEventByKey(year, key);
}

async function fetchSignedInUserPositions(): Promise<void> {
    const user = await usersUseCase.getUserDetailsForSignedInUser();
    signedInUserPositions.value = user.positionKeys;
}

async function onEventChanged() {
    emit('update:title', event.value?.name || '');
    if (!event.value) {
        return;
    }

    if (statesWithHiddenCrew.includes(event.value.state)) {
        tab.value = Tab.WaitingList;
    }
    await fetchTeam(event.value);
}

async function fetchTeam(event: Event): Promise<void> {
    const registrations = await eventUseCase.resolveRegistrations(event);
    team.value = eventUseCase.filterForCrew(event, registrations);
    waitingList.value = eventUseCase.filterForWaitingList(event, registrations);
    // const slots = await usersUseCase.resolveEventSlots(event);
    // team.value = slots.filter((it) => it.criticality >= 1 || it.userName);
    // waitingList.value = await usersUseCase.resolveWaitingList(event);
}

async function choosePositionAndJoinEvent(evt: Event): Promise<void> {
    const position = await positionSelectDialog.value?.open();
    if (position) {
        // default position might have changed
        await fetchSignedInUserPositions();
        event.value = await eventUseCase.joinEvent(evt, position);
    }
}

async function joinEvent(): Promise<void> {
    if (event.value) {
        event.value = await eventUseCase.joinEvent(event.value, signedInUserPositions.value[0]);
    }
}

async function leaveEvent(): Promise<void> {
    if (event.value) {
        event.value = await eventUseCase.leaveEvent(event.value);
    }
}

init();
</script>
