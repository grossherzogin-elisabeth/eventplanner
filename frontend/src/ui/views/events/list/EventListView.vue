<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-x-hidden xl:overflow-y-auto">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" :placeholder="$t('views.event-list.filter.search')" />
            </div>
        </teleport>

        <div v-if="signedInUser?.positions.length === 0" class="px-4 md:px-12 xl:px-16">
            <VInfo class="mt-4 xl:mt-8" clamp>
                {{ $t('views.event-list.note-no-position') }}
            </VInfo>
        </div>

        <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 hidden items-stretch gap-2 pb-2 lg:flex 2xl:mr-0">
                    <VSearchButton v-model="filter" :placeholder="$t('views.event-list.filter.search')" />
                </div>
            </template>
        </VTabs>

        <div class="filter-panel scrollbar-invisible mt-4">
            <FilterMultiselect
                v-model="filterEventType"
                data-test-id="filter-event-type"
                :placeholder="$t('views.event-list.filter.all-types')"
                :options="eventTypes.options.value"
            />
            <FilterToggle v-model="filterAssigned" data-test-id="filter-assigned" :label="$t('views.event-list.filter.assigned')" />
            <FilterToggle
                v-model="filterWaitingList"
                data-test-id="filter-waiting-list"
                :label="$t('views.event-list.filter.waitinglist')"
            />
            <FilterToggle v-model="filterFreeSlots" data-test-id="filter-free-slots" :label="$t('views.event-list.filter.free-slots')" />
        </div>

        <div class="w-full">
            <VTable
                :items="filteredEvents"
                multiselection
                query
                :page-size="20"
                class="interactive-table no-header scrollbar-invisible xs:px-8 overflow-x-auto px-4 pt-4 md:px-16 xl:px-20"
                @click="openEvent($event.item, $event.event)"
            >
                <template #row="{ item }">
                    <EventListRow :event="item" />
                </template>
                <template #context-menu="{ item }">
                    <EventListRowActions :event="item" @join="joinEvents([$event])" @leave="leaveEvents([$event])" />
                </template>
            </VTable>
        </div>

        <VConfirmationDialog />

        <div class="flex-1"></div>

        <VMultiSelectActions
            v-if="selectedEvents && selectedEvents.length > 0"
            :count="selectedEvents.length"
            @select-all="selectAll()"
            @select-none="selectNone()"
        >
            <template #action>
                <div class="hidden sm:inline">
                    <button
                        class="btn-ghost"
                        :disabled="!hasAnySelectedEventInFuture || signedInUser?.positions.length === 0"
                        @click="joinEvents(selectedEvents)"
                    >
                        <i class="fa-solid fa-user-plus"></i>
                        <span>{{ $t('views.event-list.action.signup') }}</span>
                    </button>
                </div>
            </template>
            <template #menu>
                <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntries(selectedEvents)">
                    <i class="fa-solid fa-calendar-alt" />
                    <span>{{ $t('views.event-list.action.create-calendar-entry') }}</span>
                </li>
                <li
                    v-if="hasAnySelectedEventWhichSignedInUserCanJoin"
                    class="permission-write-own-registrations context-menu-item"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="joinEvents(selectedEvents)"
                >
                    <i class="fa-solid fa-user-plus" />
                    <span>{{ $t('views.event-list.action.signup') }}</span>
                </li>
                <li
                    v-if="hasAnySelectedEventWithSignedInUserOnWaitingList"
                    class="permission-write-own-registrations context-menu-item"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="leaveEvents(selectedEvents, true)"
                >
                    <i class="fa-solid fa-user-minus" />
                    <span>{{ $t('views.event-list.action.leave-waitinglist') }}</span>
                </li>
                <li
                    v-if="hasAnySelectedEventWithSignedInUserInTeam"
                    class="permission-write-own-registrations context-menu-item text-error"
                    :class="{ disabled: !hasAnySelectedEventInFuture }"
                    @click="leaveEvents(selectedEvents)"
                >
                    <i class="fa-solid fa-ban" />
                    <span>{{ $t('views.event-list.action.cancel') }}</span>
                </li>
            </template>
        </VMultiSelectActions>

        <RegistrationDetailsSheet ref="createRegistrationSheet" />
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useEventUseCase } from '@/application';
import type { Event, EventType, InputSelectOption, Registration } from '@/domain';
import { useEventService } from '@/domain';
import type { Sheet } from '@/ui/components/common';
import { VConfirmationDialog, VInfo, VMultiSelectActions, VTable, VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import { FilterMultiselect, FilterToggle } from '@/ui/components/filters';
import RegistrationDetailsSheet from '@/ui/components/sheets/RegistrationDetailsSheet.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useEventTypes } from '@/ui/composables/EventTypes';
import { useQuery } from '@/ui/composables/QueryState';
import { useSession } from '@/ui/composables/Session';
import { restoreScrollPosition } from '@/ui/plugins/router';
import { Routes } from '@/ui/views/Routes';
import EventListRow from './EventListRow.vue';
import type { Selectable } from '@/ui/model/Selectable';
import EventListRowActions from './EventListRowActions.vue';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const eventUseCase = useEventUseCase();
const eventService = useEventService();
const router = useRouter();
const eventTypes = useEventTypes();
const { signedInUser } = useSession();

const filter = useQuery<string>('filter', '').parameter;
const filterAssigned = useQuery<boolean>('assigned', false).parameter;
const filterWaitingList = useQuery<boolean>('waitinglist', false).parameter;
const filterFreeSlots = useQuery<boolean>('has-free-slots', false).parameter;
const filterEventType = useQuery<EventType[]>('types', []).parameter;

const events = ref<(Event & Selectable)[] | null>(null);
const tab = ref<string>('future');

const createRegistrationSheet = ref<Sheet<
    {
        registration?: Registration;
        event: Event | Event[];
    },
    Registration | undefined
> | null>(null);

const hasAnySelectedEventInFuture = computed<boolean>(() => {
    const now = Date.now();
    return selectedEvents.value?.find((it) => it.start.getTime() > now) !== undefined;
});

const hasAnySelectedEventWhichSignedInUserCanJoin = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => !it.signedInUserRegistration) !== undefined;
});

const hasAnySelectedEventWithSignedInUserOnWaitingList = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserRegistration && !it.isSignedInUserAssigned) !== undefined;
});

const hasAnySelectedEventWithSignedInUserInTeam = computed<boolean>(() => {
    return selectedEvents.value?.find((it) => it.signedInUserRegistration && it.isSignedInUserAssigned) !== undefined;
});

const filteredEvents = computed<(Event & Selectable)[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value
        ?.filter((it) => eventService.doesEventMatchFilter(it, f))
        .filter((it) => filterEventType.value.length === 0 || filterEventType.value.includes(it.type))
        .filter((it) => {
            if (filterAssigned.value || filterWaitingList.value || filterFreeSlots.value) {
                let state = 0;
                if (it.isSignedInUserAssigned) {
                    state = 1;
                } else if (it.signedInUserRegistration) {
                    state = 2;
                } else if (eventService.hasOpenSlots(it)) {
                    state = 3;
                }
                return (
                    (filterAssigned.value && state === 1) ||
                    (filterWaitingList.value && state === 2) ||
                    (filterFreeSlots.value && state === 3)
                );
            }
            return true;
        });
});

const selectedEvents = computed<(Event & Selectable)[] | undefined>(() => {
    return filteredEvents.value?.filter((it) => it.selected);
});

const tabs = computed<InputSelectOption[]>(() => {
    const currentYear = new Date().getFullYear();
    return [
        { value: 'future', label: t('views.event-list.tab.future') },
        { value: String(currentYear + 1), label: String(currentYear + 1) },
        { value: String(currentYear), label: String(currentYear) },
        { value: String(currentYear - 1), label: String(currentYear - 1) },
    ];
});

async function init(): Promise<void> {
    emit('update:tab-title', 'Alle Veranstaltungen');
    watch(tab, () => fetchEvents());
    await nextTick(); // wait for the tab to have the correct value before fetching
    await fetchEvents();
    restoreScrollPosition();
}

function selectNone(): void {
    events.value?.forEach((it) => (it.selected = false));
}

function selectAll(): void {
    events.value?.forEach((it) => (it.selected = true));
}

async function fetchEvents(): Promise<void> {
    if (tab.value === tabs.value[0].value) {
        // future events
        const now = new Date();
        const currentYear = await fetchEventsByYear(now.getFullYear());
        const nextYear = await fetchEventsByYear(now.getFullYear() + 1);
        events.value = currentYear.concat(nextYear).filter((it) => it.end.getTime() > now.getTime());
    } else {
        const year = Number.parseInt(tab.value);
        if (year) {
            events.value = await fetchEventsByYear(year);
        }
    }
}

async function fetchEventsByYear(year: number): Promise<(Event & Selectable)[]> {
    const eventsByYear = await eventUseCase.getEvents(year);
    return eventsByYear.map((evt) => {
        const tableItem: Event & Selectable = {
            ...evt,
            selected: false,
        };
        return tableItem;
    });
}

async function openEvent(item: Event, evt: MouseEvent): Promise<void> {
    const to: RouteLocationRaw = {
        name: Routes.EventDetails,
        params: { year: item.start.getFullYear(), key: item.key },
    };
    if (evt.ctrlKey || evt.metaKey) {
        window.open(router.resolve(to).href, '_blank');
    } else {
        await router.push(to);
    }
}

async function joinEvents(events: Event[]): Promise<void> {
    const registration = await createRegistrationSheet.value?.open({
        event: events,
        registration: undefined,
    });
    if (registration) {
        await eventUseCase.joinEvents(events, registration);
        await fetchEvents();
    }
}

async function leaveEvents(events: Event[], waitingListOnly = false): Promise<void> {
    if (waitingListOnly) {
        await eventUseCase.leaveEventsWaitingListOnly(events);
    } else {
        await eventUseCase.leaveEvents(events);
    }
    await fetchEvents();
}

init();
</script>
