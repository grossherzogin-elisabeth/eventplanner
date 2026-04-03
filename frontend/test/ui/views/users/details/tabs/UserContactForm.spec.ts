import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { Permission } from '@/domain';
import UserContactForm from '@/ui/views/users/details/tabs/UserContactForm.vue';
import { mockUserCaptain, mockUserDetails } from '~/mocks';
import { setupUserPermissions } from '~/utils';

describe('UserContactForm.vue', () => {
    let testee: VueWrapper;
    let user = mockUserDetails(mockUserCaptain());

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        testee = mount(UserContactForm, {
            props: { modelValue: user, errors: {} },
        });
    });

    const inputs = [
        { selector: '[data-test-id="address-line-1"] input', expected: user.address.addressLine1 },
        { selector: '[data-test-id="address-line-2"] input', expected: user.address.addressLine2 },
        { selector: '[data-test-id="address-town"] input', expected: user.address.town },
        { selector: '[data-test-id="address-zipcode"] input', expected: user.address.zipcode },
        { selector: '[data-test-id="address-country"] input', expected: 'Deutschland' },
        { selector: '[data-test-id="email"] input', expected: user.email },
        { selector: '[data-test-id="phone"] input', expected: user.phone },
        { selector: '[data-test-id="phone-work"] input', expected: user.phoneWork },
        { selector: '[data-test-id="mobile"] input', expected: user.mobile },
    ];

    it.each(inputs)('should render $expected', async ({ selector, expected }) => {
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
