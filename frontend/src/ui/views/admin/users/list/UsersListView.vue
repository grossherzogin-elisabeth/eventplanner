<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <NavbarFilter v-model="filter" placeholder="Nutzer durchsuchen" />
        </teleport>
        <div class="top-0 hidden bg-primary-50 px-4 pb-8 pl-16 pr-20 pt-8 xl:block">
            <div class="flex items-center space-x-4">
                <VInputText v-model="filter" class="input-search w-96" placeholder="Nutzer filtern">
                    <template #before>
                        <i class="fa-solid fa-magnifying-glass ml-4 text-primary-900 text-opacity-25" />
                    </template>
                </VInputText>
                <VInputCheckBox v-model="filterOnlyActive" label="Nur Stammcrew mit Anmeldungen" />
                <div class="hidden flex-grow md:block"></div>
                <div class="hidden items-stretch justify-end space-x-2 md:flex">
                    <button class="btn-primary flex-grow whitespace-nowrap" @click="importUsers()">
                        <i class="fa-solid fa-upload"></i>
                        <span>Nutzer importieren</span>
                    </button>
                </div>
            </div>
        </div>
        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8" />
        <div class="w-full">
            <VTable
                :items="filteredUsers"
                :page-size="20"
                :query="true"
                class="interactive-table no-header scrollbar-invisible overflow-x-auto px-8 pt-4 md:px-16 xl:px-20"
                @click="editUser($event)"
            >
                <template #head>
                    <th>Name</th>
                    <th>Positionen</th>
                    <th>Reisen</th>
                    <th>Arbeitsdienst</th>
                    <th>Zertifikate</th>
                </template>
                <template #row="{ item }">
                    <td class="w-1/4 whitespace-nowrap font-semibold">
                        <p class="mb-2">{{ item.firstName }} {{ item.lastName }}</p>
                        <p class="text-sm">Stammcrew</p>
                    </td>
                    <td class="w-1/4">
                        <div class="flex max-w-64 flex-wrap">
                            <span
                                v-for="position in item.positions"
                                :key="position.key"
                                class="position my-0.5 mr-1 bg-gray-500 opacity-75"
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
                            v-if="item.expiredQualificationCount"
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-red-100 py-1 pl-3 pr-4 text-red-700"
                        >
                            <i class="fa-solid fa-ban"></i>
                            <span class="whitespace-nowrap font-semibold"
                                >{{ item.expiredQualificationCount }} abgelaufen</span
                            >
                        </div>
                        <div
                            v-else-if="item.soonExpiringQualificationCount"
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                        >
                            <i class="fa-solid fa-warning"></i>
                            <span class="whitespace-nowrap font-semibold">
                                <template v-if="item.soonExpiringQualificationCount === 1">1 läuft bald ab</template>
                                <template v-else>{{ item.soonExpiringQualificationCount }} laufen bald ab</template>
                            </span>
                        </div>
                        <div
                            v-else-if="item.soonExpiringQualificationCount === 0"
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-200 py-1 pl-3 pr-4 text-green-700"
                        >
                            <i class="fa-solid fa-check-circle"></i>
                            <span class="whitespace-nowrap font-semibold">Alle gültig</span>
                        </div>
                        <div
                            v-else
                            class="inline-flex w-auto items-center space-x-2 rounded-full bg-gray-200 py-1 pl-3 pr-4 text-gray-700"
                        >
                            <i class="fa-solid fa-question-circle"></i>
                            <span class="whitespace-nowrap font-semibold">Keine Angaben</span>
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
                                <li class="context-menu-item" @click="createRegistration(item)">
                                    <i class="fa-solid fa-calendar-plus" />
                                    <span>Anmeldung hinzufügen</span>
                                </li>
                                <li class="context-menu-item disabled">
                                    <i class="fa-solid fa-key" />
                                    <span>Passwort zurücksetzen</span>
                                </li>
                                <li class="context-menu-item disabled text-red-700">
                                    <i class="fa-solid fa-trash" />
                                    <span>Nutzer löschen</span>
                                </li>
                            </ul>
                        </ContextMenuButton>
                    </td>
                </template>
                <template #loading>
                    <tr v-for="i in 20" :key="i" class="animate-pulse">
                        <td></td>
                        <td class="w-1/4">
                            <p class="mb-1 h-5 w-64 rounded-lg bg-primary-200"></p>
                            <p class="flex items-center space-x-2 text-sm font-light">
                                <span class="inline-block h-3 w-16 rounded-lg bg-primary-200"></span>
                                <span class="inline-block h-3 w-16 rounded-lg bg-primary-200"></span>
                            </p>
                        </td>
                        <td class="w-1/4">
                            <div class="flex gap-2">
                                <span class="inline-block h-4 w-24 rounded-lg bg-primary-200"></span>
                                <span class="inline-block h-4 w-20 rounded-lg bg-primary-200"></span>
                            </div>
                        </td>
                        <td class="w-1/5">
                            <div class="flex gap-4">
                                <div v-for="n in 4" :key="n">
                                    <p class="mb-1 h-5 w-8 rounded-lg bg-primary-200"></p>
                                    <p class="h-3 w-12 rounded-lg bg-primary-200"></p>
                                </div>
                            </div>
                        </td>

                        <td class="w-1/12">
                            <p class="mb-1 h-5 w-16 rounded-lg bg-primary-200"></p>
                            <p class="h-3 w-20 rounded-lg bg-primary-200"></p>
                        </td>

                        <td class="w-1/12">
                            <div
                                class="inline-flex h-8 w-auto items-center space-x-2 rounded-full bg-primary-200 py-1 pl-3 pr-4 text-primary-300"
                            >
                                <i class="fa-solid fa-circle"></i>
                                <p class="h-3 w-20 rounded-lg bg-primary-300"></p>
                            </div>
                        </td>

                        <td class="w-0">
                            <div class="px-4 py-2">
                                <i class="fa-solid fa-circle text-primary-200"></i>
                            </div>
                        </td>
                        <td></td>
                    </tr>
                </template>
            </VTable>
        </div>

        <CreateRegistrationForUserDlg ref="createRegistrationForUserDialog" />
        <ImportUsersDlg ref="importUsersDialog" />
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrayUtils } from '@/common';
import type { Position, User } from '@/domain';
import { EventType } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VTabs } from '@/ui/components/common';
import { ContextMenuButton, VInputCheckBox, VInputText, VTable } from '@/ui/components/common';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useAuthUseCase, useEventUseCase, useUsersUseCase } from '@/ui/composables/Application';
import { useEventService, useUserService } from '@/ui/composables/Domain';
import type { Selectable } from '@/ui/model/Selectable';
import { Routes } from '@/ui/views/Routes';
import CreateRegistrationForUserDlg from '@/ui/views/admin/users/components/CreateRegistrationForUserDlg.vue';
import ImportUsersDlg from '@/ui/views/admin/users/list/ImportUsersDlg.vue';

interface UserRegistrations extends User, Selectable {
    positions: Position[];
    singleDayEventsCount: number;
    weekendEventsCount: number;
    multiDayEventsCount: number;
    waitingListCount: number;
}

interface RouteEmits {
    (e: 'update:title', value: string): void;
}

const emit = defineEmits<RouteEmits>();

const eventUseCase = useEventUseCase();
const eventService = useEventService();
const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const authUseCase = useAuthUseCase();
const router = useRouter();

const tab = ref<string>('Alle Nutzer');
const tabs = ref<string[]>(['Alle Nutzer']);
const filter = ref<string>('');
const filterOnlyActive = ref<boolean>(false);
const users = ref<UserRegistrations[] | undefined>(undefined);

const importUsersDialog = ref<Dialog | null>(null);
const createRegistrationForUserDialog = ref<Dialog<User> | null>(null);

const filteredUsers = computed<UserRegistrations[] | undefined>(() =>
    users.value?.filter(
        (it) =>
            usersService.doesUserMatchFilter(it, filter.value) &&
            (!filterOnlyActive.value ||
                it.waitingListCount ||
                it.multiDayEventsCount ||
                it.weekendEventsCount ||
                it.singleDayEventsCount)
    )
);

function init(): void {
    emit('update:title', 'Nutzer verwalten');
    fetchUsers();
}

function importUsers(): void {
    importUsersDialog.value?.open().catch();
}

function editUser(user: UserRegistrations): void {
    router.push({ name: Routes.UserDetails, params: { key: user.key } });
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
            waitingListCount: registrationsWaitinglist.filter((it) => it.userKey === user.key).length,
            multiDayEventsCount: registrationsMultiDayEventsWithSlot.filter((it) => it.userKey === user.key).length,
            weekendEventsCount: registrationsWeekendEventsWithSlot.filter((it) => it.userKey === user.key).length,
            singleDayEventsCount: registrationsSingleDayEventsWithSlot.filter((it) => it.userKey === user.key).length,
            positions: user.positionKeys.map((key) => positions.get(key)).filter(ArrayUtils.filterUndefined),
        };
    });
}

init();
</script>
