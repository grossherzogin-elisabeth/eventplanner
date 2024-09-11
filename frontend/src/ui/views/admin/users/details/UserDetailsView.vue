<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <DetailsPage :back-to="{ name: Routes.UsersList }">
            <template #header>
                <div>
                    <h1>{{ user?.firstName }} {{ user?.lastName }} bearbeiten</h1>
                    <p v-if="user && signedInUser.key === user.key" class="mt-1 text-sm">Das bist du!</p>
                </div>
            </template>
            <template #content>
                <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-8 xl:top-0">
                    <template #[Tab.USER_DATA]>
                        <div class="space-y-8 xl:space-y-16">
                            <section v-if="user" class="-mx-4">
                                <div class="mb-2 md:w-1/4">
                                    <VInputLabel>Geschlecht</VInputLabel>
                                    <VInputSelect :options="genderOptions" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Vorname</VInputLabel>
                                    <VInputText v-model="user.firstName" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Zweiter Vorname</VInputLabel>
                                    <VInputText v-model="user.secondName" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Nachname</VInputLabel>
                                    <VInputText v-model="user.lastName" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Geboren am</VInputLabel>
                                    <VInputDate v-model="user.dateOfBirth" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Geburtsort</VInputLabel>
                                    <VInputText v-model="user.placeOfBirth" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Pass Nummer</VInputLabel>
                                    <VInputText v-model="user.passNr" required />
                                </div>
                            </section>
                        </div>
                    </template>
                    <template #[Tab.USER_CONTACT_DATA]>
                        <div class="space-y-8 xl:space-y-16">
                            <section v-if="user" class="-mx-4">
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Email</VInputLabel>
                                    <VInputText v-model="user.email" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Telefon</VInputLabel>
                                    <VInputText v-model="user.phone" />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Mobil</VInputLabel>
                                    <VInputText v-model="user.mobile" />
                                </div>
                                <div class="mb-2 mt-16 md:w-1/2">
                                    <VInputLabel>Straße, Hausnr</VInputLabel>
                                    <VInputText v-model="user.address.addressLine1" required />
                                </div>
                                <div class="mb-2 md:w-1/2">
                                    <VInputLabel>Adresszusatz</VInputLabel>
                                    <VInputText v-model="user.address.addressLine2" />
                                </div>
                                <div class="flex space-x-4 md:w-1/2">
                                    <div class="mb-2 w-24">
                                        <VInputLabel>PLZ</VInputLabel>
                                        <VInputText v-model="user.address.zipcode" required />
                                    </div>
                                    <div class="mb-2 flex-grow">
                                        <VInputLabel>Ort</VInputLabel>
                                        <VInputText v-model="user.address.town" required />
                                    </div>
                                </div>
                            </section>
                        </div>
                    </template>
                    <template #[Tab.USER_EVENTS]>
                        <div>
                            <div v-for="[year, events] in eventsByYear" :key="`${year}-${events?.length}`" class="">
                                <h2 class="mb-4 font-bold text-primary-800 text-opacity-50">
                                    <template v-if="year === 0">Zukünftige Reisen</template>
                                    <template v-else>Reisen {{ year }}</template>
                                </h2>
                                <UserEventsTable
                                    v-if="user && positions"
                                    :events="events"
                                    :positions="positions"
                                    :user="user"
                                />
                                <div class="mb-4 mt-8 flex items-center justify-center">
                                    <div v-if="eventsLoadedUntilYear === year">
                                        <AsyncButton :action="fetchNextEvents" class="btn-ghost">
                                            <template #label>
                                                <span v-if="eventsLoadedUntilYear" class="px-2">
                                                    Reisen {{ eventsLoadedUntilYear - 1 }} anzeigen
                                                </span>
                                                <span v-else class="px-2"> Vergangene Reisen anzeigen </span>
                                            </template>
                                        </AsyncButton>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template #[Tab.USER_CERTIFICATES]>
                        <div class="space-y-8 overflow-x-auto xl:space-y-16">
                            Hier stehen später einmal Positionen, Zertifikate und sonstige Qualifikationen
                        </div>
                    </template>
                </VTabs>
            </template>
            <template v-if="signedInUser.permissions.includes(Permission.WRITE_USERS)" #primary-button>
                <AsyncButton :action="save">
                    <template #icon>
                        <i class="fa-solid fa-save"></i>
                    </template>
                    <template #label>
                        <span>Speichern</span>
                    </template>
                </AsyncButton>
            </template>
            <template #secondary-buttons>
                <button class="btn-secondary" @click="createRegistration()">
                    <i class="fa-solid fa-user-plus"></i>
                    <span>Reise hinzufügen</span>
                </button>
            </template>
            <template #actions-menu>
                <li class="context-menu-item" @click="impersonateUser()">
                    <i class="fa-solid fa-user-secret" />
                    <span>Impersonate</span>
                </li>
                <li class="context-menu-item" @click="createRegistration()">
                    <i class="fa-solid fa-user-plus" />
                    <span>Reise hinzufügen</span>
                </li>
                <li class="context-menu-item">
                    <i class="fa-solid fa-key" />
                    <span>Passwort zurücksetzen</span>
                </li>
            </template>
        </DetailsPage>
        <CreateRegistrationForUserDlg ref="createRegistrationForUserDialog" />
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import type { Event, InputSelectOption, Position, PositionKey, UserDetails } from '@/domain';
import { Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VInputDate, VInputLabel, VInputSelect, VInputText, VTabs } from '@/ui/components/common';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import {
    useAuthUseCase,
    useErrorHandling,
    useEventUseCase,
    useUserAdministrationUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application';
import { Routes } from '@/ui/views/Routes';
import CreateRegistrationForUserDlg from '@/ui/views/admin/users/components/CreateRegistrationForUserDlg.vue';
import UserEventsTable from '@/ui/views/admin/users/details/UserEventsTable.vue';

enum Tab {
    USER_DATA = 'app.user-details.tab.data',
    USER_CONTACT_DATA = 'app.user-details.tab.contact',
    USER_CERTIFICATES = 'app.user-details.tab.certificates',
    USER_EVENTS = 'app.user-details.tab.events',
}

const route = useRoute();
const userAdministrationUseCase = useUserAdministrationUseCase();
const usersUseCase = useUsersUseCase();
const eventsUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const errorHandlingUseCase = useErrorHandling();
const signedInUser = authUseCase.getSignedInUser();

const tabs = [Tab.USER_EVENTS, Tab.USER_DATA, Tab.USER_CONTACT_DATA, Tab.USER_CERTIFICATES];
const tab = ref<Tab>(Tab.USER_EVENTS);
const user = ref<UserDetails | null>(null);
const eventsByYear = ref<Map<number, Event[] | undefined>>(new Map<number, Event[] | undefined>());
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const createRegistrationForUserDialog = ref<Dialog<UserDetails> | null>(null);
const eventsLoadedUntilYear = ref<number>(0);

const genderOptions: InputSelectOption[] = [
    { value: 'm', label: 'männlich' },
    { value: 'f', label: 'weiblich' },
    { value: 'd', label: 'divers' },
];

const userKey = computed<string>(() => (route.params.key as string) || '');

function init(): void {
    fetchUser();
    fetchPositions();
    fetchUserFutureEvents();
}

async function fetchUser(): Promise<void> {
    user.value = await userAdministrationUseCase.getUserDetailsByKey(userKey.value);
}

async function fetchNextEvents(): Promise<void> {
    if (eventsLoadedUntilYear.value === 0) {
        eventsLoadedUntilYear.value = new Date().getFullYear();
    } else {
        eventsLoadedUntilYear.value = eventsLoadedUntilYear.value - 1;
    }
    await fetchUserEventsOfYear(eventsLoadedUntilYear.value);
}

async function fetchUserFutureEvents(): Promise<void> {
    eventsByYear.value.set(0, undefined);
    let events = await eventsUseCase.getFutureEventsByUser(userKey.value);
    events = events.sort((a, b) => b.start.getTime() - a.start.getTime());
    eventsByYear.value.set(0, events);
}

async function fetchUserEventsOfYear(year: number): Promise<void> {
    eventsByYear.value.set(year, undefined);
    let events = await eventsUseCase.getEventsByUser(year, userKey.value);
    if (year === new Date().getFullYear()) {
        const loadedEventKeys = eventsByYear.value.get(0)?.map((it) => it.key);
        events = events.filter((it) => !loadedEventKeys?.includes(it.key));
    }
    events = events.sort((a, b) => b.start.getTime() - a.start.getTime());
    eventsByYear.value.set(year, events);
}

async function fetchPositions(): Promise<void> {
    positions.value = await usersUseCase.resolvePositionNames();
}

async function save(): Promise<void> {
    if (user.value) {
        try {
            await userAdministrationUseCase.updateUser(user.value);
        } catch (e) {
            errorHandlingUseCase.handleError({
                title: 'Speichern fehlgeschlagen',
                message: `Deine Änderungen konnten nicht gespeichert werden. Bitte versuche es erneut. Sollte der Fehler
                    wiederholt auftreten, melde ihn gerne.`,
                error: e,
                retry: () => save(),
            });
            throw e;
        }
    }
}

function impersonateUser() {
    if (user.value) {
        authUseCase.impersonateUser(user.value.key);
    }
}

async function createRegistration() {
    if (createRegistrationForUserDialog.value && user.value) {
        await createRegistrationForUserDialog.value.open(user.value);
        // TODO
    }
}

init();
</script>
