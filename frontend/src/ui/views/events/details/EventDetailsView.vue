<template>
    <DetailsPage :back-to="{ name: Routes.EventsCalendar }" :class="$attrs.class">
        <template #header>
            {{ event?.name }}
        </template>
        <template #content>
            <div v-if="event" class="px-8 pb-8 pt-6 md:px-16 xl:px-20">
                <div class="space-y-4 md:grid md:grid-cols-2 md:gap-x-20 md:gap-y-4 md:space-y-0 xl:max-w-5xl">
                    <!-- state info banner -->
                    <section v-if="event.state === EventState.OpenForSignup" class="col-start-2">
                        <VInfo clamp>
                            Diese Reise befindet sich noch in der Planung. Eine Anmeldung garantiert keine Teilnahme an der Reise! Sobald
                            die Crewplanung veröffentlicht wird, wirst du per Email darüber informiert.
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
                    <section v-else-if="openPositions.length > 0" class="sticky left-4 right-4 top-14 z-10 col-start-2 md:static">
                        <VWarning>
                            Für diese Reise wird noch Crew für die folgenden Positionen gesucht:
                            {{ openPositions.map((it) => it.name).join(', ') }}
                        </VWarning>
                    </section>

                    <EventDetailsCard :event="event" class="pt-4 md:col-start-2" />
                    <EventRouteCard :event="event" class="pt-4 md:col-start-2" />
                    <EventParticipantsCard :event="event" class="col-start-1 row-span-6 pt-4 md:row-start-1 md:pt-0" />
                </div>
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
            <button v-else class="btn-primary max-w-80" :disabled="!event.canSignedInUserJoin" @click="joinEvent()">
                <i class="fa-solid fa-user-plus" />
                <span class="truncate text-left"> Anmelden </span>
            </button>
        </template>
        <template v-if="event" #secondary-buttons>
            <RouterLink
                v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)"
                :to="{ name: Routes.EventEdit }"
                class="btn-secondary"
            >
                <i class="fa-solid fa-drafting-compass" />
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
                <li
                    class="context-menu-item"
                    :class="{ disabled: !event.canSignedInUserUpdateRegistration }"
                    @click="editUserRegistration()"
                >
                    <i class="fa-solid fa-edit" />
                    <span>Anmeldung bearbeiten</span>
                </li>
                <li
                    class="context-menu-item"
                    :class="{ disabled: !event.canSignedInUserUpdateRegistration }"
                    @click="editUserRegistration()"
                >
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
                    <i class="fa-solid fa-drafting-compass" />
                    <span>Reise bearbeiten</span>
                </RouterLink>
            </li>
        </template>
    </DetailsPage>
    <VConfirmationDialog ref="confirmationDialog" />
    <RegistrationDetailsSheet ref="registrationSheet" />
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { Event, Position, Registration, SignedInUser } from '@/domain';
import { EventState, Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VSuccess } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';
import { AsyncButton, VInfo, VWarning } from '@/ui/components/common';
import EventDetailsCard from '@/ui/components/events/EventDetailsCard.vue';
import EventParticipantsCard from '@/ui/components/events/EventParticipantsCard.vue';
import EventRouteCard from '@/ui/components/events/EventRouteCard.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import RegistrationDetailsSheet from '@/ui/components/sheets/RegistrationDetailsSheet.vue';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { Routes } from '@/ui/views/Routes.ts';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const router = useRouter();
const positions = usePositions();
const eventService = useEventService();
const authUseCase = useAuthUseCase();
const eventUseCase = useEventUseCase();

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const event = ref<Event | null>(null);

const registrationSheet = ref<Dialog<{ registration?: Registration; event: Event }, Registration | undefined> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);

const openPositions = computed<Position[]>(() => {
    if (!event.value) {
        return [];
    }
    const openRequiredSlots = eventService.getOpenSlots(event.value);
    return positions.all.value
        .map((position) => ({
            position: position,
            count: openRequiredSlots.filter((slot) => slot.positionKeys[0] === position.key).length,
        }))
        .filter((pos) => pos.count > 0)
        .map((it) => it.position);
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
}

async function joinEvent(): Promise<void> {
    if (!event.value) {
        return;
    }
    const registration = await registrationSheet.value?.open({
        event: event.value,
        registration: undefined,
    });
    if (event.value && registration) {
        event.value = await eventUseCase.joinEvent(event.value, registration);
    }
}

async function leaveEvent(): Promise<void> {
    if (event.value) {
        if (event.value.signedInUserAssignedSlot) {
            const confirmed = await confirmationDialog.value?.open({
                title: 'Teilnahme absagen?',
                message: `Bist du sicher, dass du deine Teilnahme an der Reise ${event.value.name} absagen möchtest?
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
        const updatedRegistration = await registrationSheet.value?.open({
            event: event.value,
            registration: event.value.signedInUserRegistration,
        });
        if (updatedRegistration) {
            await eventUseCase.updateRegistration(event.value, updatedRegistration);
            await fetchEvent();
        }
    }
}

init();
</script>
