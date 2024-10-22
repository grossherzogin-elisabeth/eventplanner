<template>
    <VTable
        :items="users"
        :page-size="-1"
        :query="true"
        class="interactive-table no-header scrollbar-invisible"
        @click="onClick($event)"
    >
        <template #head>
            <th>Name</th>
            <th>Positionen</th>
            <th>Reisen</th>
            <th>Arbeitsdienst</th>
            <th>Zertifikate</th>
        </template>
        <template #row="{ item }">
            <td class="text-xl">
                <i class="fa-solid fa-user-clock text-gray-400" />
            </td>
            <td class="w-1/3 whitespace-nowrap font-semibold">
                <p class="">
                    <span>{{ item.name }}</span>
                    <span v-if="item.isGuestCrew"> (Gastcrew)</span>
                </p>
            </td>
            <td class="w-1/3">
                <div class="flex max-w-64 flex-wrap">
                    <span
                        class="position my-0.5 mr-1 bg-gray-500 opacity-75"
                        :style="{ background: item.position.color }"
                    >
                        {{ item.position.name }}
                    </span>
                </div>
            </td>
            <td class="w-1/3 whitespace-nowrap font-semibold">
                <p class="line-clamp-2 text-sm">{{ item.note }}</p>
            </td>
            <td class="w-1/12">
                <div
                    v-if="item.hasFitnessForSeaServiceQualification"
                    class="inline-flex w-auto items-center space-x-2 rounded-full bg-blue-200 py-1 pl-3 pr-4 text-blue-700"
                >
                    <i class="fa-solid fa-anchor-circle-check"></i>
                    <span class="whitespace-nowrap font-semibold">Seediensttauglichkeit</span>
                </div>
            </td>
            <td class="w-1/12">
                <div
                    v-if="item.isGuestCrew"
                    class="inline-flex w-auto items-center space-x-2 rounded-full bg-gray-200 py-1 pl-3 pr-4 text-gray-700"
                >
                    <i class="fa-solid fa-question-circle"></i>
                    <span class="whitespace-nowrap font-semibold">keine Angabe</span>
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
                        <li v-if="!item.isGuestCrew" class="context-menu-item">
                            <i class="fa-solid fa-square-arrow-up-right" />
                            <span>Details anzeigen</span>
                        </li>
                        <li class="context-menu-item">
                            <i class="fa-solid fa-user-plus" />
                            <span>Zur Crew hinzufügen</span>
                        </li>
                        <li class="context-menu-item">
                            <i class="fa-solid fa-user-minus" />
                            <span>Auf Warteliste setzen</span>
                        </li>
                        <li class="context-menu-item text-red-600">
                            <i class="fa-solid fa-trash-alt" />
                            <span>Anmeldung löschen</span>
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
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import { ArrayUtils } from '@/common';
import type { Event, Position, QualificationKey, User, UserKey } from '@/domain';
import { ContextMenuButton, VTable } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { useUserService } from '@/ui/composables/Domain';

interface UserRegistration {
    name: string;
    user?: User;
    isGuestCrew: boolean;
    note: string;
    position: Position;
    expiredQualifications: QualificationKey[];
    hasFitnessForSeaServiceQualification: boolean;
}

interface Props {
    event: Event;
    show: 'team' | 'waitinglist';
}

interface Emits {
    (e: 'onclick', userKey: UserKey): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const usersUseCase = useUsersUseCase();
const usersService = useUserService();

const users = ref<UserRegistration[] | undefined>(undefined);

async function init(): Promise<void> {
    await fetchUsers();
    watch(
        () => props.event,
        () => fetchUsers()
    );
}

function onClick(registration: UserRegistration) {
    if (registration.user?.key) {
        emit('onclick', registration.user.key);
    }
}

async function fetchUsers(): Promise<void> {
    if (!props.event) {
        return;
    }
    const assignedRegistrations = props.event.slots
        .map((s) => s.assignedRegistrationKey)
        .filter(ArrayUtils.filterUndefined);
    const userKeys = props.event.registrations.map((r) => r.userKey).filter(ArrayUtils.filterUndefined);
    const positions = await usersUseCase.resolvePositionNames();
    const usersInEvent: User[] = await usersUseCase.getUsers(userKeys);

    users.value = props.event.registrations
        .filter((registration) => {
            if (props.show === 'team') {
                return assignedRegistrations.includes(registration.key);
            }
            return !assignedRegistrations.includes(registration.key);
        })
        .map((registration) => {
            const user = usersInEvent.find((usr) => usr.key === registration.userKey);
            return {
                name: user ? (user.nickName || user.firstName) + ' ' + user.lastName : registration.name || '',
                isGuestCrew: user === undefined,
                user: user,
                position: positions.get(registration.positionKey) || {
                    prio: 0,
                    name: 'Unknown position',
                    key: 'err',
                    color: '#fafafa',
                },
                expiredQualifications: usersService.getExpiredQualifications(user, props.event.end),
                hasFitnessForSeaServiceQualification: user?.qualifications?.find(
                    (q) =>
                        q.qualificationKey === 'fitness-for-seaservice' &&
                        q.expiresAt &&
                        q.expiresAt.getTime() > props.event.end.getTime()
                ),
                note: registration.note,
            };
        })
        .sort((a, b) => b.position.prio - a.position.prio);
}

init();
</script>
