<template>
    <div class="xl:overflow-auto">
        <div class="xs:px-8 flex px-4 pb-8 md:px-16 xl:px-20">
            <div class="w-full xl:max-w-2xl">
                <div class="bg-surface sticky top-12 z-10 -mx-6 flex h-14 justify-end pt-4 pb-2 xl:top-0 xl:h-16 xl:pt-8"></div>
                <div v-if="loading" class="-mt-10" data-test-id="loading">
                    <div class="pb-8">
                        <div class="pointer-events-none sticky top-16 z-10 flex pt-2 pb-1 xl:top-8">
                            <h2 class="text-secondary inline-block font-bold">{{ $t('views.home.loading-events') }}</h2>
                        </div>
                        <ul class="xs:-mx-4 max-w-xl">
                            <li v-for="i in 3" :key="i" class="mt-4">
                                <EventCard />
                            </li>
                        </ul>
                    </div>
                </div>
                <div
                    v-else-if="events.length === 0"
                    class="bg-surface-container xs:-mx-4 relative z-10 -mt-10 rounded-2xl p-4"
                    data-test-id="no-events"
                >
                    <h3 class="text-secondary mb-2 font-bold">{{ $t('views.home.no-upcoming-events-title') }}</h3>
                    <p class="mb-4">
                        {{ $t('views.home.no-upcoming-events-description') }}
                    </p>
                    <div class="flex">
                        <RouterLink :to="{ name: Routes.EventsList }" class="btn-primary">
                            <i class="fa-solid fa-binoculars"></i>
                            <span>{{ $t('views.home.find-next-event') }}</span>
                        </RouterLink>
                    </div>
                </div>
                <div v-else class="-mt-10" data-test-id="events">
                    <div v-for="entry in eventsByMonth.entries()" :key="entry[0]" class="pb-8">
                        <div class="pointer-events-none sticky top-16 z-10 flex pt-2 pb-1 xl:top-8">
                            <h2 class="text-secondary inline-block">
                                {{ entry[0] }}
                            </h2>
                        </div>
                        <ul class="xs:-mx-4 max-w-xl">
                            <li v-for="event in entry[1]" :key="event.key" class="mt-4">
                                <EventCard :event="event" />
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthUseCase, useEventUseCase } from '@/application';
import { DateTimeFormat, Month } from '@/common/date';
import type { Event } from '@/domain';
import { Permission } from '@/domain';
import { useEventService } from '@/domain/services';
import { Routes } from '@/ui/views/Routes';
import EventCard from '@/ui/views/home/EventCard.vue';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const router = useRouter();
const eventService = useEventService();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const i18n = useI18n();
const user = authUseCase.getSignedInUser();

const events = ref<Event[]>([]);
const loading = ref<boolean>(true);
const searchterm = ref<string>('');

const filteredEvents = computed<Event[]>(() => events.value.filter((it) => eventService.doesEventMatchFilter(it, searchterm.value)));

const eventsByMonth = computed<Map<string, Event[]>>(() =>
    filteredEvents.value.reduce((map, it) => {
        let groupName = i18n.d(it.start, DateTimeFormat.MMMM_YYYY);
        if (isThisMonth(it.start)) {
            groupName = i18n.t('views.home.this-month');
        } else if (isNextMonth(it.start)) {
            groupName = i18n.t('views.home.next-month');
        }

        const groupedEvents = map.get(groupName) || [];
        groupedEvents.push(it);
        map.set(groupName, groupedEvents);
        return map;
    }, new Map<string, Event[]>())
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
    emit('update:tab-title', i18n.t('views.home.tab-title'));
    if (user.permissions.includes(Permission.READ_EVENTS)) {
        fetchEvents();
    } else {
        router.push({ name: Routes.Onboarding });
    }
}

async function fetchEvents(): Promise<void> {
    events.value = await eventUseCase.getFutureEventsByUser(user.key);
    loading.value = false;
}

init();
</script>
