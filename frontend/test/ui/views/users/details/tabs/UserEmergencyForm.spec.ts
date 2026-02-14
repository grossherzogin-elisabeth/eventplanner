import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAuthService } from '@/application';
import { Permission } from '@/domain';
import UserEmergencyForm from '@/ui/views/users/details/tabs/UserEmergencyForm.vue';
import { mockSignedInUser, mockUserCaptain, mockUserDetails } from '~/mocks';

describe('UserEmergencyForm.vue', () => {
    const authService = useAuthService();
    let testee: VueWrapper;
    let signedInUser = mockSignedInUser();
    let user = mockUserDetails(mockUserCaptain());

    beforeEach(async () => {
        authService.setSignedInUser(signedInUser);
        user = mockUserDetails(mockUserCaptain());
        testee = mount(UserEmergencyForm, {
            props: { modelValue: user, errors: {} },
        });
    });

    const inputs = [
        { selector: '[data-test-id="emergency-contact-name"] input', expected: user.emergencyContact.name },
        { selector: '[data-test-id="emergency-contact-phone"] input', expected: user.emergencyContact.phone },
        { selector: '[data-test-id="diseases"] textarea', expected: user.diseases },
        { selector: '[data-test-id="medication"] textarea', expected: user.medication },
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
