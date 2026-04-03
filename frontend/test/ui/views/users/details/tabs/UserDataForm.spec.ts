import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { Permission } from '@/domain';
import UserDataForm from '@/ui/views/users/details/tabs/UserDataForm.vue';
import { mockUserCaptain, mockUserDetails } from '~/mocks';
import { setupUserPermissions } from '~/utils';

describe('UserDataForm.vue', () => {
    let testee: VueWrapper;
    let user = mockUserDetails(mockUserCaptain());

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        testee = mount(UserDataForm, {
            props: { modelValue: user, errors: {} },
        });
    });

    const inputs = [
        { selector: '[data-test-id="first-name"] input', expected: user.firstName },
        { selector: '[data-test-id="second-name"] input', expected: user.secondName },
        { selector: '[data-test-id="last-name"] input', expected: user.lastName },
        { selector: '[data-test-id="date-of-birth"] input', expected: '01.12.2024' },
        { selector: '[data-test-id="place-of-birth"] input', expected: user.placeOfBirth },
        { selector: '[data-test-id="pass-number"] input', expected: user.passNr },
        { selector: '[data-test-id="nationality"] input', expected: 'Deutsch' },
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
