<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-x-hidden xl:overflow-y-auto">
        <teleport to="#nav-right">
            <NavbarFilter v-model="filter" :placeholder="$t('views.event-admin-list.filter.search')" />
        </teleport>

        <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <div class="hidden lg:block">
                        <VSearchButton v-model="filter" :placeholder="$t('views.event-admin-list.filter.search')" />
                    </div>
                    <div
                        v-if="hasPermission(Permission.WRITE_USERS) && !Number.isNaN(Number.parseInt(tab, 10))"
                        class="z-10 hidden lg:block"
                    >
                        <AsyncButton class="btn-ghost" name="export" :action="() => eventExports.exportEvents(Number.parseInt(tab, 10))">
                            <template #icon>
                                <i class="fa-solid fa-download"></i>
                            </template>
                            <template #label>
                                <span>{{ $t('views.event-admin-list.export') }}</span>
                            </template>
                        </AsyncButton>
                    </div>
                    <div class="permission-create-events hidden 2xl:block">
                        <button class="btn-primary ml-2" name="create" type="button" @click="createEvent()">
                            <i class="fa-solid fa-calendar-plus"></i>
                            <span>{{ $t('generic.add') }}</span>
                        </button>
                    </div>
                </div>
            </template>
        </VTabs>

        <div class="p-content filter-panel scrollbar-invisible mt-4">
            <FilterMultiselect
                v-model="filterEventType"
                data-test-id="filter-event-type"
                :placeholder="$t('views.event-admin-list.filter.all-events')"
                :options="eventTypes.options.value"
            />
            <FilterMultiselect
                v-model="filterEventStates"
                data-test-id="filter-event-state"
                :placeholder="$t('views.event-admin-list.filter.all-status')"
                :options="eventStates.options.value"
            />
            <FilterToggle
                v-model="filterFreeSlots"
                data-test-id="filter-free-slots"
                :label="$t('views.event-admin-list.filter.free-slots')"
            />
            <FilterToggle
                v-model="filterWaitinglist"
                data-test-id="filter-waiting-list"
                :label="$t('views.event-admin-list.filter.waitinglist')"
            />
        </div>

        <MainContent>
            <div class="full-width-scrollable mt-4">
                <VTable
                    :items="filteredEvents"
                    multiselection
                    query
                    :page-size="20"
                    class="interactive-table no-header scrollbar-invisible"
                    @click="editEvent($event.item, $event.event)"
                >
                    <template #row="{ item }">
                        <EventAdminListRow :event="item" />
                    </template>
                    <template #context-menu="{ item }">
                        <EventAdminListRowActions
                            :events="[item]"
                            @update-events:edit="editEvents($event)"
                            @update-events:create-registration="createRegistration($event)"
                            @update-events:open-for-signup="openEventsForSignup($event)"
                            @update-events:publish-crew="publishCrewPlanning($event)"
                            @update-events:cancel="cancelEvents($event)"
                            @update-events:delete="deleteEvents($event)"
                        />
                    </template>
                </VTable>
            </div>
        </MainContent>

        <EventCreateDlg ref="createEventDialog" />
        <EventCancelDlg ref="cancelEventDialog" />
        <VConfirmationDialog ref="confirmationDialog" />
        <EventBatchEditDlg ref="eventBatchEditDialog" />
        <CreateRegistrationDlg ref="createRegistrationDialog" :submit-text="$t('generic.save')" />
        <EventDetailsSheet ref="eventPreviewSheet" :link-to="Routes.EventEdit" :link-label="$t('generic.edit')" />

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
                        v-if="showBatchOpenEventForSignup"
                        class="permission-write-events btn-ghost"
                        type="button"
                        @click="openEventsForSignup(selectedEvents)"
                    >
                        <i class="fa-solid fa-lock-open"></i>
                        <span class="truncate">{{ $t('domain.event.actions.start-crew-signup') }}</span>
                    </button>
                    <button
                        v-else-if="showBatchPublishPlannedCrew"
                        class="permission-write-events btn-ghost"
                        type="button"
                        @click="publishCrewPlanning(selectedEvents)"
                    >
                        <i class="fa-solid fa-earth-europe"></i>
                        <span class="truncate">{{ $t('domain.event.actions.publish-crew') }}</span>
                    </button>
                    <button v-else class="permission-write-events btn-ghost" type="button" @click="editEvents(selectedEvents)">
                        <i class="fa-solid fa-edit"></i>
                        <span class="truncate">{{ $t('domain.event.actions.edit', 2) }}</span>
                    </button>
                </div>
            </template>
            <template #menu>
                <EventAdminListRowActions
                    :events="selectedEvents"
                    @update-events:edit="editEvents($event)"
                    @update-events:create-registration="createRegistration($event)"
                    @update-events:open-for-signup="openEventsForSignup($event)"
                    @update-events:publish-crew="publishCrewPlanning($event)"
                    @update-events:cancel="cancelEvents($event)"
                    @update-events:delete="deleteEvents($event)"
                />
            </template>
        </VMultiSelectActions>
        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-else
            class="permission-create-events pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" type="button" @click="createEvent()">
                <i class="fa-solid fa-calendar-plus"></i>
                <span>{{ $t('domain.event.actions.create') }}</span>
            </button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useEventAdministrationUseCase, useEventUseCase } from '@/application';
import type { Event, EventType, InputSelectOption, Registration } from '@/domain';
import { EventState, Permission, useEventService } from '@/domain';
import type { ConfirmationDialog, Dialog, Sheet } from '@/ui/components/common';
import { AsyncButton, VConfirmationDialog, VMultiSelectActions, VSearchButton, VTable, VTabs } from '@/ui/components/common';
import CreateRegistrationDlg from '@/ui/components/events/CreateRegistrationDlg.vue';
import EventCancelDlg from '@/ui/components/events/EventCancelDlg.vue';
import EventCreateDlg from '@/ui/components/events/EventCreateDlg.vue';
import { FilterMultiselect, FilterToggle } from '@/ui/components/filters';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useEventExports } from '@/ui/composables/EventExports.ts';
import { useEventStates } from '@/ui/composables/EventStates';
import { useEventTypes } from '@/ui/composables/EventTypes';
import { usePositions } from '@/ui/composables/Positions';
import { useQuery } from '@/ui/composables/QueryState';
import { useSession } from '@/ui/composables/Session';
import { restoreScrollPosition } from '@/ui/plugins/router';
import { Routes } from '@/ui/views/Routes';
import EventBatchEditDlg from '@/ui/views/events/list-admin/EventBatchEditDlg.vue';
import EventDetailsSheet from '@/ui/components/sheets/EventDetailsSheet.vue';
import EventAdminListRow from './EventAdminListRow.vue';
import type { Selectable } from '@/ui/model/Selectable.ts';
import EventAdminListRowActions from './EventAdminListRowActions.vue';
import { useConfig } from '@/ui/composables/Config.ts';
import MainContent from '@/ui/components/partials/MainContent.vue';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const eventAdminUseCase = useEventAdministrationUseCase();
const eventUseCase = useEventUseCase();
const eventService = useEventService();
const positions = usePositions();
const router = useRouter();
const eventTypes = useEventTypes();
const eventStates = useEventStates();
const { config } = useConfig();
const { hasPermission } = useSession();

const filter = useQuery<string>('filter', '').parameter;
const filterWaitinglist = useQuery<boolean>('has-waitinglist', false).parameter;
const filterFreeSlots = useQuery<boolean>('has-free-slots', false).parameter;
const filterEventStates = useQuery<EventState[]>('states', []).parameter;
const filterEventType = useQuery<EventType[]>('types', []).parameter;

const events = ref<(Event & Selectable)[] | null>(null);
const eventExports = useEventExports();
const tab = ref<string>('future');

const createEventDialog = ref<Dialog<Event> | null>(null);
const cancelEventDialog = ref<Dialog<Event | Event[], boolean> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);
const eventBatchEditDialog = ref<Dialog<Event[], boolean> | null>(null);
const createRegistrationDialog = ref<Dialog<Event[], Registration | undefined> | null>(null);
const eventPreviewSheet = ref<Sheet<Event, Event> | null>(null);

const filteredEvents = computed<(Event & Selectable)[] | undefined>(() => {
    const f = filter.value.toLowerCase();
    return events.value
        ?.filter((it) => eventService.doesEventMatchFilter(it, f))
        .filter((it) => filterEventType.value.length === 0 || filterEventType.value.includes(it.type))
        .filter((it) => !filterFreeSlots.value || eventService.hasOpenSlots(it))
        .filter((it) => filterEventStates.value.length === 0 || filterEventStates.value.includes(it.state))
        .filter((it) => !filterWaitinglist.value || it.registrations.length - it.assignedUserCount > 0);
});

const selectedEvents = computed<(Event & Selectable)[] | undefined>(() => {
    return filteredEvents.value?.filter((it) => it.selected);
});

const showBatchOpenEventForSignup = computed<boolean>(() => {
    return (
        hasPermission(Permission.WRITE_EVENTS) &&
        selectedEvents.value != undefined &&
        selectedEvents.value.some((it) => it.state === EventState.Draft)
    );
});

const showBatchPublishPlannedCrew = computed<boolean>(() => {
    return (
        hasPermission(Permission.WRITE_EVENTS) &&
        selectedEvents.value != undefined &&
        selectedEvents.value.some((it) => it.state === EventState.OpenForSignup)
    );
});

const tabs = computed<InputSelectOption[]>(() => {
    const currentYear = new Date().getFullYear();
    return [
        { value: 'future', label: t('views.event-admin-list.tab.future') },
        { value: String(currentYear + 1), label: String(currentYear + 1) },
        { value: String(currentYear), label: String(currentYear) },
        { value: String(currentYear - 1), label: String(currentYear - 1) },
    ];
});

async function init(): Promise<void> {
    emit('update:tab-title', t('views.event-admin-list.tab-title'));
    watch(tab, () => fetchEvents());
    watch(
        () => router.currentRoute.value,
        () => eventPreviewSheet.value?.close()
    );
    await positions.loading;
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
        const now = new Date();
        const currentYear = await eventUseCase.getEvents(now.getFullYear());
        const nextYear = await eventUseCase.getEvents(now.getFullYear() + 1);
        events.value = currentYear.concat(nextYear).filter((it) => it.end.getTime() > now.getTime());
    } else {
        const year = Number.parseInt(tab.value, 10);
        if (year) {
            events.value = await eventUseCase.getEvents(year);
        }
    }
}

async function editEvent(item: Event, evt: MouseEvent): Promise<void> {
    let to: RouteLocationRaw = {
        name: Routes.EventDetails,
        params: { year: item.start.getFullYear(), key: item.key },
    };
    if (hasPermission(Permission.WRITE_EVENTS)) {
        to = {
            name: Routes.EventEdit,
            params: { year: item.start.getFullYear(), key: item.key },
        };
    }
    if (evt.ctrlKey || evt.metaKey) {
        window.open(router.resolve(to).href, '_blank');
    } else if (config.value.enableEventAdminListPreviewSheet) {
        await eventPreviewSheet.value?.open(item);
    } else {
        await router.push(to);
    }
}

async function createEvent(): Promise<void> {
    const event = await createEventDialog.value?.open().catch();
    if (event) {
        await fetchEvents();
    }
}

async function deleteEvents(events: Event[]): Promise<void> {
    const confirmed = await confirmationDialog.value?.open({
        title: t('views.event-admin-list.dialog.delete.title'),
        message: t('views.event-admin-list.dialog.delete.message', { count: events.length }),
        submit: t('views.event-admin-list.dialog.delete.submit'),
        danger: true,
    });
    if (confirmed) {
        for (const event of events) {
            await eventAdminUseCase.deleteEvent(event);
        }
        await fetchEvents();
    }
}

async function cancelEvents(events: Event[]): Promise<void> {
    const confirmed = await cancelEventDialog.value?.open(events);
    if (confirmed) {
        const keys = events.map((it) => it.key);
        await eventAdminUseCase.updateEvents(keys, { state: EventState.Canceled });
        await fetchEvents();
    }
}

async function editEvents(events: Event[], mouseEvent?: MouseEvent): Promise<void> {
    if (events.length === 1) {
        const to: RouteLocationRaw = {
            name: Routes.EventEdit,
            params: { year: events[0].start.getFullYear(), key: events[0].key },
        };
        if (mouseEvent?.ctrlKey || mouseEvent?.metaKey) {
            window.open(router.resolve(to).href, '_blank');
        } else {
            await router.push(to);
        }
    } else {
        const changed = await eventBatchEditDialog.value?.open(events);
        if (changed) {
            await fetchEvents();
        }
    }
}

async function createRegistration(events: Event[]): Promise<void> {
    const result = await createRegistrationDialog.value?.open(events);
    if (result) {
        await eventAdminUseCase.addRegistrations(events, result);
        await fetchEvents();
    }
}

async function openEventsForSignup(events: Event[]): Promise<void> {
    // filter out those events that already have the desired state
    let eventsToEdit = events.filter((it) => it.state !== EventState.OpenForSignup);
    if (eventsToEdit.some((event) => event.state !== EventState.Draft)) {
        const confirmed = await confirmationDialog.value?.open({
            title: t('views.event-admin-list.dialog.open-signup.title'),
            message: t('views.event-admin-list.dialog.open-signup.message'),
            cancel: t('views.event-admin-list.dialog.open-signup.cancel'),
            submit: t('views.event-admin-list.dialog.open-signup.submit'),
        });
        if (confirmed === undefined) {
            return;
        }
        if (confirmed === false) {
            eventsToEdit = events.filter((it) => it.state === EventState.Draft);
        }
    }
    const keys = eventsToEdit.map((it) => it.key);
    await eventAdminUseCase.updateEvents(keys, { state: EventState.OpenForSignup });
    await fetchEvents();
}

async function publishCrewPlanning(events: Event[]): Promise<void> {
    // filter out those events that already have the desired state
    let eventsToEdit = events.filter((it) => it.state !== EventState.Planned);
    if (eventsToEdit.some((event) => event.state !== EventState.OpenForSignup)) {
        const confirmed = await confirmationDialog.value?.open({
            title: t('views.event-admin-list.dialog.publish-crew.title'),
            message: t('views.event-admin-list.dialog.publish-crew.message'),
            cancel: t('views.event-admin-list.dialog.publish-crew.cancel'),
            submit: t('views.event-admin-list.dialog.publish-crew.submit'),
        });
        if (confirmed === undefined) {
            return;
        }
        if (confirmed === false) {
            eventsToEdit = events.filter((it) => it.state === EventState.OpenForSignup);
        }
    }
    const keys = eventsToEdit.map((it) => it.key);
    await eventAdminUseCase.updateEvents(keys, { state: EventState.Planned });
    await fetchEvents();
}

init();
</script>
