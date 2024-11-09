<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" placeholder="Nutzer filtern" />
            </div>
        </teleport>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <VSearchButton v-model="filter" placeholder="Nutzer filtern" />
                    <div class="hidden 2xl:block">
                        <button
                            v-if="signedInUser.permissions.includes(Permission.WRITE_USERS)"
                            class="btn-primary"
                            @click="importUsers()"
                        >
                            <i class="fa-solid fa-upload"></i>
                            <span class="">Importieren</span>
                        </button>
                    </div>
                </div>
            </template>
        </VTabs>

        <div class="scrollbar-invisible mt-4 flex items-center gap-2 overflow-x-auto px-4 md:px-16 xl:min-h-8 xl:px-20">
            <ContextMenuButton
                class="btn-tag min-w-44 max-w-80 truncate"
                :class="{ active: filterPositions.length > 0 }"
            >
                <template #icon>
                    <span v-if="filterPositions.length === 0" class="mr-2">Alle Positionen</span>
                    <span v-else-if="filterPositions.length > 4" class="mr-2"
                        >{{ filterPositions.length }} Positionen</span
                    >
                    <span v-else class="mr-2">
                        {{
                            filterPositions
                                .map(positions.get)
                                .map((it) => it.name)
                                .join(', ')
                        }}
                    </span>
                    <i class="fa-solid fa-chevron-down"></i>
                </template>
                <template #default>
                    <ul>
                        <li v-if="filterPositions.length === 0" class="context-menu-item">
                            <i class="fa-solid fa-check"></i>
                            <span>Alle Positionen</span>
                        </li>
                        <li v-else class="context-menu-item" @click="filterPositions = []">
                            <i class="w-4"></i>
                            <span>Alle Positionen</span>
                        </li>
                        <hr />
                        <template v-for="position in positions.all.value" :key="position.key">
                            <li
                                v-if="filterPositions.includes(position.key)"
                                class="context-menu-item"
                                @click="filterPositions = filterPositions.filter((it) => it !== position.key)"
                            >
                                <i class="fa-solid fa-check w-4"></i>
                                <span>{{ position.name }}</span>
                            </li>
                            <li v-else class="context-menu-item" @click="filterPositions.push(position.key)">
                                <i class="w-4"></i>
                                <span>{{ position.name }}</span>
                            </li>
                        </template>
                    </ul>
                </template>
            </ContextMenuButton>
            <button class="btn-tag" :class="{ active: filterOnlyActive }" @click="filterOnlyActive = !filterOnlyActive">
                <span class="">Aktive Stammcrew</span>
            </button>
            <button
                class="btn-tag"
                :class="{ active: filterExpiredQualifications }"
                @click="filterExpiredQualifications = !filterExpiredQualifications"
            >
                <span class="">Abgelaufene Qualifikationen</span>
            </button>
        </div>

        <div class="w-full">
            <VTable
                :items="filteredUsers"
                :page-size="10"
                query
                multiselection
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="editUser($event)"
            >
                <template #row="{ item }">
                    <td class="w-1/3 whitespace-nowrap font-semibold">
                        <p class="mb-2">{{ item.nickName || item.firstName }} {{ item.lastName }}</p>
                        <p v-if="item.rolesStr" class="max-w-64 truncate text-sm" :title="item.rolesStr">
                            {{ item.rolesStr }}
                        </p>
                        <p v-else class="max-w-64 truncate text-sm italic">Keine Rolle zugewiesen</p>
                    </td>
                    <td class="w-1/3">
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
                        <div class="flex items-center justify-end">
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
                                <span class="whitespace-nowrap font-semibold">
                                    {{ item.expiredQualifications.length }} abgelaufen
                                </span>
                            </div>
                            <div
                                v-else-if="item.soonExpiringQualifications.length > 0"
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                                :title="item.soonExpiringQualifications.join(', ')"
                            >
                                <i class="fa-solid fa-warning"></i>
                                <span class="whitespace-nowrap font-semibold">
                                    <template v-if="item.soonExpiringQualifications.length === 1"
                                        >1 läuft bald ab</template
                                    >
                                    <template v-else
                                        >{{ item.soonExpiringQualifications.length }} laufen bald ab</template
                                    >
                                </span>
                            </div>
                            <div
                                v-else
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-200 py-1 pl-3 pr-4 text-green-700"
                            >
                                <i class="fa-solid fa-check-circle"></i>
                                <span class="whitespace-nowrap font-semibold">Alle gültig</span>
                            </div>
                        </div>
                    </td>
                </template>
                <template #context-menu="{ item }">
                    <li class="context-menu-item" :class="{ disabled: !item.email }" @click="contactUsers([item])">
                        <i class="fa-solid fa-envelope" />
                        <span>Email schreiben</span>
                    </li>
                    <li class="context-menu-item" @click="impersonateUser(item)">
                        <i class="fa-solid fa-user-secret" />
                        <span>Impersonate</span>
                    </li>
                    <li class="context-menu-item" @click="createRegistration(item)">
                        <i class="fa-solid fa-calendar-plus" />
                        <span>Anmeldung hinzufügen</span>
                    </li>
                    <li class="context-menu-item" @click="editUser(item)">
                        <i class="fa-solid fa-edit" />
                        <span>Nutzer bearbeiten</span>
                    </li>
                    <li class="permission-delete-users context-menu-item text-red-700" @click="deleteUser(item)">
                        <i class="fa-solid fa-trash-alt" />
                        <span>Nutzer löschen</span>
                    </li>
                </template>
                <template #loading>
                    <UsersListSkeletonLoader :count="20" />
                </template>
            </VTable>
        </div>

        <CreateRegistrationForUserDlg ref="createRegistrationForUserDialog" />
        <VConfirmationDialog ref="deleteUserDialog" />
        <ImportUsersDlg ref="importUsersDialog" />

        <div class="flex-1"></div>

        <div v-if="selectedUsers && selectedUsers.length > 0" class="sticky bottom-0 z-20">
            <div
                class="h-full border-t border-primary-200 bg-primary-50 px-4 py-2 md:px-12 xl:rounded-bl-3xl xl:py-4 xl:pl-16 xl:pr-20"
            >
                <div class="flex h-full items-stretch gap-2 whitespace-nowrap">
                    <button class="btn-ghost" @click="selectNone()">
                        <i class="fa-solid fa-xmark w-6" />
                    </button>
                    <span class="self-center font-bold">{{ selectedUsers.length }} ausgewählt</span>
                    <div class="flex-grow"></div>

                    <div class="hidden sm:block">
                        <button class="btn-ghost" @click="contactUsers(selectedUsers)">
                            <i class="fa-solid fa-envelope" />
                            <span class="">Email schreiben</span>
                        </button>
                    </div>
                    <div class="hidden lg:block xl:hidden 2xl:block">
                        <button class="btn-ghost" disabled>
                            <i class="fa-solid fa-screwdriver-wrench"></i>
                            <span class="">Arbeitsdienst eintragen*</span>
                        </button>
                    </div>
                    <ContextMenuButton class="btn-ghost">
                        <template #default>
                            <li class="context-menu-item" @click="selectAll">
                                <i class="fa-solid fa-list-check" />
                                <span>Alle auswählen</span>
                            </li>
                            <li class="context-menu-item" @click="contactUsers(selectedUsers)">
                                <i class="fa-solid fa-envelope" />
                                <span>Email schreiben</span>
                            </li>
                            <li class="context-menu-item disabled">
                                <i class="fa-solid fa-screwdriver-wrench" />
                                <span>Arbeitsdienst eintragen*</span>
                            </li>
                            <li class="permission-delete-users context-menu-item disabled text-red-700">
                                <i class="fa-solid fa-trash-alt" />
                                <span>Nutzer löschen*</span>
                            </li>
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
import { filterUndefined, hasAnyOverlap } from '@/common';
import type { Position, PositionKey, QualificationKey, User } from '@/domain';
import { Permission } from '@/domain';
import { EventType, Role } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { ContextMenuButton, VConfirmationDialog, VTable, VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import {
    useAuthUseCase,
    useEventUseCase,
    useUserAdministrationUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application.ts';
import { useEventService, useUserService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQueryStateSync } from '@/ui/composables/QueryState.ts';
import type { Selectable } from '@/ui/model/Selectable.ts';
import { Routes } from '@/ui/views/Routes.ts';
import CreateRegistrationForUserDlg from '@/ui/views/users/components/CreateRegistrationForUserDlg.vue';
import ImportUsersDlg from '@/ui/views/users/list/ImportUsersDlg.vue';
import UsersListSkeletonLoader from '@/ui/views/users/list/UsersListSkeletonLoader.vue';

enum Tab {
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
const positions = usePositions();

const tabs = [Tab.TEAM_MEMBERS, Tab.ADMINS, Tab.UNMATCHED_USERS];
const tab = ref<string>(tabs[0]);
const filter = ref<string>('');
const filterOnlyActive = ref<boolean>(false);
const filterExpiredQualifications = ref<boolean>(false);
const filterPositions = ref<PositionKey[]>([]);

const users = ref<UserRegistrations[] | undefined>(undefined);

const importUsersDialog = ref<Dialog | null>(null);
const createRegistrationForUserDialog = ref<Dialog<User> | null>(null);
const deleteUserDialog = ref<ConfirmationDialog | null>(null);

useQueryStateSync<boolean>(
    'active',
    () => filterOnlyActive.value,
    (v) => (filterOnlyActive.value = v)
);
useQueryStateSync<boolean>(
    'expired',
    () => filterExpiredQualifications.value,
    (v) => (filterExpiredQualifications.value = v)
);
useQueryStateSync<string>(
    'positions',
    () => filterPositions.value.join('_'),
    (v) => (filterPositions.value = v.split('_'))
);

const filteredUsers = computed<UserRegistrations[] | undefined>(() =>
    users.value?.filter(
        (it) =>
            matchesActiveCategory(it) &&
            (!filterOnlyActive.value || hasAnyEvents(it)) &&
            (!filterExpiredQualifications.value || it.expiredQualifications.length > 0) &&
            (filterPositions.value.length === 0 || hasAnyOverlap(filterPositions.value, it.positionKeys)) &&
            usersService.doesUserMatchFilter(it, filter.value)
    )
);

const selectedUsers = computed<UserRegistrations[] | undefined>(() => {
    return filteredUsers.value?.filter((it) => it.selected);
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

async function editUser(user: UserRegistrations): Promise<void> {
    if (signedInUser.permissions.includes(Permission.WRITE_USERS)) {
        await router.push({ name: Routes.UserDetails, params: { key: user.key } });
    }
}

function impersonateUser(user: UserRegistrations): void {
    authUseCase.impersonateUser(user.key);
}

async function createRegistration(user: UserRegistrations): Promise<void> {
    const created = await createRegistrationForUserDialog.value?.open(user);
    if (created) {
        user.waitingListCount = user.waitingListCount + 1;
    }
}

async function deleteUser(user: UserRegistrations): Promise<void> {
    const confirmed = await deleteUserDialog.value?.open({
        title: `${user.nickName || user.firstName} ${user.lastName} löschen`,
        message: `Bist du sicher, das du ${user.nickName || user.firstName} ${user.lastName} löschen möchtest? Wenn
            ${user.nickName || user.firstName} sich schon zu Reisen angemeldet hat, wird dies dazu führen, das in den
            Crew oder Wartelisten ein ungültiger Eintrag existiert. Löschen von Nutzern sollte darum nur nach
            reichlicher Überlegung passieren.`,
        submit: 'Löschen',
        danger: true,
    });
    if (confirmed) {
        await userAdministrationUseCase.deleteUserByKey(user.key);
        await fetchUsers();
    }
}

async function contactUsers(users: User[]): Promise<void> {
    await userAdministrationUseCase.contactUsers(users);
}

function selectNone(): void {
    users.value?.forEach((it) => (it.selected = false));
}

function selectAll(): void {
    users.value?.forEach((it) => (it.selected = true));
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
