<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <NavbarFilter v-model="filter" placeholder="Nutzer filtern" />
        </teleport>
        <div v-if="false" class="z-20 hidden bg-primary-50 px-4 pb-16 pt-4 md:px-16 md:pt-8 xl:top-0 xl:block xl:px-20">
            <div class="-ml-6 flex items-center space-x-4">
                <VInputText v-model="filter" class="input-search w-96" placeholder="Nutzer filtern">
                    <template #before>
                        <i class="fa-solid fa-magnifying-glass ml-4 text-primary-900 text-opacity-25" />
                    </template>
                </VInputText>
                <!--                <VInputCheckBox v-model="filterOnlyActive" label="Nur Stammcrew mit Anmeldungen" />-->
                <div class="hidden flex-grow md:block"></div>
                <div class="hidden items-stretch justify-end space-x-2 md:flex">
                    <button class="btn-primary flex-grow whitespace-nowrap" @click="importUsers()">
                        <i class="fa-solid fa-upload"></i>
                        <span>Nutzer importieren</span>
                    </button>
                </div>
            </div>
            <div class="flex items-center">
                <span></span>
            </div>
        </div>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-8 xl:top-0">
            <template #end>
                <div class="flex items-stretch gap-2 pb-2">
                    <div
                        class="hidden w-44 cursor-pointer items-center gap-2 rounded-lg px-4 py-2 transition-all duration-100 focus-within:w-64 focus-within:cursor-text focus-within:bg-primary-100 hover:bg-primary-100 lg:flex xxl:focus-within:w-80"
                    >
                        <i class="fa-solid fa-search text-primary-700"></i>
                        <input
                            v-model="filter"
                            class="w-0 flex-grow cursor-pointer bg-transparent placeholder-primary-700 focus-within:cursor-text focus-within:placeholder-primary-300"
                            placeholder="Nutzer filtern"
                        />
                        <button v-if="filter !== ''" @click="filter = ''">
                            <i class="fa-solid fa-xmark"></i>
                        </button>
                    </div>
                    <button
                        v-if="signedInUser.permissions.includes(Permission.WRITE_USERS)"
                        class="btn-ghost"
                        @click="importUsers()"
                    >
                        <i class="fa-solid fa-upload"></i>
                        <span class="text-base">Importieren</span>
                    </button>
                    <div class="hidden 2xl:block">
                        <button class="btn-primary ml-2">
                            <i class="fa-solid fa-user-plus"></i>
                            <span class="">Hinzufügen</span>
                        </button>
                    </div>
                </div>
            </template>
        </VTabs>

        <div class="w-full">
            <VTable
                :items="filteredUsers"
                :page-size="10"
                :query="true"
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="editUser($event)"
                @click-ctrl="selectUser($event)"
            >
                <template #row="{ item }">
                    <td @click.stop="item.selected = !item.selected">
                        <span v-if="item.selected">
                            <i class="fa-solid fa-check-square text-2xl text-primary-600"></i>
                        </span>
                        <span v-else>
                            <i class="fa-solid fa-square text-2xl text-primary-200"></i>
                        </span>
                    </td>
                    <td class="w-1/3 whitespace-nowrap font-semibold">
                        <p class="mb-2">{{ item.nickName || item.firstName }} {{ item.lastName }}</p>
                        <p v-if="item.rolesStr" class="max-w-64 truncate text-sm" :title="item.rolesStr">
                            {{ item.rolesStr }}
                        </p>
                        <p v-else class="max-w-64 truncate text-sm italic">Keine Rolle zugewiesen</p>
                    </td>
                    <td class="w-1/4">
                        <div class="flex max-w-64 flex-wrap">
                            <span
                                v-for="position in item.positions"
                                :key="position.key"
                                class="position my-0.5 mr-1 bg-gray-500 text-xs opacity-75"
                                :style="{ background: position.color }"
                            >
                                {{ position.name }}
                            </span>
                        </div>
                    </td>
                    <td class="w-1/5">
                        <div class="flex space-x-8">
                            <div :class="{ 'opacity-25': !item.singleDayEventsCount }">
                                <p class="mb-1 font-semibold">{{ item.singleDayEventsCount || '-' }}</p>
                                <p class="text-sm" title="Tagesfahrten">TF</p>
                            </div>
                            <div :class="{ 'opacity-25': !item.weekendEventsCount }">
                                <p class="mb-1 font-semibold">{{ item.weekendEventsCount || '-' }}</p>
                                <p class="text-sm" title="Wochenendfahrten">WE</p>
                            </div>
                            <div :class="{ 'opacity-25': !item.multiDayEventsCount }">
                                <p class="mb-1 font-semibold">{{ item.multiDayEventsCount || '-' }}</p>
                                <p class="text-sm" title="Sommerreisen und mehrtägige Fahrten">SR</p>
                            </div>
                            <div :class="{ 'opacity-25': !item.waitingListCount }">
                                <p class="mb-1 font-semibold">{{ item.waitingListCount || '-' }}</p>
                                <p class="text-sm" title="Warteliste">WL</p>
                            </div>
                        </div>
                    </td>
                    <td class="w-1/12">
                        <p class="mb-1 font-semibold">????</p>
                        <p class="text-sm">Stunden</p>
                    </td>
                    <td class="w-1/12">
                        <div
                            v-if="item.qualifications?.length === 0"
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-gray-200 py-1 pl-3 pr-4 text-gray-700"
                        >
                            <i class="fa-solid fa-question-circle"></i>
                            <span class="whitespace-nowrap font-semibold">Keine Angaben</span>
                        </div>
                        <div
                            v-else-if="item.expiredQualifications.length > 0"
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-red-100 py-1 pl-3 pr-4 text-red-700"
                            :title="item.expiredQualifications.join(', ')"
                        >
                            <i class="fa-solid fa-ban"></i>
                            <span class="whitespace-nowrap font-semibold"
                                >{{ item.expiredQualifications.length }} abgelaufen</span
                            >
                        </div>
                        <div
                            v-else-if="item.soonExpiringQualifications.length > 0"
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                            :title="item.soonExpiringQualifications.join(', ')"
                        >
                            <i class="fa-solid fa-warning"></i>
                            <span class="whitespace-nowrap font-semibold">
                                <template v-if="item.soonExpiringQualifications.length === 1">1 läuft bald ab</template>
                                <template v-else>{{ item.soonExpiringQualifications.length }} laufen bald ab</template>
                            </span>
                        </div>
                        <div
                            v-else
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-200 py-1 pl-3 pr-4 text-green-700"
                        >
                            <i class="fa-solid fa-check-circle"></i>
                            <span class="whitespace-nowrap font-semibold">Alle gültig</span>
                        </div>
                    </td>
                    <td class="w-0">
                        <ContextMenuButton class="px-4 py-2">
                            <ul>
                                <li class="context-menu-item" @click="editUser(item)">
                                    <i class="fa-solid fa-user-edit" />
                                    <span>Bearbeiten</span>
                                </li>
                                <li class="context-menu-item" @click="impersonateUser(item)">
                                    <i class="fa-solid fa-user-secret" />
                                    <span>Impersonate</span>
                                </li>
                                <li v-if="item.email">
                                    <a :href="`mailto:${item.email}`" class="context-menu-item">
                                        <i class="fa-solid fa-envelope" />
                                        <span>Email schreiben</span>
                                    </a>
                                </li>
                                <li class="context-menu-item" @click="createRegistration(item)">
                                    <i class="fa-solid fa-calendar-plus" />
                                    <span>Anmeldung hinzufügen</span>
                                </li>
                                <li
                                    v-if="signedInUser.permissions.includes(Permission.DELETE_USERS)"
                                    class="context-menu-item text-red-700"
                                    @click="deleteUser(item)"
                                >
                                    <i class="fa-solid fa-trash" />
                                    <span>Nutzer löschen</span>
                                </li>
                            </ul>
                        </ContextMenuButton>
                    </td>
                </template>
                <template #loading>
                    <UsersListSkeletonLoader :count="20" />
                </template>
            </VTable>
        </div>

        <div class="sticky bottom-0 right-0 z-10 flex justify-end pb-4 pr-3 md:pr-14 2xl:hidden">
            <button class="btn-primary btn-floating">
                <i class="fa-solid fa-calendar-plus"></i>
                <span>Hinzufügen</span>
            </button>
        </div>

        <CreateRegistrationForUserDlg ref="createRegistrationForUserDialog" />
        <VConfirmationDialog ref="deleteUserDialog">
            <template #title>Nutzer löschen?</template>
            <template #message>
                Bist du sicher, das du den Nutzer löschen möchtest? Wenn der Nutzer sich schon zu Reisen angemeldet hat,
                wird dies dazu führen, das in den Crew oder Wartelisten ein ungültiger Eintrag existiert. Löschen von
                Nutzern sollte darum nur nach reichlicher Überlegung passieren.
            </template>
            <template #submit>Löschen</template>
        </VConfirmationDialog>
        <ImportUsersDlg ref="importUsersDialog" />

        <div class="flex-1"></div>

        <div v-if="selectedCount > 0" class="sticky bottom-0 z-20">
            <div
                class="h-full border-t border-primary-200 bg-primary-50 px-4 md:px-12 xl:rounded-bl-3xl xl:pb-4 xl:pl-16 xl:pr-20"
            >
                <div class="flex h-full items-stretch gap-2 py-2">
                    <button class="btn-ghost" @click="selectNone()">
                        <i class="fa-solid fa-xmark text-base"></i>
                    </button>
                    <span class="self-center text-base font-bold">{{ selectedCount }} Nutzer ausgewählt</span>
                    <div class="flex-grow"></div>
                    <button class="btn-ghost">
                        <i class="fa-solid fa-envelope"></i>
                        <span class="text-base">Email schreiben</span>
                    </button>
                    <button class="btn-ghost">
                        <i class="fa-solid fa-wrench"></i>
                        <span class="text-base">Arbeitsdienst eintragen</span>
                    </button>
                    <ContextMenuButton class="btn-ghost">
                        <template #default>
                            <li class="context-menu-item">
                                <i class="fa-solid fa-wrench" />
                                <span>Arbeitsdienst eintragen</span>
                            </li>
                            <template v-if="signedInUser.permissions.includes(Permission.DELETE_USERS)">
                                <li class="context-menu-item text-red-700">
                                    <i class="fa-solid fa-trash-alt" />
                                    <span>Nutzer löschen</span>
                                </li>
                            </template>
                        </template>
                    </ContextMenuButton>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { filterUndefined } from '@/common';
import type { Position, QualificationKey, User } from '@/domain';
import { Permission } from '@/domain';
import { EventType, Role } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { ContextMenuButton, VConfirmationDialog, VInputText, VTable, VTabs } from '@/ui/components/common';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import {
    useAuthUseCase,
    useEventUseCase,
    useUserAdministrationUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application';
import { useEventService, useUserService } from '@/ui/composables/Domain';
import type { Selectable } from '@/ui/model/Selectable';
import { Routes } from '@/ui/views/Routes';
import CreateRegistrationForUserDlg from '@/ui/views/admin/users/components/CreateRegistrationForUserDlg.vue';
import ImportUsersDlg from '@/ui/views/admin/users/list/ImportUsersDlg.vue';
import UsersListSkeletonLoader from '@/ui/views/admin/users/list/UsersListSkeletonLoader.vue';

enum Tab {
    ACTIVE_TEAM_MEMBERS = 'Stammcrew (aktive)',
    TEAM_MEMBERS = 'Stammcrew',
    ADMINS = 'Verwalter',
    UNMATCHED_USERS = 'Nutzer ohne Rolle',
}

interface UserRegistrations extends User, Selectable {
    positions: Position[];
    rolesStr: string;
    singleDayEventsCount: number;
    weekendEventsCount: number;
    multiDayEventsCount: number;
    waitingListCount: number;
    expiredQualifications: QualificationKey[];
    soonExpiringQualifications: QualificationKey[];
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const i18n = useI18n();
const eventUseCase = useEventUseCase();
const eventService = useEventService();
const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const authUseCase = useAuthUseCase();
const userAdministrationUseCase = useUserAdministrationUseCase();
const router = useRouter();
const signedInUser = authUseCase.getSignedInUser();

const tabs = [Tab.TEAM_MEMBERS, Tab.ACTIVE_TEAM_MEMBERS, Tab.ADMINS, Tab.UNMATCHED_USERS];
const tab = ref<string>(tabs[0]);
const filter = ref<string>('');
const users = ref<UserRegistrations[] | undefined>(undefined);

const importUsersDialog = ref<Dialog | null>(null);
const createRegistrationForUserDialog = ref<Dialog<User> | null>(null);
const deleteUserDialog = ref<Dialog<User> | null>(null);

const filteredUsers = computed<UserRegistrations[] | undefined>(() =>
    users.value?.filter((it) => matchesActiveCategory(it) && usersService.doesUserMatchFilter(it, filter.value))
);

const selectedCount = computed<number>(() => {
    return filteredUsers.value?.filter((it) => it.selected).length || 0;
});

function init(): void {
    emit('update:title', 'Nutzer verwalten');
    fetchUsers();
}

function hasAnyEvents(user: UserRegistrations): boolean {
    return (
        user.waitingListCount > 0 ||
        user.multiDayEventsCount > 0 ||
        user.weekendEventsCount > 0 ||
        user.singleDayEventsCount > 0
    );
}

function matchesActiveCategory(user: UserRegistrations): boolean {
    switch (tab.value) {
        case Tab.ACTIVE_TEAM_MEMBERS:
            return user.roles !== undefined && user.roles.includes(Role.TEAM_MEMBER) && hasAnyEvents(user);
        case Tab.TEAM_MEMBERS:
            return user.roles !== undefined && user.roles.includes(Role.TEAM_MEMBER);
        case Tab.UNMATCHED_USERS:
            return user.roles === undefined || user.roles.length === 0;
        case Tab.ADMINS:
            return (
                user.roles !== undefined &&
                (user.roles.includes(Role.ADMIN) ||
                    user.roles.includes(Role.USER_MANAGER) ||
                    user.roles.includes(Role.TEAM_PLANNER) ||
                    user.roles.includes(Role.EVENT_PLANNER) ||
                    user.roles.includes(Role.EVENT_LEADER))
            );
    }
    return true;
}

function importUsers(): void {
    importUsersDialog.value?.open().catch();
}

function selectUser(user: UserRegistrations): void {
    user.selected = !user.selected;
}

async function editUser(user: UserRegistrations): Promise<void> {
    if (selectedCount.value > 0) {
        user.selected = !user.selected;
    } else if (signedInUser.permissions.includes(Permission.WRITE_USERS)) {
        await router.push({ name: Routes.UserDetails, params: { key: user.key } });
    }
}

function impersonateUser(user: UserRegistrations): void {
    authUseCase.impersonateUser(user.key);
}

async function createRegistration(user: UserRegistrations): Promise<void> {
    await createRegistrationForUserDialog.value
        ?.open(user)
        .then(() => (user.waitingListCount = user.waitingListCount + 1))
        .catch(() => {
            // ignore
        });
}

async function deleteUser(user: UserRegistrations): Promise<void> {
    await deleteUserDialog.value
        ?.open()
        .then(async () => {
            await userAdministrationUseCase.deleteUserByKey(user.key);
            await fetchUsers();
        })
        .catch(() => {
            // ignore
        });
}

function selectNone(): void {
    users.value?.forEach((it) => (it.selected = false));
}

async function fetchUsers(): Promise<void> {
    const positions = await usersUseCase.resolvePositionNames();
    const userlist: User[] = await usersUseCase.getUsers();
    const events = await eventUseCase.getEvents(new Date().getFullYear());
    const registrationsSingleDayEventsWithSlot = events
        .filter((evt) => evt.type === EventType.SingleDayEvent)
        .flatMap((evt) => eventService.getAssignedRegistrations(evt));
    const registrationsWeekendEventsWithSlot = events
        .filter((evt) => evt.type === EventType.WeekendEvent)
        .flatMap((evt) => eventService.getAssignedRegistrations(evt));
    const registrationsMultiDayEventsWithSlot = events
        .filter((evt) => evt.type === EventType.MultiDayEvent)
        .flatMap((evt) => eventService.getAssignedRegistrations(evt));
    const registrationsWaitinglist = events.flatMap((evt) => eventService.getRegistrationsOnWaitinglist(evt));

    users.value = userlist.map((user: User) => {
        return {
            ...user,
            rolesStr: user.roles?.map((k) => i18n.t(`app.role.${k}`)).join(', ') || '',
            waitingListCount: registrationsWaitinglist.filter((it) => it.userKey === user.key).length,
            multiDayEventsCount: registrationsMultiDayEventsWithSlot.filter((it) => it.userKey === user.key).length,
            weekendEventsCount: registrationsWeekendEventsWithSlot.filter((it) => it.userKey === user.key).length,
            singleDayEventsCount: registrationsSingleDayEventsWithSlot.filter((it) => it.userKey === user.key).length,
            positions: user.positionKeys.map((key) => positions.get(key)).filter(filterUndefined),
            expiredQualifications: usersService.getExpiredQualifications(user),
            soonExpiringQualifications: usersService.getSoonExpiringQualifications(user),
        };
    });
}

init();
</script>
