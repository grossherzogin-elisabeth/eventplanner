import type { Router } from 'vue-router';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { AuthService } from '@/application';
import { useAuthService } from '@/application';
import type { Event, SignedInUser, UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { usePositions } from '@/ui/composables/Positions.ts';
import UserEventsTable from '@/ui/views/users/details/tabs/UserEventsTable.vue';
import {
    REGISTRATION_CAPTAIN,
    mockEvent,
    mockLocations,
    mockRegistrationCaptain,
    mockRouter,
    mockSignedInUser,
    mockSlotCaptain,
    mockUserCaptain,
    mockUserDetails,
} from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('UserEventsTable.vue', () => {
    const authService: AuthService = useAuthService();
    let testee: VueWrapper;
    let signedInUser: SignedInUser;
    let user: UserDetails;
    let events: Event[];

    beforeEach(async () => {
        authService.setSignedInUser(signedInUser);
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
        beforeAll(() => {
            signedInUser = mockSignedInUser();
            signedInUser.permissions.push(Permission.READ_USER_DETAILS);
            authService.setSignedInUser(signedInUser);
        });

        it('should not render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(false);
        });
    });

    describe('Users with permission users:write', () => {
        beforeAll(() => {
            signedInUser = mockSignedInUser();
            signedInUser.permissions.push(Permission.WRITE_USERS);
            authService.setSignedInUser(signedInUser);
        });

        it('should render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(true);
        });
    });
});
