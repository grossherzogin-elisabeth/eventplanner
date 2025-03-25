<template>
    <div class="h-full overflow-y-auto px-8 pb-8 pt-8 md:px-16 xl:px-20">
        <div class="w-full max-w-2xl">
            <div v-if="event">
                <h1 class="mb-4">Moin liebes Crewmitglied,</h1>
                <p class="mb-8 text-sm sm:text-base">
                    In {{ daysUntilStart }} Tagen startet die Reise {{ event.name }}, bei der du als Crew eingeplant bist. Bitte bestätige
                    deine Teilnahme bis <b>spätestens 7 Tage vor Reisebeginn</b>, damit das Büro noch genügend Zeit hat, um gegebenenfalls
                    einen Ersatz für dich zu finden.
                </p>

                <div class="mb-8">
                    <table class="block sm:table">
                        <tbody>
                            <tr class="mb-2 block sm:table-row">
                                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Reise:</td>
                                <td class="block py-1 font-bold sm:table-cell">{{ event.name }}</td>
                            </tr>
                            <tr class="mb-2 block sm:table-row">
                                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Crew an Bord:</td>
                                <td class="block py-1 font-bold sm:table-cell">
                                    {{ $d(event.start, DateTimeFormat.DD_MM_YYYY) }}
                                    {{ $d(event.start, DateTimeFormat.hh_mm) }}
                                </td>
                            </tr>
                            <tr class="mb-2 block sm:table-row">
                                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Crew von Bord:</td>
                                <td class="block py-1 font-bold sm:table-cell">
                                    {{ $d(event.end, DateTimeFormat.DD_MM_YYYY) }}
                                    {{ $d(event.end, DateTimeFormat.hh_mm) }}
                                </td>
                            </tr>
                            <tr class="mb-2 block sm:table-row">
                                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Geplante Route:</td>
                                <td class="block py-1 font-bold sm:table-cell">
                                    {{ event.locations.map((l) => l.name).join(' - ') }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div v-if="registrationState === State.REGISTRATION_UNCONFIRMED" class="">
                <div class="hidden items-center gap-4 sm:flex">
                    <button class="btn-primary" @click="confirm()">
                        <i class="fa-solid fa-check"></i>
                        <span class="py-2 sm:py-0">Ja, ich nehme teil</span>
                    </button>
                    <button class="btn-danger" @click="decline()">
                        <i class="fa-solid fa-xmark"></i>
                        <span class="py-2 sm:py-0">Ich muss leider absagen</span>
                    </button>
                </div>
                <div class="ms:hidden h-20"></div>
                <div class="fixed bottom-0 left-0 right-0 flex items-stretch gap-4 bg-surface px-4 py-2 sm:hidden">
                    <button class="btn-primary w-1/2" @click="confirm()">
                        <i class="fa-solid fa-check"></i>
                        <span class="whitespace-normal py-2 text-sm">Ja, ich nehme teil</span>
                    </button>
                    <button class="btn-danger w-1/2" @click="decline()">
                        <i class="fa-solid fa-xmark"></i>
                        <span class="whitespace-normal py-2 text-sm">Ich muss leider absagen</span>
                    </button>
                </div>
            </div>
            <div v-else-if="registrationState === State.REGISTRATION_WAS_CANCELED && !signedInUserKey">
                <div class="-mx-4 mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">Anmeldung nicht gefunden</span>
                    </p>
                    <p class="mb-4">
                        Wir konnten keine Anmeldung für diesen Link finden. Das kann daran liegen, dass du die Reise bereits abgesagt hast,
                        oder dein Link ungültig ist. Falls du bereits abgesagt hast, aber doch an der Reise teilnehmen möchtest, oder du
                        Probleme mit der Teilnahmebestätigung hast, melde dich bitte telefonisch im Büro.
                    </p>
                </div>
            </div>
            <div v-else-if="registrationState === State.REGISTRATION_WAS_CANCELED">
                <div class="-mx-4 mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">Anmeldung wurde bereits abgesagt</span>
                    </p>
                    <p class="mb-4">
                        Deine Anmeldung wurde bereits abgesagt und du stehst nicht mehr auf der Crew Liste. Falls du doch an der Reise
                        teilnehmen möchtest, melde dich bitte telefonisch im Büro.
                    </p>
                </div>
            </div>
            <div v-else-if="registrationState === State.SIGNED_IN_USER_HAS_NO_REGISTRATION">
                <div class="-mx-4 mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">Keine Anmeldung zur Reise gefunden</span>
                    </p>
                    <p class="mb-4">
                        Leider konnten wir für dich keine Anmeldung zu dieser Reise finden. Das kann daran liegen, dass du deine Teilnahme
                        bereits abgesagt hast, oder das du dich mit einem anderen Account angemeldet hast. Wenn du bereits abgesagt hast und
                        doch teilnehmen möchtest, melde dich bitte telefonisch im Büro.
                    </p>
                </div>
            </div>
            <div v-else-if="registrationState === State.REGISTRATION_BELONGS_TO_OTHER_USER">
                <div class="-mx-4 mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">Ungültiger Link</span>
                    </p>
                    <p class="mb-4">
                        Der Link, mit dem du auf diese Seite gekommen bist, ist ungültig oder gehört zu einem anderen Nutzer. Du kannst nur
                        die Teilname an deinen eigenen Reisen bestätigen. Bitte überprüfe, ob die Email Adresse deines Accounts, mit dem du
                        dich angemeldet hast mit der Email Adresse übereinstimmt, über die du diesen Link erhalten hast. Wenn du weiterhin
                        Probleme bei der Bestätigung der Reise hast, melde dich gerne telefonisch im Büro.
                    </p>
                </div>
            </div>
            <div
                v-else-if="registrationState === State.REGISTRATION_WAS_CONFIRMED"
                class="-mx-4 mb-8 rounded-2xl bg-green-container p-4 font-bold text-ongreen-container"
            >
                <p class="mb-4 text-lg">
                    <i class="fa-solid fa-check"></i>
                    <span class="ml-4">Teilnahme bestätigt</span>
                </p>
                <p class="mb-4">
                    Vielen Dank für deine Rückmeldung. Deine Teilnahme an der Reise wurde als bestätigt markiert. Bitte denk daran, deine
                    Ausweisdokumente und Qualifikationsnachweise im Original mitzuführen. Wir wünschen dir und der gesamten Crew einen
                    angenehmen Törn und immer eine handbreit Wasser unterm Kiel!
                </p>
            </div>
            <div
                v-else-if="registrationState === State.REGISTRATION_WAS_JUST_CANCELED"
                class="-mx-4 mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container"
            >
                <p class="mb-4 text-lg">
                    <i class="fa-solid fa-xmark"></i>
                    <span class="ml-4">Reise abgesagt</span>
                </p>
                <p class="mb-4">
                    Vielen Dank für deine Rückmeldung. Schade, dass du nicht teilnehmen kannst. Deine Teilnahme an der Reise wurde
                    storniert. Bitte versuche kurzfristige Absagen soweit möglich zu vermeiden, da es dann schwierig ist noch einen Ersatz
                    für dich zu finden.
                </p>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { DateTimeFormat } from '@/common/date';
import type { Event, EventKey, RegistrationKey } from '@/domain';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';

enum State {
    REGISTRATION_UNCONFIRMED = 'registration_unconfirmed',
    REGISTRATION_WAS_CANCELED = 'registration_canceled',
    REGISTRATION_WAS_JUST_CANCELED = 'registration_just_canceled',
    REGISTRATION_WAS_CONFIRMED = 'registration_confirmed',
    SIGNED_IN_USER_HAS_NO_REGISTRATION = 'user_has_no_registration',
    REGISTRATION_BELONGS_TO_OTHER_USER = 'registration_belongs_to_other_user',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const eventUseCase = useEventUseCase();
const auth = useAuthUseCase();
const signedInUserKey = auth.getSignedInUser().key;

const event = ref<Event | null>(null);
const registrationState = ref<State | null>(null);

const daysUntilStart = computed<number>(() => {
    if (!event.value) {
        return 0;
    }
    const nowSec = new Date().getTime() / 1000;
    const startSec = event.value.start.getTime() / 1000;
    const diffSec = startSec - nowSec;
    return Math.floor(diffSec / 60 / 60 / 24);
});

function init(): void {
    emit('update:tab-title', 'Teilnahme bestätigen');
    fetchEvent();
}

async function fetchEvent(): Promise<void> {
    try {
        const eventKey = route.params.eventKey as EventKey;
        const accessKey = route.query.accessKey as string;
        event.value = await eventUseCase.getEventByAccessKey(eventKey, accessKey);

        registrationState.value = State.REGISTRATION_UNCONFIRMED;
        const registration = event.value?.registrations.find((it) => it.key === route.params.registrationKey);
        if (!registration) {
            registrationState.value = State.REGISTRATION_WAS_CANCELED;
        } else if (signedInUserKey && registration.userKey !== signedInUserKey) {
            registrationState.value = State.REGISTRATION_BELONGS_TO_OTHER_USER;
        } else if (registration.confirmed) {
            registrationState.value = State.REGISTRATION_WAS_CONFIRMED;
        }
    } catch (e: unknown) {
        if ((e as Response).status === 404) {
            registrationState.value = State.REGISTRATION_WAS_CANCELED;
        }
    }
}

async function confirm(): Promise<void> {
    const eventKey = route.params.eventKey as EventKey;
    const registrationKey = route.params.registrationKey as RegistrationKey;
    const accessKey = route.query.accessKey as string;
    await eventUseCase.confirmParticipation(eventKey, registrationKey, accessKey);
    registrationState.value = State.REGISTRATION_WAS_CONFIRMED;
}

async function decline(): Promise<void> {
    const eventKey = route.params.eventKey as EventKey;
    const registrationKey = route.params.registrationKey as RegistrationKey;
    const accessKey = route.query.accessKey as string;
    await eventUseCase.declineParticipation(eventKey, registrationKey, accessKey);
    registrationState.value = State.REGISTRATION_WAS_JUST_CANCELED;
}

init();
</script>
