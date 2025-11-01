<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <DetailsPage>
            <template #header> {{ $t('navigation.account') }} </template>
            <template #content>
                <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-20 xl:pt-8">
                    <template #[Tab.PERSONAL_DATA]>
                        <div v-if="userDetails" class="items-start gap-16 md:flex lg:gap-20 xl:max-w-5xl">
                            <div class="w-full max-w-2xl space-y-8 md:w-2/3 md:flex-grow 2xl:w-1/2">
                                <section class="app-data">
                                    <h2 class="mb-4 font-bold text-secondary">
                                        {{ $t('views.account.data.title-app') }}
                                    </h2>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.nickName"
                                            :label="$t('views.account.data.display-name')"
                                            :placeholder="userDetails.firstName"
                                            :errors="validation.errors.value['nickName']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                </section>
                                <section class="diet-data">
                                    <h2 class="mb-4 font-bold text-secondary">
                                        {{ $t('views.account.data.diet.title') }}
                                    </h2>
                                    <div class="mb-4 sm:w-64">
                                        <VInputSelect
                                            v-model="userDetails.diet"
                                            :label="$t('views.account.data.diet.label')"
                                            :options="[
                                                { value: 'omnivore', label: $t('views.account.data.diet.values.omnivore') },
                                                { value: 'vegetarian', label: $t('views.account.data.diet.values.vegetarian') },
                                                { value: 'vegan', label: $t('views.account.data.diet.values.vegan') },
                                            ]"
                                            :errors="validation.errors.value['diet']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputTextArea
                                            v-model.trim="userDetails.intolerances"
                                            :label="$t('views.account.data.diet.intolerances')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['intolerances']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                </section>
                                <section class="personal-data">
                                    <h2 class="mb-4 font-bold text-secondary">
                                        {{ $t('views.account.data.personal.title') }}
                                    </h2>
                                    <div class="mb-4 sm:w-64">
                                        <VInputSelect
                                            v-model="userDetails.gender"
                                            :label="$t('views.account.data.personal.gender.label')"
                                            :options="genderOptions"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['gender']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            :label="$t('views.account.data.personal.first-name')"
                                            :model-value="`${userDetails.firstName} ${userDetails.secondName || ''}`.trim()"
                                            required
                                            disabled
                                            :placeholder="$t('generic.no-information')"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.secondName"
                                            :label="$t('views.account.data.personal.middle-name')"
                                            disabled
                                            :placeholder="$t('generic.no-information')"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.lastName"
                                            :label="$t('views.account.data.personal.last-name')"
                                            required
                                            disabled
                                            :placeholder="$t('generic.no-information')"
                                        />
                                    </div>
                                    <div class="flex flex-col sm:flex-row sm:space-x-4">
                                        <div class="mb-4 sm:w-64">
                                            <VInputDate
                                                v-model="userDetails.dateOfBirth"
                                                :label="$t('views.account.data.personal.birthday')"
                                                required
                                                :disabled="!enableEditingDateOfBirth"
                                                :placeholder="$t('generic.no-information')"
                                                :errors="validation.errors.value['dateOfBirth']"
                                                :errors-visible="validation.showErrors.value"
                                            />
                                        </div>
                                        <div class="mb-4 sm:flex-grow">
                                            <VInputText
                                                v-model="userDetails.placeOfBirth"
                                                :label="$t('views.account.data.personal.place-of-birth')"
                                                required
                                                :disabled="!enableEditingPlaceOfBirth"
                                                :placeholder="$t('generic.no-information')"
                                                :errors="validation.errors.value['placeOfBirth']"
                                                :errors-visible="validation.showErrors.value"
                                            />
                                        </div>
                                    </div>
                                    <div class="flex flex-col sm:flex-row sm:space-x-4">
                                        <div class="mb-4 sm:w-64">
                                            <VInputText
                                                v-model.trim="userDetails.passNr"
                                                :label="$t('views.account.data.personal.passport-number')"
                                                required
                                                :placeholder="$t('generic.no-information')"
                                                :errors="validation.errors.value['passNr']"
                                                :errors-visible="validation.showErrors.value"
                                            />
                                        </div>
                                        <div class="mb-4 sm:flex-grow">
                                            <VInputCombobox
                                                v-model="userDetails.nationality"
                                                :label="$t('views.account.data.personal.nationality')"
                                                :options="nationalities.options"
                                                required
                                                :placeholder="$t('generic.no-information')"
                                                :errors="validation.errors.value['nationality']"
                                                :errors-visible="validation.showErrors.value"
                                            />
                                        </div>
                                    </div>
                                </section>
                            </div>
                            <div class="my-8 md:my-0 md:w-1/3 md:max-w-96 2xl:w-1/2">
                                <VInfo class="py-2 xs:-mx-4">
                                    <h2 class="mb-2">{{ $t('views.account.data.hint-name-passport.title') }}</h2>
                                    <i18n-t
                                        v-for="(message, index) in $tm('views.account.data.hint-name-passport.messages')"
                                        :key="message"
                                        tag="p"
                                        class="mb-2"
                                        :keypath="`views.account.data.hint-name-passport.messages.${index}`"
                                    >
                                        <template #link>
                                            <a class="link" :href="$t('views.account.data.hint-name-passport.link.url')" target="_blank">
                                                {{ $t('views.account.data.hint-name-passport.link.label') }}
                                                <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
                                            </a>
                                        </template>
                                    </i18n-t>
                                </VInfo>
                            </div>
                        </div>
                    </template>
                    <template #[Tab.CONTACT_DATA]>
                        <div v-if="userDetails" class="items-start gap-16 md:flex lg:gap-20 xl:max-w-5xl">
                            <div class="w-full max-w-2xl space-y-8 md:w-2/3 md:flex-grow 2xl:w-1/2">
                                <section>
                                    <h2 class="mb-4 font-bold text-secondary">
                                        {{ $t('views.account.contact.mail-phone.title') }}
                                    </h2>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.email"
                                            :label="$t('views.account.contact.mail-phone.email')"
                                            required
                                            disabled
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['email']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.phone"
                                            :label="$t('views.account.contact.mail-phone.phone')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['phone']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.phoneWork"
                                            :label="$t('views.account.contact.mail-phone.phone-work')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['phoneWork']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.mobile"
                                            :label="$t('views.account.contact.mail-phone.mobile')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['mobile']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                </section>
                                <section>
                                    <h2 class="mb-4 font-bold text-secondary">
                                        {{ $t('views.account.contact.address.title') }}
                                    </h2>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.address.addressLine1"
                                            :label="$t('views.account.contact.address.address-line1')"
                                            required
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['address.addressLine1']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.address.addressLine2"
                                            :label="$t('views.account.contact.address.address-line2')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['address.addressLine2']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="flex flex-col sm:flex-row sm:space-x-4">
                                        <div class="mb-4 sm:w-36">
                                            <VInputText
                                                v-model.trim="userDetails.address.zipcode"
                                                :label="$t('views.account.contact.address.zip')"
                                                required
                                                :placeholder="$t('generic.no-information')"
                                                :errors="validation.errors.value['address.zipcode']"
                                                :errors-visible="validation.showErrors.value"
                                            />
                                        </div>
                                        <div class="mb-4 sm:flex-grow">
                                            <VInputText
                                                v-model.trim="userDetails.address.town"
                                                :label="$t('views.account.contact.address.city')"
                                                required
                                                :placeholder="$t('generic.no-information')"
                                                :errors="validation.errors.value['address.town']"
                                                :errors-visible="validation.showErrors.value"
                                            />
                                        </div>
                                    </div>
                                    <div class="mb-4">
                                        <VInputCombobox
                                            v-model="userDetails.address.country"
                                            :label="$t('views.account.contact.address.country')"
                                            :options="countries.options"
                                            required
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['address.country']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                </section>
                            </div>
                            <div class="my-8 md:my-0 md:w-1/3 md:max-w-96 2xl:w-1/2">
                                <VInfo class="py-2 xs:-mx-4">
                                    <h2 class="mb-2">{{ $t('views.account.contact.hint-mail.title') }}</h2>
                                    <i18n-t
                                        v-for="(message, index) in $tm('views.account.contact.hint-mail.messages')"
                                        :key="message"
                                        tag="p"
                                        class="mb-2"
                                        :keypath="`views.account.contact.hint-mail.messages.${index}`"
                                    >
                                        <template #link>
                                            <a class="link" :href="`mailto:${config.supportEmail}`">
                                                {{ $t('views.account.contact.hint-mail.link') }}
                                                <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
                                            </a>
                                        </template>
                                    </i18n-t>
                                </VInfo>
                            </div>
                        </div>
                    </template>
                    <template #[Tab.QUALIFICATIONS]>
                        <div class="xl:max-w-5xl">
                            <div class="-mx-4 xs:-mx-8 md:-mx-16 xl:-mx-20">
                                <UserQualificationsTable v-if="userDetails" :user="userDetails" />
                            </div>
                        </div>
                    </template>
                    <template #[Tab.EMERGENCY]>
                        <div v-if="userDetails" class="items-start gap-16 md:flex lg:gap-20 xl:max-w-5xl">
                            <div class="w-full max-w-2xl space-y-8 md:w-2/3 md:flex-grow 2xl:w-1/2">
                                <section>
                                    <h2 class="mb-4 font-bold text-secondary">
                                        {{ $t('views.account.emergency.contact.title') }}
                                    </h2>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.emergencyContact.name"
                                            :label="$t('views.account.emergency.contact.name')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['emergencyContact.name']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputText
                                            v-model.trim="userDetails.emergencyContact.phone"
                                            :label="$t('views.account.emergency.contact.phone')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['emergencyContact.phone']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                </section>
                                <section>
                                    <h2 class="mb-4 font-bold text-secondary">{{ $t('views.account.emergency.medical.title') }}</h2>
                                    <div class="mb-4">
                                        <VInputTextArea
                                            v-model.trim="userDetails.diseases"
                                            :label="$t('views.account.emergency.medical.diseases')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['diseases']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputTextArea
                                            v-model.trim="userDetails.medication"
                                            :label="$t('views.account.emergency.medical.medication')"
                                            :placeholder="$t('generic.no-information')"
                                            :errors="validation.errors.value['medication']"
                                            :errors-visible="validation.showErrors.value"
                                        />
                                    </div>
                                </section>
                            </div>
                            <div class="my-8 md:my-0 md:w-1/3 md:max-w-96 2xl:w-1/2">
                                <VInfo class="py-2 xs:-mx-4 md:mx-0">
                                    <h2 class="mb-2">{{ $t('views.account.emergency.hint-emergency-contact.title') }}</h2>
                                    <i18n-t
                                        v-for="(message, index) in $tm('views.account.emergency.hint-emergency-contact.messages')"
                                        :key="message"
                                        tag="p"
                                        class="mb-2"
                                        :keypath="`views.account.emergency.hint-emergency-contact.messages.${index}`"
                                    >
                                        <template #encrypted>
                                            <b>{{ $t(`views.account.emergency.hint-emergency-contact.encrypted`) }}</b>
                                        </template>
                                        <template #hint-all-data-optional>
                                            <b>{{ $t(`views.account.emergency.hint-emergency-contact.hint-all-data-optional`) }}</b>
                                        </template>
                                    </i18n-t>
                                </VInfo>
                            </div>
                        </div>
                    </template>
                </VTabs>
            </template>
            <template #primary-button>
                <AsyncButton v-if="userDetails" :action="save" name="save" :disabled="validation.disableSubmit.value">
                    <template #icon>
                        <i class="fa-solid fa-save"></i>
                    </template>
                    <template #label>
                        <span>{{ $t('generic.save') }}</span>
                    </template>
                </AsyncButton>
            </template>
        </DetailsPage>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { deepCopy } from '@/common';
import type { InputSelectOption, UserDetails } from '@/domain';
import { AsyncButton, VInfo, VInputCombobox, VInputDate, VInputSelect, VInputText, VInputTextArea, VTabs } from '@/ui/components/common';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useConfig, useUsersUseCase } from '@/ui/composables/Application';
import { useCountries } from '@/ui/composables/Countries.ts';
import { useUserService } from '@/ui/composables/Domain.ts';
import { useNationalities } from '@/ui/composables/Nationalities.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import UserQualificationsTable from './UserQualificationsTable.vue';

enum Tab {
    PERSONAL_DATA = 'data',
    CONTACT_DATA = 'contact',
    EMERGENCY = 'emergency',
    QUALIFICATIONS = 'qualifications',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const config = useConfig();
const usersUseCase = useUsersUseCase();
const userService = useUserService();
const nationalities = useNationalities();
const countries = useCountries();
const userDetails = ref<UserDetails | null>(null);
const userDetailsOriginal = ref<UserDetails | null>(null);
const validation = useValidation<UserDetails | null>(userDetails, userService.validate);

const genderOptions: InputSelectOption[] = [
    { value: 'm', label: t('views.account.data.personal.gender.values.male') },
    { value: 'f', label: t('views.account.data.personal.gender.values.female') },
    { value: 'd', label: t('views.account.data.personal.gender.values.diverse') },
];

const tabs: InputSelectOption[] = [Tab.PERSONAL_DATA, Tab.CONTACT_DATA, Tab.EMERGENCY, Tab.QUALIFICATIONS].map((it) => ({
    value: it,
    label: t(`views.account.tab.${it}`),
}));
const tab = ref<string>(tabs[0].value);
const enableEditingDateOfBirth = ref<boolean>(false);
const enableEditingPlaceOfBirth = ref<boolean>(false);

async function fetchUserDetails(): Promise<void> {
    userDetailsOriginal.value = await usersUseCase.getUserDetailsForSignedInUser();
    userDetails.value = deepCopy(userDetailsOriginal.value);
    enableEditingDateOfBirth.value = userDetails.value.dateOfBirth === undefined;
    enableEditingPlaceOfBirth.value = userDetails.value.placeOfBirth === undefined;
    validation.showErrors.value = true;
}

async function save(): Promise<void> {
    if (userDetailsOriginal.value && userDetails.value) {
        userDetailsOriginal.value = await usersUseCase.updateUserDetailsForSignedInUser(userDetailsOriginal.value, userDetails.value);
        userDetails.value = deepCopy(userDetailsOriginal.value);
    }
}

function init(): void {
    emit('update:tab-title', t('views.account.title'));
    fetchUserDetails();
}

init();
</script>
