import type { Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useEventCachingService } from '@/application';
import { wait } from '@/common';
import type { Event, UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { usePositions } from '@/ui/composables/Positions.ts';
import UserEventsTable from '@/ui/views/users/details/tabs/UserEventsTable.vue';
import {
    REGISTRATION_CAPTAIN,
    mockEvent,
    mockLocations,
    mockRegistrationCaptain,
    mockRouter,
    mockSlotCaptain,
    mockUserCaptain,
    mockUserDetails,
} from '~/mocks';
import { openTableContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('UserEventsTable.vue', () => {
    let testee: VueWrapper;
    let user: UserDetails;
    let events: Event[];

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        events = [
            mockEvent({
                key: 'user-is-assigned',
                name: 'user is assigned',
                slots: [mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN })],
                registrations: [mockRegistrationCaptain()],
            }),
            mockEvent({
                key: 'user-is-on-waiting-list',
                name: 'user is on waiting list',
                slots: [mockSlotCaptain({ assignedRegistrationKey: undefined })],
                registrations: [mockRegistrationCaptain()],
            }),
            mockEvent({
                key: 'event-with-locations',
                name: 'event with locations',
                registrations: [mockRegistrationCaptain()],
                locations: mockLocations(),
            }),
        ];
        const eventCachingService = useEventCachingService();
        for (const event of events) {
            await eventCachingService.updateCache(event);
        }
        testee = mount(UserEventsTable, {
            props: { user, events },
        });
        await usePositions().loading;
    });

    it('should render all events', async () => {
        const tableRows = testee.findAll('tbody tr');
        expect(tableRows.length).toBe(events.length);
    });

    it('should render event names', async () => {
        const tableRow = testee.find('tbody tr');
        expect(tableRow.text()).toContain(events[0].name);
    });

    it('should render event locations', async () => {
        const tableRow = testee.findAll('tbody tr')[2];
        events[2].locations.forEach((location) => {
            expect(tableRow.text()).toContain(location.name);
        });
    });

    it('should render users position', async () => {
        const tableRow = testee.find('tbody tr');
        expect(tableRow.text()).toContain('Captain');
    });

    it('should render checkmark for events where user is assigned', async () => {
        const tableRow = testee.find('tbody tr');
        expect(tableRow.html()).toContain('fa-check');
    });

    it('should render hourglass for events where user is on waiting list', async () => {
        const tableRow = testee.findAll('tbody tr')[1];
        expect(tableRow.html()).toContain('fa-hourglass');
    });

    it('should open event on row click', async () => {
        const row = testee.findAll('tbody tr')[0];
        await row.trigger('click');
        expect(router.push).toHaveBeenCalled();
    });

    describe('Users with permission users:read-details', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_USER_DETAILS]);
        });

        it('should not render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(false);
        });
    });

    describe('Users with permission users:write', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_USER_DETAILS, Permission.WRITE_USERS]);
        });

        it('should render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(true);
        });

        it('should render context menu actions', async () => {
            const menu = await openTableContextMenu(testee, 1);
            expect(menu.find('[data-test-id="action-view-event"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-add-to-crew"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-delete-registration"]').exists()).toBe(true);
        });

        it('should not render add-to-crew-action when user is already assigned', async () => {
            const menu = await openTableContextMenu(testee, 0);
            expect(menu.find('[data-test-id="action-view-event"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-add-to-crew"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-delete-registration"]').exists()).toBe(true);
        });

        it('should add user to crew', async () => {
            const row = testee.findAll('tbody tr')[1];
            expect(row.find('[data-test-id="crew-count"]').text()).toContain(0);
            const menu = await openTableContextMenu(testee, row);
            const action = menu.find('[data-test-id="action-add-to-crew"]');
            await action.trigger('click');
            await wait(1); // ugly (but working) way to make sure all requests are finished before the tests exits
            expect(row.find('[data-test-id="crew-count"]').text()).toContain(1);
            expect(events[1].slots[0].assignedRegistrationKey).toEqual(events[1].registrations[0].key);
            expect(events[1].assignedUserCount).toBe(1);
        });

        it('should remove user registration', async () => {
            const row = testee.findAll('tbody tr')[0];
            const menu = await openTableContextMenu(testee, row);
            const action = menu.find('[data-test-id="action-delete-registration"]');
            await action.trigger('click');
            await wait(1); // ugly (but working) way to make sure all requests are finished before the tests exits
            expect(testee.findAll('tbody tr').length).toBe(2);
        });
    });
});
