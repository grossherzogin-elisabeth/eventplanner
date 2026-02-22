import type { Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
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
import { setupUserPermissions } from '~/utils';

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
                name: 'user is assigned',
                slots: [mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN })],
                registrations: [mockRegistrationCaptain()],
            }),
            mockEvent({
                name: 'user is on waiting list',
                slots: [mockSlotCaptain({ assignedRegistrationKey: undefined })],
                registrations: [mockRegistrationCaptain()],
            }),
            mockEvent({
                name: 'event with locations',
                registrations: [mockRegistrationCaptain()],
                locations: mockLocations(),
            }),
        ];
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

    it('should render hourglass for events where user is on waitinglist', async () => {
        const tableRow = testee.findAll('tbody tr')[1];
        expect(tableRow.html()).toContain('fa-hourglass');
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
    });
});
