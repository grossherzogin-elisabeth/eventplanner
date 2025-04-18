<template>
    <DetailsPage :back-to="{ name: Routes.EventsCalendar }" :class="$attrs.class">
        <template #header>
            <h1 class="mb-2 hidden w-full truncate pt-8 xl:block">
                {{ event?.name }}
            </h1>
        </template>
        <template #content>
            <div v-if="event" class="space-y-4 px-8 pb-8 pt-6 md:grid md:grid-cols-2 md:gap-x-20 md:gap-y-4 md:space-y-0 md:px-16 xl:px-20">
                <!-- state info banner -->
                <section v-if="event.state === EventState.OpenForSignup" class="col-start-2">
                    <VInfo clamp>
                        Diese Reise befindet sich noch in der Planung. Eine Anmeldung garantiert keine Teilnahme an der Reise! Sobald die
                        Crewplanung veröffentlicht wird, wirst du per Email darüber informiert.
                    </VInfo>
                </section>
                <section v-if="event.state === EventState.Canceled" class="sticky left-4 right-4 top-14 z-10 col-start-2 md:static">
                    <VWarning> Diese Reise wurde abgesagt! </VWarning>
                </section>
                <section
                    v-else-if="event.signedInUserRegistration && event.signedInUserAssignedSlot"
                    class="sticky left-4 right-4 top-14 z-10 col-start-2 md:static"
                >
                    <VSuccess icon="fa-check">
                        Du bist für diese Reise als
                        <b>{{ positions.get(event.signedInUserRegistration.positionKey).name }}</b>
                        eingeplant.
                        <template v-if="event.signedInUserRegistration.confirmed"> Du hast deine Teilnahme bestätigt. </template>
                    </VSuccess>
                </section>
                <section v-else-if="event.signedInUserRegistration" class="sticky left-4 right-4 top-14 z-10 col-start-2 md:static">
                    <VInfo icon="fa-hourglass-half">
                        Du stehst für diese Reise als
                        <b>{{ positions.get(event.signedInUserRegistration.positionKey).name }}</b>
                        auf der Warteliste
                    </VInfo>
                </section>

                <!-- details -->
                <section class="pt-4 md:col-start-2">
                    <h2 class="mb-2 font-bold text-secondary">
                        {{ $t('app.event-details.title') }}
                    </h2>
                    <div class="space-y-1 rounded-2xl bg-surface-container-low p-4">
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-route w-4" />
                            <span class="truncate">{{ event.name }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-calendar-day w-4" />
                            <span>{{ formatDateRange(event.start, event.end) }}</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell w-4" />
                            <span>Crew an Bord: {{ $d(event.start, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-bell-slash w-4" />
                            <span>Crew von Bord: {{ $d(event.end, DateTimeFormat.hh_mm) }} Uhr</span>
                        </p>
                        <p class="flex items-center space-x-4">
                            <i class="fa-solid fa-users w-4" />
                            <span v-if="event.state === EventState.OpenForSignup"> {{ event.registrations.length }} Anmeldungen </span>
                            <span v-else-if="event.assignedUserCount && waitingListCount">
                                {{ event.assignedUserCount }} Crew, {{ waitingListCount }} Warteliste
                            </span>
                            <span v-else-if="event.assignedUserCount"> {{ event.assignedUserCount }} Crew </span>
                            <span v-else> {{ event.registrations.length }} Anmeldungen </span>
                        </p>
                        <p v-if="event.description" class="flex items-center space-x-4">
                            <i class="fa-solid fa-info-circle w-4" />
                            <span>{{ event.description }} </span>
                        </p>
                    </div>
                </section>

                <!-- route -->
                <section class="pt-4 md:col-start-2">
                    <h2 class="mb-2 font-bold text-secondary">
                        <template v-if="event.locations.length === 1">Ort</template>
                        <template v-else>Route</template>
                    </h2>
                    <div class="space-y-1 rounded-2xl bg-surface-container-low p-4">
                        <p v-if="event.locations.length === 0" class="text-sm">
                            Für diese Reise wurde noch keine Reiseroute bekannt gegeben. Sobald diese Informationen verfügbar sind, kannst
                            du sie hier sehen.
                        </p>

                        <div v-else class="relative -ml-4">
                            <div class="absolute bottom-4 left-0 top-4 flex w-12 justify-center">
                                <div class="border-r-2 border-dashed border-current"></div>
                            </div>
                            <div
                                v-for="(location, index) in event.locations"
                                :key="index"
                                class="relative mb-4 flex items-center last:mb-0"
                            >
                                <div class="flex w-12 flex-col items-center self-stretch">
                                    <div
                                        class="-mt-1 flex h-7 w-7 items-center justify-center rounded-full border-current bg-surface-container-low"
                                    >
                                        <i class="fa-solid text-sm" :class="location.icon"></i>
                                    </div>
                                    <div v-if="index === event.locations.length - 1" class="w-full flex-1 bg-surface-container-low"></div>
                                </div>
                                <div class="w-0 flex-grow">
                                    <h3 class="mb-1 flex items-center justify-between space-x-2">
                                        <span>{{ location.name }}</span>
                                        <ContextMenuButton v-if="location.information">
                                            <template #icon>
                                                <i class="fa-solid fa-info-circle text-primary text-opacity-75 hover:text-opacity-100"></i>
                                            </template>
                                            <template #default>
                                                <div class="overflow-hidden" @click.stop @mouseup.stop>
                                                    <p class="text-sm">
                                                        <VMarkdown :value="location.information" />
                                                    </p>
                                                    <p v-if="location.informationLink">
                                                        <a :href="location.informationLink" class="link">Weitere Informationen</a>
                                                    </p>
                                                </div>
                                            </template>
                                        </ContextMenuButton>
                                    </h3>
                                    <p v-if="location.eta" class="text-sm">
                                        <span class="inline-block w-8">ETA:</span> {{ $d(location.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}
                                    </p>
                                    <p v-if="location.etd" class="text-sm">
                                        <span class="inline-block w-8">ETD:</span> {{ $d(location.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}
                                    </p>
                                    <p v-if="location.addressLink" class="line-clamp-3 text-sm">
                                        <a :href="location.addressLink" target="_blank" class="link">
                                            {{ location.address || 'Anreiseinformationen' }}
                                            <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i>
                                        </a>
                                    </p>
                                    <p v-else-if="location.address" class="line-clamp-3 text-sm">
                                        {{ location.address }}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- crew -->
                <section class="col-start-1 row-span-6 pt-4 md:row-start-1 md:pt-0">
                    <h2
                        v-if="statesWithHiddenCrew.includes(event.state)"
                        class="mb-2 flex space-x-4 font-bold text-secondary md:mb-6 md:ml-0"
                    >
                        <span>Anmeldungen</span>
                    </h2>
                    <h2 v-else class="mb-2 flex space-x-4 font-bold text-secondary md:mb-6 md:ml-0">
                        <button class="hover:text-primary" :class="{ 'text-primary underline': tab === Tab.Team }" @click="tab = Tab.Team">
                            Crew ({{ event.assignedUserCount }})
                        </button>
                        <button
                            class="hover:text-primary"
                            :class="{ 'text-primary underline': tab === Tab.WaitingList }"
                            @click="tab = Tab.WaitingList"
                        >
                            Warteliste ({{ waitingListCount }})
                        </button>
                    </h2>
                    <div
                        v-if="event.assignedUserCount === 0 && waitingListCount === 0"
                        class="rounded-2xl bg-surface-container-low px-4 md:-mx-4 md:-mt-4"
                    >
                        <div class="flex items-center py-4">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-trophy opacity-75"></i>
                                    <span class="ml-4">Du könntest der erste sein!</span>
                                </h3>
                                <p class="text-sm">
                                    Für diese Reise hat sich bisher noch niemand angemeldet. Du kannst den Anfang machen und dich anmelden.
                                    Alle Anmeldungen werden zuerst auf der Warteliste gesammelt und anschließend wird vom Büro eine Crew
                                    zusammengestellt.
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8">
                            <li v-for="i in 10" :key="i" class="flex items-center space-x-4 rounded-xl py-1">
                                <i class="fa-solid fa-circle text-surface-container-highest"></i>
                                <span class="mx-2 inline-block h-4 w-64 rounded-full bg-surface-container-highest"> </span>
                                <span class="flex-grow"></span>
                                <span class="position h-4 w-16 bg-surface-container-highest"> </span>
                            </li>
                        </ul>
                    </div>
                    <div v-else class="rounded-2xl bg-surface-container-low p-4 md:rounded-none md:bg-transparent md:p-0">
                        <template v-if="tab === Tab.Team">
                            <ul class="space-y-2">
                                <template v-for="it in team" :key="it.slot?.key || ''">
                                    <li class="flex items-center space-x-4">
                                        <i v-if="it.name" class="fa-solid fa-user-circle text-secondary" />
                                        <i v-else class="fa-solid fa-user-circle text-error" />
                                        <RouterLink
                                            v-if="it.user && signedInUser.permissions.includes(Permission.READ_USER_DETAILS)"
                                            :to="{ name: Routes.UserDetails, params: { key: it.user.key } }"
                                            class="truncate hover:text-primary hover:underline"
                                        >
                                            {{ it.name }}
                                        </RouterLink>
                                        <span v-else-if="it.name" class="truncate">{{ it.name }}</span>
                                        <span v-else-if="it.user?.key" class="italic text-error"> Unbekannter Nutzer </span>
                                        <span v-else class="truncate italic text-error text-opacity-60">Noch nicht besetzt</span>
                                        <span class="flex-grow"></span>
                                        <span
                                            :style="{ background: it.position.color }"
                                            class="position ml-auto text-xs"
                                            :class="{ 'opacity-50': !it.registration }"
                                        >
                                            {{ it.position.name }}
                                        </span>
                                    </li>
                                </template>
                            </ul>
                        </template>
                        <template v-else-if="tab === Tab.WaitingList">
                            <ul class="space-y-2">
                                <li v-for="(it, index) in waitingList" :key="index" class="flex items-center justify-between space-x-4">
                                    <i class="fa-solid fa-user-circle text-secondary" />
                                    <RouterLink
                                        v-if="it.user && signedInUser.permissions.includes(Permission.READ_USER_DETAILS)"
                                        :to="{ name: Routes.UserDetails, params: { key: it.user.key } }"
                                        class="flex-grow truncate hover:text-primary hover:underline"
                                    >
                                        {{ it.name }}
                                    </RouterLink>
                                    <span v-else-if="it.name" class="flex-grow truncate">{{ it.name }}</span>
                                    <span :style="{ background: it.position.color }" class="position text-xs">
                                        {{ it.position.name }}
                                    </span>
                                </li>
                            </ul>
                            <div v-if="waitingList.length === 0" class="-mx-4 -mt-4 rounded-xl bg-surface-container-low p-4 text-sm">
                                <p v-if="statesWithHiddenCrew.includes(event.state)">Für diese Reise gibt es noch keine Anmeldungen.</p>
                                <p v-else>Für diese Reise gibt es aktuell keine Anmeldungen auf der Warteliste.</p>
                            </div>
                        </template>
                    </div>
                </section>
            </div>
        </template>
        <template v-if="event && signedInUser.permissions.includes(Permission.WRITE_OWN_REGISTRATIONS)" #primary-button>
            <AsyncButton
                v-if="event.signedInUserAssignedSlot"
                class="btn-danger"
                :disabled="!event.canSignedInUserLeave"
                :action="() => leaveEvent()"
            >
                <template #icon><i class="fa-solid fa-cancel" /></template>
                <template #label>Reise absagen</template>
            </AsyncButton>
            <AsyncButton
                v-else-if="event.signedInUserRegistration"
                class="btn-danger"
                :disabled="!event.canSignedInUserLeave"
                :action="() => leaveEvent()"
            >
                <template #icon>
                    <i class="fa-solid fa-user-minus" />
                </template>
                <template #label> Warteliste verlassen </template>
            </AsyncButton>
            <div v-else-if="signedInUser.positions.length > 1" class="btn-split">
                <AsyncButton class="btn-primary max-w-64 sm:max-w-80" :disabled="!event.canSignedInUserJoin" :action="() => joinEvent()">
                    <template #icon>
                        <i class="fa-solid fa-user-plus" />
                    </template>
                    <template #label> Anmelden als {{ positions.get(signedInUser.positions[0]).name }} </template>
                </AsyncButton>
                <button
                    v-if="signedInUser.positions.length > 1"
                    class="btn-primary"
                    :disabled="!event.canSignedInUserJoin"
                    @click="choosePositionAndJoinEvent(event)"
                >
                    <i class="fa-solid fa-chevron-down" />
                </button>
            </div>
            <AsyncButton v-else class="btn-primary max-w-80" :disabled="!event.canSignedInUserJoin" :action="() => joinEvent()">
                <template #icon>
                    <i class="fa-solid fa-user-plus" />
                </template>
                <template #label>
                    <span class="truncate text-left"> Anmelden als {{ positions.get(signedInUser.positions[0]).name }} </span>
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
            <template v-if="event.signedInUserRegistration">
                <li class="context-menu-item" @click="editUserRegistration()">
                    <i class="fa-solid fa-edit" />
                    <span>Anmeldung bearbeiten</span>
                </li>
                <li class="context-menu-item" @click="editUserRegistration()">
                    <i class="fa-solid fa-note-sticky" />
                    <span>Notiz fürs Büro hinzufügen</span>
                </li>
            </template>
            <li class="permission-read-user-details context-menu-item" @click="eventUseCase.downloadImoList(event)">
                <i class="fa-solid fa-clipboard-user" />
                <span>IMO Liste generieren</span>
            </li>
            <li class="permission-read-user-details context-menu-item" @click="eventUseCase.downloadConsumptionList(event)">
                <i class="fa-solid fa-beer-mug-empty" />
                <span>Verzehrliste generieren</span>
            </li>
            <li class="permission-read-user-details context-menu-item" @click="eventUseCase.downloadCaptainList(event)">
                <i class="fa-solid fa-file-medical" />
                <span>Kapitänsliste generieren</span>
            </li>
            <li class="permission-write-events">
                <RouterLink :to="{ name: Routes.EventEdit }" class="context-menu-item">
                    <i class="fa-solid fa-edit" />
                    <span>Reise bearbeiten</span>
                </RouterLink>
            </li>
        </template>
    </DetailsPage>
    <VConfirmationDialog ref="confirmationDialog" />
    <PositionSelectDlg ref="positionSelectDialog" />
    <RegistrationEditDlg v-if="event" ref="editRegistrationDialog" :event="event" />
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DateTimeFormat } from '@/common/date';
import type { Event, PositionKey, Registration, SignedInUser } from '@/domain';
import { EventState, Permission } from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot.ts';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VSuccess } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';
import { ContextMenuButton } from '@/ui/components/common';
import { AsyncButton, VInfo, VWarning } from '@/ui/components/common';
import VMarkdown from '@/ui/components/common/VMarkdown.vue';
import PositionSelectDlg from '@/ui/components/events/PositionSelectDlg.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { Routes } from '@/ui/views/Routes.ts';
import RegistrationEditDlg from '@/ui/views/events/details/RegistrationEditDlg.vue';

enum Tab {
    Team = 'team',
    WaitingList = 'waitinglist',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const router = useRouter();
const positions = usePositions();
const authUseCase = useAuthUseCase();
const eventUseCase = useEventUseCase();

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const statesWithHiddenCrew = [EventState.OpenForSignup, EventState.Draft];
const event = ref<Event | null>(null);
const tab = ref<Tab>(Tab.Team);

const waitingList = ref<ResolvedRegistrationSlot[]>([]);
const team = ref<ResolvedRegistrationSlot[]>([]);

const positionSelectDialog = ref<Dialog<unknown, PositionKey> | null>(null);
const editRegistrationDialog = ref<Dialog<Registration, Registration | undefined> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);

const waitingListCount = computed<number>(() => {
    if (!event.value) return 0;
    return event.value.registrations.length - event.value.assignedUserCount;
});

function init(): void {
    fetchEvent();
    watch(event, onEventChanged);
}

async function fetchEvent(): Promise<void> {
    try {
        const key = route.params.key as string;
        const year = parseInt(route.params.year as string, 10) || new Date().getFullYear();
        event.value = await eventUseCase.getEventByKey(year, key);
    } catch (e) {
        console.error(e);
        await router.push({ name: Routes.EventsCalendar });
    }
}

async function onEventChanged(): Promise<void> {
    emit('update:tab-title', event.value?.name || '');
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
}

async function choosePositionAndJoinEvent(evt: Event): Promise<void> {
    const position = await positionSelectDialog.value?.open();
    if (position) {
        // default position might have changed
        signedInUser.value = authUseCase.getSignedInUser();
        event.value = await eventUseCase.joinEvent(evt, position);
    }
}

async function joinEvent(): Promise<void> {
    if (event.value) {
        event.value = await eventUseCase.joinEvent(event.value, signedInUser.value.positions[0]);
    }
}

async function leaveEvent(): Promise<void> {
    if (event.value) {
        if (event.value.signedInUserAssignedSlot) {
            const confirmed = await confirmationDialog.value?.open({
                title: 'Teilnahme absagen?',
                message: `Bist du sicher, das du deine Teilnahme an der Reise ${event.value.name} absagen möchtest?
                    Du hast dann keinen Anspruch mehr auf eine Teilname an der Reise und dein Platz wird an eine
                    andere Person vergeben.`,
                submit: 'Reise absagen',
                danger: true,
            });
            if (!confirmed) {
                return;
            }
        }
        event.value = await eventUseCase.leaveEvent(event.value);
    }
}

async function editUserRegistration(): Promise<void> {
    if (event.value && event.value.signedInUserRegistration) {
        const updatedRegistration = await editRegistrationDialog.value?.open(event.value.signedInUserRegistration);
        if (updatedRegistration) {
            await eventUseCase.updateRegistration(event.value, updatedRegistration);
            await fetchTeam(event.value);
        }
    }
}

init();
</script>
