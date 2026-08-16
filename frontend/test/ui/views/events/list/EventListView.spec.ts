import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { EventUseCase } from '@/application';
import { useEventUseCase } from '@/application';
import type { Event, Registration } from '@/domain';
import { EventType, Permission, SlotCriticality } from '@/domain';
import { Routes } from '@/ui/views/Routes';
import EventListView from '@/ui/views/events/list/EventListView.vue';
import { DECKHAND, mockEvent, mockRegistrationDeckhand, mockRouter } from '~/mocks';
import { openTableContextMenu, setupSignedInUser, setupUserPermissions, stubs } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('EventListView.vue', () => {
    let testee: VueWrapper;
    let events: Event[];
    let eventUseCase: EventUseCase;

    beforeEach(async () => {
        eventUseCase = useEventUseCase();
        vi.setSystemTime(new Date(2024, 3, 1).getTime());
        const signedInUser = setupUserPermissions([Permission.READ_EVENTS, Permission.EXPORT_EVENTS]);
        events = [
            mockEvent({
                name: 'event in past year',
                start: new Date('2023-05-05T16:00'),
                end: new Date('2023-05-08T16:00'),
            }),
            mockEvent({
                name: 'past event in current year',
                start: new Date('2024-02-05T16:00'),
                end: new Date('2024-02-08T16:00'),
            }),
            mockEvent({
                name: 'future event in current year',
                start: new Date('2024-04-05T16:00'),
                end: new Date('2024-04-08T16:00'),
            }),
            mockEvent({
                type: EventType.SingleDayEvent,
                name: 'single day event',
                start: new Date('2024-04-05T16:00'),
                end: new Date('2024-04-08T16:00'),
            }),
            mockEvent({
                type: EventType.MultiDayEvent,
                name: 'multi day event',
                start: new Date('2024-05-05T16:00'),
                end: new Date('2024-05-08T16:00'),
            }),
            mockEvent({
                type: EventType.WorkEvent,
                name: 'work event',
                start: new Date('2024-06-05T16:00'),
                end: new Date('2024-06-08T16:00'),
            }),
            mockEvent({
                name: 'event with signed-in user on waiting list',
                start: new Date('2024-07-05T16:00'),
                end: new Date('2024-07-08T16:00'),
                registrations: [{ key: 'reg-1-key', positionKey: DECKHAND, userKey: signedInUser.key }],
                isSignedInUserAssigned: false,
                canSignedInUserLeave: true,
                canSignedInUserJoin: false,
                signedInUserRegistration: { key: 'reg-1-key', positionKey: DECKHAND, userKey: signedInUser.key },
            }),
            mockEvent({
                name: 'event with signed-in user in crew',
                start: new Date('2024-08-05T16:00'),
                end: new Date('2024-08-08T16:00'),
                registrations: [{ key: 'reg-2-key', positionKey: DECKHAND, userKey: signedInUser.key }],
                slots: [
                    {
                        key: 'slot-2-key',
                        order: 1,
                        positionKeys: [DECKHAND],
                        criticality: SlotCriticality.Optional,
                        assignedRegistrationKey: 'reg-2-key',
                    },
                ],
                isSignedInUserAssigned: true,
                canSignedInUserLeave: true,
                canSignedInUserJoin: false,
                signedInUserRegistration: { key: 'reg-1-key', positionKey: DECKHAND, userKey: signedInUser.key },
            }),
            mockEvent({
                name: 'event in next year',
                start: new Date('2025-05-05T16:00'),
                end: new Date('2025-05-08T16:00'),
            }),
        ];
        events.forEach((e, i) => (e.key = String(i)));

        vi.spyOn(eventUseCase, 'getEvents').mockImplementation(async (year: number) => {
            return events.filter((event) => event.start.getFullYear() === year);
        });

        await router.push({ name: Routes.EventsList });
        testee = mount(EventListView, { global: { plugins: [router] } });
    });

    afterEach(() => testee.unmount());

    it('should show tabs for last, current and next year', async () => {
        expect(testee.find('[data-test-id="tab-2023"]').exists()).toBe(true);
        expect(testee.find('[data-test-id="tab-2024"]').exists()).toBe(true);
        expect(testee.find('[data-test-id="tab-2025"]').exists()).toBe(true);
    });

    it('should show all future events in initial tab', async () => {
        await loading();
        const rows = testee.findAll('tbody tr');
        expect(rows).toHaveLength(events.filter((it) => it.start.getFullYear() === 2024).length);
    });

    it('should render future events', async () => {
        await loading();
        const table = testee.find('tbody');
        expect(table.text()).not.toContain('event in past year');
        expect(table.text()).not.toContain('past event in current year');
        expect(table.text()).toContain('future event in current year');
        expect(table.text()).toContain('event in next year');
    });

    it('should filter events to show only work events', async () => {
        await loading();
        await testee.find('[data-test-id="filter-event-type"]').trigger('click');
        await testee.find('[data-test-id="filter-work-event"]').trigger('click');
        const rows = testee.findAll('tbody tr');
        expect(rows).toHaveLength(1);
        expect(rows[0].text()).toContain('work event');
    });

    it('should filter events to show only waiting list events', async () => {
        await loading();
        await testee.find('[data-test-id="filter-waiting-list"]').trigger('click');
        const rows = testee.findAll('tbody tr');
        expect(rows).toHaveLength(1);
        expect(rows[0].text()).toContain('event with signed-in user on waiting list');
    });

    it('should filter events to show only assigned events', async () => {
        await loading();
        await testee.find('[data-test-id="filter-assigned"]').trigger('click');
        const rows = testee.findAll('tbody tr');
        expect(rows).toHaveLength(1);
        expect(rows[0].text()).toContain('event with signed-in user in crew');
    });

    it('should show events of current year', async () => {
        await loading();
        await testee.find('[data-test-id="tab-2024"]').trigger('click');
        const table = testee.find('tbody');
        // await loading after tab switch
        await expect.poll(() => table.text()).toContain('past event in current year');
        expect(table.text()).not.toContain('event in past year');
        expect(table.text()).toContain('future event in current year');
        expect(table.text()).not.toContain('event in next year');
    });

    it('should render export actions in row context menu', async () => {
        await loading();
        const menu = await openTableContextMenu(testee, 0);
        await expect.poll(() => menu.findAll('[data-test-id="action-export"]')).toHaveLength(2);
        const exports = menu.findAll('[data-test-id="action-export"]');
        expect(exports[0].text()).toContain('some template');
        expect(exports[1].text()).toContain('some other template');
    });

    it('should join event', async () => {
        const joinEventFunc = vi
            .spyOn(eventUseCase, 'joinEvents')
            .mockImplementation(async (events: Event[], registration: Registration) => {
                events.forEach((event) => event.registrations.push(registration));
            });

        const registration = mockRegistrationDeckhand({ userKey: 'mocked' });
        const openSpy = vi.fn(async () => registration);
        testee.unmount();
        testee = mount(EventListView, {
            global: {
                plugins: [router],
                stubs: { RegistrationDetailsSheet: stubs('RegistrationDetailsSheet', openSpy) },
            },
        });
        await loading();

        const menu = await openTableContextMenu(testee, 0); // row 0: future event without user registration
        const signUpAction = menu
            .findAll('.context-menu-item')
            .find((item) => item.text().includes(testee.vm.$t('domain.event.actions.sign-up')));
        await signUpAction!.trigger('click');

        await expect.poll(() => joinEventFunc).toHaveBeenCalled();
    });

    it('should delete waiting list registration', async () => {
        const leaveEventsFunc = vi.spyOn(eventUseCase, 'leaveEventsWaitingListOnly').mockImplementation(async (events: Event[]) => {
            events.forEach((event) => (event.registrations = []));
        });

        await loading();

        const menu = await openTableContextMenu(testee, 4); // row 4: event with signed-in user on waiting list
        const leaveAction = menu
            .findAll('.context-menu-item')
            .find((item) => item.text().includes(testee.vm.$t('domain.event.actions.leave-waiting-list')));
        await leaveAction!.trigger('click');

        await expect.poll(() => leaveEventsFunc).toHaveBeenCalled();
    });

    it('should cancel crew registration', async () => {
        const leaveEventsFunc = vi.spyOn(eventUseCase, 'leaveEvents').mockImplementation(async (events: Event[]) => {
            events.forEach((event) => (event.registrations = []));
        });

        await loading();

        const menu = await openTableContextMenu(testee, 5); // row 5: event with signed-in user in crew
        const cancelAction = menu.find('[data-test-id="action-leave-all"]');
        await cancelAction!.trigger('click');

        await expect.poll(() => leaveEventsFunc).toHaveBeenCalled();
    });

    it('should not show no-position banner when signed-in user has positions', () => {
        expect(testee.text()).not.toContain(testee.vm.$t('views.event-list.note-no-position'));
    });

    it('should show no-position banner when signed-in user has no positions', () => {
        testee.unmount();
        setupSignedInUser({ positions: [] });
        testee = mount(EventListView, { global: { plugins: [router] } });

        expect(testee.text()).toContain(testee.vm.$t('views.event-list.note-no-position'));
    });

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
