import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { AuthUseCase, EventUseCase, PositionUseCase, UserAdministrationUseCase, UsersUseCase } from '@/application';
import { useAuthUseCase, useEventUseCase, usePositionUseCase, useUserAdministrationUseCase, useUsersUseCase } from '@/application';
import type { Event, User } from '@/domain';
import { EventType, Permission, Role } from '@/domain';
import { Routes } from '@/ui/views/Routes.ts';
import UsersListView from '@/ui/views/users/list/UsersListView.vue';
import { mockEvent, mockRegistrationCaptain, mockRouter, mockSlotCaptain, mockUserCaptain } from '~/mocks';
import { openTableContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('UsersListView.vue', () => {
    let testee: VueWrapper;
    let eventUseCase: EventUseCase;
    let usersUseCase: UsersUseCase;
    let authUseCase: AuthUseCase;
    let userAdministrationUseCase: UserAdministrationUseCase;
    let positionUseCase: PositionUseCase;
    let users: User[];
    let events: Event[];

    beforeEach(async () => {
        setupUserPermissions([Permission.READ_USER_DETAILS, Permission.WRITE_USERS, Permission.WRITE_REGISTRATIONS]);
        vi.setSystemTime(new Date('2026-01-10T08:00:00Z'));
        eventUseCase = useEventUseCase();
        usersUseCase = useUsersUseCase();
        authUseCase = useAuthUseCase();
        userAdministrationUseCase = useUserAdministrationUseCase();
        positionUseCase = usePositionUseCase();

        users = [
            mockUserCaptain({ key: 'member-active-unverified', firstName: 'Tina', lastName: 'Member', verified: false }),
            mockUserCaptain({ key: 'member-inactive', firstName: 'Ina', lastName: 'Member', verified: true }),
            mockUserCaptain({ key: 'admin-user', firstName: 'Adam', lastName: 'Admin', roles: [Role.ADMIN] }),
            mockUserCaptain({ key: 'unknown-user', firstName: 'Una', lastName: 'Unknown', roles: [] }),
        ];
        events = [
            mockEvent({
                key: 'future-event',
                name: 'Future Event',
                type: EventType.WeekendEvent,
                start: new Date('2026-05-10T08:00:00Z'),
                registrations: [mockRegistrationCaptain({ userKey: users[0].key })],
                slots: [mockSlotCaptain({ assignedRegistrationKey: mockRegistrationCaptain().key })],
            }),
        ];

        vi.spyOn(positionUseCase, 'getPositions').mockResolvedValue([]);
        vi.spyOn(usersUseCase, 'getUsers').mockImplementation(async () =>
            users.map((it) => ({ ...it, positionKeys: [...(it.positionKeys ?? [])] }))
        );
        vi.spyOn(eventUseCase, 'getEvents').mockImplementation(async (year: number) =>
            events.filter((event) => event.start.getFullYear() === year).map((it) => ({ ...it }))
        );
        vi.spyOn(authUseCase, 'impersonateUser').mockImplementation(() => undefined);
        vi.spyOn(userAdministrationUseCase, 'contactUsers').mockResolvedValue();

        await router.push({ name: Routes.UsersList });
        testee = mount(UsersListView, { global: { plugins: [router] } });
    });

    afterEach(() => testee.unmount());

    it('should render non admin users in default tab', async () => {
        await loading();
        expect(testee.text()).toContain(`${users[0].firstName} ${users[0].lastName}`);
        expect(testee.text()).toContain(`${users[1].firstName} ${users[1].lastName}`);
        expect(testee.text()).not.toContain(`${users[2].firstName} ${users[2].lastName}`);
        expect(testee.text()).not.toContain(`${users[3].firstName} ${users[3].lastName}`);
    });

    it('should render admins on admin tab', async () => {
        await loading();
        await testee.find('[data-test-id="tab-admins"]').trigger('click');
        expect(testee.text()).toContain(`${users[2].firstName} ${users[2].lastName}`);
        expect(testee.text()).not.toContain(`${users[0].firstName} ${users[0].lastName}`);
    });

    it('should filter team members to only unverified users', async () => {
        await loading();
        await testee.find('[data-test-id="filter-not-verified"]').trigger('click');
        expect(testee.text()).toContain(`${users[0].firstName} ${users[0].lastName}`);
        expect(testee.text()).not.toContain(`${users[1].firstName} ${users[1].lastName}`);
    });

    it('should contact selected user from row context menu', async () => {
        const contactUsers = vi.spyOn(userAdministrationUseCase, 'contactUsers');
        await loading();

        const menu = await openTableContextMenu(testee, 0);
        await menu.find('[data-test-id="action-contact"]').trigger('click');

        expect(contactUsers).toHaveBeenCalledWith([expect.objectContaining({ key: users[0].key })]);
    });

    it('should impersonate selected user from row context menu', async () => {
        const impersonateUser = vi.spyOn(authUseCase, 'impersonateUser');
        await loading();

        const menu = await openTableContextMenu(testee, 0);
        await menu.find('[data-test-id="action-impersonate"]').trigger('click');

        expect(impersonateUser).toHaveBeenCalledWith(users[0].key);
    });

    it('should navigate to user details on row click', async () => {
        await loading();
        const row = testee.findAll('tbody tr')[0];
        await row.trigger('click');

        expect(router.push).toHaveBeenCalledWith({ name: Routes.UserDetails, params: { key: users[0].key } });
    });

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="table-loading"]').exists()).toBe(false);
    }
});
