<template>
    <div v-if="!loading" class="-mx-4 flex h-full flex-col px-4 2xl:flex-row 2xl:items-start">
        <!-- position counters -->
        <div class="top-14 -mx-8 mb-4 overflow-x-auto bg-surface pb-4 2xl:sticky 2xl:mr-8 2xl:w-64 2xl:pt-10">
            <div class="scrollbar-invisible flex items-start gap-2 px-8 text-sm font-bold text-onsurface-variant md:flex-wrap 2xl:flex-col">
                <div class="flex items-center rounded-2xl bg-surface-container p-1">
                    <span class="px-2"> Crew </span>
                    <span
                        class="flex h-5 w-5 items-center justify-center whitespace-nowrap rounded-full bg-white bg-opacity-25 px-1 pt-0.5 text-center text-xs"
                    >
                        {{ props.event?.assignedUserCount }}
                    </span>
                </div>
                <div
                    class="flex items-center rounded-2xl p-1"
                    :class="
                        secureMinimumCrewMembers >= 8
                            ? 'bg-secondary-container text-onsecondary-container'
                            : 'bg-error-container text-onerror-container'
                    "
                >
                    <span class="whitespace-nowrap px-2"> Sichere Besatzung </span>
                    <span
                        class="flex h-5 w-5 items-center justify-center whitespace-nowrap rounded-full bg-white bg-opacity-25 px-1 pt-0.5 text-center text-xs"
                    >
                        {{ secureMinimumCrewMembers }}
                    </span>
                </div>
                <div
                    v-for="pos in positions.all.value"
                    :key="pos.key"
                    :style="{ 'background-color': pos.color }"
                    class="flex items-center rounded-2xl p-1"
                >
                    <span class="truncate px-2">
                        {{ pos.name }}
                    </span>
                    <span
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-white bg-opacity-25 px-1 pt-0.5 text-center text-xs"
                    >
                        {{ summary[pos.key] || '0' }}
                    </span>
                </div>
            </div>
        </div>
        <div class="-mx-4 flex-1 gap-16 md:flex xl:pr-8 2xl:w-0">
            <div class="mb-8 px-4 pb-4 md:w-1/2 lg:mb-0">
                <h2 class="mb-4 font-bold text-secondary">Crew</h2>
                <!-- slot list-admin dropzone -->
                <div class="sticky top-24">
                    <div class="absolute z-10 w-full" :class="{ hidden: dragSource !== DragSource.FROM_WAITING_LIST }">
                        <VDropzone class="h-96" @drop="addToTeam($event as ResolvedRegistrationSlot)">
                            <div class="dropzone-add">
                                <i class="fa-regular fa-calendar-plus text-3xl opacity-75"></i>
                                <span>Zur Crew hinzufügen</span>
                            </div>
                        </VDropzone>
                    </div>
                </div>
                <div class="-mx-4" :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_WAITING_LIST }">
                    <!-- empty slot list placeholder -->
                    <div v-if="team.length === 0" class="rounded-xl bg-secondary-container text-onsecondary-container">
                        <div class="flex items-center py-8 pl-4 pr-8">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-list-check opacity-75"></i>
                                    Hier ist noch nichts...
                                </h3>
                                <p class="text-sm">
                                    Für diese Reise wurden noch keine Slots definiert. Im "Slots" Tab kannst du Slots für diese Reise
                                    definieren. Anschließend kannst du Nutzer von der Warteliste in die Crew ziehen.
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8 opacity-10">
                            <li v-for="i in 5" :key="i" class="mr-4 flex items-center rounded-xl px-4 py-2 md:space-x-4">
                                <i class="fa-solid fa-grip-vertical hidden text-sm lg:inline"></i>
                                <i class="fa-regular fa-circle"></i>
                                <span class="mx-2 inline-block h-4 w-64 rounded-full bg-onsecondary-container"> </span>
                                <span class="flex-grow"></span>
                                <span class="position h-4 w-20 bg-onsecondary-container"></span>
                            </li>
                        </ul>
                    </div>
                    <!-- slot list -->
                    <ul v-else>
                        <template v-for="it in team" :key="it.slot?.key + it.registration?.key + it.user?.key + it.position.key">
                            <VTooltip :disabled="it.registration === undefined">
                                <template #default>
                                    <ContextMenuButton class="w-full" anchor-align-x="right" dropdown-position-x="left">
                                        <template #icon>
                                            <VDraggable
                                                class="flex items-center rounded-xl px-4 py-2 hover:bg-surface-container md:space-x-4"
                                                :class="{
                                                    'cursor-move': it.registration !== undefined,
                                                    'pointer-events-none': it.registration === undefined,
                                                    '': it.expiredQualifications.length === 0,
                                                }"
                                                component="li"
                                                :value="it"
                                                @dragend="dragSource = null"
                                                @dragstart="dragSource = DragSource.FROM_TEAM"
                                            >
                                                <!-- drag icon -->
                                                <i
                                                    class="fa-solid fa-grip-vertical hidden text-sm text-onsurface-variant opacity-50 lg:inline"
                                                ></i>

                                                <!-- user status -->
                                                <span v-if="it.name && !it.registration?.confirmed" class="">
                                                    <i class="fa-solid fa-user-clock opacity-50"></i>
                                                </span>
                                                <span v-else-if="it.name && it.registration?.confirmed">
                                                    <i class="fa-solid fa-user-check text-green"></i>
                                                </span>
                                                <span v-else>
                                                    <i class="fa-solid fa-user-xmark text-error text-opacity-50"></i>
                                                </span>

                                                <!-- user name -->
                                                <span v-if="it.name" class="mx-2 truncate">{{ it.name }}</span>
                                                <span v-else-if="it.user" class="mx-2 truncate italic text-error"> Gelöschter Nutzer </span>
                                                <span v-else class="mx-2 truncate italic text-error text-opacity-50">
                                                    Noch nicht besetzt
                                                </span>

                                                <i
                                                    v-if="it.registration?.note"
                                                    class="fa-solid fa-comment-dots text-sm text-onsurface-variant opacity-50"
                                                ></i>
                                                <!-- user qualification status -->
                                                <!--                                        <i v-if="it.expiredQualifications.length > 0" class="fa-solid fa-warning text-error"></i>-->
                                                <!-- user position -->
                                                <span class="flex-grow"></span>
                                                <span :style="{ background: it.position.color }" class="position text-xs">
                                                    {{ it.slot?.positionName || it.position.name }}
                                                    <i
                                                        v-if="it.expiredQualifications.length > 0"
                                                        class="fa-solid fa-warning text-error"
                                                    ></i>
                                                    <i v-else-if="it.registration" class="fa-solid fa-check"></i>
                                                </span>
                                            </VDraggable>
                                        </template>
                                        <template #default>
                                            <ul>
                                                <RouterLink
                                                    :to="{ name: Routes.UserDetails, params: { key: it.user?.key } }"
                                                    target="_blank"
                                                    class="context-menu-item"
                                                    :class="{ disabled: !it.user }"
                                                >
                                                    <i class="fa-solid fa-arrow-up-right-from-square"></i>
                                                    <span>Nutzer anzeigen</span>
                                                </RouterLink>
                                                <li class="context-menu-item" :class="{ disabled: !it.user }" @click="removeFromTeam(it)">
                                                    <i class="fa-solid fa-arrow-right"></i>
                                                    <span>Auf Warteliste setzen</span>
                                                </li>
                                                <li
                                                    class="context-menu-item"
                                                    :class="{ disabled: !it.user }"
                                                    @click="editSlotRegistration(it)"
                                                >
                                                    <i class="fa-solid fa-edit"></i>
                                                    <span>Anmeldung bearbeiten</span>
                                                </li>
                                                <li class="context-menu-item" @click="editSlot(it)">
                                                    <i class="fa-solid fa-edit"></i>
                                                    <span>Slot bearbeiten</span>
                                                </li>
                                                <li v-if="it.user" class="context-menu-item text-error" @click="cancelRegistration(it)">
                                                    <i class="fa-solid fa-user-minus"></i>
                                                    <span>Anmeldung löschen</span>
                                                </li>
                                                <li v-else class="context-menu-item text-error" @click="deleteSlot(it)">
                                                    <i class="fa-solid fa-trash-alt"></i>
                                                    <span>Slot löschen</span>
                                                </li>
                                            </ul>
                                        </template>
                                    </ContextMenuButton>
                                </template>
                                <template #tooltip>
                                    <RegistrationTooltip :registration="it" />
                                </template>
                            </VTooltip>
                        </template>
                    </ul>
                </div>
            </div>

            <div class="px-4 md:w-1/2">
                <h2 class="mb-4 font-bold text-secondary">Warteliste</h2>
                <!-- waitinglist dropzone -->
                <div class="sticky top-24">
                    <div class="absolute w-full space-y-8" :class="{ hidden: dragSource !== DragSource.FROM_TEAM }">
                        <VDropzone class="h-44" @drop="removeFromTeam($event as ResolvedRegistrationSlot)">
                            <div class="dropzone-remove">
                                <i class="fa-regular fa-calendar-minus text-3xl opacity-75"></i>
                                <span>Auf Warteliste verschieben</span>
                            </div>
                        </VDropzone>
                        <VDropzone class="h-44" @drop="cancelRegistration($event as ResolvedRegistrationSlot)">
                            <div class="dropzone-delete">
                                <i class="fa-regular fa-calendar-xmark text-3xl opacity-75"></i>
                                <span>Anmeldung löschen</span>
                            </div>
                        </VDropzone>
                    </div>
                </div>
                <div class="-mx-4" :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_TEAM }">
                    <!-- empty waitinglist placeholder -->
                    <div v-if="registrations.length === 0" class="rounded-xl bg-surface-container-low text-onsurface">
                        <div class="flex items-center py-8 pl-4 pr-8">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-list-check opacity-75"></i>
                                    Die Warteliste ist leer...
                                </h3>
                                <p class="text-sm">
                                    Für diese Reise gibt es gerade keine Anmeldungen. Sobald Nutzer sich für diese Reise anmelden, werden
                                    sie hier aufgelistet und können für die Crew eingeplant werden.
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8 opacity-10">
                            <li v-for="i in 5" :key="i" class="mr-4 flex items-center rounded-xl px-4 py-2 md:space-x-4">
                                <i class="fa-solid fa-grip-vertical hidden text-sm lg:inline"></i>
                                <span class="mx-2 inline-block h-4 w-64 rounded-full bg-onsurface"> </span>
                                <span class="flex-grow"></span>
                                <span class="position block h-4 w-20 bg-onsurface"> </span>
                            </li>
                        </ul>
                    </div>
                    <!-- waitinglist -->
                    <ul>
                        <template v-for="it in registrations" :key="it.registration?.key + it.user?.key + it.name + it.position.key">
                            <VTooltip>
                                <template #default>
                                    <ContextMenuButton class="w-full" anchor-align-x="right" dropdown-position-x="left">
                                        <template #icon>
                                            <VDraggable
                                                class="flex cursor-move items-center rounded-xl px-4 py-2 hover:bg-surface-container"
                                                :class="{
                                                    'cursor-move': it.registration !== undefined,
                                                    'pointer-events-none': it.registration === undefined,
                                                    '': it.expiredQualifications.length === 0,
                                                }"
                                                component="li"
                                                :value="it"
                                                @dragend="dragSource = null"
                                                @dragstart="dragSource = DragSource.FROM_WAITING_LIST"
                                            >
                                                <i
                                                    class="fa-solid fa-grip-vertical mr-4 hidden text-sm text-onsurface-variant opacity-50 lg:inline"
                                                ></i>
                                                <span v-if="it.name" class="truncate">{{ it.name }}</span>
                                                <span v-else-if="it.user" class="flex-grow italic text-error"> Unbekannt </span>
                                                <i
                                                    v-if="it.registration?.note"
                                                    class="fa-solid fa-comment-dots text-sm text-onsurface-variant opacity-50"
                                                ></i>
                                                <span class="flex-grow"></span>
                                                <div>
                                                    <ContextMenuButton>
                                                        <template #icon>
                                                            <span
                                                                :style="{ background: it.position.color }"
                                                                :class="{ '': it.expiredQualifications.length > 0 }"
                                                                class="position cursor-pointer text-xs"
                                                            >
                                                                {{ it.position.name }}
                                                                <i
                                                                    v-if="it.expiredQualifications.length > 0"
                                                                    class="fa-solid fa-warning text-error"
                                                                ></i>
                                                                <i v-else-if="it.user" class="fa-solid fa-check"></i>
                                                                <i v-else class="fa-solid fa-question"></i>
                                                            </span>
                                                        </template>
                                                        <template #default>
                                                            <ul>
                                                                <template v-for="pos in positions.all.value" :key="pos.key">
                                                                    <li
                                                                        v-if="!it.user || it.user.positionKeys.includes(pos.key)"
                                                                        class="context-menu-item"
                                                                        @click="changePosition(it, pos.key)"
                                                                    >
                                                                        <span class="flex-grow">{{ pos.name }}</span>
                                                                    </li>
                                                                </template>
                                                                <!-- show also non matching positions ??? -->
                                                                <template v-if="it.user">
                                                                    <template v-for="pos in positions.all.value" :key="pos.key">
                                                                        <li
                                                                            v-if="!it.user.positionKeys.includes(pos.key)"
                                                                            class="context-menu-item"
                                                                            @click="changePosition(it, pos.key)"
                                                                        >
                                                                            <span class="flex-grow">{{ pos.name }}</span>
                                                                            <i class="fa-solid fa-warning text-error"></i>
                                                                        </li>
                                                                    </template>
                                                                </template>
                                                            </ul>
                                                        </template>
                                                    </ContextMenuButton>
                                                </div>
                                            </VDraggable>
                                        </template>
                                        <template #default>
                                            <ul>
                                                <RouterLink
                                                    :to="{ name: Routes.UserDetails, params: { key: it.user?.key } }"
                                                    target="_blank"
                                                    class="context-menu-item"
                                                    :class="{ disabled: !it.user }"
                                                >
                                                    <i class="fa-solid fa-arrow-up-right-from-square"></i>
                                                    <span>Nutzer anzeigen</span>
                                                </RouterLink>
                                                <li class="context-menu-item" :class="{ disabled: !it.user }" @click="addToTeam(it)">
                                                    <i class="fa-solid fa-arrow-left"></i>
                                                    <span>Zur Crew hinzufügen</span>
                                                </li>
                                                <li
                                                    class="context-menu-item"
                                                    :class="{ disabled: !it.user }"
                                                    @click="editSlotRegistration(it)"
                                                >
                                                    <i class="fa-solid fa-edit"></i>
                                                    <span>Anmeldung bearbeiten</span>
                                                </li>
                                                <li
                                                    class="context-menu-item text-error"
                                                    :class="{ disabled: !it.user }"
                                                    @click="cancelRegistration(it)"
                                                >
                                                    <i class="fa-solid fa-user-minus"></i>
                                                    <span>Anmeldung löschen</span>
                                                </li>
                                            </ul>
                                        </template>
                                    </ContextMenuButton>
                                </template>
                                <template #tooltip>
                                    <RegistrationTooltip :registration="it" />
                                </template>
                            </VTooltip>
                        </template>
                    </ul>
                </div>
            </div>
        </div>
        <RegistrationEditDlg ref="editRegistrationDialog" :event="props.event" />
        <SlotEditDlg ref="editSlotDialog" />
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import type { Event, PositionKey, Registration, Slot } from '@/domain';
import { SlotCriticality } from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot.ts';
import type { Dialog } from '@/ui/components/common';
import { ContextMenuButton, VDraggable, VDropzone, VTooltip } from '@/ui/components/common';
import { useErrorHandling, useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { Routes } from '@/ui/views/Routes.ts';
import SlotEditDlg from '@/ui/views/events/edit/SlotEditDlg.vue';
import { v4 as uuid } from 'uuid';
import RegistrationEditDlg from './RegistrationEditDlg.vue';
import RegistrationTooltip from './RegistrationTooltip.vue';

enum DragSource {
    FROM_TEAM = 'team',
    FROM_WAITING_LIST = 'waitinglist',
}

interface Props {
    event: Event;
}

type Emits = (e: 'update:event', value: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const eventUseCase = useEventUseCase();
const eventAdminUseCase = useEventAdministrationUseCase();
const eventService = useEventService();
const errorHandler = useErrorHandling();
const positions = usePositions();

const registrations = ref<ResolvedRegistrationSlot[]>([]);
const team = ref<ResolvedRegistrationSlot[]>([]);
const dragSource = ref<DragSource | null>(null);
const loading = ref<boolean>(true);

const editRegistrationDialog = ref<Dialog<Registration, Registration | undefined> | null>(null);
const editSlotDialog = ref<Dialog<Slot, Slot | undefined> | null>(null);

const summary = computed<Record<PositionKey, number>>(() => {
    const sum: Record<PositionKey, number> = {};
    team.value
        .filter((it) => it.name)
        .forEach((it) => {
            let count = sum[it.position.key] || 0;
            count++;
            sum[it.position.key] = count;
        });
    return sum;
});

const secureMinimumCrewMembers = computed<number>(() => {
    return team.value.filter((it) => it.registration).filter((it) => it.expiredQualifications.length === 0).length;
});

async function init(): Promise<void> {
    await fetchTeam();
    watch(props.event.registrations, () => fetchTeam(), { deep: true });
    watch(props.event.slots, () => fetchTeam(), { deep: true });
    loading.value = false;
}

async function addToTeam(aggregate: ResolvedRegistrationSlot): Promise<void> {
    const slot = eventService.getOpenSlots(props.event).find((it) => it.positionKeys.includes(aggregate.position.key));
    if (!slot) {
        errorHandler.handleError({
            title: 'Zuweisung nicht möglich',
            message: `${aggregate.name} kann nicht zur Crew hinzugefügt werden, da es keinen freien Slot für die
                Position ${aggregate.position.name} gibt. Du kannst entweder einen passenden Slot freigeben, indem du
                jemand anderes aus der Crew entfernst oder einen neuen Slot für diese Reise hinzufügen.`,
            cancelText: 'Abbrechen',
            retryText: 'Slot hinzufügen',
            retry: async () => {
                const event = props.event;
                event.slots.push({
                    key: uuid(),
                    positionKeys: [aggregate.position.key],
                    criticality: SlotCriticality.Optional,
                    assignedRegistrationKey: aggregate.registration?.key,
                    order: event.slots.length,
                });
                emit('update:event', event);
                await fetchTeam();
            },
        });
    } else if (aggregate.user) {
        emit('update:event', eventService.assignUserToSlot(props.event, aggregate.user, slot.key));
        await fetchTeam();
    } else {
        emit('update:event', eventService.assignGuestToSlot(props.event, aggregate.name, slot.key));
        await fetchTeam();
    }
}

async function removeFromTeam(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (aggregate.slot) {
        emit('update:event', eventService.unassignSlot(props.event, aggregate.slot.key));
        await fetchTeam();
    }
}

async function cancelRegistration(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (aggregate.user) {
        emit('update:event', eventService.cancelUserRegistration(props.event, aggregate.user?.key));
    } else if (aggregate.name) {
        emit('update:event', eventService.cancelGuestRegistration(props.event, aggregate.name));
    }
    await fetchTeam();
}

async function editSlot(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (!aggregate.slot) {
        return;
    }
    const editedSlot = await editSlotDialog.value?.open(aggregate.slot);
    if (editedSlot) {
        const updatedEvent = eventService.updateSlot(props.event, editedSlot);
        emit('update:event', updatedEvent);
    }
}

async function deleteSlot(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (!aggregate.slot || aggregate.user) {
        return;
    }
    const updatedEvent = eventService.removeSlot(props.event, aggregate.slot);
    emit('update:event', updatedEvent);
}

async function editSlotRegistration(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (aggregate.user || aggregate.name) {
        const registration = eventService.findRegistration(props.event, aggregate.user?.key, aggregate.name);
        if (registration) {
            const updatedRegistration = await editRegistrationDialog.value?.open(registration);
            if (updatedRegistration) {
                registration.positionKey = updatedRegistration.positionKey;
                registration.note = updatedRegistration.note;
                registration.name = updatedRegistration.name;
            }
        }
    }
}

function changePosition(resolvedRegistration: ResolvedRegistrationSlot, newPositionKey: PositionKey): void {
    const registration = eventService.findRegistration(props.event, resolvedRegistration.user?.key, resolvedRegistration.name);
    if (registration) {
        registration.positionKey = newPositionKey;
    }
}

async function fetchTeam(): Promise<void> {
    const all = await eventUseCase.resolveRegistrations(props.event);
    team.value = eventAdminUseCase.filterForCrew(all);
    registrations.value = eventAdminUseCase.filterForWaitingList(all);
}

init();
</script>

<style>
.dropzone-add,
.dropzone-remove,
.dropzone-delete {
    @apply flex h-full flex-col items-center justify-center space-y-8;
    @apply rounded-xl;
    @apply border-2 border-dashed border-outline;
}

.dropzone span {
    @apply text-sm font-black uppercase;
}

.dropzone.hover .dropzone-add {
    @apply border-2 border-dashed border-onprimary-container bg-primary-container text-onprimary-container;
}

.dropzone.hover .dropzone-remove {
    @apply border-2 border-dashed border-onprimary-container bg-primary-container text-onprimary-container;
}

.dropzone.hover .dropzone-delete {
    @apply border-2 border-dashed border-onerror-container bg-error-container text-onerror-container;
}
</style>
