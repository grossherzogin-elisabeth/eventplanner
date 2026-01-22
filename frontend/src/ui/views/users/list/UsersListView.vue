<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-x-hidden xl:overflow-y-auto">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-model="filter" placeholder="Einträge filtern" />
            </div>
        </teleport>

        <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <div class="permission-write-users hidden 2xl:block">
                        <button class="btn-primary" name="create" @click="createUser()">
                            <i class="fa-solid fa-user-plus"></i>
                            <span>Hinzufügen</span>
                        </button>
                    </div>
                    <div class="hidden lg:block">
                        <VSearchButton v-model="filter" placeholder="Einträge filtern" />
                    </div>
                </div>
            </template>
        </VTabs>

        <div class="scrollbar-invisible mt-4 flex items-center gap-2 overflow-x-auto px-4 md:px-16 xl:min-h-8 xl:px-20">
            <FilterMultiselect v-model="filterPositions" placeholder="Alle Positionen" :options="positions.options.value" />
            <FilterToggle v-model="filterOnlyActive" label="Aktive Stammcrew" />
            <FilterToggle v-model="filterExpiredQualifications" label="Abgelaufene Qualifikationen" />
            <FilterSelect
                v-model="filterEventKey"
                :options="
                    events.map((it) => ({
                        label: it.name,
                        value: it.key,
                    }))
                "
                placeholder="Alle Veranstaltungen"
            />
            <FilterToggle v-model="filterPendingVerification" label="Verifizierung ausstehend" />
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
                    <td class="w-1/3 font-semibold whitespace-nowrap">
                        <p class="mb-2">
                            {{ item.nickName || item.firstName }} {{ item.lastName }}
                            <span
                                v-if="item.verified"
                                class="bg-success-container/50 inline-flex h-5 w-5 items-center justify-center rounded-full"
                            >
                                <i class="fa-solid fa-check text-onsuccess-container text-xs"></i>
                            </span>
                        </p>
                        <p v-if="item.rolesStr" class="max-w-64 truncate text-sm" :title="item.rolesStr">
                            {{ item.rolesStr }}
                        </p>
                        <p v-else class="max-w-64 truncate text-sm italic">Keine Rolle zugewiesen</p>
                    </td>
                    <td class="w-1/3">
                        <div class="flex max-w-64 flex-wrap gap-1">
                            <span
                                v-for="position in item.positions"
                                :key="position.key"
                                class="tag custom"
                                :style="{ '--color': position.color }"
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
                        <div class="flex items-center justify-end">
                            <div v-if="item.qualifications?.length === 0" class="status-badge neutral">
                                <i class="fa-solid fa-question-circle"></i>
                                <span>Keine Angaben</span>
                            </div>
                            <div
                                v-else-if="item.expiredQualifications.length > 0"
                                class="status-badge error"
                                :title="item.expiredQualifications.join(', ')"
                            >
                                <i class="fa-solid fa-ban"></i>
                                <span> {{ item.expiredQualifications.length }} abgelaufen </span>
                            </div>
                            <div
                                v-else-if="item.soonExpiringQualifications.length > 0"
                                class="status-badge warning"
                                :title="item.soonExpiringQualifications.join(', ')"
                            >
                                <i class="fa-solid fa-warning"></i>
                                <span>
                                    <template v-if="item.soonExpiringQualifications.length === 1"> 1 läuft bald ab </template>
                                    <template v-else> {{ item.soonExpiringQualifications.length }} laufen bald ab </template>
                                </span>
                            </div>
                            <div v-else class="status-badge success">
                                <i class="fa-solid fa-check-circle"></i>
                                <span>Alle gültig</span>
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

        <VMultiSelectActions
            v-if="selectedUsers && selectedUsers.length > 0"
            :count="selectedUsers.length"
            @select-all="selectAll()"
            @select-none="selectNone()"
        >
            <template #action>
                <div class="permission-read-user-details hidden sm:block">
                    <button class="btn-ghost" @click="contactUsers(selectedUsers)">
                        <i class="fa-solid fa-envelope" />
                        <span>Email schreiben</span>
                    </button>
                </div>
            </template>
            <template #menu>
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
        </VMultiSelectActions>
        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-else
            class="permission-write-users pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12 2xl:hidden"
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
import { useAuthUseCase, useEventUseCase, useUserAdministrationUseCase, useUsersUseCase } from '@/application';
import { filterUndefined, hasAnyOverlap } from '@/common';
import type { Event, EventKey, Position, PositionKey, QualificationKey, User } from '@/domain';
import { EventType, Permission, Role, useEventService, useUserService } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VMultiSelectActions } from '@/ui/components/common';
import { VConfirmationDialog, VTable, VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import { FilterMultiselect, FilterSelect, FilterToggle } from '@/ui/components/filters';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQuery } from '@/ui/composables/QueryState.ts';
import { useSession } from '@/ui/composables/Session.ts';
import type { Selectable } from '@/ui/model/Selectable.ts';
import { restoreScrollPosition } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes.ts';
import CreateRegistrationForUserDlg from '@/ui/views/users/components/CreateRegistrationForUserDlg.vue';
import CreateUserDlg from '@/ui/views/users/list/CreateUserDlg.vue';
import UsersListSkeletonLoader from '@/ui/views/users/list/UsersListSkeletonLoader.vue';

enum Tab {
    TEAM_MEMBERS = 'members',
    ADMINS = 'admins',
    UNMATCHED_USERS = 'unknown',
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

const { t } = useI18n();
const eventUseCase = useEventUseCase();
const eventService = useEventService();
const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const authUseCase = useAuthUseCase();
const userAdministrationUseCase = useUserAdministrationUseCase();
const router = useRouter();
const positions = usePositions();
const { hasPermission } = useSession();

const filter = useQuery<string>('filter', '').parameter;
const filterOnlyActive = useQuery<boolean>('active', false).parameter;
const filterExpiredQualifications = useQuery<boolean>('expired', false).parameter;
const filterPendingVerification = useQuery<boolean>('unverified', false).parameter;
const filterEventKey = useQuery<EventKey>('event', '').parameter;
const filterPositions = useQuery<PositionKey[]>('positions', []).parameter;

const tabs = [Tab.TEAM_MEMBERS, Tab.ADMINS, Tab.UNMATCHED_USERS].map((it) => ({
    value: it,
    label: t(`views.users.list.tab.${it}`),
}));
const tab = ref<string>(tabs[0].value);

const events = ref<Event[]>([]);
const users = ref<UserRegistrations[] | undefined>(undefined);

const createUserDialog = ref<Dialog<void, User | undefined> | null>(null);
const createRegistrationForUserDialog = ref<Dialog<User> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);

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
    return filterEvent.value.slots.some((it) => it.assignedRegistrationKey === userRegistration.key);
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
    if (!hasPermission(Permission.WRITE_USERS)) {
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
        message: `Bist du sicher, dass du ${user.nickName || user.firstName} ${user.lastName} löschen möchtest? Wenn
            ${user.nickName || user.firstName} sich schon zu Veranstaltungen angemeldet hat, wird dies dazu führen, das in den
            Crew und Wartelisten ein ungültiger Eintrag existiert. Löschen von Nutzern sollte darum nur nach reichlicher
            Überlegung passieren.`,
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
            rolesStr: user.roles?.map((k) => t(`generic.role.${k}`)).join(', ') || '',
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
