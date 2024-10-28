<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <DetailsPage :back-to="{ name: Routes.UsersList }">
            <template #header>
                <div class="hidden pt-8 xl:block">
                    <h1>{{ user?.firstName }} {{ user?.lastName }} bearbeiten</h1>
                    <p v-if="user && signedInUser.key === user.key" class="mt-1 text-sm">Das bist du!</p>
                </div>
            </template>
            <template #content>
                <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-8 xl:top-0">
                    <template #[Tab.USER_DATA]>
                        <div class="max-w-2xl space-y-8 xl:space-y-16">
                            <UserDataForm v-if="user" v-model="user" />
                        </div>
                    </template>
                    <template #[Tab.USER_CONTACT_DATA]>
                        <div class="max-w-2xl space-y-8 xl:space-y-16">
                            <UserContactForm v-if="user" v-model="user" />
                        </div>
                    </template>
                    <template #[Tab.USER_EVENTS]>
                        <div class="xl:max-w-5xl">
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
                        <div class="xl:max-w-5xl">
                            <div class="-mx-8 md:-mx-16 xl:-mx-20">
                                <UserQualificationsTable v-if="user" v-model="user" />
                            </div>
                        </div>
                    </template>
                    <template #[Tab.USER_ROLES]>
                        <div class="xl:max-w-5xl">
                            <div class="-mx-8 md:-mx-16 xl:-mx-20">
                                <UserRolesTable v-if="user" v-model="user" />
                            </div>
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
                <button v-if="tab === Tab.USER_EVENTS" class="btn-secondary" @click="createRegistration()">
                    <i class="fa-solid fa-user-plus"></i>
                    <span>Reise hinzufügen</span>
                </button>
                <button v-if="tab === Tab.USER_CERTIFICATES" class="btn-secondary" @click="addUserQualification()">
                    <i class="fa-solid fa-file-circle-plus"></i>
                    <span>Qualifikation hinzufügen</span>
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
                <li class="context-menu-item" @click="addUserQualification()">
                    <i class="fa-solid fa-file-circle-plus" />
                    <span>Qualifikation hinzufügen</span>
                </li>
                <li class="context-menu-item">
                    <i class="fa-solid fa-key" />
                    <span>Passwort zurücksetzen</span>
                </li>
            </template>
        </DetailsPage>
        <CreateRegistrationForUserDlg ref="createRegistrationForUserDialog" />
        <QualificationEditDlg ref="addQualificationDialog" />
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { deepCopy } from '@/common';
import type { Event, Position, PositionKey, UserDetails, UserQualification } from '@/domain';
import { Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VTabs } from '@/ui/components/common';
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
import QualificationEditDlg from '@/ui/views/admin/users/details/QualificationEditDlg.vue';
import UserContactForm from '@/ui/views/admin/users/details/UserContactForm.vue';
import UserDataForm from '@/ui/views/admin/users/details/UserDataForm.vue';
import UserEventsTable from '@/ui/views/admin/users/details/UserEventsTable.vue';
import UserQualificationsTable from '@/ui/views/admin/users/details/UserQualificationsTable.vue';
import UserRolesTable from '@/ui/views/admin/users/details/UserRolesTable.vue';

enum Tab {
    USER_DATA = 'app.user-details.tab.data',
    USER_CONTACT_DATA = 'app.user-details.tab.contact',
    USER_CERTIFICATES = 'app.user-details.tab.certificates',
    USER_EVENTS = 'app.user-details.tab.events',
    USER_ROLES = 'app.user-details.tab.roles',
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const userAdministrationUseCase = useUserAdministrationUseCase();
const usersUseCase = useUsersUseCase();
const eventsUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const errorHandlingUseCase = useErrorHandling();
const signedInUser = authUseCase.getSignedInUser();

const tabs = [Tab.USER_EVENTS, Tab.USER_DATA, Tab.USER_CONTACT_DATA, Tab.USER_CERTIFICATES, Tab.USER_ROLES];
const tab = ref<Tab>(Tab.USER_EVENTS);
const userOriginal = ref<UserDetails | null>(null);
const user = ref<UserDetails | null>(null);
const eventsByYear = ref<Map<number, Event[] | undefined>>(new Map<number, Event[] | undefined>());
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const createRegistrationForUserDialog = ref<Dialog<UserDetails> | null>(null);
const addQualificationDialog = ref<Dialog<UserQualification | undefined, UserQualification> | null>(null);
const eventsLoadedUntilYear = ref<number>(0);

const userKey = computed<string>(() => (route.params.key as string) || '');

function init(): void {
    fetchUser();
    fetchPositions();
    fetchUserFutureEvents();
}

async function fetchUser(): Promise<void> {
    userOriginal.value = await userAdministrationUseCase.getUserDetailsByKey(userKey.value);
    user.value = deepCopy(userOriginal.value);
    emit('update:title', `${user.value.firstName} ${user.value.lastName}`);
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
    if (userOriginal.value && user.value) {
        try {
            userOriginal.value = await userAdministrationUseCase.updateUser(userOriginal.value, user.value);
            user.value = deepCopy(userOriginal.value);
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
        // TODO add to buffer and persist on save
    }
}

function addUserQualification(): void {
    if (addQualificationDialog.value) {
        addQualificationDialog.value
            .open(undefined)
            .then((newQualification) => {
                if (user.value?.qualifications) {
                    user.value.qualifications.push(newQualification);
                } else if (user.value) {
                    user.value.qualifications = [newQualification];
                }
            })
            .catch();
    }
}

init();
</script>
