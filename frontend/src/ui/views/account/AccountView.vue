<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <DetailsPage>
            <template #content>
                <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8">
                    <template #[Tab.ACCOUNT_DATA]>
                        <div class="items-start gap-32 2xl:flex">
                            <section v-if="userDetails" class="w-full max-w-2xl">
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Geschlecht</VInputLabel>
                                    <VInputSelect v-model="userDetails.gender" :options="genderOptions" required />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Anzeigename</VInputLabel>
                                    <VInputText v-model="userDetails.nickName" :placeholder="userDetails.firstName" />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Vorname</VInputLabel>
                                    <VInputText
                                        :model-value="`${userDetails.firstName} ${userDetails.secondName || ''}`.trim()"
                                        required
                                        disabled
                                    />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Nachname</VInputLabel>
                                    <VInputText v-model="userDetails.lastName" required disabled />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Geboren am</VInputLabel>
                                    <VInputDate
                                        v-model="userDetails.dateOfBirth"
                                        required
                                        :disabled="!enableEditingDateOfBirth"
                                    />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Geburtsort</VInputLabel>
                                    <VInputText
                                        v-model="userDetails.placeOfBirth"
                                        required
                                        :disabled="!enableEditingPlaceOfBirth"
                                    />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Personalausweis Nummer</VInputLabel>
                                    <VInputText v-model="userDetails.passNr" required />
                                </div>
                            </section>
                            <div class="my-8 max-w-2xl flex-grow 2xl:my-0 2xl:w-0">
                                <div class="-mx-4 rounded-xl bg-primary-100 p-4 text-sm">
                                    <h2 class="mb-2">Name und Personalausweis Nummer</h2>
                                    <p class="mb-2">
                                        Dein Name wie du ihn hier angibst, muss genau so auch auf deinem Ausweis stehen,
                                        da dieser auch zum Erstellen der IMO Liste verwendet wird. Bitte beachte, das du
                                        den Personalausweis bei Reisen mitführen musst!
                                    </p>
                                    <p class="mb-2">
                                        Solltest du mit einem anderen Namen oder Spitznamen angesprochen werden wollen,
                                        kannst du einen abweichenden Anzeigenamen angeben. Dieser wird dann in der App
                                        angezeigt und auch beim Kammerplan und der Getränkeliste verwendet.
                                    </p>
                                    <p>
                                        <a
                                            href="https://commons.wikimedia.org/wiki/File:Personalausweis-nummer.png"
                                            class="text-primary-600"
                                            target="_blank"
                                        >
                                            Wo finde ich meine Personalausweis Nummer?
                                            <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
                                        </a>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template #[Tab.ACCOUNT_CONTACT_DATA]>
                        <div class="items-start gap-32 2xl:flex">
                            <section v-if="userDetails" class="w-full max-w-2xl">
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Email</VInputLabel>
                                    <VInputText v-model="userDetails.email" required disabled />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Straße, Hausnr</VInputLabel>
                                    <VInputText v-model="userDetails.address.addressLine1" required />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Adresszusatz</VInputLabel>
                                    <VInputText v-model="userDetails.address.addressLine2" />
                                </div>
                                <div class="-mx-4 flex space-x-4">
                                    <div class="mb-4 w-24">
                                        <VInputLabel>PLZ</VInputLabel>
                                        <VInputText v-model="userDetails.address.zipcode" required />
                                    </div>
                                    <div class="mb-4 flex-grow">
                                        <VInputLabel>Ort</VInputLabel>
                                        <VInputText v-model="userDetails.address.town" required />
                                    </div>
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Telefon</VInputLabel>
                                    <VInputText v-model="userDetails.phone" />
                                </div>
                                <div class="-mx-4 mb-4">
                                    <VInputLabel>Mobil</VInputLabel>
                                    <VInputText v-model="userDetails.mobile" />
                                </div>
                            </section>
                            <div class="my-8 max-w-2xl flex-grow 2xl:my-0 2xl:w-0">
                                <div class="-mx-4 rounded-xl bg-primary-100 p-4 text-sm">
                                    <h2 class="mb-2">Du möchtest deine Email Adresse ändern?</h2>
                                    <p>
                                        Deine Email Adresse wird sowohl als Kontakt Email, als auch für den Login
                                        verwendet. Wenn du die Email Adresse ändern möchtest wende dich bitte an
                                        <a class="text-primary-600" :href="`mailto:${config.supportEmail}`">
                                            {{ config.supportEmail }}
                                        </a>
                                        .
                                    </p>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template #[Tab.ACCOUNT_CREDENTIALS]>
                        <section class="max-w-2xl">
                            <div class="-mx-4 mb-2">
                                <VInputLabel>Benutzername</VInputLabel>
                                <VInputText v-model="user.email" disabled required />
                            </div>
                            <div class="-mx-4 mb-2">
                                <VInputLabel>Password</VInputLabel>
                                <VInputText disabled model-value="loremipsumdolorsitamet" required type="password" />
                            </div>
                            <div class="-mx-4 mt-8 rounded-xl bg-primary-100 p-4 text-sm">
                                <h2 class="mb-2">Hinweis zum Passwort ändern:</h2>
                                <p>
                                    Aktuell kannst du dein Passwort noch nicht direkt hier in der App ändern. Bitte
                                    nutze dafür vorerst die "Passwort vergessen" Funktion beim Login. Bei Fragen zu
                                    deinem Account wende dich gerne an
                                    <a class="text-primary-600" href="mailto:admin@grossherzogin-elisabeth.de">
                                        admin@grossherzogin-elisabeth.de </a
                                    >.
                                </p>
                            </div>
                        </section>
                    </template>
                    <template #[Tab.QUALIFICATIONS]>
                        <div class="max-w-2xl">
                            <div class="-mx-8 md:-mx-16 xl:-mx-20">
                                <UserQualificationsTable v-if="userDetails" :user="userDetails" />
                            </div>
                        </div>
                    </template>
                </VTabs>
            </template>
            <template #primary-button>
                <AsyncButton v-if="userDetails" :action="save">
                    <template #icon>
                        <i class="fa-solid fa-save"></i>
                    </template>
                    <template #label>
                        <span>Speichern</span>
                    </template>
                </AsyncButton>
            </template>
        </DetailsPage>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy } from '@/common';
import type { InputSelectOption, UserDetails } from '@/domain';
import { AsyncButton, VInputDate, VInputLabel, VInputSelect, VInputText, VTabs } from '@/ui/components/common';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useAuthUseCase, useConfig, useUsersUseCase } from '@/ui/composables/Application';
import UserQualificationsTable from './UserQualificationsTable.vue';

enum Tab {
    ACCOUNT_DATA = 'app.account.tab.data',
    ACCOUNT_CONTACT_DATA = 'app.account.tab.contact',
    ACCOUNT_CREDENTIALS = 'app.account.tab.credentials',
    QUALIFICATIONS = 'app.account.tab.qualifications',
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const config = useConfig();
const authUseCase = useAuthUseCase();
const usersUseCase = useUsersUseCase();
const user = ref(authUseCase.getSignedInUser());
const userDetails = ref<UserDetails | null>(null);
const userDetailsOriginal = ref<UserDetails | null>(null);

const genderOptions: InputSelectOption[] = [
    { value: 'm', label: 'männlich' },
    { value: 'f', label: 'weiblich' },
    { value: 'd', label: 'divers' },
];

const tabs = [Tab.ACCOUNT_CREDENTIALS, Tab.ACCOUNT_DATA, Tab.ACCOUNT_CONTACT_DATA, Tab.QUALIFICATIONS];
const tab = ref<Tab>(tabs[0]);
const enableEditingDateOfBirth = ref<boolean>(false);
const enableEditingPlaceOfBirth = ref<boolean>(false);

async function fetchUserDetails(): Promise<void> {
    userDetailsOriginal.value = await usersUseCase.getUserDetailsForSignedInUser();
    userDetails.value = deepCopy(userDetailsOriginal.value);
    enableEditingDateOfBirth.value = userDetails.value.dateOfBirth === undefined;
    enableEditingPlaceOfBirth.value = userDetails.value.placeOfBirth === undefined;
}

async function save(): Promise<void> {
    if (userDetailsOriginal.value && userDetails.value) {
        userDetailsOriginal.value = await usersUseCase.updateUserDetailsForSignedInUser(
            userDetailsOriginal.value,
            userDetails.value
        );
        userDetails.value = deepCopy(userDetailsOriginal.value);
    }
}

function init(): void {
    emit('update:title', 'Meine Daten');
    fetchUserDetails();
}

init();
</script>
