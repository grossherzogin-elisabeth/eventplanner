<template>
    <div class="bg-surface flex flex-1 flex-col">
        <div v-if="events" class="flex h-full flex-1 flex-col">
            <div class="relative flex h-full flex-1 items-stretch">
                <div class="bg-surface absolute top-0 left-0 z-30 hidden w-14 pt-0.5 lg:block xl:pt-6">
                    <button class="btn-icon ml-5" type="button" name="previous" @click="scrollLeft()">
                        <i class="fa-solid fa-chevron-left"></i>
                    </button>
                </div>
                <div class="bg-surface absolute top-0 right-0 z-30 hidden w-14 pt-0.5 lg:block xl:pt-6">
                    <button class="btn-icon" type="button" name="next" @click="scrollRight()">
                        <i class="fa-solid fa-chevron-right"></i>
                    </button>
                </div>
                <div
                    ref="calendar"
                    :style="calendarStyle"
                    class="calendar"
                    :class="{ 'enable-create': hasPermission(Permission.UPDATE_EVENTS) }"
                >
                    <div v-for="m in months.entries()" :key="m[0]" class="calendar-month">
                        <div class="calendar-header">
                            <span>{{ $t(`generic.month.${m[0]}`) }}</span>
                            <span class="ml-2 sm:hidden">{{ events[0]?.end.getFullYear() }}</span>
                        </div>
                        <div
                            v-for="d in m[1]"
                            :key="d.date.getDate()"
                            :class="{ weekend: d.isWeekend, holiday: d.isHoliday, today: d.isToday }"
                            class="calendar-day"
                            @mousedown="startCreateEventDrag(d.date)"
                            @mouseover="updateCreateEventDrag(d.date)"
                            @mouseup="stopCreateEventDrag(d.date)"
                        >
                            <div class="calendar-day-label">{{ $d(d.date, DateTimeFormat.DDD) }}</div>
                            <div class="calendar-day-label">{{ d.date.getDate() }}</div>
                            <div class="w-0 grow self-start">
                                <div class="relative flex">
                                    <div v-for="evt in d.eventsStartingOnThisDay" :key="evt.key" class="relative w-0 grow">
                                        <EventCalendarItem
                                            :title="evt.title"
                                            :event="events[evt.key]"
                                            :class="evt.classes.join(' ')"
                                            :duration="evt.duration"
                                            :duration-in-month="evt.durationInMonth"
                                            :start="evt.offset"
                                            @update:event="updateEvent"
                                            @click.stop=""
                                            @mousedown.stop=""
                                        />
                                    </div>
                                </div>
                                <div v-if="d.eventsOnThisDay.length > 0" class="flex">
                                    <!-- there is an event on this day, that was started before -->
                                    <div v-if="d.events.length > d.eventsOnThisDay.length" class="w-1/2"></div>
                                    <div v-for="evt in d.eventsOnThisDay" :key="evt.key" class="relative w-0 grow">
                                        <EventCalendarItem
                                            :title="evt.title"
                                            :event="events[evt.key]"
                                            :class="evt.classes.join(' ')"
                                            :duration="evt.duration"
                                            :duration-in-month="evt.durationInMonth"
                                            :start="evt.offset"
                                            @update:event="updateEvent"
                                            @click.stop=""
                                            @mousedown.stop=""
                                        />
                                    </div>
                                </div>
                                <div class="relative flex">
                                    <div v-if="createEventFromDate === d.date" class="create-event-overlay">
                                        <span>{{ $t('views.calendar.create-event') }}</span>
                                        <span v-if="calendarStyle['--create-event-days'] > 1" class="text-xs">
                                            {{ $t('generic.days', { count: calendarStyle['--create-event-days'] }) }}
                                        </span>
                                    </div>
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
import { useEventUseCase } from '@/application';
import { DateTimeFormat, Month, addToDate, cropToPrecision, isSameDate } from '@/common/date';
import type { EventKey } from '@/domain';
import { type Event, EventState, Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import CreateEventDlg from '@/ui/components/events/EventCreateDlg.vue';
import { useSession } from '@/ui/composables/Session.ts';
import { isHoliday } from 'feiertagejs';
import EventCalendarItem from './EventCalendarItem.vue';

interface CalendarDay {
    date: Date;
    isHoliday: boolean;
    isWeekend: boolean;
    isToday: boolean;
    events: CalendarDayEvent[];
    eventsOnThisDay: CalendarDayEvent[];
    eventsStartingOnThisDay: CalendarDayEvent[];
}

interface CalendarDayEvent {
    key: EventKey;
    title: string;
    durationInMonth: number;
    duration: number;
    start: Date;
    end: Date;
    classes: string[];
    offset: number;
    isContinuation?: boolean;
    isEnclosed?: boolean;
    overlapsWithPrevious?: boolean;
    overlapsWithNext?: boolean;
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const i18n = useI18n();
const eventUseCase = useEventUseCase();
const { hasPermission } = useSession();

const createEventDialog = ref<Dialog<Partial<Event>, Event> | null>(null);
const createEventFromDate = ref<Date | null>(null);
const year = ref<number>(new Date().getFullYear());
const events = ref<Record<EventKey, Event>>({});

const months = ref<Map<Month, CalendarDay[]>>(new Map<Month, CalendarDay[]>());
const calendar = ref<HTMLDivElement | null>(null);
const calendarStyle = ref({
    '--scrollcontainer-width': '100vw',
    '--scrollcontainer-height': '100vh',
    '--create-event-days': 1,
});

function init(): void {
    emit('update:tab-title', `${i18n.t('views.calendar.title')} ${route.params.year}`);
    watch(route, () => fetchEvents());
    watch(
        () => events.value,
        () => populateCalendar(Object.values(events.value)),
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
    year.value = Number.parseInt(route.params.year as string, 10) || new Date().getFullYear();
    if (months.value.size === 0) {
        months.value = buildCalender(year.value);
    }
    let evts = await eventUseCase.getEvents(year.value);
    evts = evts.filter((it) => it.state !== EventState.Canceled);
    months.value = buildCalender(year.value);
    events.value = {};
    evts.forEach((it) => (events.value[it.key] = it));
}

function buildCalender(year: number): Map<Month, CalendarDay[]> {
    let date = new Date(year, Month.JANUARY, 1);
    const today = cropToPrecision(new Date(), 'days');
    const temp: Map<Month, CalendarDay[]> = new Map<Month, CalendarDay[]>();
    while (date.getFullYear() === year) {
        if (!temp.has(date.getMonth())) {
            temp.set(date.getMonth(), []);
        }
        temp.get(date.getMonth())?.push({
            date: cropToPrecision(date, 'days'),
            isHoliday: isHoliday(date, 'NI'),
            isWeekend: date.getDay() === 0 || date.getDay() === 6,
            isToday: date.getTime() === today.getTime(),
            events: [],
            eventsStartingOnThisDay: [],
            eventsOnThisDay: [],
        });
        date = addToDate(date, { days: 1 });
    }
    return temp;
}

function updateEvent(event: Event): void {
    events.value[event.key] = event;
}

function startCreateEventDrag(date: Date): void {
    if (hasPermission(Permission.UPDATE_EVENTS)) {
        createEventFromDate.value = date;
        calendarStyle.value['--create-event-days'] = 1;
    }
}

async function stopCreateEventDrag(date: Date): Promise<void> {
    if (hasPermission(Permission.UPDATE_EVENTS)) {
        const from = createEventFromDate.value;
        const to = date;
        if (from && to && createEventDialog.value) {
            const result = await createEventDialog.value.open({ start: from, end: to });
            if (result) {
                await fetchEvents();
            }
            createEventFromDate.value = null;
            calendarStyle.value['--create-event-days'] = 1;
        }
    }
}

function updateCreateEventDrag(date: Date): void {
    if (hasPermission(Permission.UPDATE_EVENTS) && createEventFromDate.value !== null) {
        const durationMillis = date.getTime() - createEventFromDate.value.getTime();
        if (durationMillis >= 0) {
            calendarStyle.value['--create-event-days'] = new Date(durationMillis).getDate();
        } else if (durationMillis === 0) {
            calendarStyle.value['--create-event-days'] = 1;
        }
    }
}

function populateCalendar(evts: Event[]): Map<Month, CalendarDay[]> {
    resetCalendar();
    const days = fillCalendarDays(evts);
    smoothenOverlappingDays(days);
    smoothenParallelEvents(days);
    return months.value;
}

function resetCalendar(): void {
    [...months.value.values()].forEach((month) => {
        month.forEach((day) => {
            day.events = [];
            day.eventsStartingOnThisDay = [];
            day.eventsOnThisDay = [];
        });
    });
}

function fillCalendarDays(evts: Event[]): CalendarDay[] {
    for (let i = 0; i < evts.length; i++) {
        const event: Event = evts[i];
        const startMonth = months.value.get(event.start.getMonth()) ?? [];
        const startDate = cropToPrecision(event.start, 'days');
        const endDate = cropToPrecision(event.end, 'days');

        let calendarDayEvent: CalendarDayEvent = {
            key: event.key,
            title: event.name,
            duration: event.days,
            durationInMonth: Math.min(startMonth.length - event.start.getDate() + 1, event.days),
            start: startDate,
            end: endDate,
            classes: computeEventClasses(event),
            offset: 0,
        };

        const startDay = startMonth[startDate.getDate() - 1];
        startDay.events.push(calendarDayEvent);
        if (event.days <= 1) {
            startDay.eventsOnThisDay.push(calendarDayEvent);
        } else {
            startDay.eventsStartingOnThisDay.push(calendarDayEvent);
        }

        // add this event to each calendar day it spans
        let date = addToDate(startDate, { days: 1 });
        while (date <= endDate) {
            const month = months.value.get(date.getMonth()) ?? [];
            const dayIndex = date.getDate() - 1;
            if (date.getDate() === 1 && date.getMonth() != startDate.getMonth() && date.getFullYear() === startDate.getFullYear()) {
                // event is reaches into next month
                calendarDayEvent = {
                    ...calendarDayEvent,
                    isContinuation: true,
                    title: `...${event.name}`,
                    offset: 0,
                    duration: new Date(event.end.getTime() - event.start.getTime()).getDate(),
                    durationInMonth: calendarDayEvent.duration - calendarDayEvent.durationInMonth,
                };
                month[dayIndex].eventsStartingOnThisDay.push(calendarDayEvent);
            }

            month[dayIndex].events.push(calendarDayEvent);
            date = addToDate(date, { days: 1 });
        }
    }
    return [...months.value.values()].flat();
}

function smoothenOverlappingDays(days: CalendarDay[]): void {
    for (const day of days) {
        const nonSingleDayEvents = day.events.filter((it) => it.duration > 1);
        const eventsStartingOnThisDay = nonSingleDayEvents.filter((it) => isSameDate(it.start, day.date));
        const eventsEndingOnThisDay = nonSingleDayEvents.filter((it) => isSameDate(it.end, day.date));
        if (eventsStartingOnThisDay.length >= 1 && eventsEndingOnThisDay.length >= 1) {
            for (const event of eventsEndingOnThisDay) {
                event.durationInMonth -= 0.5;
            }
            for (const event of eventsStartingOnThisDay) {
                event.offset = 0.5;
                event.durationInMonth -= 0.5;
            }
        }
    }
}

function smoothenParallelEvents(days: CalendarDay[]): void {
    for (let i = 0; i < days.length; i++) {
        const day = days[i];
        if (day.events.length <= 1 || day.eventsOnThisDay.length <= 0) {
            continue;
        }
        day.eventsStartingOnThisDay.forEach((it) => it.classes.push('overlapped'));
    }
}

function computeEventClasses(event: Event): string[] {
    const classes: string[] = [];
    if (event.isSignedInUserAssigned) {
        classes.push('assigned');
    } else if (event.signedInUserRegistration) {
        classes.push('waiting-list');
    }
    if (event.end.getTime() < Date.now()) {
        classes.push('in-past');
    }
    if (event.state === EventState.Draft) {
        classes.push('draft');
    }
    return classes;
}

init();
</script>

<style>
@reference "tailwindcss";

.calendar {
    --row-height: max(2rem, calc((var(--viewport-height) - var(--nav-height) - 3.5rem) / 31));
    --scrollcontainer-width: 100vw;
    --scrollcontainer-height: 100vh;
    --create-event-days: 1;
    --columns: 1;
    height: var(--viewport-height);
    position: fixed;
    left: 0;
    right: 0;
    top: 0;
    display: flex;
    align-items: stretch;
    overflow: scroll;
    scroll-snap-stop: always;
    @apply snap-x;
}

.calendar-month {
    scroll-snap-align: start;
}

.calendar-header {
    position: sticky;
    top: 0;
    z-index: 50;
    height: var(--nav-height);
    border-right-width: 1px;
    border-right-color: transparent;
    background-color: var(--color-primary);
    color: var(--color-onprimary);

    @apply py-2.5;
    @apply pl-20;
    @apply pr-4;
    font-size: var(--text-lg);
    font-weight: var(--font-weight-bold);

    @apply sm:z-20;
    @apply sm:ml-2;
    @apply sm:pl-16;

    @apply md:ml-2;
    @apply md:pl-20;

    @apply xl:pb-4;
    @apply xl:pt-8;
}

@media (width >= 40rem) {
    .calendar-header {
        height: auto;
        border-right-color: var(--color-surface);
    }
}

html.dark .calendar-header {
    background-color: var(--color-surface-container);
    color: var(--color-onsurface);
}

.impersonated .calendar-header {
    @apply mb-16;

    @apply sm:mb-0;
}

.calendar-day {
    height: var(--row-height);
    width: calc(var(--scrollcontainer-width) / var(--columns));
    position: relative;
    display: flex;
    align-items: center;
    border-bottom-width: 1px;
    border-right-width: 1px;
    user-select: none;
    border-color: var(--color-surface);
    background-color: var(--color-surface);
    @apply pl-2;
    @apply pr-1;
}

.calendar-day:nth-child(2) {
    @apply mt-2;

    @apply sm:mt-0;
}

.calendar-filler {
    height: var(--row-height);
    border-bottom-width: 1px;
    border-bottom-color: transparent;
    border-right-width: 1px;
    border-right-color: var(--color-surface);
}

.calendar-day-label {
    @apply w-7;
    font-size: var(--text-sm);
    font-weight: var(--font-weight-bold);
    color: var(--color-outline-variant);
    @apply md:w-8;
}

.calendar-day.holiday .calendar-day-label,
.calendar-day.weekend .calendar-day-label {
    color: var(--color-secondary);
}

.calendar-day.holiday:before,
.calendar-day.weekend:before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    top: 0;
    border-radius: var(--radius-lg);
    background-color: --alpha(var(--color-secondary) / 5%);
}

.create-event-overlay {
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    z-index: 20;
    opacity: 0.8;
    pointer-events: none;
    height: calc((var(--create-event-days) * var(--row-height)) - 0.125rem);
    display: flex;
    flex-direction: column;
    cursor: pointer;
    border-width: 1px;
    border-style: dashed;
    border-radius: var(--radius-lg);
    border-color: var(--color-onprimary-container);
    background-color: var(--color-primary-container);
    color: var(--color-onprimary-container);
    font-size: var(--text-sm);
    font-weight: var(--font-weight-semibold);
    @apply px-4;
    @apply py-1;
}

.calendar-day.today {
    position: relative;
}

.calendar-day.today:after {
    content: '';
    pointer-events: none;
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    top: 0;
    z-index: 10;
    border-width: 2px;
    border-radius: var(--radius-lg);
    border-color: --alpha(var(--color-error) / 50%);
    background-color: --alpha(var(--color-error-container) / 25%);
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
        background-color: transparent;
        font-weight: var(--font-weight-normal);
        color: var(--color-onsurface);
    }

    .calendar-header::before {
        content: '';
        position: absolute;
        bottom: 0;
        top: 0;
        right: 0;
        z-index: -10;
        @apply -left-8;
        background-color: --alpha(var(--color-surface) / 95%);
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
