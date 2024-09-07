<template>
    <div class="xl:overflow-auto">
        <div class="flex px-8 pb-8 md:px-16 xl:px-20">
            <div class="w-full xl:max-w-2xl">
                <div
                    class="sticky top-12 z-10 -mx-4 flex h-14 justify-end bg-primary-50 pb-2 pt-4 xl:top-0 xl:h-16 xl:pt-8"
                ></div>
                <div v-if="events.length === 0" class="-mx-4 rounded-2xl bg-primary-100 p-4">
                    <h3 class="mb-2">Keine zukünftigen Reisen</h3>
                    <p>Du hast dich bisher noch für keine Reise angemeldet</p>
                </div>
                <div v-else class="-mt-10">
                    <div v-for="entry in eventsByMonth.entries()" :key="entry[0]" class="pb-8">
                        <div class="pointer-events-none sticky top-16 z-10 flex pb-1 pt-2 xl:top-8">
                            <h2 class="inline-block text-primary-800 text-opacity-50">
                                {{ entry[0] }}
                            </h2>
                        </div>
                        <ul class="-mx-4 max-w-xl">
                            <li v-for="event in entry[1]" :key="event.key" class="mt-4">
                                <EventCard :event="event" />
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="hidden w-96 flex-grow p-16 xl:block">
                <div class="sticky top-16 z-10 hidden">
                    <div class="ml-auto max-w-lg">
                        <!--                        <h2 class="text-sm pl-8 mb-4">News</h2>-->
                        <div v-for="i in 3" :key="i" class="mb-4 rounded-2xl bg-gray-200 p-8">
                            <div>
                                <h3>Newspost #{{ i }}</h3>
                                <p class="line-clamp-3">
                                    Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod
                                    tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero
                                    eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea
                                    takimata sanctus est Lorem ipsum dolor sit amet.
                                </p>
                            </div>
                        </div>
                    </div>
                    <!-- https://icons8.de/illustrations/style--daily -->
                    <!--                    <img src="https://ouch-cdn2.icons8.com/2SYnU_bK_BVVRKAxE-1J9RjgVuuBGw0cblRg9fmIXVQ/rs:fit:570:456/extend:false/wm:1:re:0:0:0.8/wmid:ouch/czM6Ly9pY29uczgu/b3VjaC1wcm9kLmFz/c2V0cy9zdmcvNTMz/LzQxODc3YTAxLTYx/ZTUtNGQwMi04MDlm/LTM4ODI4ZDc1OWUz/Yi5zdmc.png">-->
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat, Month } from '@/common/date';
import type { Event } from '@/domain';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import EventCard from '@/ui/views/home/EventCard.vue';

const eventService = useEventService();
const eventUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const i18n = useI18n();
const user = authUseCase.getSignedInUser();

const events = ref<Event[]>([]);
const searchterm = ref<string>('');

const filteredEvents = computed<Event[]>(() =>
    events.value.filter((it) => eventService.doesEventMatchFilter(it, searchterm.value))
);

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
    fetchEvents();
}

async function fetchEvents(): Promise<void> {
    events.value = await eventUseCase.getFutureEventsByUser(user.key);
}

init();
</script>
