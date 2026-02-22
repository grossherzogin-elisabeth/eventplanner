import type { Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserDetails } from '@/domain';
import { Role } from '@/domain';
import { Permission } from '@/domain';
import UserRolesTable from '@/ui/views/users/details/tabs/UserRolesTable.vue';
import { mockRouter, mockUserCaptain, mockUserDetails } from '~/mocks';
import { openTableContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('UserRolesTable.vue', () => {
    let testee: VueWrapper;
    let user: UserDetails;

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        testee = mount(UserRolesTable, {
            props: { modelValue: user },
            global: { plugins: [router] },
        });
    });

    it('should render all roles', async () => {
        const tableRows = testee.findAll('tbody tr');
        expect(tableRows.length).toBe(Object.values(Role).length - 1); // exclude role NONE
    });

    it('should render correct role status', async () => {
        const rowAssigned = testee.findAll('tbody tr')[0];
        expect(rowAssigned.find('[data-test-id="status-assigned"]').exists()).toBe(true);
        expect(rowAssigned.find('[data-test-id="status-not-assigned"]').exists()).toBe(false);

        const rowNotAssigned = testee.findAll('tbody tr')[5];
        expect(rowNotAssigned.find('[data-test-id="status-assigned"]').exists()).toBe(false);
        expect(rowNotAssigned.find('[data-test-id="status-not-assigned"]').exists()).toBe(true);
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

        it('should add role', async () => {
            const row = testee.findAll('tbody tr')[5];
            const menu = await openTableContextMenu(testee, row);
            const action = menu.find('[data-test-id="action-add-role"]');
            await action.trigger('click');
            expect(row.find('[data-test-id="status-assigned"]').exists()).toBe(true);
            expect(row.find('[data-test-id="status-not-assigned"]').exists()).toBe(false);
        });

        it('should remove role', async () => {
            const row = testee.findAll('tbody tr')[0];
            const menu = await openTableContextMenu(testee, row);
            const action = menu.find('[data-test-id="action-remove-role"]');
            await action.trigger('click');
            expect(row.find('[data-test-id="status-assigned"]').exists()).toBe(false);
            expect(row.find('[data-test-id="status-not-assigned"]').exists()).toBe(true);
        });
    });
});
