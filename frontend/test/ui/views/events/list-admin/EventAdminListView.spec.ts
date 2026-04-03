import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { EventRepresentation } from '@/adapter/rest/EventRestRepository.ts';
import { Permission } from '@/domain';
import { EventState } from '@/domain';
import { Routes } from '@/ui/views/Routes.ts';
import EventsAdminListView from '@/ui/views/events/list-admin/EventsAdminListView.vue';
import { mockEventRepresentation, mockRouter, server } from '~/mocks';
import { setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('EventsAdminListView.vue', () => {
    let testee: VueWrapper;
    let events: EventRepresentation[];

    beforeEach(async () => {
        setupUserPermissions([Permission.WRITE_EVENTS, Permission.WRITE_EVENT_DETAILS, Permission.WRITE_EVENT_SLOTS]);
        vi.setSystemTime(new Date(2024, 3, 1).getTime());

        events = [
            mockEventRepresentation({
                key: 'a',
                name: 'past event in current year',
                start: '2024-01-05T16:00',
                end: '2024-01-08T16:00',
            }),
            mockEventRepresentation({
                key: 'b',
                name: 'future event in current year',
                start: '2024-04-05T16:00',
                end: '2024-04-08T16:00',
            }),
            mockEventRepresentation({
                key: 'c',
                state: EventState.Draft,
                name: 'future event in state DRAFT',
                start: '2024-05-05T16:00',
                end: '2024-05-08T16:00',
            }),
            mockEventRepresentation({
                key: 'd',
                state: EventState.Canceled,
                name: 'future event in state CANCELED',
                start: '2024-06-05T16:00',
                end: '2024-06-08T16:00',
            }),
            mockEventRepresentation({
                key: 'e',
                name: 'future event in next year',
                start: '2025-05-05T16:00',
                end: '2025-05-08T16:00',
            }),
        ];
        server.use(http.get('/api/v1/events', () => HttpResponse.json(events)));
        await router.push({ name: Routes.EventsListAdmin });
        testee = mount(EventsAdminListView, { global: { plugins: [router] } });
    });

    it('should show tabs for last, current and next year', async () => {
        expect(testee.find('[data-test-id="tab-2023"]').exists()).toBe(true);
        expect(testee.find('[data-test-id="tab-2024"]').exists()).toBe(true);
        expect(testee.find('[data-test-id="tab-2025"]').exists()).toBe(true);
    });

    it('should show all future events in initial tab', async () => {
        await loading();
        const rows = testee.findAll('tbody tr');
        expect(rows.length).toBe(4);
    });

    it('should render names of future events', async () => {
        await loading();
        const table = testee.find('tbody');
        // event[0] is in past and not visible on this tab
        expect(table.text()).toContain(events[1].name);
        expect(table.text()).toContain(events[2].name);
        expect(table.text()).toContain(events[3].name);
        expect(table.text()).toContain(events[4].name);
    });

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
