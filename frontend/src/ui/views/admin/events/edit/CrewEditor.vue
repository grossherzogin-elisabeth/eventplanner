<template>
    <div v-if="!loading" class="-mx-4 flex h-full flex-col px-4 xl:flex-row xl:items-start">
        <!-- position counters -->
        <div class="top-14 -mx-8 mb-4 overflow-x-auto bg-primary-50 pb-4 xl:sticky xl:mr-8 xl:w-64 xl:pt-10">
            <div
                class="scrollbar-invisible flex items-start gap-2 px-8 text-sm font-bold text-white md:flex-wrap xl:flex-col"
            >
                <div class="flex cursor-pointer items-center rounded-2xl bg-gray-500 p-1">
                    <span class="px-2"> Alle </span>
                    <span
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-white bg-opacity-25 px-1 pt-0.5 text-center text-xs"
                    >
                        {{ props.event?.assignedUserCount }}
                    </span>
                </div>
                <div
                    v-for="pos in positions"
                    :key="pos.key"
                    :style="{ 'background-color': pos.color }"
                    class="flex cursor-pointer items-center rounded-2xl p-1"
                >
                    <span class="px-2">
                        {{ pos.name }}
                    </span>
                    <span
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-white bg-opacity-25 px-1 pt-0.5 text-center text-xs"
                    >
                        {{ summary[pos.key] || '-' }}
                    </span>
                </div>
            </div>
        </div>
        <div class="-mx-4 flex-1 gap-16 lg:flex">
            <div class="mb-8 w-full max-w-[35rem] px-4 pb-4 lg:mb-0">
                <h2 class="mb-4">Crew</h2>
                <!-- slot list dropzone -->
                <div class="sticky top-24">
                    <div class="absolute z-10 w-full" :class="{ hidden: dragSource !== DragSource.FROM_WAITING_LIST }">
                        <VDropzone class="h-96" @drop="addToTeam($event as ResolvedRegistration)">
                            <div class="dropzone-add">
                                <i class="fa-regular fa-calendar-plus text-3xl opacity-75"></i>
                                <span>Zur Crew hinzufügen</span>
                            </div>
                        </VDropzone>
                    </div>
                </div>
                <div
                    class="-mx-4"
                    :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_WAITING_LIST }"
                >
                    <!-- empty slot list placeholder -->
                    <div v-if="team.length === 0" class="rounded-xl bg-primary-100">
                        <div class="flex items-center py-8 pl-4 pr-8">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-list-check opacity-75"></i>
                                    Hier ist noch nichts...
                                </h3>
                                <p class="text-sm">
                                    Für diese Reise wurden noch keine Slots definiert. Im "Slots" Tab kannst du Slots
                                    für diese Reise definieren. Anschließend kannst du Nutzer von der Warteliste in die
                                    Crew ziehen.
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8 opacity-20">
                            <li
                                v-for="i in 5"
                                :key="i"
                                class="mr-4 flex items-center rounded-xl px-4 py-2 md:space-x-4"
                            >
                                <i class="fa-solid fa-grip-vertical hidden text-sm text-gray-400 lg:inline"></i>
                                <i class="fa-regular fa-circle text-gray-400"></i>
                                <span class="mx-2 inline-block h-4 w-64 rounded-full bg-gray-400"> </span>
                                <span class="flex-grow"></span>
                                <span class="position bg-gray-400">
                                    <span class="mx-2 inline-block h-2 w-16 rounded-full bg-gray-100"> </span>
                                </span>
                            </li>
                        </ul>
                    </div>
                    <!-- slot list -->
                    <ul>
                        <template v-for="it in team" :key="it.key + it.userKey + it.userName + it.positionName">
                            <VDraggable
                                class="flex items-center rounded-xl px-4 py-2 hover:bg-primary-100 md:space-x-4"
                                :class="{ 'cursor-move': it.userName !== undefined }"
                                component="li"
                                :value="it.userKey || it.userName ? it : undefined"
                                @dragend="dragSource = null"
                                @dragstart="dragSource = DragSource.FROM_TEAM"
                                @click="editSlotRegistration(it)"
                            >
                                <i class="fa-solid fa-grip-vertical hidden text-sm opacity-25 lg:inline"></i>
                                <span v-if="it.userName && !it.confirmed" class="">
                                    <i class="fa-solid fa-user-clock text-gray-400"></i>
                                </span>
                                <span v-else-if="it.userName && it.confirmed">
                                    <i class="fa-solid fa-user-check text-green-600"></i>
                                </span>
                                <span v-else>
                                    <i class="fa-solid fa-user-xmark text-red-500"></i>
                                </span>
                                <span v-if="it.userName" class="mx-2 w-0 flex-grow truncate">
                                    {{ it.userName }}
                                    <template v-if="it.userName && !it.userKey"> (Gastcrew) </template>
                                </span>
                                <span v-else-if="it.userKey" class="mx-2 w-0 flex-grow truncate italic text-red-500">
                                    err: {{ it.userKey }}
                                </span>
                                <span v-else class="mx-2 w-0 flex-grow truncate italic text-red-500">
                                    Noch nicht besetzt
                                </span>
                                <span :style="{ background: it.position.color }" class="position">
                                    {{ it.positionName }}
                                </span>
                            </VDraggable>
                        </template>
                    </ul>
                </div>
            </div>

            <div class="w-full max-w-[35rem] px-4">
                <h2 class="mb-4">Warteliste</h2>
                <!-- waitinglist dropzone -->
                <div class="sticky top-24">
                    <div class="absolute w-full space-y-8" :class="{ hidden: dragSource !== DragSource.FROM_TEAM }">
                        <VDropzone class="h-44" @drop="removeFromTeam($event as ResolvedSlot)">
                            <div class="dropzone-remove">
                                <i class="fa-regular fa-calendar-minus text-3xl opacity-75"></i>
                                <span>Auf Warteliste verschieben</span>
                            </div>
                        </VDropzone>
                        <VDropzone class="h-44" @drop="cancelRegistration($event as ResolvedSlot)">
                            <div class="dropzone-delete">
                                <i class="fa-regular fa-calendar-xmark text-3xl opacity-75"></i>
                                <span>Anmeldung löschen</span>
                            </div>
                        </VDropzone>
                    </div>
                </div>
                <div class="-mx-4" :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_TEAM }">
                    <!-- empty waitinglist placeholder -->
                    <div v-if="registrations.length === 0" class="rounded-xl bg-gray-100">
                        <div class="flex items-center py-8 pl-4 pr-8">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-list-check opacity-75"></i>
                                    Die Warteliste ist leer...
                                </h3>
                                <p class="text-sm">
                                    Für diese Reise gibt es gerade keine Anmeldungen. Sobald Nutzer sich für diese Reise
                                    anmelden, werden sie hier aufgelistet und können für die Crew eingeplant werden.
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8 opacity-20">
                            <li
                                v-for="i in 5"
                                :key="i"
                                class="mr-4 flex items-center rounded-xl px-4 py-2 md:space-x-4"
                            >
                                <i class="fa-solid fa-grip-vertical hidden text-sm text-gray-400 lg:inline"></i>
                                <span class="mx-2 inline-block h-4 w-64 rounded-full bg-gray-400"> </span>
                                <span class="flex-grow"></span>
                                <span class="position bg-gray-400">
                                    <span class="mx-2 inline-block h-2 w-16 rounded-full bg-gray-100"> </span>
                                </span>
                            </li>
                        </ul>
                    </div>
                    <!-- waitinglist -->
                    <ul>
                        <template v-for="it in registrations" :key="it.positionKey + it.userKey + it.name">
                            <VDraggable
                                class="flex cursor-move items-center rounded-xl px-4 py-2 hover:bg-primary-100 md:space-x-4"
                                component="li"
                                :value="it"
                                @dragend="dragSource = null"
                                @dragstart="dragSource = DragSource.FROM_WAITING_LIST"
                                @click="editWaitinglistRegistration(it)"
                            >
                                <i class="fa-solid fa-grip-vertical mr-2 hidden text-sm opacity-25 lg:inline"></i>
                                <span v-if="it.name" class="w-0 flex-grow truncate">{{ it.name }}</span>
                                <span v-else-if="it.userKey" class="w-0 flex-grow italic text-red-500">
                                    err: {{ it.userKey }}
                                </span>
                                <div>
                                    <ContextMenuButton>
                                        <template #icon>
                                            <span
                                                :style="{ background: it.position.color }"
                                                class="position cursor-pointer"
                                            >
                                                {{ it.position.name }}
                                                <i class="fa-solid fa-chevron-down"> </i>
                                            </span>
                                        </template>
                                        <template #default>
                                            <ul>
                                                <template v-for="pos in position.values()" :key="pos.key">
                                                    <li
                                                        v-if="it.user?.positionKeys.includes(pos.key)"
                                                        class="-mx-4 flex cursor-pointer items-center justify-between px-4 py-1 hover:bg-primary-200"
                                                        @click="changePosition(it, pos.key)"
                                                    >
                                                        <span>{{ pos.name }}</span>
                                                    </li>
                                                </template>
                                                <!-- show also non matching positions ??? -->
                                                <hr />
                                                <template v-for="pos in position.values()" :key="pos.key">
                                                    <li
                                                        v-if="!it.user?.positionKeys.includes(pos.key)"
                                                        class="-mx-4 flex cursor-pointer items-center justify-between px-4 py-1 hover:bg-primary-200"
                                                        @click="changePosition(it, pos.key)"
                                                    >
                                                        <span>{{ pos.name }}</span>
                                                        <i class="fa-solid fa-warning text-yellow-500"></i>
                                                    </li>
                                                </template>
                                            </ul>
                                        </template>
                                    </ContextMenuButton>
                                </div>
                            </VDraggable>
                        </template>
                    </ul>
                </div>
            </div>
        </div>
        <RegistrationEditDlg ref="editRegistrationDialog" />
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import type { Event, Position, PositionKey, Registration, ResolvedRegistration, ResolvedSlot } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { ContextMenuButton, VDraggable, VDropzone } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import RegistrationEditDlg from './RegistrationEditDlg.vue';

enum DragSource {
    FROM_TEAM = 'team',
    FROM_WAITING_LIST = 'waitinglist',
}

interface Props {
    event: Event;
}

interface Emits {
    (e: 'update:event', value: Event): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const usersUseCase = useUsersUseCase();
const eventService = useEventService();

const position = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const registrations = ref<ResolvedRegistration[]>([]);
const team = ref<ResolvedSlot[]>([]);
const dragSource = ref<DragSource | null>(null);
const loading = ref<boolean>(true);

const editRegistrationDialog = ref<Dialog<Registration, Registration> | null>(null);

const summary = computed<Record<PositionKey, number>>(() => {
    const sum: Record<PositionKey, number> = {};
    team.value
        .filter((it) => it.userName)
        .forEach((it) => {
            let count = sum[it.position.key] || 0;
            count++;
            sum[it.position.key] = count;
        });
    return sum;
});

const positions = computed<Position[]>(() => {
    return [...position.value.values()].sort((a, b) => b.prio - a.prio);
});

async function init(): Promise<void> {
    await fetchPositions();
    await fetchTeam();
    watch(props.event.registrations, () => fetchTeam(), { deep: true });
    watch(props.event.slots, () => fetchTeam(), { deep: true });
    loading.value = false;
}

async function addToTeam(registration: ResolvedRegistration): Promise<void> {
    const user = await usersUseCase.resolveRegistrationUser(registration);

    const slot = eventService
        .getOpenSlots(props.event)
        .find((it) => it.positionKeys.includes(registration.positionKey));
    if (!slot) {
        // TODO handle error
        alert('Das geht nicht');
    } else if (user) {
        emit('update:event', eventService.assignUserToSlot(props.event, user, slot.key));
    } else {
        emit('update:event', eventService.assignGuestToSlot(props.event, registration.name, slot.key));
    }
}

async function removeFromTeam(slot: ResolvedSlot) {
    emit('update:event', eventService.unassignSlot(props.event, slot.key));
}

async function cancelRegistration(slot: ResolvedSlot) {
    if (slot.userKey) {
        emit('update:event', eventService.cancelUserRegistration(props.event, slot.userKey));
    } else if (slot.userName) {
        emit('update:event', eventService.cancelGuestRegistration(props.event, slot.userName));
    }
    await fetchTeam();
}

async function editWaitinglistRegistration(resolvedRegistration: ResolvedRegistration): Promise<void> {
    if (editRegistrationDialog.value && (resolvedRegistration.userKey || resolvedRegistration.name)) {
        const registration = eventService.findRegistration(
            props.event,
            resolvedRegistration.userKey,
            resolvedRegistration.name
        );
        if (registration) {
            try {
                const edited = await editRegistrationDialog.value.open(registration);
                registration.positionKey = edited.positionKey;
            } catch (e) {
                // canceled
            }
        } else {
            console.error('Registration not found');
        }
    }
}

async function editSlotRegistration(resolvedslot: ResolvedSlot): Promise<void> {
    if (editRegistrationDialog.value && (resolvedslot.userKey || resolvedslot.userName)) {
        const registration = eventService.findRegistration(props.event, resolvedslot.userKey, resolvedslot.userName);
        if (registration) {
            try {
                const edited = await editRegistrationDialog.value.open(registration);
                registration.positionKey = edited.positionKey;
            } catch (e) {
                // canceled
            }
        } else {
            console.error('Registration not found');
        }
    }
}

function changePosition(resolvedRegistration: ResolvedRegistration, newPositionKey: PositionKey): void {
    const registration = eventService.findRegistration(
        props.event,
        resolvedRegistration.userKey,
        resolvedRegistration.name
    );
    if (registration) {
        registration.positionKey = newPositionKey;
    }
}

async function fetchPositions(): Promise<void> {
    position.value = await usersUseCase.resolvePositionNames();
}

async function fetchTeam(): Promise<void> {
    console.log('fetch team');
    const resolvedSlots = await usersUseCase.resolveEventSlots(props.event);
    team.value = resolvedSlots.filter((it) => it.userName || it.userKey || it.criticality >= 1);
    registrations.value = await usersUseCase.resolveWaitingList(props.event);
}

init();
</script>

<style>
.dropzone-add,
.dropzone-remove,
.dropzone-delete {
    @apply flex h-full flex-col items-center justify-center space-y-8;
    @apply rounded-xl;
    @apply border-2 border-dashed border-gray-800 text-gray-800;
}

.dropzone span {
    @apply text-sm font-black uppercase;
}

.dropzone.hover .dropzone-add {
    @apply border-2 border-dashed border-primary-600 bg-primary-100 text-primary-600;
}

.dropzone.hover .dropzone-remove {
    @apply border-2 border-dashed border-primary-600 bg-primary-100 text-primary-600;
}

.dropzone.hover .dropzone-delete {
    @apply border-2 border-dashed border-red-600 bg-red-100 text-red-600;
}
</style>
