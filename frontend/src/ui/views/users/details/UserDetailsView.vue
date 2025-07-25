<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <DetailsPage :back-to="{ name: Routes.UsersList }">
            <template #header> {{ user?.firstName }} {{ user?.lastName }} bearbeiten </template>
            <template #content>
                <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-20 xl:pt-8">
                    <template #[Tab.USER_DATA]>
                        <div class="max-w-2xl space-y-8 xl:space-y-16">
                            <UserDataForm v-if="user" v-model="user" :errors="validation.errors.value" />
                        </div>
                    </template>
                    <template #[Tab.USER_CONTACT_DATA]>
                        <div class="max-w-2xl space-y-8 xl:space-y-16">
                            <UserContactForm v-if="user" v-model="user" :errors="validation.errors.value" />
                        </div>
                    </template>
                    <template #[Tab.USER_EVENTS]>
                        <div class="xl:max-w-5xl">
                            <div v-for="[year, events] in eventsByYear" :key="`${year}-${events?.length}`">
                                <h2 class="mb-4 font-bold text-secondary">
                                    <template v-if="year === 0">Zukünftige Reisen</template>
                                    <template v-else>Reisen {{ year }}</template>
                                </h2>
                                <UserEventsTable
                                    v-if="user"
                                    :events="events"
                                    :user="user"
                                    @update:events="year === 0 ? fetchUserFutureEvents() : fetchUserEventsOfYear(year)"
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
                            <div class="-mx-4 xs:-mx-8 md:-mx-16 xl:-mx-20">
                                <UserQualificationsTable v-if="user" v-model="user" />
                            </div>
                        </div>
                    </template>
                    <template #[Tab.USER_ROLES]>
                        <div class="xl:max-w-5xl">
                            <div class="-mx-4 xs:-mx-8 md:-mx-16 xl:-mx-20">
                                <UserRolesTable v-if="user" v-model="user" />
                            </div>
                        </div>
                    </template>
                    <template #[Tab.USER_EMERGENCY]>
                        <div class="max-w-2xl space-y-8 xl:space-y-16">
                            <UserEmergencyForm v-if="user" v-model="user" :errors="validation.errors.value" />
                        </div>
                    </template>
                    <template #[Tab.USER_OTHER]>
                        <div class="max-w-2xl space-y-8 xl:space-y-16">
                            <UserOtherForm v-if="user" v-model="user" :errors="validation.errors.value" />
                        </div>
                    </template>
                </VTabs>
            </template>
            <template v-if="signedInUser.permissions.includes(Permission.WRITE_USERS)" #primary-button>
                <AsyncButton :action="save" name="save" :disabled="validation.disableSubmit.value">
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
                    <span>Anmeldung hinzufügen</span>
                </button>
                <button v-else-if="tab === Tab.USER_CERTIFICATES" class="btn-secondary" @click="addUserQualification()">
                    <i class="fa-solid fa-file-circle-plus"></i>
                    <span>Qualifikation hinzufügen</span>
                </button>
                <a
                    v-else-if="tab === Tab.USER_CONTACT_DATA && user?.email"
                    class="btn-secondary"
                    :href="`mailto:${user.email}`"
                    target="_blank"
                >
                    <i class="fa-solid fa-envelope"></i>
                    <span>Email schreiben</span>
                </a>
            </template>
            <template #actions-menu>
                <li class="context-menu-item" @click="impersonateUser()">
                    <i class="fa-solid fa-user-secret" />
                    <span>Impersonate</span>
                </li>
                <li>
                    <a v-if="user?.email" class="context-menu-item" :href="`mailto:${user.email}`" target="_blank">
                        <i class="fa-solid fa-envelope"></i>
                        <span>Email schreiben</span>
                    </a>
                </li>
                <li class="context-menu-item" @click="createRegistration()">
                    <i class="fa-solid fa-user-plus" />
                    <span>Anmeldung hinzufügen</span>
                </li>
                <li class="context-menu-item" @click="addUserQualification()">
                    <i class="fa-solid fa-file-circle-plus" />
                    <span>Qualifikation hinzufügen</span>
                </li>
            </template>
        </DetailsPage>
        <CreateRegistrationForUserDlg ref="createRegistrationForUserDialog" />
        <UserQualificationDetailsDlg ref="addUserQualificationDialog" />
        <VConfirmationDialog ref="confirmDialog" />
    </div>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { deepCopy, diff } from '@/common';
import type { Event, UserDetails, UserQualification } from '@/domain';
import { Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { AsyncButton, VConfirmationDialog, VTabs } from '@/ui/components/common';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useAuthUseCase, useErrorHandling, useEventUseCase, useUserAdministrationUseCase } from '@/ui/composables/Application.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { Routes } from '@/ui/views/Routes.ts';
import CreateRegistrationForUserDlg from '@/ui/views/users/components/CreateRegistrationForUserDlg.vue';
import UserEmergencyForm from '@/ui/views/users/details/UserEmergencyForm.vue';
import UserOtherForm from '@/ui/views/users/details/UserOtherForm.vue';
import UserContactForm from './UserContactForm.vue';
import UserDataForm from './UserDataForm.vue';
import UserEventsTable from './UserEventsTable.vue';
import UserQualificationDetailsDlg from './UserQualificationDetailsDlg.vue';
import UserQualificationsTable from './UserQualificationsTable.vue';
import UserRolesTable from './UserRolesTable.vue';

enum Tab {
    USER_DATA = 'data',
    USER_CONTACT_DATA = 'contact',
    USER_CERTIFICATES = 'certificates',
    USER_EVENTS = 'events',
    USER_ROLES = 'roles',
    USER_EMERGENCY = 'emergency',
    USER_OTHER = 'other',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const userAdministrationUseCase = useUserAdministrationUseCase();
const eventsUseCase = useEventUseCase();
const authUseCase = useAuthUseCase();
const errorHandlingUseCase = useErrorHandling();
const signedInUser = authUseCase.getSignedInUser();

const tabs = [
    Tab.USER_EVENTS,
    Tab.USER_CERTIFICATES,
    Tab.USER_DATA,
    Tab.USER_CONTACT_DATA,
    Tab.USER_EMERGENCY,
    Tab.USER_OTHER,
    Tab.USER_ROLES,
].map((it) => ({
    value: it,
    label: t(`views.user-details.tab.${it}`),
}));
const tab = ref<Tab>(Tab.USER_EVENTS);
const userOriginal = ref<UserDetails | null>(null);
const user = ref<UserDetails | null>(null);
const hasChanges = ref<boolean>(false);
const eventsByYear = ref<Map<number, Event[] | undefined>>(new Map<number, Event[] | undefined>());
const eventsLoadedUntilYear = ref<number>(0);
const validation = useValidation(user, userAdministrationUseCase.validate);

const createRegistrationForUserDialog = ref<Dialog<UserDetails, boolean> | null>(null);
const addUserQualificationDialog = ref<Dialog<void, UserQualification | undefined> | null>(null);
const confirmDialog = ref<ConfirmationDialog | null>(null);

const userKey = computed<string>(() => (route.params.key as string) || '');

function init(): void {
    fetchUser();
    fetchUserFutureEvents();
    preventPageUnloadOnUnsavedChanges();
}

async function fetchUser(): Promise<void> {
    userOriginal.value = await userAdministrationUseCase.getUserDetailsByKey(userKey.value);
    user.value = deepCopy(userOriginal.value);
    emit('update:tab-title', `${user.value.firstName} ${user.value.lastName}`);
    validation.showErrors.value = true;
}

function preventPageUnloadOnUnsavedChanges(): void {
    watch(() => user.value, updateHasChanges, { deep: true });
    const removeNavigationGuard = router.beforeEach(async (to, from) => {
        if (to.name === from.name) {
            // we stay on the same page and only change
            return true;
        }
        if (hasChanges.value) {
            const continueNavigation = await confirmDialog.value?.open({
                title: 'Änderungen verwerfen?',
                message: `Du hast ungespeicherte Änderungen. Wenn du die Seite verlässt oder neu lädst, werden
                    diese Änderungen verworfen. Möchtest du forfahren?`,
                cancel: 'Abbrechen',
                submit: 'Änderungen verwerfen',
            });
            if (!continueNavigation) {
                return false;
            }
        }
        removeNavigationGuard();
        return true;
    });
}

function updateHasChanges(): void {
    if (userOriginal.value !== null && user.value !== null) {
        const changes = diff(userOriginal.value, user.value);
        hasChanges.value = Object.keys(changes).length > 0;
    } else {
        hasChanges.value = true;
    }
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

function impersonateUser(): void {
    if (user.value) {
        authUseCase.impersonateUser(user.value.key);
    }
}

async function createRegistration(): Promise<void> {
    if (createRegistrationForUserDialog.value && user.value) {
        const created = await createRegistrationForUserDialog.value.open(user.value);
        if (created) {
            await fetchUserFutureEvents();
        }
    }
}

async function addUserQualification(): Promise<void> {
    const result = await addUserQualificationDialog.value?.open();
    if (result && user.value) {
        if (!user.value.qualifications) {
            user.value.qualifications = [];
        }
        user.value.qualifications.push(result);
    }
}

init();
</script>
