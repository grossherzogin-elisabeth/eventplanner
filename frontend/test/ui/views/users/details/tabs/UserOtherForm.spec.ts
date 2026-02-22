import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { Permission } from '@/domain';
import UserOtherForm from '@/ui/views/users/details/tabs/UserOtherForm.vue';
import { mockUserCaptain, mockUserDetails } from '~/mocks';
import { setupUserPermissions } from '~/utils';

describe('UserOtherForm.vue', () => {
    let testee: VueWrapper;
    let user = mockUserDetails(mockUserCaptain());

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        testee = mount(UserOtherForm, {
            props: { modelValue: user, errors: {} },
        });
    });

    const inputs = [
        { selector: '[data-test-id="diet"] input', expected: 'Fleisch' },
        { selector: '[data-test-id="comment"] textarea', expected: user.comment },
        { selector: '[data-test-id="intolerances"] textarea', expected: user.intolerances },
    ];

    it.each(inputs)('should render $expected in $selector', async ({ selector, expected }) => {
        const input = testee.find(selector);
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        expect((input.element as any).value).toContain(expected);
    });

    describe('Users with permission users:read-details', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_USER_DETAILS]);
        });

        it.each(inputs)('should disable $selector input', async ({ selector }) => {
            const input = testee.find(selector);
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            expect((input.element as any).disabled).toBe(true);
        });
    });

    describe('Users with permission users:write', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_USER_DETAILS, Permission.WRITE_USERS]);
        });

        it.each(inputs)('should enable $selector input', async ({ selector }) => {
            const input = testee.find(selector);
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            expect((input.element as any).disabled).toBe(false);
        });
    });
});
