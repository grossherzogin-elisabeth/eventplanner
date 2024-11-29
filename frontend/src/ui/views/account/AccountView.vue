<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <DetailsPage>
            <template #content>
                <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
                    <template #[Tab.PERSONAL_DATA]>
                        <div v-if="userDetails" class="items-start gap-16 md:flex lg:gap-20 xl:max-w-5xl">
                            <div class="w-full max-w-2xl space-y-8 md:w-2/3 md:flex-grow 2xl:w-1/2">
                                <section class="app-data">
                                    <h2 class="mb-4 font-bold text-secondary">App</h2>
                                    <div class="mb-4">
                                        <VInputLabel>Anzeigename</VInputLabel>
                                        <VInputText v-model.trim="userDetails.nickName" :placeholder="userDetails.firstName" />
                                    </div>
                                </section>
                                <section class="diet-data">
                                    <h2 class="mb-4 font-bold text-secondary">Ernährung</h2>
                                    <div class="mb-4 sm:w-64">
                                        <VInputLabel>Ernährungsweise</VInputLabel>
                                        <VInputSelect
                                            v-model="userDetails.diet"
                                            :options="[
                                                { value: 'omnivore', label: 'Fleisch' },
                                                { value: 'vegetarian', label: 'Vegetarisch' },
                                                { value: 'vegan', label: 'Vegan' },
                                            ]"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Unverträglichkeiten</VInputLabel>
                                        <VInputTextArea v-model.trim="userDetails.intolerances" placeholder="Keine Angabe" />
                                    </div>
                                </section>
                                <section class="personal-data">
                                    <h2 class="mb-4 font-bold text-secondary">Persönliche Daten</h2>
                                    <div class="mb-4 sm:w-64">
                                        <VInputLabel>Geschlecht</VInputLabel>
                                        <VInputSelect v-model="userDetails.gender" :options="genderOptions" placeholder="Keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Vorname</VInputLabel>
                                        <VInputText
                                            :model-value="`${userDetails.firstName} ${userDetails.secondName || ''}`.trim()"
                                            required
                                            disabled
                                            placeholder="Keine Angabe"
                                        />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Zweiter Vornahme</VInputLabel>
                                        <VInputText v-model.trim="userDetails.secondName" disabled placeholder="Keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Nachname</VInputLabel>
                                        <VInputText v-model.trim="userDetails.lastName" required disabled placeholder="Keine Angabe" />
                                    </div>
                                    <div class="flex flex-col sm:flex-row sm:space-x-4">
                                        <div class="mb-4 sm:w-64">
                                            <VInputLabel>Geboren am</VInputLabel>
                                            <VInputDate
                                                v-model="userDetails.dateOfBirth"
                                                required
                                                :disabled="!enableEditingDateOfBirth"
                                                placeholder="Keine Angabe"
                                            />
                                        </div>
                                        <div class="mb-4 sm:flex-grow">
                                            <VInputLabel>Geburtsort</VInputLabel>
                                            <VInputText
                                                v-model="userDetails.placeOfBirth"
                                                required
                                                :disabled="!enableEditingPlaceOfBirth"
                                                placeholder="Keine Angabe"
                                            />
                                        </div>
                                    </div>
                                    <div class="flex flex-col sm:flex-row sm:space-x-4">
                                        <div class="mb-4 sm:w-64">
                                            <VInputLabel>Personalausweis Nummer</VInputLabel>
                                            <VInputText v-model.trim="userDetails.passNr" required placeholder="Keine Angabe" />
                                        </div>
                                        <div class="mb-4 sm:flex-grow">
                                            <VInputLabel>Nationalität</VInputLabel>
                                            <VInputCombobox
                                                v-model="userDetails.nationality"
                                                :options="nationalities.options"
                                                required
                                                placeholder="Keine Angabe"
                                            />
                                        </div>
                                    </div>
                                </section>
                            </div>
                            <div class="my-8 md:my-0 md:w-1/3 md:max-w-96 2xl:w-1/2">
                                <VInfo class="-mx-4 py-2">
                                    <h2 class="mb-2">Name und Personalausweis Nummer</h2>
                                    <p class="mb-2">
                                        Dein Name wie du ihn hier angibst, muss genau so auch auf deinem Ausweis stehen, da dieser auch zum
                                        Erstellen der IMO Liste verwendet wird. Bitte beachte, das du den Personalausweis bei Reisen
                                        mitführen musst!
                                    </p>
                                    <p class="mb-2">
                                        Solltest du mit einem anderen Namen oder Spitznamen angesprochen werden wollen, kannst du einen
                                        abweichenden Anzeigenamen angeben. Dieser wird dann in der App angezeigt und auch beim Kammerplan
                                        und der Getränkeliste verwendet.
                                    </p>
                                    <p>
                                        <a
                                            href="https://commons.wikimedia.org/wiki/File:Personalausweis-nummer.png"
                                            class="link"
                                            target="_blank"
                                        >
                                            Wo finde ich meine Personalausweis Nummer?
                                            <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
                                        </a>
                                    </p>
                                </VInfo>
                            </div>
                        </div>
                    </template>
                    <template #[Tab.CONTACT_DATA]>
                        <div v-if="userDetails" class="items-start gap-16 md:flex lg:gap-20 xl:max-w-5xl">
                            <div class="w-full max-w-2xl space-y-8 md:w-2/3 md:flex-grow 2xl:w-1/2">
                                <section>
                                    <h2 class="mb-4 font-bold text-secondary">Email & Telefon</h2>
                                    <div class="mb-4">
                                        <VInputLabel>Email</VInputLabel>
                                        <VInputText v-model.trim="userDetails.email" required disabled placeholder="keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Telefon</VInputLabel>
                                        <VInputText v-model.trim="userDetails.phone" placeholder="keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Telefon (dienstlich)</VInputLabel>
                                        <VInputText v-model.trim="userDetails.phoneWork" placeholder="keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Mobil</VInputLabel>
                                        <VInputText v-model.trim="userDetails.mobile" placeholder="keine Angabe" />
                                    </div>
                                </section>
                                <section>
                                    <h2 class="mb-4 font-bold text-secondary">Adresse</h2>
                                    <div class="mb-4">
                                        <VInputLabel>Straße, Hausnr</VInputLabel>
                                        <VInputText v-model.trim="userDetails.address.addressLine1" required placeholder="keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Adresszusatz</VInputLabel>
                                        <VInputText v-model.trim="userDetails.address.addressLine2" placeholder="keine Angabe" />
                                    </div>
                                    <div class="flex flex-col sm:flex-row sm:space-x-4">
                                        <div class="mb-4 sm:w-36">
                                            <VInputLabel>PLZ</VInputLabel>
                                            <VInputText v-model.trim="userDetails.address.zipcode" required placeholder="keine Angabe" />
                                        </div>
                                        <div class="mb-4 sm:flex-grow">
                                            <VInputLabel>Ort</VInputLabel>
                                            <VInputText v-model.trim="userDetails.address.town" required placeholder="keine Angabe" />
                                        </div>
                                    </div>
                                </section>
                            </div>
                            <div class="my-8 md:my-0 md:w-1/3 md:max-w-96 2xl:w-1/2">
                                <VInfo class="-mx-4 py-2">
                                    <h2 class="mb-2">Du möchtest deine Email Adresse ändern?</h2>
                                    <p>
                                        Deine Email Adresse wird sowohl als Kontakt Email, als auch für den Login verwendet. Wenn du die
                                        Email Adresse ändern möchtest wende dich bitte ans
                                        <a class="link" :href="`mailto:${config.supportEmail}`">
                                            Büro
                                            <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i> </a
                                        >.
                                    </p>
                                </VInfo>
                            </div>
                        </div>
                    </template>
                    <template #[Tab.ACCOUNT_CREDENTIALS]>
                        <section class="max-w-2xl">
                            <h2 class="mb-4 font-bold text-secondary">Zugangsdaten</h2>
                            <div class="mb-2">
                                <VInputLabel>Benutzername</VInputLabel>
                                <VInputText v-model.trim="user.email" disabled required />
                            </div>
                            <div class="mb-2">
                                <VInputLabel>Password</VInputLabel>
                                <VInputText disabled model-value="this is not your real password, sorry" required type="password" />
                            </div>
                            <VInfo class="-mx-4 mt-8 py-2">
                                <h2 class="mb-2">Hinweis zum Passwort ändern:</h2>
                                <p>
                                    Wenn du dein Passwort ändern willst, nutze dazu die "Passwort vergessen" Funktion beim Login. Du
                                    bekommst dann einen Link per Email zugesandt, mit dem du ein neues Passwort vergeben kannst.
                                </p>
                            </VInfo>
                        </section>
                    </template>
                    <template #[Tab.QUALIFICATIONS]>
                        <div class="xl:max-w-5xl">
                            <div class="-mx-8 md:-mx-16 xl:-mx-20">
                                <UserQualificationsTable v-if="userDetails" :user="userDetails" />
                            </div>
                        </div>
                    </template>
                    <template #[Tab.EMERGENCY]>
                        <div v-if="userDetails" class="items-start gap-16 md:flex lg:gap-20 xl:max-w-5xl">
                            <div class="w-full max-w-2xl space-y-8 md:w-2/3 md:flex-grow 2xl:w-1/2">
                                <section class="">
                                    <h2 class="mb-4 font-bold text-secondary">Notfallkontakt</h2>
                                    <div class="mb-4">
                                        <VInputLabel>Name des Notfallkontakts</VInputLabel>
                                        <VInputText v-model.trim="userDetails.emergencyContact.name" placeholder="Keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Telefonnummer des Notfallkontakts</VInputLabel>
                                        <VInputText v-model.trim="userDetails.emergencyContact.phone" placeholder="Keine Angabe" />
                                    </div>
                                </section>
                                <section class="">
                                    <h2 class="mb-4 font-bold text-secondary">Wichtige gesundheitliche Informationen</h2>
                                    <div class="mb-4">
                                        <VInputLabel>Krankheiten</VInputLabel>
                                        <VInputTextArea v-model.trim="userDetails.diseases" placeholder="Keine Angabe" />
                                    </div>
                                    <div class="mb-4">
                                        <VInputLabel>Medikamente</VInputLabel>
                                        <VInputTextArea v-model.trim="userDetails.medication" placeholder="Keine Angabe" />
                                    </div>
                                </section>
                            </div>
                            <div class="my-8 md:my-0 md:w-1/3 md:max-w-96 2xl:w-1/2">
                                <VInfo class="-mx-4 py-2 md:mx-0">
                                    <h2 class="mb-2">Notfall Informationen</h2>
                                    <p class="mb-2">
                                        Wenn auf einer Reise mal etwas passiert, ist es wichtig, dass jemand an Bord weiß, das du bestimmte
                                        Krankheiten hast oder welche wichtigen Medikamente du benötigst. Außerdem kannst du einen
                                        Notfallkontakt angeben, den wir kontaktieren können, falls du mal nicht mehr ansprechbar bist.
                                    </p>
                                    <p class="mb-2">
                                        Deine Daten werden <b>verschlüsselt</b> gespeichert und sind nur für autorisierte Personen
                                        einsehbar. In der Regel sind dies das Büro und der Kapitän der jeweiligen Reise.
                                    </p>
                                    <p class="mb-2">
                                        <b> All diese Angaben sind freiwillig und dienen deiner eigenen Sicherheit an Bord! </b>
                                    </p>
                                </VInfo>
                            </div>
                        </div>
                    </template>
                </VTabs>
            </template>
            <template #primary-button>
                <AsyncButton v-if="userDetails" :action="save" name="save">
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
import {
    AsyncButton,
    VInfo,
    VInputCombobox,
    VInputDate,
    VInputLabel,
    VInputSelect,
    VInputText,
    VInputTextArea,
    VTabs,
} from '@/ui/components/common';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useAuthUseCase, useConfig, useUsersUseCase } from '@/ui/composables/Application';
import { useNationalities } from '@/ui/composables/Nationalities.ts';
import UserQualificationsTable from './UserQualificationsTable.vue';

enum Tab {
    PERSONAL_DATA = 'app.account.tab.data',
    CONTACT_DATA = 'app.account.tab.contact',
    ACCOUNT_CREDENTIALS = 'app.account.tab.credentials',
    EMERGENCY = 'app.account.tab.emergency',
    QUALIFICATIONS = 'app.account.tab.qualifications',
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const config = useConfig();
const authUseCase = useAuthUseCase();
const usersUseCase = useUsersUseCase();
const user = ref(authUseCase.getSignedInUser());
const nationalities = useNationalities();
const userDetails = ref<UserDetails | null>(null);
const userDetailsOriginal = ref<UserDetails | null>(null);

const genderOptions: InputSelectOption[] = [
    { value: 'm', label: 'männlich' },
    { value: 'f', label: 'weiblich' },
    { value: 'd', label: 'divers' },
];

const tabs = [Tab.PERSONAL_DATA, Tab.CONTACT_DATA, Tab.EMERGENCY, Tab.QUALIFICATIONS];
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
        userDetailsOriginal.value = await usersUseCase.updateUserDetailsForSignedInUser(userDetailsOriginal.value, userDetails.value);
        userDetails.value = deepCopy(userDetailsOriginal.value);
    }
}

function init(): void {
    emit('update:title', 'Meine Daten');
    fetchUserDetails();
}

init();
</script>
