import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { UserDetails } from '@/domain';
import { useCountries } from '@/ui/composables/Countries';
import { useNationalities } from '@/ui/composables/Nationalities';
import AccountDataTab from '@/ui/views/account/tabs/AccountDataTab.vue';
import { mockUserCaptain, mockUserDetails } from '~/mocks';
import { getLastEmittedModelValue, openCard, selectSelectionListOption, submit } from '~/utils';

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

    function mountTestee(overwrite?: Partial<UserDetails>): void {
        user = mockUserDetails(mockUserCaptain());
        if (overwrite) {
            Object.assign(user, overwrite);
        }
        testee = mount(AccountDataTab, {
            props: {
                modelValue: user,
            },
        });
    }

    function getUpdatedUser(): UserDetails {
        return getLastEmittedModelValue<UserDetails>(testee);
    }

    describe('name', () => {
        it('should render full name', async () => {
            expect(user.firstName).toBeTruthy();
            expect(user.secondName).toBeTruthy();
            expect(user.lastName).toBeTruthy();
            const nameCard = testee.find('[data-test-id="account-name-card"]');
            expect(nameCard.exists()).toBe(true);
            expect(nameCard.text()).toContain(user.firstName);
            expect(nameCard.text()).toContain(user.secondName);
            expect(nameCard.text()).toContain(user.lastName);
        });

        it('should render nick name instead of first name', async () => {
            testee.unmount();
            mountTestee({ nickName: 'Nickname' });

            const nameCard = testee.find('[data-test-id="account-name-card"]');
            expect(nameCard.exists()).toBe(true);
            expect(nameCard.text()).toContain('Nickname');
            expect(nameCard.text()).not.toContain(user.firstName);
        });

        it('should update nick name', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-name-card"]');
            await testee.find('[data-test-id="account-name-input-nick-name"] input').setValue('New Nickname');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.nickName).toEqual('New Nickname');
        });
    });

    describe('gender', () => {
        it('should render selected gender', async () => {
            expect(user.gender).toBeTruthy();
            const genderCard = testee.find('[data-test-id="account-gender-card"]');
            expect(genderCard.exists()).toBe(true);
            expect(genderCard.text()).toContain(testee.vm.$t(`generic.gender.${user.gender}`));
        });

        it('should update gender', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-gender-card"]');
            await selectSelectionListOption(testee, '[data-test-id="account-gender-input"]', testee.vm.$t('generic.gender.m'));
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.gender).toEqual('m');
        });
    });

    describe('date-and-place-of-birth', () => {
        it('should render date and place of birth', async () => {
            expect(user.placeOfBirth).toBeTruthy();
            expect(user.dateOfBirth).toBeTruthy();
            const dateAndPlaceCard = testee.find('[data-test-id="account-date-and-place-of-birth-card"]');
            expect(dateAndPlaceCard.exists()).toBe(true);
            expect(dateAndPlaceCard.text()).toContain('01.12.2024');
            expect(dateAndPlaceCard.text()).toContain(user.placeOfBirth);
        });

        it('should not allow editing date and place of birth', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-date-and-place-of-birth-card"]');
            const dateInput = dialog.find('[data-test-id="account-date-and-place-of-birth-input-date-of-birth"] input')
                .element as HTMLInputElement;
            const placeInput = dialog.find('[data-test-id="account-date-and-place-of-birth-input-place-of-birth"] input')
                .element as HTMLInputElement;
            expect(dateInput.disabled).toBe(true);
            expect(placeInput.disabled).toBe(true);
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.dateOfBirth).toEqual(user.dateOfBirth);
            expect(updatedUser.placeOfBirth).toEqual(user.placeOfBirth);
        });
    });

    describe('nationality', () => {
        it('should render nationality', async () => {
            expect(user.nationality).toBeTruthy();
            const nationalities = useNationalities();
            const nationalityCard = testee.find('[data-test-id="account-nationality-card"]');
            expect(nationalityCard.exists()).toBe(true);
            expect(nationalityCard.text()).toContain(nationalities.getName(user.nationality!));
        });

        it('should update nationality', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-nationality-card"]');
            const targetNationality = useNationalities().options.find((it) => it.value === 'AT');
            expect(targetNationality).toBeTruthy();
            await selectSelectionListOption(testee, '[data-test-id="account-nationality-input"]', targetNationality!.label);
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.nationality).toEqual('AT');
        });
    });

    describe('pass-number', () => {
        it('should render pass number', async () => {
            expect(user.passNr).toBeTruthy();
            const passNumberCard = testee.find('[data-test-id="account-pass-number-card"]');
            expect(passNumberCard.exists()).toBe(true);
            expect(passNumberCard.text()).toContain(user.passNr);
        });

        it('should update pass number', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-pass-number-card"]');
            await testee.find('[data-test-id="account-pass-number-input"] input').setValue('new pass 1');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.passNr).toEqual('NEW PASS 1');
        });
    });

    describe('email', () => {
        it('should render email', async () => {
            expect(user.email).toBeTruthy();
            const emailCard = testee.find('[data-test-id="account-email-card"]');
            expect(emailCard.exists()).toBe(true);
            expect(emailCard.text()).toContain(user.email);
        });

        it('should not allow editing email', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-email-card"]');
            const emailInput = dialog.find('[data-test-id="account-email-input"] input').element as HTMLInputElement;
            expect(emailInput.disabled).toBe(true);
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.email).toEqual(user.email);
        });
    });

    describe('phone', () => {
        it('should render phone number', async () => {
            expect(user.phone).toBeTruthy();
            const phoneCard = testee.find('[data-test-id="account-phone-card"]');
            expect(phoneCard.exists()).toBe(true);
            expect(phoneCard.text()).toContain(user.phone);
        });

        it('should update phone number', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-phone-card"]');
            await testee.find('[data-test-id="account-phone-input"] input').setValue('+49 999 11111');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.phone).toEqual('+49 999 11111');
        });
    });

    describe('phone-mobile', () => {
        it('should render phone number', async () => {
            expect(user.mobile).toBeTruthy();
            const phoneMobileCard = testee.find('[data-test-id="account-phone-mobile-card"]');
            expect(phoneMobileCard.exists()).toBe(true);
            expect(phoneMobileCard.text()).toContain(user.mobile);
        });

        it('should update phone number', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-phone-mobile-card"]');
            await testee.find('[data-test-id="account-phone-mobile-input"] input').setValue('+49 999 22222');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.mobile).toEqual('+49 999 22222');
        });
    });

    describe('phone-work', () => {
        it('should render phone number', async () => {
            expect(user.phoneWork).toBeTruthy();
            const phoneWorkCard = testee.find('[data-test-id="account-phone-work-card"]');
            expect(phoneWorkCard.exists()).toBe(true);
            expect(phoneWorkCard.text()).toContain(user.phoneWork);
        });

        it('should update phone number', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-phone-work-card"]');
            await testee.find('[data-test-id="account-phone-work-input"] input').setValue('+49 999 33333');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.phoneWork).toEqual('+49 999 33333');
        });
    });

    describe('address', () => {
        it('should render address', async () => {
            expect(user.address.addressLine1).toBeTruthy();
            expect(user.address.addressLine2).toBeTruthy();
            expect(user.address.zipcode).toBeTruthy();
            expect(user.address.town).toBeTruthy();
            expect(user.address.country).toBeTruthy();

            const countries = useCountries();
            const addressCard = testee.find('[data-test-id="account-address-card"]');

            expect(addressCard.exists()).toBe(true);
            expect(addressCard.text()).toContain(user.address.addressLine1);
            expect(addressCard.text()).toContain(user.address.addressLine2 ?? '');
            expect(addressCard.text()).toContain(user.address.zipcode);
            expect(addressCard.text()).toContain(user.address.town);
            expect(addressCard.text()).toContain(countries.getName(user.address.country));
        });

        it('should update address', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-address-card"]');
            await testee.find('[data-test-id="account-address-input-line1"] input').setValue('Updated Street 13');
            await testee.find('[data-test-id="account-address-input-line2"] input').setValue('3rd Floor');
            await testee.find('[data-test-id="account-address-input-zipcode"] input').setValue('54321');
            await testee.find('[data-test-id="account-address-input-town"] input').setValue('Updated Town');
            await testee.find('[data-test-id="account-address-input-country"] input').trigger('keydown.down');
            await testee.find('[data-test-id="account-address-input-country"] input').trigger('keydown.down');
            await testee.find('[data-test-id="account-address-input-country"] input').trigger('keydown.enter');

            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.address.addressLine1).toEqual('Updated Street 13');
            expect(updatedUser.address.addressLine2).toEqual('3rd Floor');
            expect(updatedUser.address.zipcode).toEqual('54321');
            expect(updatedUser.address.town).toEqual('Updated Town');
            expect(updatedUser.address.country).not.toEqual(user.address.country);
        });
    });

    describe('diet', () => {
        it('should render diet', async () => {
            expect(user.diet).toBeTruthy();
            const dietCard = testee.find('[data-test-id="account-diet-card"]');
            expect(dietCard.exists()).toBe(true);
            expect(dietCard.text()).toContain(testee.vm.$t(`generic.diet.${user.diet}`));
        });

        it('should update diet', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-diet-card"]');
            await selectSelectionListOption(testee, '[data-test-id="account-diet-input"]', testee.vm.$t('generic.diet.vegetarian'));
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.diet).toEqual('vegetarian');
        });
    });

    describe('intolerances', () => {
        it('should render intolerances', async () => {
            expect(user.intolerances).toBeTruthy();
            const intolerancesCard = testee.find('[data-test-id="account-intolerances-card"]');
            expect(intolerancesCard.exists()).toBe(true);
            expect(intolerancesCard.text()).toContain(user.intolerances);
        });

        it('should update intolerances', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-intolerances-card"]');
            await testee.find('[data-test-id="account-intolerances-input"] textarea').setValue('Updated intolerances');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.intolerances).toEqual('Updated intolerances');
        });
    });

    describe('emergency-contact', () => {
        it('should render emergency contact name', async () => {
            expect(user.emergencyContact.name).toBeTruthy();
            const emergencyContactCard = testee.find('[data-test-id="account-emergency-contact-card"]');
            expect(emergencyContactCard.exists()).toBe(true);
            expect(emergencyContactCard.text()).toContain(user.emergencyContact.name);
        });

        it('should update emergency contact', async () => {
            const dialog = await openCard(testee, '[data-test-id="account-emergency-contact-card"]');
            await testee.find('[data-test-id="account-emergency-contact-input-name"] input').setValue('Updated Contact');
            await testee.find('[data-test-id="account-emergency-contact-input-phone"] input').setValue('+49 999 44444');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.emergencyContact.name).toEqual('Updated Contact');
            expect(updatedUser.emergencyContact.phone).toEqual('+49 999 44444');
        });
    });

    describe('medication', () => {
        it('should not render medication in overview', async () => {
            expect(user.medication).toBeTruthy();
            const medicationCard = testee.find('[data-test-id="account-medication-card"]');
            expect(medicationCard.exists()).toBe(true);
            expect(medicationCard.text()).not.toContain(user.medication);
        });

        it('should render medication in dialog', async () => {
            expect(user.medication).toBeTruthy();
            const dialog = await openCard(testee, 'div[data-test-id="account-medication-card"]');
            const input = dialog.find('[data-test-id="account-medication-input"] textarea') as DOMWrapper<HTMLTextAreaElement>;
            expect(input.element.value).toContain(user.medication);
        });

        it('should update medication', async () => {
            const card = testee.find('[data-test-id="account-medication-card"]');
            expect(card.exists()).toBe(true);
            await card.trigger('click');
            const dialog = testee.find('[data-test-id="dialog"]');
            await testee.find('[data-test-id="account-medication-input"] textarea').setValue('Updated medication');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.medication).toEqual('Updated medication');
        });
    });

    describe('diseases', () => {
        it('should not render diseases in overview', async () => {
            expect(user.diseases).toBeTruthy();
            const diseasesCard = testee.find('[data-test-id="account-diseases-card"]');
            expect(diseasesCard.exists()).toBe(true);
            expect(diseasesCard.text()).not.toContain(user.diseases);
        });

        it('should render diseases in dialog', async () => {
            const dialog = await openCard(testee, 'div[data-test-id="account-diseases-card"]');
            const input = dialog.find('[data-test-id="account-diseases-input"] textarea') as DOMWrapper<HTMLTextAreaElement>;
            expect(user.diseases).toBeTruthy();
            expect(input.element.value).toContain(user.diseases);
        });

        it('should update diseases', async () => {
            const card = testee.find('[data-test-id="account-diseases-card"]');
            expect(card.exists()).toBe(true);
            await card.trigger('click');
            const dialog = testee.find('[data-test-id="dialog"]');
            await testee.find('[data-test-id="account-diseases-input"] textarea').setValue('Updated diseases');
            await submit(dialog);

            const updatedUser = getUpdatedUser();
            expect(updatedUser.diseases).toEqual('Updated diseases');
        });
    });
});
