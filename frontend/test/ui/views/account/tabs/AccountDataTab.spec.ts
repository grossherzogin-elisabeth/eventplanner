import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserDetails } from '@/domain';
import { useCountries } from '@/ui/composables/Countries';
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

    afterEach(() => testee.unmount());

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

    describe('address', () => {
        it('should render users address', async () => {
            const countries = useCountries();
            const addressCard = testee.find('[data-test-id="account-address-card"]');

            expect(addressCard.exists()).toBe(true);
            expect(addressCard.text()).toContain(user.address.addressLine1);
            expect(addressCard.text()).toContain(user.address.addressLine2 ?? '');
            expect(addressCard.text()).toContain(user.address.zipcode);
            expect(addressCard.text()).toContain(user.address.town);
            expect(addressCard.text()).toContain(countries.getName(user.address.country));
        });

        it('should update users address', async () => {
            const addressCard = testee.find('[data-test-id="account-address-card"]');
            expect(addressCard.exists()).toBe(true);
            await addressCard.trigger('click');

            const dialog = testee.find('[data-test-id="dialog"]');
            expect(dialog.exists()).toBe(true);

            await testee.find('[data-test-id="account-address-input-line1"] input').setValue('Updated Street 13');
            await testee.find('[data-test-id="account-address-input-line2"] input').setValue('3rd Floor');
            await testee.find('[data-test-id="account-address-input-zipcode"] input').setValue('54321');
            await testee.find('[data-test-id="account-address-input-town"] input').setValue('Updated Town');
            await dialog.find('[data-test-id="button-submit"]').trigger('click');

            const emitted = testee.emitted('update:modelValue');
            expect(emitted).toHaveLength(1);

            const updatedUser = emitted?.[0][0] as UserDetails;
            expect(updatedUser.address.addressLine1).toEqual('Updated Street 13');
            expect(updatedUser.address.addressLine2).toEqual('3rd Floor');
            expect(updatedUser.address.zipcode).toEqual('54321');
            expect(updatedUser.address.town).toEqual('Updated Town');
        });
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
