<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto">
        <teleport to="#nav-right">
            <NavbarFilter v-model="filter" placeholder="Nutzer durchsuchen" />
        </teleport>
        <div
            class="top-12 z-10 hidden bg-primary-50 px-4 pb-8 pt-4 md:pl-12 md:pr-16 md:pt-8 xl:top-0 xl:block xl:pl-16 xl:pr-20"
        >
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
        <div class="w-full overflow-x-auto px-8 pt-6 md:px-16 xl:px-20 xl:pt-0">
            <div class="-mx-4 px-4">
                <VTable
                    :items="filteredCrewMembers"
                    :page-size="50"
                    class="interactive-table"
                    @click="editUser($event)"
                >
                    <template #head>
                        <th data-sortby="firstName">Name</th>
                        <th class="hidden md:table-cell" data-sortby="lastName">Nachname</th>
                        <th>Zertifikate</th>
                        <th data-sortby="role">Rolle</th>
                        <th data-sortby="eventCount">Reisen</th>
                        <th data-sortby="registrationCount">Warteliste</th>
                    </template>
                    <template #row="{ item }">
                        <td class="whitespace-nowrap font-semibold">
                            <span>{{ item.firstName }}</span>
                            <span class="ml-2 md:hidden">{{ item.lastName }}</span>
                        </td>
                        <td class="hidden font-semibold md:table-cell">{{ item.lastName }}</td>
                        <td>
                            <div
                                class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-100 py-1 pl-3 pr-4 text-green-700"
                            >
                                <i class="fa-solid fa-check-circle"></i>
                                <span class="whitespace-nowrap font-semibold">Alle gültig</span>
                            </div>
                        </td>
                        <td>
                            <div class="flex">
                                <span
                                    v-for="position in item.positionKeys"
                                    :key="position"
                                    class="position mr-2 bg-gray-500"
                                >
                                    {{ position }}
                                </span>
                            </div>
                        </td>
                        <td>
                            <span class="rounded-full bg-primary-200 px-4 py-1 text-xs font-bold">
                                {{ item.eventCount || '-' }}
                            </span>
                        </td>
                        <td>
                            <span class="rounded-full bg-yellow-200 px-4 py-1 text-xs font-bold">
                                {{ item.registrationCount || '-' }}
                            </span>
                        </td>
                    </template>
                </VTable>
            </div>
        </div>

        <ImportUsersDlg ref="importUsersDialog" />
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { User } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputCheckBox, VInputText, VTable } from '@/ui/components/common';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useEventUseCase, useUsersUseCase } from '@/ui/composables/Application';
import { useUserService } from '@/ui/composables/Domain';
import type { Selectable } from '@/ui/model/Selectable';
import { Routes } from '@/ui/views/Routes';
import ImportUsersDlg from '@/ui/views/admin/users/list/ImportUsersDlg.vue';

interface UserRegistrations extends User, Selectable {
    eventCount?: number;
    registrationCount?: number;
}

const eventUseCase = useEventUseCase();
const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const router = useRouter();

const importUsersDialog = ref<Dialog | null>(null);
const filter = ref<string>('');
const filterOnlyActive = ref<boolean>(false);
const crewMembers = ref<UserRegistrations[]>([]);

const filteredCrewMembers = computed<UserRegistrations[]>(() =>
    crewMembers.value.filter(
        (it) =>
            usersService.doesUserMatchFilter(it, filter.value) &&
            (!filterOnlyActive.value || it.registrationCount || it.eventCount)
    )
);

function init(): void {
    fetchCrewMembers();
}

function importUsers(): void {
    importUsersDialog.value?.open().catch();
}

function editUser(user: User): void {
    router.push({ name: Routes.UserDetails, params: { key: user.key } });
}

async function fetchCrewMembers(): Promise<void> {
    const users: UserRegistrations[] = await usersUseCase.getUsers();
    const events = await eventUseCase.getEvents(new Date().getFullYear());
    const registrations = events.flatMap((it) => it.registrations);
    users.forEach((user: UserRegistrations) => {
        const userRegistrations = registrations.filter((it) => it.userKey === user.key);
        user.eventCount = userRegistrations.filter((it) => it.slotKey).length;
        user.registrationCount = userRegistrations.length - user.eventCount;
    });
    crewMembers.value = users;
}

init();
</script>
