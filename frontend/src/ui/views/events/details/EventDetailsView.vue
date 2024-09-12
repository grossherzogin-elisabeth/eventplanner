<template>
    <DetailsPage :back-to="{ name: Routes.Events }">
        <template #header>
            <h1 class="mb-2 w-full truncate">{{ event?.name }}</h1>
        </template>
        <template #content>
            <div
                v-if="event"
                class="gap-x-20 gap-y-8 space-y-8 px-8 pb-8 pt-6 md:grid md:grid-cols-2 md:space-y-0 md:px-16 xl:px-20"
            >
                <!-- state info banner -->
                <section v-if="state" class="sticky left-4 right-4 top-14 col-start-2 -mx-4 md:static xl:mx-0">
                    <div class="overflow-hidden rounded-2xl bg-white">
                        <div :class="state.color">
                            <div class="flex items-center space-x-4 px-4 py-4 lg:px-8">
                                <i :class="state.icon" class="fa-solid" />
                                <!-- eslint-disable-next-line vue/no-v-html -->
                                <p class="text-sm font-bold" v-html="state.text"></p>
                            </div>
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
                            <i class="fa-solid fa-tag w-4 text-gray-700" />
                            <span>{{ event.name }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-calendar-day w-4 text-gray-700" />
                            <span>{{ formatDateRange(event.start, event.end) }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-clock w-4 text-gray-700" />
                            <span>Crew an Board: 16:00 Uhr</span>
                        </p>
                        <!--                    <p class="flex items-center space-x-4">-->
                        <!--                        <i class="fa-solid fa-route w-4 text-gray-700" />-->
                        <!--                        <span>300 Seemeilen (geschätzt)</span>-->
                        <!--                    </p>-->
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-users w-4 text-gray-700" />
                            <span v-if="event.assignedUserCount && waitingListCount">
                                {{ event.assignedUserCount }} Crew, {{ waitingListCount }} Warteliste
                            </span>
                            <span v-else-if="event.assignedUserCount"> {{ event.assignedUserCount }} Crew </span>
                            <span v-else> {{ event.registrations.length }} Anmeldungen </span>
                        </p>
                        <p v-if="event.description" class="flex items-center space-x-4">
                            <i class="fa-solid fa-info-circle w-4 text-gray-700" />
                            <span>{{ event.description }}</span>
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
                        <div v-if="event.locations.length === 0">
                            <i>Wird noch bekannt gegeben</i>
                        </div>
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
                        v-if="event.state !== EventState.OpenForSignup"
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
                    <h2
                        v-else
                        class="mb-2 ml-4 flex space-x-4 font-bold text-primary-800 text-opacity-50 md:mb-6 md:ml-0"
                    >
                        <span>Anmeldungen</span>
                    </h2>
                    <div class="rounded-2xl bg-primary-100 p-4 md:rounded-none md:bg-transparent md:p-0">
                        <template v-if="tab === Tab.Team">
                            <ul class="space-y-2">
                                <template v-for="(it, index) in team" :key="index">
                                    <li class="flex items-center space-x-2 md:space-x-4">
                                        <i v-if="it.userName" class="fa-solid fa-user-circle text-gray-500" />
                                        <i v-else class="fa-solid fa-user-circle text-red-500" />
                                        <RouterLink
                                            v-if="
                                                it.userName && user.permissions.includes(Permission.READ_USER_DETAILS)
                                            "
                                            :to="{ name: Routes.UserDetails, params: { key: it.userKey } }"
                                            class="truncate"
                                        >
                                            {{ it.userName }}
                                        </RouterLink>
                                        <span v-else-if="it.userName" class="truncate">{{ it.userName }}</span>
                                        <span v-else-if="it.userKey" class="italic text-red-500"
                                            >err: {{ it.userKey }}</span
                                        >
                                        <span v-else class="truncate italic text-red-500">Noch nicht besetzt</span>
                                        <span v-if="it.userName && !it.userKey" class="">(Gastcrew)</span>
                                        <span class="flex-grow"></span>
                                        <span :style="{ background: it.position.color }" class="position ml-auto">
                                            {{ it.positionName }}
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
                                    <span :style="{ background: it.position.color }" class="position">
                                        {{ it.position.name }}
                                    </span>
                                </li>
                            </ul>
                            <div
                                v-if="waitingList.length === 0"
                                class="-mx-4 -mt-4 rounded-xl bg-primary-100 p-4 text-sm lg:-mx-8 lg:px-8"
                            >
                                <p v-if="event.state === EventState.OpenForSignup">
                                    Für diesen Termin gibt es noch keine Anmeldungen.
                                </p>
                                <p v-else>Für diesen Termin gibt es keine Anmeldungen auf der Warteliste.</p>
                            </div>
                        </template>
                    </div>
                </section>

                <!-- documents -->
                <section
                    v-if="user.permissions.includes(Permission.BETA_FEATURES)"
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
        <template v-if="event && user.permissions.includes(Permission.EVENT_TEAM_WRITE_SELF)" #primary-button>
            <button
                v-if="event.signedInUserAssignedPosition"
                class="btn-danger"
                :disabled="!canRegistrationStillBeCancelled"
                @click="leaveEvent(event)"
            >
                <i class="fa-solid fa-cancel" />
                <span>Reise absagen</span>
            </button>
            <button
                v-else-if="event.signedInUserWaitingListPosition"
                class="btn-danger"
                :disabled="isInPast"
                @click="leaveEvent(event)"
            >
                <i class="fa-solid fa-user-minus" />
                <span>Abmelden</span>
            </button>
            <button v-else class="btn-primary" :disabled="isInPast" @click="joinEvent(event)">
                <i class="fa-solid fa-user-plus" />
                <span>Anmelden</span>
            </button>
        </template>
        <template v-if="event" #secondary-buttons>
            <button class="btn-secondary" @click="eventUseCase.downloadCalendarEntry(event)">
                <i class="fa-solid fa-calendar-alt" />
                <span>In Kalender speichern</span>
            </button>
            <RouterLink
                v-if="user.permissions.includes(Permission.WRITE_EVENTS)"
                :to="{ name: Routes.EventEdit }"
                class="btn-secondary"
            >
                <i class="fa-solid fa-edit" />
                <span>Reise bearbeiten</span>
            </RouterLink>
        </template>
        <template v-if="event" #actions-menu>
            <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntry(event)">
                <i class="fa-solid fa-calendar-alt" />
                <span>Kalendereintrag erstellen</span>
            </li>
            <li v-if="user.permissions.includes(Permission.WRITE_EVENTS)">
                <RouterLink :to="{ name: Routes.EventEdit }" class="context-menu-item">
                    <i class="fa-solid fa-edit" />
                    <span>Reise bearbeiten</span>
                </RouterLink>
            </li>
        </template>
    </DetailsPage>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { DateUtils } from '@/common';
import type { Event, Position, PositionKey, ResolvedRegistration, ResolvedSlot } from '@/domain';
import { EventState, Permission } from '@/domain';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import CountryFlag from '@/ui/components/utils/CountryFlag.vue';
import { useAuthUseCase, useEventUseCase, useUsersUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { Routes } from '@/ui/views/Routes';

interface State {
    text: string;
    icon: string;
    color?: string;
}

enum Tab {
    Team = 'team',
    WaitingList = 'waitinglist',
}

const i18n = useI18n();
const route = useRoute();
const authUseCase = useAuthUseCase();
const eventUseCase = useEventUseCase();
const usersUseCase = useUsersUseCase();
const user = authUseCase.getSignedInUser();

const event = ref<Event | null>(null);
const tab = ref<Tab>(Tab.Team);
const ownPosition = ref<Position | null>(null);
const documentsMock = ['Kammerplan', 'Wachplan', 'Getränkeliste Crew'];

const position = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const waitingList = ref<ResolvedRegistration[]>([]);
const team = ref<ResolvedSlot[]>([]);

const canRegistrationStillBeCancelled = ref<boolean>(false);
const isInPast = ref<boolean>(false);
const state = ref<State | null>(null);

const waitingListCount = computed<number>(() => {
    if (!event.value) return 0;
    return event.value.registrations.length - event.value.assignedUserCount;
});

function init(): void {
    fetchPositions();
    fetchEvent();
}

async function fetchPositions(): Promise<void> {
    position.value = await usersUseCase.resolvePositionNames();
}

async function fetchEvent(): Promise<void> {
    const key = route.params.key as string;
    const year = parseInt(route.params.year as string, 10) || new Date().getFullYear();
    event.value = await eventUseCase.getEventByKey(year, key);
    if (event.value.state === EventState.OpenForSignup) {
        tab.value = Tab.WaitingList;
    }

    const latestCancelTime = DateUtils.subtract(event.value.start, { days: 7 });
    canRegistrationStillBeCancelled.value = latestCancelTime.getTime() > new Date().getTime();
    isInPast.value = event.value.start.getTime() < new Date().getTime();

    if (event.value?.signedInUserAssignedPosition !== undefined) {
        state.value = {
            text: ownPosition.value
                ? i18n.t('app.event-details.note-assigned-position', { position: ownPosition.value.name })
                : i18n.t('app.event-details.note-assigned'),
            icon: 'fa-check',
            color: 'bg-green-200 text-green-800',
        };
    } else if (event.value?.signedInUserWaitingListPosition) {
        state.value = {
            text: ownPosition.value
                ? i18n.t('app.event-details.note-waitinglist-position', { position: ownPosition.value.name })
                : i18n.t('app.event-details.note-waitinglist'),
            icon: 'fa-clock',
            color: 'bg-yellow-100 text-yellow-900',
        };
    }

    await fetchTeam(event.value);
}

async function fetchTeam(event: Event): Promise<void> {
    const slots = await usersUseCase.resolveEventSlots(event);
    team.value = slots.filter((it) => it.criticality >= 1 || it.userName);
    waitingList.value = await usersUseCase.resolveWaitingList(event);
}

async function joinEvent(evt: Event): Promise<void> {
    // TODO get real user position
    event.value = await eventUseCase.joinEvent(evt, 'deckshand');
    await fetchTeam(event.value);
}

async function leaveEvent(evt: Event): Promise<void> {
    event.value = await eventUseCase.leaveEvent(evt);
    await fetchTeam(event.value);
}

init();
</script>
