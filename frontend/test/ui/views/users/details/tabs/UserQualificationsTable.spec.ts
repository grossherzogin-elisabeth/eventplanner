import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { useQualifications } from '@/ui/composables/Qualifications.ts';
import UserQualificationsTable from '@/ui/views/users/details/tabs/UserQualificationsTable.vue';
import { mockRouter, mockUserCaptain, mockUserDetails } from '~/mocks';
import { setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('UserQualificationsTable.vue', () => {
    let testee: VueWrapper;
    let user: UserDetails;

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        testee = mount(UserQualificationsTable, {
            props: { modelValue: user },
        });
        await nextTick();
    });

    it('should render all qualifications', async () => {
        const tableRows = testee.findAll('tbody tr');
        expect(user.qualifications.length).toBeGreaterThan(0);
        expect(tableRows.length).toBe(user.qualifications.length);
    });

    it('should render qualification names', async () => {
        const qualifications = useQualifications();
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
            setupUserPermissions([Permission.READ_USER_DETAILS]);
        });

        it('should not render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(false);
        });
    });

    describe('users with permission users:write', () => {
        beforeAll(() => {
            setupUserPermissions([Permission.READ_USER_DETAILS, Permission.WRITE_USERS]);
        });

        it('should render context menu', async () => {
            expect(testee.find('[data-test-id="table-context-menu-trigger"]').exists()).toBe(true);
        });
    });
});
