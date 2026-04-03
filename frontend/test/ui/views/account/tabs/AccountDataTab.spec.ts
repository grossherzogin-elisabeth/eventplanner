import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserDetails } from '@/domain';
import AccountDataTab from '@/ui/views/account/tabs/AccountDataTab.vue';
import { mockUserCaptain, mockUserDetails } from '~/mocks';

describe('AccountDataTab.vue', () => {
    let testee: VueWrapper;
    let user: UserDetails;

    beforeEach(async () => {
        user = mockUserDetails(mockUserCaptain());
        testee = mount(AccountDataTab, {
            props: {
                modelValue: user,
            },
        });
    });

    it('should render users full name', async () => {
        expect(testee.text()).toContain(user.firstName);
        expect(testee.text()).toContain(user.secondName);
        expect(testee.text()).toContain(user.lastName);
    });

    it('should render users gender', async () => {
        expect(testee.text()).toContain(testee.vm.$t(`generic.gender.${user.gender}`));
    });

    it('should render users date of birth', async () => {
        expect(testee.text()).toContain('01.12.2024');
    });

    it('should render users place of birth', async () => {
        expect(testee.text()).toContain(user.placeOfBirth);
    });

    it('should render users nationality', async () => {
        expect(testee.text()).toContain('Deutsch');
    });

    it('should render users pass number', async () => {
        expect(testee.text()).toContain(user.passNr);
    });

    it('should render users email address', async () => {
        expect(testee.text()).toContain(user.email);
    });

    it('should render users phone number', async () => {
        expect(testee.text()).toContain(user.phone);
    });

    it('should render users work phone number', async () => {
        expect(testee.text()).toContain(user.phoneWork);
    });

    it('should render users mobile phone number', async () => {
        expect(testee.text()).toContain(user.mobile);
    });

    it('should render users diet', async () => {
        expect(testee.text()).toContain(testee.vm.$t(`generic.diet.${user.diet}`));
    });

    it('should render users intolerances', async () => {
        expect(testee.text()).toContain(user.intolerances);
    });

    it('should render users emergency contact', async () => {
        expect(testee.text()).toContain(user.emergencyContact.name);
    });

    it('should not render users medication', async () => {
        // should only be visible when editing
        expect(testee.text()).not.toContain(user.medication);
    });

    it('should not render users diseases', async () => {
        // should only be visible when editing
        expect(testee.text()).not.toContain(user.diseases);
    });
});
