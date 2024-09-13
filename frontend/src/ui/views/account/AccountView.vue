<template>
    <div>
        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-10 bg-primary-50 pt-8 xl:top-0">
            <template #[Tab.ACCOUNT_DATA]>
                <section v-if="userDetails" class="max-w-2xl">
                    <div class="-mx-4 mb-2 hidden">
                        <VInputLabel>Geschlecht</VInputLabel>
                        <VInputSelect v-model="user.gender" :options="genderOptions" required />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Vorname</VInputLabel>
                        <VInputText v-model="userDetails.firstName" required disabled />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Nachname</VInputLabel>
                        <VInputText v-model="userDetails.lastName" required disabled />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Geboren am</VInputLabel>
                        <VInputDate v-model="userDetails.dateOfBirth" required disabled />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Geburtsort</VInputLabel>
                        <VInputText v-model="userDetails.placeOfBirth" required disabled />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Pass Nummer</VInputLabel>
                        <VInputText v-model="userDetails.passNr" required disabled />
                    </div>
                    <div class="-mx-4 mt-8 rounded-xl bg-primary-100 p-4 text-sm">
                        <h2 class="mb-2">Hinweis zum Daten ändern:</h2>
                        <p>
                            Aktuell kannst du deine Daten noch nicht direkt hier in der App ändern. Bitte kontaktiere
                            dafür vorerst weiterhin das Büro unter
                            <a class="text-primary-600" href="mailto:office@grossherzogin-elisabeth.de">
                                office@grossherzogin-elisabeth.de </a
                            >.
                        </p>
                    </div>
                </section>
            </template>
            <template #[Tab.ACCOUNT_CONTACT_DATA]>
                <section v-if="userDetails" class="max-w-2xl">
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Email</VInputLabel>
                        <VInputText v-model="userDetails.email" required disabled />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Straße, Hausnr</VInputLabel>
                        <VInputText v-model="userDetails.address.addressLine1" required disabled />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Adresszusatz</VInputLabel>
                        <VInputText v-model="userDetails.address.addressLine2" disabled />
                    </div>
                    <div class="-mx-4 flex space-x-4">
                        <div class="mb-2 w-24">
                            <VInputLabel>PLZ</VInputLabel>
                            <VInputText v-model="userDetails.address.zipcode" required disabled />
                        </div>
                        <div class="mb-2 flex-grow">
                            <VInputLabel>Ort</VInputLabel>
                            <VInputText v-model="userDetails.address.town" required disabled />
                        </div>
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Telefon</VInputLabel>
                        <VInputText v-model="userDetails.phone" disabled />
                    </div>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Mobil</VInputLabel>
                        <VInputText v-model="userDetails.mobile" disabled />
                    </div>
                    <div class="-mx-4 mt-8 rounded-xl bg-primary-100 p-4 text-sm">
                        <h2 class="mb-2">Hinweis zum Daten ändern:</h2>
                        <p>
                            Aktuell kannst du deine Daten noch nicht direkt hier in der App ändern. Bitte kontaktiere
                            dafür vorerst weiterhin das Büro unter
                            <a class="text-primary-600" href="mailto:office@grossherzogin-elisabeth.de">
                                office@grossherzogin-elisabeth.de </a
                            >.
                        </p>
                    </div>
                </section>
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
                            Aktuell kannst du dein Passwort noch nicht direkt hier in der App ändern. Bitte nutze dafür
                            vorerst die "Passwort vergessen" Funktion beim Login. Bei Fragen zu deinem Account wende
                            dich gerne an
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
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import type { InputSelectOption, UserDetails } from '@/domain';
import { VInputDate, VInputLabel, VInputSelect, VInputText, VTabs } from '@/ui/components/common';
import { useAuthUseCase, useUsersUseCase } from '@/ui/composables/Application';
import UserQualificationsTable from './UserQualificationsTable.vue';

enum Tab {
    ACCOUNT_DATA = 'app.account.tab.data',
    ACCOUNT_CONTACT_DATA = 'app.account.tab.contact',
    ACCOUNT_CREDENTIALS = 'app.account.tab.credentials',
    QUALIFICATIONS = 'Qualifikationen',
}

const authUseCase = useAuthUseCase();
const usersUseCase = useUsersUseCase();
const user = ref(authUseCase.getSignedInUser());
const userDetails = ref<UserDetails | null>(null);

const genderOptions: InputSelectOption[] = [
    { value: 'm', label: 'männlich' },
    { value: 'f', label: 'weiblich' },
    { value: 'd', label: 'divers' },
];

const tabs = [Tab.ACCOUNT_CREDENTIALS, Tab.ACCOUNT_DATA, Tab.ACCOUNT_CONTACT_DATA, Tab.QUALIFICATIONS];
const tab = ref<Tab>(Tab.ACCOUNT_CREDENTIALS);

async function fetchUserDetails() {
    userDetails.value = await usersUseCase.getUserDetailsForSignedInUser();
}

function init() {
    fetchUserDetails();
}

init();
</script>
