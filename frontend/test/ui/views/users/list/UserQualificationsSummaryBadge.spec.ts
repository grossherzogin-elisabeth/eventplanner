import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { User } from '@/domain';
import UserQualificationsSummaryBadge from '@/ui/views/users/list/UserQualificationsSummaryBadge.vue';
import { QUALIFICATION_CAPTAIN, QUALIFICATION_ENGINEER, QUALIFICATION_EXPIRES, mockUserEngineer, mockUserMate } from '~/mocks';

describe('UserQualificationsSummaryBadge.vue', () => {
    let testee: VueWrapper;

    beforeEach(() => {
        vi.setSystemTime(new Date('2024-01-01T00:00:00Z'));
    });

    afterEach(() => testee?.unmount());

    it('should render no-information badge when user has no qualifications', () => {
        const user = mockUserEngineer({ qualifications: [] });
        testee = mountTestee(user);

        expect(testee.find('[data-test-id="qualification-summary-no-information"]').exists()).toBe(true);
    });

    it('should render expired badge when user has expired qualifications', () => {
        const user = mockUserMate();
        testee = mountTestee(user);

        const badge = testee.find('[data-test-id="qualification-summary-expired"]');
        expect(badge.exists()).toBe(true);
        expect(badge.attributes('title')).toContain(QUALIFICATION_EXPIRES);
    });

    it('should render expiring-soon badge when qualifications expire in less than three months', () => {
        const user = mockUserEngineer({
            qualifications: [{ qualificationKey: QUALIFICATION_CAPTAIN, expires: true, expiresAt: new Date('2024-02-20T00:00:00Z') }],
        });
        testee = mountTestee(user);

        const badge = testee.find('[data-test-id="qualification-summary-expiring-soon"]');
        expect(badge.exists()).toBe(true);
    });

    it('should render all-valid badge when all qualifications are valid', () => {
        const user = mockUserEngineer({
            qualifications: [{ qualificationKey: QUALIFICATION_ENGINEER, expires: false }],
        });
        testee = mountTestee(user);

        const badge = testee.find('[data-test-id="qualification-summary-all-valid"]');
        expect(badge.exists()).toBe(true);
    });

    it('should render neutral placeholder when no user is provided', () => {
        testee = mount(UserQualificationsSummaryBadge);
        expect(testee.find('[data-test-id="qualification-summary-placeholder"]').exists()).toBe(true);
    });

    function mountTestee(user?: User): VueWrapper {
        return mount(UserQualificationsSummaryBadge, { props: { user } });
    }
});
