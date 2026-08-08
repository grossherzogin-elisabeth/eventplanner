import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useEventRepository } from '@/adapter';
import type { EventRepository } from '@/application';
import { addToDate } from '@/common';
import type { Event } from '@/domain';
import { EventState, EventType, Permission } from '@/domain';
import { Routes } from '@/ui/views/Routes.ts';
import EventAdminListView from '@/ui/views/events/list-admin/EventAdminListView.vue';
import { mockEvent, mockRouter } from '~/mocks';
import { openTableContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('EventAdminListView.vue', () => {
    let eventRepository: EventRepository;
    let testee: VueWrapper;
    let events: Event[];
    let futureEventCount: number = 0;
    let nextMockDate: Date = new Date('2024-05-05T16:00');

    beforeEach(async () => {
        eventRepository = useEventRepository();
        eventRepository.findAll = vi.fn();
        setupUserPermissions([
            Permission.WRITE_EVENTS,
            Permission.WRITE_EVENT_DETAILS,
            Permission.WRITE_EVENT_SLOTS,
            Permission.WRITE_USERS,
            Permission.EXPORT_EVENTS,
        ]);
        vi.setSystemTime(new Date(2024, 3, 1).getTime());
        nextMockDate = new Date('2024-05-05T16:00');

        events = [
            mockEvent({
                key: 'event-in-past',
                name: 'event-in-past',
                start: new Date('2024-01-05T16:00'),
                end: new Date('2024-01-08T16:00'),
            }),
            mockEvent({
                key: 'no-registrations',
                name: 'no-registrations',
                registrations: [],
                ...nextFutureEventDates(),
            }),
            mockEvent({
                key: 'no-free-slots',
                name: 'no-free-slots',
                slots: [],
                ...nextFutureEventDates(),
            }),
            mockEventInState('state-draft-1', EventState.Draft),
            mockEventInState('state-draft-2', EventState.Draft),
            mockEventInState('state-open-for-signup-1', EventState.OpenForSignup),
            mockEventInState('state-open-for-signup-2', EventState.OpenForSignup),
            mockEventInState('state-planned-1', EventState.Planned),
            mockEventInState('state-planned-2', EventState.Planned),
            mockEventInState('state-canceled-1', EventState.Canceled),
            mockEventInState('state-canceled-2', EventState.Canceled),
            mockEventOfType('type-work-event-1', EventType.WorkEvent),
            mockEventOfType('type-work-event-2', EventType.WorkEvent),
            mockEventOfType('type-multi-day-event-1', EventType.Other),
            mockEventOfType('type-multi-day-event-2', EventType.Other),
            mockEvent({
                key: 'next-year',
                name: 'next-year',
                start: new Date('2025-05-05T16:00'),
                end: new Date('2025-05-08T16:00'),
            }),
        ];
        futureEventCount = events.filter((event) => event.start.getTime() > new Date().getTime()).length;

        vi.spyOn(eventRepository, 'findAll').mockImplementation(async (year: number) => {
            return events.filter((event) => event.start.getFullYear() === year);
        });

        await router.push({ name: Routes.EventsListAdmin });
        testee = mount(EventAdminListView, { global: { plugins: [router] } });
    });

    afterEach(() => testee.unmount());

    it('should render names of future events', async () => {
        await loading();
        const table = testee.find('tbody');
        // event[0] is in past and not visible on this tab
        expect(table.text()).not.toContain(events[0].name);
        expect(table.text()).toContain(events[1].name);
        // this event is in the next year, but should still be visible on the future tab
        expect(table.text()).toContain(events[events.length - 1].name);
    });

    describe('tabs', () => {
        it('should show tabs for last, current and next year', async () => {
            expect(testee.find('[data-test-id="tab-2023"]').exists()).toBe(true);
            expect(testee.find('[data-test-id="tab-2024"]').exists()).toBe(true);
            expect(testee.find('[data-test-id="tab-2025"]').exists()).toBe(true);
        });

        it('should show all future events in initial tab', async () => {
            await loading();
            const rows = testee.findAll('tbody tr');
            expect(rows.length).toBe(futureEventCount);
        });

        it('should not render matrix export button on future tab', async () => {
            await loading();
            expect(testee.find('button[name="export"]').exists()).toBe(false);
        });

        it('should render matrix export button when year tab is active', async () => {
            await testee.find('[data-test-id="tab-2024"]').trigger('click');
            await loading();
            expect(testee.find('button[name="export"]').exists()).toBe(true);
        });
    });

    describe('filters', () => {
        it('should filter events to show only work events', async () => {
            await loading();
            await testee.find('[data-test-id="filter-event-type"]').trigger('click');
            await testee.find('[data-test-id="filter-work-event"]').trigger('click');

            const rows = testee.findAll('tbody tr');
            expect(rows).toHaveLength(2);
            expect(rows[0].text()).toContain('type-work-event-1');
            expect(rows[1].text()).toContain('type-work-event-2');
        });

        it('should filter events to show only draft events', async () => {
            await loading();
            await testee.find('[data-test-id="filter-event-state"]').trigger('click');
            await testee.find('[data-test-id="filter-draft"]').trigger('click');

            const rows = testee.findAll('tbody tr');
            expect(rows).toHaveLength(2);
            expect(rows[0].text()).toContain('state-draft-1');
            expect(rows[1].text()).toContain('state-draft-2');
        });

        it('should filter events to show only events with free slots', async () => {
            await loading();
            const eventName = 'no-free-slots';
            expect(testee.find('tbody').text()).toContain(eventName);

            await testee.find('[data-test-id="filter-free-slots"]').trigger('click');

            const rows = testee.findAll('tbody tr');
            expect(rows).toHaveLength(futureEventCount - 1);
            expect(testee.find('tbody').text()).not.toContain(eventName);
        });

        it('should filter events to show only events with waiting list entries', async () => {
            await loading();
            const eventName = 'no-registrations';
            expect(testee.find('tbody').text()).toContain(eventName);

            await testee.find('[data-test-id="filter-waiting-list"]').trigger('click');

            const rows = testee.findAll('tbody tr');
            expect(rows).toHaveLength(futureEventCount - 1);
            expect(testee.find('tbody').text()).not.toContain(eventName);
        });
    });

    describe('export action', () => {
        it('should render export actions in row context menu', async () => {
            await loading();
            const menu = await openTableContextMenu(testee, 0);
            const exports = menu.findAll('[data-test-id="action-export"]');
            expect(exports).toHaveLength(2);
            expect(exports[0].text()).toContain('some template');
            expect(exports[1].text()).toContain('some other template');
        });

        it('should render export actions in multi select context menu', async () => {
            await loading();
            await selectEvents(events[1], events[2]);
            const menu = await openMultiSelectContextMenu();
            const exports = menu.findAll('[data-test-id="action-export"]');
            expect(exports).toHaveLength(2);
            expect(exports[0].text()).toContain('some template');
            expect(exports[1].text()).toContain('some other template');
        });
    });

    describe('edit action', () => {
        it('should navigate to event edit page when only one event selected', async () => {
            await loading();
            await selectEvents(events[1]);
            const menu = await openMultiSelectContextMenu();
            await menu.find('[data-test-id="action-edit"]').trigger('click');

            expect(router.push).toHaveBeenCalledWith({
                name: Routes.EventEdit,
                params: { year: 2024, key: events[1].key },
            });
        });

        it('should open batch edit dialog when multiple events selected', async () => {
            await loading();
            await selectEvents(events[1], events[2]);
            const menu = await openMultiSelectContextMenu();
            await menu.find('[data-test-id="action-edit"]').trigger('click');

            expect(testee.find('[data-test-id="event-batch-edit-dialog"]').exists()).toBe(true);
        });
    });

    describe('open for signup action', () => {
        it('should update all events state to open for signup', async () => {
            const eventRepository = useEventRepository();
            const updateFunc = vi.spyOn(eventRepository, 'updateEvent');
            const event1 = findEvent('state-draft-1');
            const event2 = findEvent('state-draft-2');
            await loading();

            await selectEvents(event1, event2);
            const menu = await openMultiSelectContextMenu();
            await menu.find('[data-test-id="action-open-for-signup"]').trigger('click');

            await expect.poll(() => updateFunc).toHaveBeenCalledWith(event1.key, { state: EventState.OpenForSignup }, [], [], []);
            await expect.poll(() => updateFunc).toHaveBeenCalledWith(event2.key, { state: EventState.OpenForSignup }, [], [], []);
        });
    });

    describe('publish crew action', () => {
        it('should update all event state to planned', async () => {
            const eventRepository = useEventRepository();
            const updateFunc = vi.spyOn(eventRepository, 'updateEvent');
            const event1 = findEvent('state-open-for-signup-1');
            const event2 = findEvent('state-open-for-signup-2');
            await loading();

            await selectEvents(event1, event2);
            const menu = await openMultiSelectContextMenu();
            await menu.find('[data-test-id="action-publish-crew"]').trigger('click');

            await expect.poll(() => updateFunc).toHaveBeenCalledWith(event1.key, { state: EventState.Planned }, [], [], []);
            await expect.poll(() => updateFunc).toHaveBeenCalledWith(event2.key, { state: EventState.Planned }, [], [], []);
        });
    });

    describe('cancel action', () => {
        it('should update multiple events state to canceled', async () => {
            const eventRepository = useEventRepository();
            const updateFunc = vi.spyOn(eventRepository, 'updateEvent');
            const event1 = findEvent('state-draft-1');
            const event2 = findEvent('state-open-for-signup-2');

            await loading();
            await selectEvents(event1, event2);
            const menu = await openMultiSelectContextMenu();
            await menu.find('[data-test-id="action-cancel"]').trigger('click');

            const cancelDialogButtons = testee.findAll('[data-test-id="cancel-event-dialog"] .btn-ghost-danger');
            await cancelDialogButtons[cancelDialogButtons.length - 1].trigger('click');

            await expect.poll(() => updateFunc).toHaveBeenCalledWith(event1.key, { state: EventState.Canceled }, [], [], []);
            await expect.poll(() => updateFunc).toHaveBeenCalledWith(event2.key, { state: EventState.Canceled }, [], [], []);
        });
    });

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }

    function getRowForEvent(event: Event): DOMWrapper<Element> {
        const rows = testee.findAll('tbody tr');
        const row = rows.find((it) => it.text().includes(event.name));
        expect(row).toBeDefined();
        expect(row?.exists()).toBe(true);
        return row!;
    }

    async function selectEvents(...events: Event[]): Promise<void> {
        for (const event of events) {
            const row = getRowForEvent(event);
            const menu = await openTableContextMenu(testee, row);
            await menu.find('[data-test-id="action-select"]').trigger('click');
        }
    }

    function findEvent(key: string): Event {
        const event = events.find((it) => it.key === key);
        expect(event).toBeDefined();
        return event!;
    }

    async function openMultiSelectContextMenu(): Promise<DOMWrapper<Element>> {
        const multiSelectActions = testee.find('[data-test-id="multi-select-actions"]');
        await multiSelectActions.find('[data-test-id="context-menu-trigger"]').trigger('click');
        return multiSelectActions.find('[data-test-id="context-menu"]');
    }

    function nextFutureEventDates(): Partial<Event> {
        nextMockDate = addToDate(nextMockDate, { days: 4 });
        return { start: nextMockDate, end: addToDate(nextMockDate, { days: 2 }) };
    }

    function mockEventInState(key: string, state: EventState): Event {
        return mockEvent({ key: key, state: state, name: key, ...nextFutureEventDates() });
    }

    function mockEventOfType(key: string, type: EventType): Event {
        return mockEvent({ key: key, type: type, name: key, ...nextFutureEventDates() });
    }
});
