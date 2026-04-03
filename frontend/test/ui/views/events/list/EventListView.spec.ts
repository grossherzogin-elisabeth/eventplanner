import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { EventRepresentation } from '@/adapter/rest/EventRestRepository';
import { EventType, Permission, SlotCriticality } from '@/domain';
import { Routes } from '@/ui/views/Routes';
import EventsListView from '@/ui/views/events/list/EventsListView.vue';
import { DECKHAND, mockEventRepresentation, mockRouter, server } from '~/mocks';
import { setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('EventsListView.vue', () => {
    let testee: VueWrapper;
    let events: EventRepresentation[];

    beforeEach(async () => {
        vi.setSystemTime(new Date(2024, 3, 1).getTime());
        const signedInUser = setupUserPermissions([Permission.READ_EVENTS]);
        events = [
            mockEventRepresentation({
                name: 'event in past year',
                start: '2023-05-05T16:00',
                end: '2023-05-08T16:00',
            }),
            mockEventRepresentation({
                name: 'past event in current year',
                start: '2024-02-05T16:00',
                end: '2024-02-08T16:00',
            }),
            mockEventRepresentation({
                name: 'future event in current year',
                start: '2024-04-05T16:00',
                end: '2024-04-08T16:00',
            }),
            mockEventRepresentation({
                type: EventType.SingleDayEvent,
                name: 'single day event',
                start: '2024-04-05T16:00',
                end: '2024-04-08T16:00',
            }),
            mockEventRepresentation({
                type: EventType.MultiDayEvent,
                name: 'multi day event',
                start: '2024-05-05T16:00',
                end: '2024-05-08T16:00',
            }),
            mockEventRepresentation({
                type: EventType.WorkEvent,
                name: 'work event',
                start: '2024-06-05T16:00',
                end: '2024-06-08T16:00',
            }),
            mockEventRepresentation({
                name: 'event with signed in user on waiting list',
                start: '2024-07-05T16:00',
                end: '2024-07-08T16:00',
                registrations: [{ key: 'reg-1-key', positionKey: DECKHAND, userKey: signedInUser.key }],
            }),
            mockEventRepresentation({
                name: 'event with signed in user in crew',
                start: '2024-08-05T16:00',
                end: '2024-08-08T16:00',
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
            }),
            mockEventRepresentation({
                name: 'event in next year',
                start: '2025-05-05T16:00',
                end: '2025-05-08T16:00',
            }),
        ];
        events.forEach((e, i) => (e.key = String(i)));

        server.use(http.get('/api/v1/events', () => HttpResponse.json(events)));
        await router.push({ name: Routes.EventsList });
        testee = mount(EventsListView, { global: { plugins: [router] } });
    });

    it('should show tabs for last, current and next year', async () => {
        expect(testee.find('[data-test-id="tab-2023"]').exists()).toBe(true);
        expect(testee.find('[data-test-id="tab-2024"]').exists()).toBe(true);
        expect(testee.find('[data-test-id="tab-2025"]').exists()).toBe(true);
    });

    it('should show all future events in initial tab', async () => {
        await loading();
        const rows = testee.findAll('tbody tr');
        expect(rows.length).toBe(events.filter((it) => it.start.startsWith('2024')).length);
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
        expect(rows[0].text()).toContain('event with signed in user on waiting list');
    });

    it('should filter events to show only assigned events', async () => {
        await loading();
        await testee.find('[data-test-id="filter-assigned"]').trigger('click');
        const rows = testee.findAll('tbody tr');
        expect(rows).toHaveLength(1);
        expect(rows[0].text()).toContain('event with signed in user in crew');
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

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
