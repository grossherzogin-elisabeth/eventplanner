<template>
    <div class="xl:overflow-auto">
        <div class="flex px-8 pb-8 md:px-16 xl:px-20">
            <div class="w-full xl:max-w-2xl">
                <div
                    class="sticky top-12 z-10 -mx-4 flex h-14 justify-end bg-surface pb-2 pt-4 xl:top-0 xl:h-16 xl:pt-8"></div>
                <div v-if="loading" class="-mt-10">
                    <div class="pb-8">
                        <div class="pointer-eventDetails-none sticky top-16 z-10 flex pb-1 pt-2 xl:top-8">
                            <h2 class="inline-block font-bold text-secondary">Reisen werden geladen...</h2>
                        </div>
                        <ul class="-mx-4 max-w-xl">
                            <li v-for="i in 3" :key="i" class="mt-4">
                                <EventCard />
                            </li>
                        </ul>
                    </div>
                </div>
                <div v-else-if="eventDetails.length === 0"
                     class="relative z-10 -mx-4 -mt-10 rounded-2xl bg-surface-container p-4">
                    <h3 class="mb-2 font-bold text-secondary">Keine zukünftigen Reisen</h3>
                    <p class="mb-4">
                        Du hast aktuell keine anstehenden Reisen. Du kannst im Kalender oder der Reiseliste nach Reisen
                        suchen und dich für
                        Reisen anmelden, die du gerne mitfahren möchtest. Du wirst dann auf die Warteliste gesetzt und
                        per Email
                        benachrichtigt, wenn du für die Crew der Reise eingeplant wirst.
                    </p>
                    <div class="flex">
                        <RouterLink :to="{ name: Routes.EventsList }" class="btn-primary">
                            <i class="fa-solid fa-binoculars"></i>
                            <span>Finde deine nächste Reise</span>
                        </RouterLink>
                    </div>
                </div>
                <div v-else class="-mt-10">
                    <div v-for="entry in eventsByMonth.entries()" :key="entry[0]" class="pb-8">
                        <div class="pointer-eventDetails-none sticky top-16 z-10 flex pb-1 pt-2 xl:top-8">
                            <h2 class="inline-block text-secondary">
                                {{ entry[0] }}
                            </h2>
                        </div>
                        <ul class="-mx-4 max-w-xl">
                            <li v-for="eventDetails in entry[1]" :key="eventDetails.key" class="mt-4">
                                <EventCard :eventDetails="eventDetails" />
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <!--            <div class="pt-16">-->
            <!--                <MonthOverview />-->
            <!--            </div>-->
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat, Month } from '@/common/date';
import type { Event } from '@/domain';
import { Permission } from '@/domain';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { Routes } from '@/ui/views/Routes';
import EventCard from '@/ui/views/home/EventCard.vue';

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const router = useRouter();
const eventService = useEventService();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const i18n = useI18n();
const user = authUseCase.getSignedInUser();

const eventDetails = ref<Event[]>([]);
const loading = ref<boolean>(true);
const searchterm = ref<string>('');

const filteredEvents = computed<Event[]>(() => eventDetails.value.filter((it) => eventService.doesEventMatchFilter(it, searchterm.value)));

const eventsByMonth = computed<Map<string, Event[]>>(() =>
    filteredEvents.value.reduce((map, it) => {
        let groupName = i18n.d(it.start, DateTimeFormat.MMMM_YYYY);
        if (isThisMonth(it.start)) {
            groupName = 'Diesen Monat';
        } else if (isNextMonth(it.start)) {
            groupName = 'Nächsten Monat';
        }

        const groupedEvents = map.get(groupName) || [];
        groupedEvents.push(it);
        map.set(groupName, groupedEvents);
        return map;
    }, new Map<string, Event[]>()),
);

function isThisMonth(date: Date): boolean {
    const now = new Date();
    return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth();
}

function isNextMonth(date: Date): boolean {
    const now = new Date();
    if (date.getFullYear() === now.getFullYear()) {
        return date.getMonth() === now.getMonth() + 1;
    }
    if (date.getFullYear() === now.getFullYear() + 1) {
        return date.getMonth() === Month.JANUARY && now.getMonth() === Month.DECEMBER;
    }
    return false;
}

function init(): void {
    emit('update:title', 'Meine nächsten Reisen');
    if (user.permissions.includes(Permission.READ_EVENTS)) {
        fetchEvents();
    } else {
        router.push({ name: Routes.Onboarding });
    }
}

async function fetchEvents(): Promise<void> {
    eventDetails.value = await eventUseCase.getFutureEventsByUser(user.key);
    loading.value = false;
}

init();
</script>
