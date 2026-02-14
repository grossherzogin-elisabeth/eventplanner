import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAuthService } from '@/application';
import { Permission } from '@/domain';
import UserDataForm from '@/ui/views/users/details/tabs/UserDataForm.vue';
import { mockSignedInUser, mockUserCaptain, mockUserDetails } from '~/mocks';

describe('UserDataForm.vue', () => {
    const authService = useAuthService();
    let testee: VueWrapper;
    let signedInUser = mockSignedInUser();
    let user = mockUserDetails(mockUserCaptain());

    beforeEach(async () => {
        authService.setSignedInUser(signedInUser);
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
        beforeAll(() => {
            signedInUser = mockSignedInUser();
            signedInUser.permissions.push(Permission.READ_USER_DETAILS);
            authService.setSignedInUser(signedInUser);
        });

        it.each(inputs)('should disable $selector input', async ({ selector }) => {
            const input = testee.find(selector);
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            expect((input.element as any).disabled).toBe(true);
        });
    });

    describe('Users with permission users:write', () => {
        beforeAll(() => {
            signedInUser = mockSignedInUser();
            signedInUser.permissions.push(Permission.WRITE_USERS);
            authService.setSignedInUser(signedInUser);
        });

        it.each(inputs)('should enable $selector input', async ({ selector }) => {
            const input = testee.find(selector);
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            expect((input.element as any).disabled).toBe(false);
        });
    });
});
