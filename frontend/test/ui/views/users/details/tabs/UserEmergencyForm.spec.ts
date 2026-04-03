import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { Permission } from '@/domain';
import UserEmergencyForm from '@/ui/views/users/details/tabs/UserEmergencyForm.vue';
import { mockUserCaptain, mockUserDetails } from '~/mocks';
import { setupUserPermissions } from '~/utils';

describe('UserEmergencyForm.vue', () => {
    let testee: VueWrapper;
    let user = mockUserDetails(mockUserCaptain());

    beforeEach(async () => {
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
