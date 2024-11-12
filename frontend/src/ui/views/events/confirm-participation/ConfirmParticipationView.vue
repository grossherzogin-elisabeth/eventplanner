<template>
    <div class="h-full overflow-y-auto px-8 pb-8 pt-8 md:px-16 xl:px-20">
        <div class="w-full max-w-2xl">
            <h1 class="mb-4">Hallo Max,</h1>
            <p class="mb-8 text-sm sm:text-base">
                In {{ daysUntilStart }} Tagen startet die Reise {{ event.name }}, bei der du als
                {{ event.signedInUserAssignedPosition }} eingeplant bist. Bitte bestätige deine Teilnahme bis spätestens
                7 Tage vor Reisebeginn, damit das Büro noch genügend Zeit hat, um gegebenenfalls einen Ersatz für dich
                zu finden.
            </p>

            <div v-if="showDetails" class="mb-8">
                <table class="block sm:table">
                    <tbody>
                        <tr class="mb-2 block sm:table-row">
                            <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Name der Reise:</td>
                            <td class="block py-1 font-bold sm:table-cell">{{ event.name }}</td>
                        </tr>
                        <tr class="mb-2 block sm:table-row">
                            <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Crew an Board:</td>
                            <td class="block py-1 font-bold sm:table-cell">
                                {{ $d(event.start, DateTimeFormat.DD_MM_YYYY) }}
                                {{ $d(event.start, DateTimeFormat.hh_mm) }}
                            </td>
                        </tr>
                        <tr class="mb-2 block sm:table-row">
                            <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Crew von Board:</td>
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
                        <tr class="mb-2 block sm:table-row">
                            <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Deine Position:</td>
                            <td class="block py-1 font-bold sm:table-cell">
                                {{ event.signedInUserAssignedPosition }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div v-if="state === null" class="">
                <div class="hidden items-center gap-4 sm:flex">
                    <button class="btn-primary" @click="state = true">
                        <i class="fa-solid fa-check"></i>
                        <span class="py-2 sm:py-0">Ja, ich nehme teil</span>
                    </button>
                    <button class="btn-danger" @click="state = false">
                        <i class="fa-solid fa-xmark"></i>
                        <span class="py-2 sm:py-0">Ich muss leider absagen</span>
                    </button>
                </div>
                <div class="ms:hidden h-20"></div>
                <div class="bg-surface fixed bottom-0 left-0 right-0 flex items-stretch gap-4 px-4 py-2 sm:hidden">
                    <button class="btn-primary w-1/2" @click="state = true">
                        <i class="fa-solid fa-check"></i>
                        <span class="whitespace-normal py-2 text-sm">Ja, ich nehme teil</span>
                    </button>
                    <button class="btn-danger w-1/2" @click="state = false">
                        <i class="fa-solid fa-xmark"></i>
                        <span class="whitespace-normal py-2 text-sm">Ich muss leider absagen</span>
                    </button>
                </div>
            </div>

            <div
                v-if="state === true"
                class="bg-green-container text-ongreen-container -mx-4 mb-8 rounded-2xl p-4 font-bold"
            >
                <p class="mb-4 text-lg">
                    <i class="fa-solid fa-check"></i>
                    <span class="ml-4">Teilnahme bestätigt</span>
                </p>
                <p class="mb-4">
                    Vielen Dank für deine Rückmeldung. Deine Teilnahme an der Reise wurde als bestätigt markiert. Bitte
                    denk daran, deine Ausweisdokumente und Qualifikationsnachweise im Original mitzuführen. Wir wünschen
                    dir und der gesamten Crew einen angenehmen Törn und immer eine handbreit Wasser unterm Kiel!
                </p>
            </div>
            <div
                v-else-if="state === false"
                class="bg-error-container text-onerror-container -mx-4 mb-8 rounded-2xl p-4 font-bold"
            >
                <p class="mb-4 text-lg">
                    <i class="fa-solid fa-xmark"></i>
                    <span class="ml-4">Reise abgesagt</span>
                </p>
                <p class="mb-4">
                    Vielen Dank für deine Rückmeldung. Schade, dass du nicht teilnehmen kannst. Deine Teilnahme an der
                    Reise wurde storniert. Bitte versuche kurzfristige Absagen soweit möglich zu vermeiden, da es dann
                    schwierig ist noch einen Ersatz für dich zu finden.
                </p>
            </div>

            <!--            <div class="mt-16 flex">-->
            <!--                <button class="btn-primary">-->
            <!--                    <i class="fa-solid fa-search"></i>-->
            <!--                    <span>Alle Reisedetails anzeigen</span>-->
            <!--                </button>-->
            <!--            </div>-->

            <div
                v-if="state !== null && false"
                class="bg-blue-container text-onblue-container -mx-4 mb-8 rounded-lg p-4 font-bold"
            >
                <p class="mb-4 text-lg">
                    <i class="fa-solid fa-info"></i>
                    <span class="ml-4">Hast du schon einen Lissi Account?</span>
                </p>
                <p class="mb-4">
                    Mit einem kostenlosen Lissi Account kannst du jederzeit den Status deiner kommenden Reisen einsehen
                    und dich noch einfacher für Reisen an und abmelden.
                </p>
                <div class="flex items-center space-x-2">
                    <RouterLink :to="{ name: Routes.Login }" class="btn-primary">
                        Jetzt anmelden oder registrieren
                    </RouterLink>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { DateTimeFormat, addToDate } from '@/common/date';
import type { Event } from '@/domain';
import { EventState, EventType } from '@/domain';
import { Routes } from '@/ui/views/Routes.ts';

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const state = ref<boolean | null>(null);
const showDetails = ref<boolean>(true);
const event = ref<Event>({
    key: '',
    start: addToDate(new Date(), { days: 14 }),
    end: addToDate(new Date(), { days: 20 }),
    name: 'Sommerreise 2',
    type: EventType.MultiDayEvent,
    assignedUserCount: 0,
    canSignedInUserJoin: false,
    canSignedInUserLeave: false,
    state: EventState.Planned,
    description: '',
    signedInUserWaitingListPosition: undefined,
    signedInUserAssignedPosition: 'Leichtmatrose:in',
    locations: [
        { name: 'Mariehamn', icon: '', order: 1 },
        { name: 'Ostsee', icon: '', order: 2 },
        { name: 'Stettin', icon: '', order: 3 },
    ],
    registrations: [],
    slots: [],
});

const daysUntilStart = computed<number>(() => {
    const nowSec = new Date().getTime() / 1000;
    const startSec = event.value.start.getTime() / 1000;
    const diffSec = startSec - nowSec;
    return Math.floor(diffSec / 60 / 60 / 24);
});

function init(): void {
    emit('update:title', 'Teilnahme bestätigen');
}

init();
</script>
