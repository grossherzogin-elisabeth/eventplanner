<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" placeholder="Nutzer filtern" />
            </div>
        </teleport>

        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <VSearchButton v-model="filter" placeholder="Nutzer filtern" />
                    <div class="permission-write-users hidden 2xl:block">
                        <button class="btn-primary" name="create" @click="createUser()">
                            <i class="fa-solid fa-user-plus"></i>
                            <span class="">Hinzufügen</span>
                        </button>
                    </div>
                </div>
            </template>
        </VTabs>

        <div class="scrollbar-invisible mt-4 flex items-center gap-2 overflow-x-auto px-4 md:px-16 xl:min-h-8 xl:px-20">
            <ContextMenuButton
                anchor-align-x="left"
                dropdown-position-x="right"
                class="btn-tag"
                :class="{ active: filterPositions.length > 0 }"
            >
                <template #icon>
                    <span v-if="filterPositions.length === 0" class="">Nach Positionen filtern...</span>
                    <span v-else-if="filterPositions.length > 4" class="">{{ filterPositions.length }} Positionen</span>
                    <span v-else class="block max-w-64 truncate">
                        {{
                            filterPositions
                                .map(positions.get)
                                .map((it) => it.name)
                                .join(', ')
                        }}
                    </span>
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
            <ContextMenuButton anchor-align-x="left" dropdown-position-x="right" class="btn-tag" :class="{ active: filterEventKey }">
                <template #icon>
                    <span v-if="!filterEventKey" class="">Nach Reisen filtern...</span>
                    <span v-else class="block max-w-64 truncate"> Nimmt teil an {{ filterEvent?.name }} </span>
                </template>
                <template #default>
                    <ul>
                        <li v-if="!filterEventKey" class="context-menu-item">
                            <i class="fa-solid fa-check"></i>
                            <span>Alle Reisen</span>
                        </li>
                        <li v-else class="context-menu-item" @click="filterEventKey = ''">
                            <i class="w-4"></i>
                            <span>Alle Reisen</span>
                        </li>
                        <template v-for="event in events" :key="event.key">
                            <li v-if="filterEventKey === event.key" class="context-menu-item" @click="filterEventKey = ''">
                                <i class="fa-solid fa-check w-4"></i>
                                <span class="truncate">{{ event.name }}</span>
                            </li>
                            <li v-else class="context-menu-item" @click="filterEventKey = event.key">
                                <i class="w-4"></i>
                                <span class="truncate">{{ event.name }}</span>
                            </li>
                        </template>
                    </ul>
                </template>
            </ContextMenuButton>
            <button
                class="btn-tag"
                :class="{ active: filterPendingVerification }"
                @click="filterPendingVerification = !filterPendingVerification"
            >
                <span class="">Verifizierung ausstehend</span>
            </button>
        </div>

        <div class="w-full">
            <VTable
                :items="filteredUsers"
                :page-size="20"
                query
                multiselection
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="editUser($event.item, $event.event)"
            >
                <template #row="{ item }">
                    <td class="w-1/3 whitespace-nowrap font-semibold">
                        <p class="mb-2">
                            {{ item.nickName || item.firstName }} {{ item.lastName }}
                            <span
                                v-if="item.verified"
                                class="inline-flex h-5 w-5 items-center justify-center rounded-full bg-green-container"
                            >
                                <i class="fa-solid fa-check text-xs text-ongreen-container"></i>
                            </span>
                        </p>
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
                                class="position my-0.5 mr-1 bg-surface-container-highest text-xs opacity-75"
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
                            <div v-if="item.qualifications?.length === 0" class="status-panel bg-surface-container-highest text-onsurface">
                                <i class="fa-solid fa-question-circle"></i>
                                <span class="whitespace-nowrap font-semibold">Keine Angaben</span>
                            </div>
                            <div
                                v-else-if="item.expiredQualifications.length > 0"
                                class="status-panel bg-red-container text-onred-container"
                                :title="item.expiredQualifications.join(', ')"
                            >
                                <i class="fa-solid fa-ban"></i>
                                <span class="whitespace-nowrap font-semibold"> {{ item.expiredQualifications.length }} abgelaufen </span>
                            </div>
                            <div
                                v-else-if="item.soonExpiringQualifications.length > 0"
                                class="status-panel bg-yellow-container text-onyellow-container"
                                :title="item.soonExpiringQualifications.join(', ')"
                            >
                                <i class="fa-solid fa-warning"></i>
                                <span class="whitespace-nowrap font-semibold">
                                    <template v-if="item.soonExpiringQualifications.length === 1"> 1 läuft bald ab </template>
                                    <template v-else> {{ item.soonExpiringQualifications.length }} laufen bald ab </template>
                                </span>
                            </div>
                            <div v-else class="status-panel bg-green-container text-ongreen-container">
                                <i class="fa-solid fa-check-circle"></i>
                                <span class="whitespace-nowrap font-semibold">Alle gültig</span>
                            </div>
                        </div>
                    </td>
                </template>
                <template #context-menu="{ item }">
                    <li
                        class="permission-read-user-details context-menu-item"
                        :class="{ disabled: !item.email }"
                        @click="contactUsers([item])"
                    >
                        <i class="fa-solid fa-envelope" />
                        <span>Email schreiben</span>
                    </li>
                    <li class="permission-write-registrations context-menu-item" @click="impersonateUser(item)">
                        <i class="fa-solid fa-user-secret" />
                        <span>Impersonate</span>
                    </li>
                    <li class="permission-write-registrations context-menu-item" @click="createRegistration(item)">
                        <i class="fa-solid fa-calendar-plus" />
                        <span>Anmeldung hinzufügen</span>
                    </li>
                    <li class="permission-write-users context-menu-item" @click="editUser(item, $event)">
                        <i class="fa-solid fa-edit" />
                        <span>Nutzer bearbeiten</span>
                    </li>
                    <li class="permission-delete-users context-menu-item text-error" @click="deleteUser(item)">
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
        <VConfirmationDialog ref="confirmationDialog" />
        <CreateUserDlg ref="createUserDialog" />

        <div class="flex-1"></div>

        <div v-if="selectedUsers && selectedUsers.length > 0" class="sticky bottom-0 z-20">
            <div class="h-full border-t border-outline-variant bg-surface px-4 py-2 md:px-12 xl:rounded-bl-3xl xl:py-4 xl:pl-16 xl:pr-20">
                <div class="flex h-full items-stretch gap-2 whitespace-nowrap">
                    <button class="btn-ghost" @click="selectNone()">
                        <i class="fa-solid fa-xmark w-6" />
                    </button>
                    <span class="self-center font-bold">{{ selectedUsers.length }} ausgewählt</span>
                    <div class="flex-grow"></div>

                    <div class="permission-read-user-details hidden sm:block">
                        <button class="btn-ghost" @click="contactUsers(selectedUsers)">
                            <i class="fa-solid fa-envelope" />
                            <span class="">Email schreiben</span>
                        </button>
                    </div>
                    <div class="permission-write-users hidden lg:block xl:hidden 2xl:block">
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
                            <li class="permission-read-user-details context-menu-item" @click="contactUsers(selectedUsers)">
                                <i class="fa-solid fa-envelope" />
                                <span>Email schreiben</span>
                            </li>
                            <li class="permission-write-users context-menu-item disabled">
                                <i class="fa-solid fa-screwdriver-wrench" />
                                <span>Arbeitsdienst eintragen*</span>
                            </li>
                            <li class="permission-delete-users context-menu-item disabled text-error">
                                <i class="fa-solid fa-trash-alt" />
                                <span>Nutzer löschen*</span>
                            </li>
                        </template>
                    </ContextMenuButton>
                </div>
            </div>
        </div>
        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-else
            class="permission-write-users pointer-events-none sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" @click="createUser()">
                <i class="fa-solid fa-user-plus"></i>
                <span>Nutzer hinzufügen</span>
            </button>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { filterUndefined, hasAnyOverlap } from '@/common';
import type { Event, EventKey, Position, PositionKey, QualificationKey, User } from '@/domain';
import { Permission } from '@/domain';
import { EventType, Role } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { ContextMenuButton, VConfirmationDialog, VTable, VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventUseCase, useUserAdministrationUseCase, useUsersUseCase } from '@/ui/composables/Application.ts';
import { useEventService, useUserService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQueryStateSync } from '@/ui/composables/QueryState.ts';
import type { Selectable } from '@/ui/model/Selectable.ts';
import { restoreScrollPosition } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes.ts';
import CreateRegistrationForUserDlg from '@/ui/views/users/components/CreateRegistrationForUserDlg.vue';
import CreateUserDlg from '@/ui/views/users/list/CreateUserDlg.vue';
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

type RouteEmits = (e: 'update:tab-title', value: string) => void;

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
const filterOnlyActive = ref<boolean>(true);
const filterExpiredQualifications = ref<boolean>(false);
const filterPendingVerification = ref<boolean>(false);
const filterPositions = ref<PositionKey[]>([]);
const filterEventKey = ref<EventKey>('');

const events = ref<Event[]>([]);
const users = ref<UserRegistrations[] | undefined>(undefined);

const createUserDialog = ref<Dialog<void, User | undefined> | null>(null);
const createRegistrationForUserDialog = ref<Dialog<User> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);

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
useQueryStateSync<boolean>(
    'unverified',
    () => filterPendingVerification.value,
    (v) => (filterPendingVerification.value = v)
);
useQueryStateSync<string>(
    'positions',
    () => filterPositions.value.join('_'),
    (v) => (filterPositions.value = v.split('_'))
);
useQueryStateSync<string>(
    'filter',
    () => filter.value,
    (v) => (filter.value = v)
);
useQueryStateSync<string>(
    'event',
    () => filterEventKey.value,
    (v) => (filterEventKey.value = v)
);

const filterEvent = computed<Event | undefined>(() => events.value.find((evt) => evt.key === filterEventKey.value));

const filteredUsers = computed<UserRegistrations[] | undefined>(() =>
    users.value?.filter(
        (it) =>
            matchesActiveCategory(it) &&
            (!filterOnlyActive.value || hasAnyEvents(it)) &&
            (!filterExpiredQualifications.value || it.expiredQualifications.length > 0) &&
            (!filterPendingVerification.value || !it.verified) &&
            (filterPositions.value.length === 0 || hasAnyOverlap(filterPositions.value, it.positionKeys ?? [])) &&
            (filterEvent.value === undefined || participatesInEvent(it)) &&
            usersService.doesUserMatchFilter(it, filter.value)
    )
);

function participatesInEvent(user: UserRegistrations): boolean {
    if (!filterEvent.value) {
        return true;
    }
    const userRegistration = filterEvent.value.registrations.find((it) => it.userKey === user.key);
    if (!userRegistration) {
        return false;
    }
    return filterEvent.value.slots.find((it) => it.assignedRegistrationKey === userRegistration.key) !== undefined;
}

const selectedUsers = computed<UserRegistrations[] | undefined>(() => {
    return filteredUsers.value?.filter((it) => it.selected);
});

async function init(): Promise<void> {
    emit('update:tab-title', 'Nutzer verwalten');
    await fetchUsers();
    await fetchEvents();
    restoreScrollPosition();
}

function hasAnyEvents(user: UserRegistrations): boolean {
    return user.waitingListCount > 0 || user.multiDayEventsCount > 0 || user.weekendEventsCount > 0 || user.singleDayEventsCount > 0;
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

function createUser(): void {
    createUserDialog.value?.open().catch();
}

async function editUser(user: UserRegistrations, evt: MouseEvent): Promise<void> {
    if (!signedInUser.permissions.includes(Permission.WRITE_USERS)) {
        console.error('User has no permission to edit users.');
        return;
    }
    const to: RouteLocationRaw = { name: Routes.UserDetails, params: { key: user.key } };
    if (evt.ctrlKey || evt.metaKey) {
        window.open(router.resolve(to).href, '_blank');
    } else {
        await router.push(to);
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
    const confirmed = await confirmationDialog.value?.open({
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

async function fetchEvents(): Promise<void> {
    const allEvents = await eventUseCase.getFutureEvents();
    events.value = allEvents.slice(0, 10);
}

async function fetchUsers(): Promise<void> {
    const positions = await usersUseCase.resolvePositionNames();
    const userlist: User[] = await usersUseCase.getUsers();
    const currentYear = new Date().getFullYear();
    const events = (
        await Promise.all([
            eventUseCase.getEvents(currentYear - 2),
            eventUseCase.getEvents(currentYear - 1),
            eventUseCase.getEvents(currentYear),
            eventUseCase.getEvents(currentYear + 1),
        ])
    ).flatMap((evts) => evts);
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
            positions: user.positionKeys?.map((key) => positions.get(key)).filter(filterUndefined) ?? [],
            expiredQualifications: usersService.getExpiredQualifications(user),
            soonExpiringQualifications: usersService.getSoonExpiringQualifications(user),
        };
    });
}

init();
</script>
