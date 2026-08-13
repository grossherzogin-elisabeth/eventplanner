<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-x-hidden xl:overflow-y-auto">
        <teleport to="#nav-right">
            <NavbarFilter v-model="filter" :placeholder="$t('generic.filter-entries')" />
        </teleport>

        <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div class="-mr-4 flex items-stretch gap-2 pb-2 2xl:mr-0">
                    <div class="hidden lg:block">
                        <VSearchButton v-model="filter" :placeholder="$t('generic.filter-entries')" />
                    </div>
                    <div v-if="hasPermission(Permission.WRITE_USERS)" class="z-10 hidden 2xl:block">
                        <button class="btn-primary" name="create" type="button" @click="createUser()">
                            <i class="fa-solid fa-user-plus"></i>
                            <span>{{ $t('domain.user.actions.create') }}</span>
                        </button>
                    </div>
                </div>
            </template>
        </VTabs>

        <div class="p-content filter-panel scrollbar-invisible mt-4">
            <FilterMultiselect
                v-model="filterPositions"
                data-test-id="filter-positions"
                :placeholder="$t('views.user-list.filter.all-positions')"
                :options="positions.options.value"
            />
            <FilterToggle v-model="filterOnlyActive" data-test-id="filter-only-active" :label="$t('views.user-list.filter.active-crew')" />
            <FilterToggle
                v-model="filterExpiredQualifications"
                data-test-id="filter-expired-qualifications"
                :label="$t('views.user-list.filter.expired-qualifications')"
            />
            <FilterSelect
                v-model="filterEventKey"
                data-test-id="filter-event"
                :options="futureEvents"
                :placeholder="$t('views.user-list.filter.all-events')"
            />
            <FilterToggle
                v-model="filterPendingVerification"
                data-test-id="filter-not-verified"
                :label="$t('views.user-list.filter.not-verified')"
            />
        </div>

        <MainContent>
            <div class="full-width-scrollable mt-4">
                <VTable
                    :items="filteredUsers"
                    :page-size="20"
                    query
                    multiselection
                    class="interactive-table no-header scrollbar-invisible"
                    @click="editUser($event.item, $event.event)"
                >
                    <template #icon="{ item }">
                        <UserAvatar :user="item" class="h-full w-full" :class="item?.verified ? 'text-success' : 'text-secondary'" />
                        <span v-if="item?.verified" data-test-id="user-verified-badge">
                            <i class="fa-solid fa-check-circle text-onsuccess-container absolute -right-1 -bottom-1 text-xs sm:text-sm"></i>
                        </span>
                    </template>
                    <template #row="{ item }">
                        <UserListRow :user="item" :events="events" />
                    </template>
                    <template #context-menu="{ item }">
                        <UserListRowActions
                            :users="[item]"
                            @contact="contactUsers($event)"
                            @impersonate="impersonateUser($event)"
                            @edit="editUser($event.user, $event.event)"
                            @delete="deleteUser($event)"
                            @create-registration="createRegistration($event)"
                        />
                    </template>
                </VTable>
            </div>
        </MainContent>

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
                    <button class="btn-ghost" type="button" @click="contactUsers(selectedUsers)">
                        <i class="fa-solid fa-envelope" />
                        <span>{{ $t('domain.user.actions.write-email') }}</span>
                    </button>
                </div>
            </template>
            <template #menu>
                <UserListRowActions
                    :users="selectedUsers"
                    @contact="contactUsers($event)"
                    @impersonate="impersonateUser($event)"
                    @edit="editUser($event.user, $event.event)"
                    @delete="deleteUser($event)"
                    @create-registration="createRegistration($event)"
                />
            </template>
        </VMultiSelectActions>
        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-else
            class="permission-write-users pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" type="button" @click="createUser()">
                <i class="fa-solid fa-user-plus"></i>
                <span>{{ $t('domain.user.actions.create') }} </span>
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
import { hasAnyOverlap, subtractFromDate } from '@/common';
import type { Event, EventKey, InputSelectOption, PositionKey, User } from '@/domain';
import { Permission, Role, useUserService } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VConfirmationDialog, VMultiSelectActions, VTable, VTabs } from '@/ui/components/common';
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
import UserListRowActions from '@/ui/views/users/list/UserListRowActions.vue';
import UserListRow from '@/ui/views/users/list/UserListRow.vue';
import UserAvatar from '@/ui/components/users/UserAvatar.vue';
import MainContent from '@/ui/components/partials/MainContent.vue';

enum Tab {
    TEAM_MEMBERS = 'members',
    ADMINS = 'admins',
    UNMATCHED_USERS = 'unknown',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const eventUseCase = useEventUseCase();
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
    label: t(`views.user-list.tab.${it}`),
}));
const tab = ref<string>(tabs[0].value);

const events = ref<Event[]>([]);
const users = ref<(User & Selectable)[] | undefined>(undefined);

const createUserDialog = ref<Dialog<void, User | undefined> | null>(null);
const createRegistrationForUserDialog = ref<Dialog<User> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);

const futureEvents = computed<InputSelectOption<EventKey>[]>(() => {
    return events.value
        .filter((evt) => evt.start > new Date())
        .slice(0, 10)
        .map((it) => ({
            label: it.name,
            value: it.key,
        }));
});

const filterEvent = computed<Event | undefined>(() => events.value.find((evt) => evt.key === filterEventKey.value));

const filteredUsers = computed<(User & Selectable)[] | undefined>(() =>
    users.value?.filter(
        (it) =>
            matchesActiveCategory(it) &&
            (!filterOnlyActive.value || isActive(it)) &&
            (!filterExpiredQualifications.value || usersService.getExpiredQualifications(it).length > 0) &&
            (!filterPendingVerification.value || !it.verified) &&
            (filterPositions.value.length === 0 || hasAnyOverlap(filterPositions.value, it.positionKeys ?? [])) &&
            (filterEvent.value === undefined || participatesInEvent(it)) &&
            usersService.doesUserMatchFilter(it, filter.value)
    )
);

function participatesInEvent(user: User): boolean {
    if (!filterEvent.value) {
        return true;
    }
    const userRegistration = filterEvent.value.registrations.find((it) => it.userKey === user.key);
    if (!userRegistration) {
        return false;
    }
    return filterEvent.value.slots.some((it) => it.assignedRegistrationKey === userRegistration.key);
}

function isActive(user: User): boolean {
    // has the user logged in at least once in the last 6 months?
    return user.lastLoginAt !== undefined && user.lastLoginAt >= subtractFromDate(new Date(), { months: 6 });
}

const selectedUsers = computed<(User & Selectable)[] | undefined>(() => {
    return filteredUsers.value?.filter((it) => it.selected);
});

async function init(): Promise<void> {
    emit('update:tab-title', 'Nutzer verwalten');
    await fetchEvents();
    await fetchUsers();
    restoreScrollPosition();
}

function matchesActiveCategory(user: User): boolean {
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

async function editUser(user: User, evt: MouseEvent): Promise<void> {
    if (!hasPermission(Permission.READ_USER_DETAILS)) {
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

function impersonateUser(user: User): void {
    authUseCase.impersonateUser(user.key);
}

async function createRegistration(user: User): Promise<void> {
    const created = await createRegistrationForUserDialog.value?.open(user);
    if (created) {
        // user.waitingListCount = user.waitingListCount + 1; // FIXME
    }
}

async function deleteUser(user: User): Promise<void> {
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
    const currentYear = new Date().getFullYear();
    events.value = (
        await Promise.all([
            eventUseCase.getEvents(currentYear - 2),
            eventUseCase.getEvents(currentYear - 1),
            eventUseCase.getEvents(currentYear),
            eventUseCase.getEvents(currentYear + 1),
        ])
    ).flatMap((array) => array);
}

async function fetchUsers(): Promise<void> {
    users.value = await usersUseCase.getUsers();
}

init();
</script>
