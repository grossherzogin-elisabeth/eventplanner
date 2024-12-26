<template>
    <div class="h-full overflow-y-auto px-8 pb-8 pt-8 md:px-16 xl:px-20">
        <div v-if="event" class="w-full max-w-2xl">
            <h1 v-if="signedInUser?.firstname" class="mb-4">Moin {{ signedInUser.firstname }},</h1>
            <h1 v-else class="mb-4">Moin liebes Crewmitglied,</h1>
            <p class="mb-8 text-sm sm:text-base">
                In {{ daysUntilStart }} Tagen startet die Reise {{ event.name }}, bei der du als Crew eingeplant bist. Bitte bestätige deine
                Teilnahme bis <b>spätestens 7 Tage vor Reisebeginn</b>, damit das Büro noch genügend Zeit hat, um gegebenenfalls einen
                Ersatz für dich zu finden.
            </p>

            <div v-if="showDetails" class="mb-8">
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

            <div v-if="confirmed === null" class="">
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
                    <button class="btn-primary w-1/2" @click="confirmed = true">
                        <i class="fa-solid fa-check"></i>
                        <span class="whitespace-normal py-2 text-sm">Ja, ich nehme teil</span>
                    </button>
                    <button class="btn-danger w-1/2" @click="confirmed = false">
                        <i class="fa-solid fa-xmark"></i>
                        <span class="whitespace-normal py-2 text-sm">Ich muss leider absagen</span>
                    </button>
                </div>
            </div>

            <div v-if="confirmed === true" class="-mx-4 mb-8 rounded-2xl bg-green-container p-4 font-bold text-ongreen-container">
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
            <div v-else-if="confirmed === false" class="-mx-4 mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container">
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
        <div v-else-if="fetchError?.status === 404">
            <div class="-mx-4 mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container">
                <p class="mb-4 text-lg">
                    <i class="fa-solid fa-xmark"></i>
                    <span class="ml-4">Anmeldung nicht gefunden</span>
                </p>
                <p class="mb-4">
                    Leider konnten wir für deinen Link keine Anmeldung zu einer Reise finden. Das kann daran liegen, dass du deine Teilnahme
                    bereits abgesagt hast, oder das dein Link falsch ist. Bitte überprüfe den Link aus der Email und versuche es erneut,
                    falls du noch nicht zu oder abgesagt hast. Wenn du bereits abgesagt hast und doch teilnehmen möchtest, melde dich bitte
                    telefonisch im Büro.
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
import { useEventService } from '@/ui/composables/Domain.ts';

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const eventService = useEventService();
const eventUseCase = useEventUseCase();
const auth = useAuthUseCase();
const signedInUser = auth.getSignedInUser();

const confirmed = ref<boolean | null>(null);
const showDetails = ref<boolean>(true);
const event = ref<Event | null>(null);
const fetchError = ref<Response | null>(null);

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
    emit('update:title', 'Teilnahme bestätigen');
    fetchEvent();
}

async function fetchEvent(): Promise<void> {
    try {
        console.log(signedInUser);
        const eventKey = route.params.eventKey as EventKey;
        const accessKey = route.query.accessKey as string;
        event.value = await eventUseCase.getEventByAccessKey(eventKey, accessKey);
        if (signedInUser && !eventService.findRegistration(event.value, signedInUser.key, undefined)) {
            confirmed.value = false;
        }
    } catch (e: unknown) {
        fetchError.value = e as Response;
    }
}

async function confirm(): Promise<void> {
    const eventKey = route.params.eventKey as EventKey;
    const registrationKey = route.params.registrationKey as RegistrationKey;
    const accessKey = route.query.accessKey as string;
    await eventUseCase.confirmParticipation(eventKey, registrationKey, accessKey);
    confirmed.value = true;
}

async function decline(): Promise<void> {
    const eventKey = route.params.eventKey as EventKey;
    const registrationKey = route.params.registrationKey as RegistrationKey;
    const accessKey = route.query.accessKey as string;
    await eventUseCase.declineParticipation(eventKey, registrationKey, accessKey);
    confirmed.value = false;
}

init();
</script>
