<template>
    <div class="flex flex-1 flex-col bg-surface">
        <div v-if="events" class="flex h-full flex-1 flex-col">
            <div class="relative flex h-full flex-1 items-stretch">
                <div class="absolute left-0 top-0 z-30 hidden w-14 bg-surface pt-0.5 lg:block xl:pt-6">
                    <button class="icon-button ml-5" name="previous" @click="scrollLeft()">
                        <i class="fa-solid fa-chevron-left"></i>
                    </button>
                </div>
                <div class="absolute right-0 top-0 z-30 hidden w-14 bg-surface pt-0.5 lg:block xl:pt-6">
                    <button class="icon-button" name="next" @click="scrollRight()">
                        <i class="fa-solid fa-chevron-right"></i>
                    </button>
                </div>
                <div
                    ref="calendar"
                    :style="calendarStyle"
                    class="calendar"
                    :class="{ 'enable-create': signedInUser.permissions.includes(Permission.WRITE_EVENTS) }"
                >
                    <div v-for="m in months.entries()" :key="m[0]" class="calendar-month">
                        <div class="calendar-header">
                            <span>{{ $t(`month.${m[0]}`) }}</span>
                            <span class="ml-2 sm:hidden">{{ events[0]?.end.getFullYear() }}</span>
                        </div>
                        <div
                            v-for="d in m[1]"
                            :key="d.dayOfMonth"
                            :class="{ weekend: d.isWeekend, holiday: d.isHoliday, today: d.isToday }"
                            class="calendar-day"
                            @mousedown="startCreateEventDrag(d.date)"
                            @mouseover="updateCreateEventDrag(d.date)"
                            @mouseup="stopCreateEventDrag(d.date)"
                        >
                            <div class="calendar-day-label">{{ d.weekday }}</div>
                            <div class="calendar-day-label">{{ d.dayOfMonth }}</div>
                            <div class="relative w-0 flex-grow self-start">
                                <template v-if="d.events.length > 0">
                                    <EventCalendarItem
                                        v-for="evt in d.events"
                                        :key="evt.event.key"
                                        :event="evt.event"
                                        :class="evt.class"
                                        :duration="evt.duration"
                                        :duration-in-month="evt.durationInMonth"
                                        :start="evt.offset"
                                        @update:event="updateEvent"
                                        @click.stop=""
                                        @mousedown.stop=""
                                    />
                                </template>
                                <div v-else-if="createEventFromDate === d.date" class="create-event-overlay">
                                    <span>Neue Reise</span>
                                    <span v-if="calendarStyle['--create-event-days'] > 1" class="text-xs">
                                        {{ calendarStyle['--create-event-days'] }} Tage
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div v-for="i in 31 - m[1].length" :key="i" class="calendar-filler"></div>
                    </div>
                </div>
            </div>
        </div>
        <CreateEventDlg ref="createEventDialog" />
    </div>
</template>

<script lang="ts" setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat, Month, addToDate } from '@/common/date';
import { type Event, EventState, Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import CreateEventDlg from '@/ui/components/events/EventCreateDlg.vue';
import { useAuthUseCase, useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { isHoliday } from 'feiertagejs';
import EventCalendarItem from './EventCalendarItem.vue';

interface CalendarDay {
    date: Date;
    dayOfMonth: number;
    weekday: string;
    isHoliday: boolean;
    isWeekend: boolean;
    isToday: boolean;
    events: CalendarDayEvent[];
}

interface CalendarDayEvent {
    event: Event;
    durationInMonth: number;
    duration: number;
    class: string;
    isContinuation: boolean;
    offset: number;
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const i18n = useI18n();
const eventUseCase = useEventUseCase();
const eventService = useEventService();
const eventAdministrationService = useEventAdministrationUseCase();
const authUseCase = useAuthUseCase();
const signedInUser = authUseCase.getSignedInUser();

const createEventDialog = ref<Dialog<Partial<Event>, Event> | null>(null);
const createEventFromDate = ref<Date | null>(null);
const year = ref<number>(new Date().getFullYear());
const events = ref<Event[]>([]);

const months = ref<Map<Month, CalendarDay[]>>(new Map<Month, CalendarDay[]>());
const calendar = ref<HTMLDivElement | null>(null);
const calendarStyle = ref({
    '--scrollcontainer-width': '100vw',
    '--scrollcontainer-height': '100vh',
    '--create-event-days': 1,
});

function init(): void {
    emit('update:title', `Alle Reisen ${route.params.year}`);
    watch(route, () => fetchEvents());
    watch(
        () => events.value,
        () => populateCalendar(),
        { deep: true }
    );
    onMounted(() => mounted());
    onBeforeUnmount(() => beforeUnmount());
    window.addEventListener('resize', updateCalendarWith, { passive: true });
    fetchEvents();
}

async function mounted(): Promise<void> {
    await updateCalendarWith();
    const savedScrollPosition = localStorage.getItem('eventplanner.calendar.scrollposition');
    if (calendar.value && savedScrollPosition) {
        const scrollPosition = JSON.parse(savedScrollPosition);
        calendar.value.scrollLeft = scrollPosition.left;
        calendar.value.scrollTop = scrollPosition.top;
    }
}

function beforeUnmount(): void {
    if (calendar.value) {
        const scrollPosition = {
            top: calendar.value.scrollTop,
            left: calendar.value.scrollLeft,
        };
        localStorage.setItem('eventplanner.calendar.scrollposition', JSON.stringify(scrollPosition));
    }
}

async function updateCalendarWith(): Promise<void> {
    await nextTick();
    if (calendar.value) {
        calendarStyle.value['--scrollcontainer-width'] = `${calendar.value.clientWidth}px`;
        calendarStyle.value['--scrollcontainer-height'] = `${calendar.value.clientHeight}px`;
    }
}

function scrollLeft(): void {
    if (calendar.value) {
        const w = calendar.value.scrollWidth;
        let l = calendar.value.scrollLeft;
        l = Math.max(l - w / 12, 0);
        calendar.value.scrollTo({ left: l, behavior: 'smooth' });
    }
}

function scrollRight(): void {
    if (calendar.value) {
        const w = calendar.value.scrollWidth;
        let l = calendar.value.scrollLeft;
        l = Math.min(l + w / 12, w);
        calendar.value.scrollTo({ left: l, behavior: 'smooth' });
    }
}

async function fetchEvents(): Promise<void> {
    year.value = parseInt(route.params.year as string, 10) || new Date().getFullYear();
    if (months.value.size === 0) {
        months.value = buildCalender(year.value);
    }
    let evts = await eventUseCase.getEvents(year.value);
    evts = evts.filter((it) => it.state !== EventState.Canceled);
    months.value = buildCalender(year.value);
    events.value = evts;
}

function buildCalender(year: number): Map<Month, CalendarDay[]> {
    let date = new Date(year, Month.JANUARY, 1);
    const today = new Date();
    const temp: Map<Month, CalendarDay[]> = new Map<Month, CalendarDay[]>();
    while (date.getFullYear() === year) {
        if (!temp.has(date.getMonth())) {
            temp.set(date.getMonth(), []);
        }
        temp.get(date.getMonth())?.push({
            date: date,
            dayOfMonth: date.getDate(),
            weekday: i18n.d(date, DateTimeFormat.DDD),
            isHoliday: isHoliday(date, 'NI'),
            isWeekend: date.getDay() === 0 || date.getDay() === 6,
            isToday:
                date.getDate() === today.getDate() && date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear(),
            events: [],
        });
        date = addToDate(date, { days: 1 });
    }
    return temp;
}

function updateEvent(event: Event): void {
    events.value = events.value.map((it) => (it.key === event.key ? event : it));
}

function startCreateEventDrag(date: Date): void {
    if (signedInUser.permissions.includes(Permission.WRITE_EVENTS)) {
        createEventFromDate.value = date;
        calendarStyle.value['--create-event-days'] = 1;
    }
}

async function stopCreateEventDrag(date: Date): Promise<void> {
    if (signedInUser.permissions.includes(Permission.WRITE_EVENTS)) {
        const from = createEventFromDate.value;
        const to = date;
        if (from && to && createEventDialog.value) {
            const result = await createEventDialog.value.open({ start: from, end: to });
            if (result) {
                await eventAdministrationService.createEvent(result);
                await fetchEvents();
            }
            createEventFromDate.value = null;
            calendarStyle.value['--create-event-days'] = 1;
        }
    }
}

function updateCreateEventDrag(date: Date): void {
    if (signedInUser.permissions.includes(Permission.WRITE_EVENTS) && createEventFromDate.value !== null) {
        const durationMillis = date.getTime() - createEventFromDate.value.getTime();
        if (durationMillis >= 0) {
            calendarStyle.value['--create-event-days'] = new Date(durationMillis).getDate();
        } else if (durationMillis === 0) {
            calendarStyle.value['--create-event-days'] = 1;
        }
    }
}

function populateCalendar(): Map<Month, CalendarDay[]> {
    // reset all events
    [...months.value.values()].forEach((month) => {
        month.forEach((day) => (day.events = []));
    });

    for (let i = 0; i < events.value.length; i++) {
        const previousEvent: Event | undefined = events.value[i - 1];
        const event: Event = events.value[i];
        const nextEvent: Event | undefined = events.value[i + 1];

        const month = months.value.get(event.start.getMonth());
        if (!month) {
            console.error(`Missing month with index ${event.start.getMonth()}!`);
            continue;
        }
        const dayIndex = event.start.getDate() - 1;
        const day = month[dayIndex];
        const overlapsWithPrevious = eventService.doEventsHaveOverlappingDays(previousEvent, event);
        const overlapsWithNext = eventService.doEventsHaveOverlappingDays(event, nextEvent);

        const calendarDayEvent: CalendarDayEvent = {
            event: event,
            duration: new Date(event.end.getTime() - event.start.getTime()).getDate(),
            durationInMonth: new Date(event.end.getTime() - event.start.getTime()).getDate(),
            class: '',
            isContinuation: false,
            offset: 0,
        };
        if (overlapsWithPrevious) {
            calendarDayEvent.durationInMonth -= 0.5;
            calendarDayEvent.offset = 0.5;
        }
        if (overlapsWithNext) {
            calendarDayEvent.durationInMonth -= 0.5;
        }
        if (day.events.length === 1) {
            calendarDayEvent.offset = 0.5;
        }

        // add user event relation class
        if (event.signedInUserAssignedPosition) {
            calendarDayEvent.class += ' assigned';
        } else if (event.signedInUserWaitingListPosition) {
            calendarDayEvent.class += ' waiting-list';
        } else if (event.assignedUserCount >= 23) {
            calendarDayEvent.class += ' full';
        }

        if (event.end.getTime() < new Date().getTime()) {
            calendarDayEvent.class += ' in-past';
        }

        if (calendarDayEvent.durationInMonth < 1) {
            calendarDayEvent.class += ' small';
        }
        if (event.state === EventState.Draft) {
            calendarDayEvent.class += ' draft';
        }

        // check if event ends in next month and split into two events
        if (dayIndex + calendarDayEvent.durationInMonth > month.length) {
            calendarDayEvent.durationInMonth = month.length - dayIndex;
            const nextMonth = months.value.get(event.start.getMonth() + 1);
            if (!nextMonth) {
                console.error(`Missing month with index ${event.start.getMonth() + 1}!`);
                continue;
            }

            const continuedCalendarDayEvent: CalendarDayEvent = {
                ...calendarDayEvent,
                duration: new Date(event.end.getTime() - event.start.getTime()).getDate(),
                durationInMonth: calendarDayEvent.duration - calendarDayEvent.durationInMonth,
                isContinuation: true,
                offset: 0,
            };
            if (overlapsWithNext) {
                continuedCalendarDayEvent.durationInMonth -= 0.5;
            }
            if (overlapsWithPrevious) {
                calendarDayEvent.durationInMonth -= 0.5;
            }
            nextMonth[0].events.push(continuedCalendarDayEvent);
        }

        day.events.push(calendarDayEvent);
    }
    return months.value;
}

init();
</script>

<style>
.calendar {
    --row-height: max(2rem, calc((var(--viewport-height) - var(--nav-height) - 3.5rem) / 31));
    --scrollcontainer-width: 100vw;
    --scrollcontainer-height: 100vh;
    --create-event-days: 1;
    --columns: 1.4;
    height: var(--viewport-height);
    @apply flex snap-x snap-always items-stretch overflow-scroll;
    @apply fixed left-0 right-0 top-0;
}

.calendar-month {
    @apply snap-start;
}

.calendar-header {
    @apply sticky top-0 z-50 sm:z-20;
    @apply bg-primary py-2.5 pl-20 pr-4 text-onprimary sm:ml-2 sm:pl-16 md:ml-2 md:pl-20 xl:pb-4 xl:pt-8;
    @apply border-r border-transparent sm:border-r-surface;
    @apply text-lg font-bold;
}

.impersonated .calendar-header {
    @apply mb-16 sm:mb-0;
}

.calendar-day {
    height: var(--row-height);
    width: calc(var(--scrollcontainer-width) / var(--columns));
    @apply relative flex items-center border-b border-r border-surface bg-surface pl-2 pr-1;
    @apply select-none;
}

.calendar-day:nth-child(2) {
    @apply mt-2 sm:mt-0;
}

.calendar-filler {
    height: var(--row-height);
    @apply border-r border-r-surface;
    @apply border-b border-b-transparent;
}

.calendar-day-label {
    @apply w-7 text-sm font-bold text-outline-variant md:w-8;
}

.calendar-day.holiday .calendar-day-label,
.calendar-day.weekend .calendar-day-label {
    @apply text-secondary;
}

.calendar-day.holiday:before,
.calendar-day.weekend:before {
    content: '';
    @apply absolute bottom-0 left-0 right-0 top-0;
    @apply rounded-lg bg-surface-variant bg-opacity-25;
}

.create-event-overlay {
    @apply pointer-events-none;
    @apply absolute left-0 right-0 top-0 z-10;
    height: calc((var(--create-event-days) * var(--row-height)) - 0.125rem);
    @apply rounded-lg border border-dashed border-onprimary-container bg-primary-container text-onprimary-container;
    @apply cursor-pointer text-sm font-semibold;
    @apply flex flex-col px-4 py-1;
}

.calendar-day.today {
    @apply relative;
}

.calendar-day.today:after {
    content: '';
    @apply pointer-events-none absolute bottom-0 left-0 right-0 top-0 z-10;
    @apply rounded-lg border-2 border-error border-opacity-50 bg-error-container bg-opacity-25;
}

@media only screen and (min-width: 450px) {
    .calendar {
        --columns: 2;
    }
}

@media only screen and (min-width: 640px) {
    .calendar {
        --columns: 3;
        height: calc(var(--viewport-height) - var(--nav-height));
        position: static;
    }

    .calendar-header {
        @apply bg-transparent font-normal text-onsurface;
    }

    .calendar-header::before {
        content: '';
        @apply absolute -left-8 bottom-0 right-0 top-0 -z-10 bg-surface bg-opacity-95;
    }
}

@media only screen and (min-width: 850px) {
    .calendar {
        --columns: 4;
    }
}

@media only screen and (min-width: 1100px) {
    .calendar {
        --columns: 4;
    }
}

@media only screen and (min-width: 1280px) {
    .calendar {
        height: var(--viewport-height);
    }
}

@media only screen and (min-width: 1500px) {
    .calendar {
        --columns: 5;
    }
}
</style>
