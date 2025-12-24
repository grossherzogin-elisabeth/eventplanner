import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAuthService } from '@/application';
import { Permission } from '@/domain';
import TabEventDetailsForm from '@/ui/views/events/edit/TabEventDetailsForm.vue';
import { mockEvent, mockSignedInUser } from '~/mocks';

describe('TabEventDetailsForm.vue', () => {
    const authService = useAuthService();
    let signedInUser = mockSignedInUser();
    let testee: VueWrapper;

    beforeEach(async () => {
        authService.setSignedInUser(signedInUser);
        testee = mount(TabEventDetailsForm, {
            props: {
                'event': mockEvent(),
                // simulate a reactive prop passed as v-model:event
                'onUpdate:event': (e) => testee.setProps({ event: e }),
            },
        });
    });

    describe('Users with permission events:write-details', () => {
        beforeAll(() => {
            signedInUser = mockSignedInUser();
            signedInUser.permissions.push(Permission.WRITE_EVENT_DETAILS);
            authService.setSignedInUser(signedInUser);
        });

        it('should enable all inputs', () => {
            const inputs = testee.findAll('input');
            for (const input of inputs) {
                expect(input.element.disabled).toBe(false);
            }
        });

        it('should run validation and display validation errors on user input', async () => {
            const inputWrapper = testee.find('[data-test-id="input-name"]');
            const input = inputWrapper.find('input');
            await input.setValue('very long text that should cause a max length validation error');
            expect(input.classes()).toContain('invalid');
        });
    });

    describe('Users without permission events:write-details', () => {
        beforeAll(() => {
            signedInUser = mockSignedInUser();
            signedInUser.permissions = signedInUser.permissions.filter((it) => it !== Permission.WRITE_EVENT_DETAILS);
            authService.setSignedInUser(signedInUser);
        });

        it('should disable all inputs', () => {
            const inputs = testee.findAll('input');
            for (const input of inputs) {
                expect(input.element.disabled).toBe(true);
            }
        });
    });
});
