import type { Router } from 'vue-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserDetails } from '@/domain';
import { Permission } from '@/domain';
import type { UsePositions } from '@/ui/composables/Positions.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import type { UseQualifications } from '@/ui/composables/Qualifications.ts';
import { useQualifications } from '@/ui/composables/Qualifications.ts';
import UserQualificationsTable from '@/ui/views/users/details/components/UserQualificationsTable.vue';
import { mockRouter, mockUserCaptain, mockUserDetails } from '~/mocks';
import { openTableContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('UserQualificationsTable.vue', () => {
    let testee: VueWrapper;
    let user: UserDetails;
    let qualifications: UseQualifications;
    let positions: UsePositions;

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        qualifications = useQualifications();
        positions = usePositions();
        await qualifications.loading;
        await positions.loading;
        testee = mount(UserQualificationsTable, {
            props: { modelValue: user },
            global: { plugins: [router] },
        });
    });

    afterEach(() => testee.unmount());

    it('should render all qualifications', async () => {
        const tableRows = testee.findAll('tbody tr');
        expect(user.qualifications.length).toBeGreaterThan(0);
        expect(tableRows.length).toBe(user.qualifications.length);
    });

    it('should render qualification names', async () => {
        const table = testee.find('tbody');
        expect(user.qualifications.length).toBeGreaterThan(0);
        user.qualifications.forEach((userQualification) => {
            const qualification = qualifications.get(userQualification.qualificationKey);
            expect(table.text()).toContain(qualification.name);
        });
    });

    it('should render expired qualification status', async () => {
        const row = testee.findAll('tbody tr')[0];
        expect(row.text()).toContain('Abgelaufen');
    });

    it('should render expiration date', async () => {
        const row = testee.findAll('tbody tr')[0];
        expect(row.text()).toContain('10.07.2024');
    });

    it('should render valid qualification status', async () => {
        const row = testee.findAll('tbody tr')[1];
        expect(row.text()).toContain('Gültig');
    });

    describe('users with permission users:read-details', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_DETAILED_USERS]);
        });

        it('should not render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(false);
        });
    });

    describe('users with permission users:write', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_DETAILED_USERS, Permission.UPDATE_USERS]);
        });

        it('should render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(true);
        });

        it('should render context menu actions', async () => {
            const menu = await openTableContextMenu(testee, 0);
            expect(menu.find('[data-test-id="action-edit-qualification"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-delete-qualification"]').exists()).toBe(true);
        });

        it('should remove qualification', async () => {
            const qualificationCount = testee.findAll('tbody tr').length;
            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-delete-qualification"]');
            await action.trigger('click');
            expect(testee.findAll('tbody tr').length).toBe(qualificationCount - 1);
        });

        it('should open edit dialog', async () => {
            const menu = await openTableContextMenu(testee, 0);
            const action = menu.find('[data-test-id="action-edit-qualification"]');
            await action.trigger('click');
            const dialog = testee.find('[data-test-id="user-qualification-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
            // error-prone, as this relies on the first input to contain the qualification name
            // better way would be to select the specific input element by data-test-id
            expect(dialog.find('input').element.value).toEqual('Captain');
        });
    });
});
