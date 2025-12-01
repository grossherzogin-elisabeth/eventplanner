<template>
    <div v-if="!loading" class="-mx-4 flex h-full flex-col px-4">
        <!-- position counters -->
        <div class="bg-surface top-32 -mx-8 mb-4 overflow-x-auto pb-4">
            <div class="scrollbar-invisible text-onsurface-variant flex items-start gap-2 px-8 text-sm font-bold md:flex-wrap">
                <div class="bg-surface-container flex items-center rounded-2xl p-1">
                    <span class="px-2">{{ $t('domain.event.crew') }}</span>
                    <span
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-white/25 px-1 pt-0.5 text-center text-xs whitespace-nowrap"
                    >
                        {{ props.event?.assignedUserCount }}
                    </span>
                </div>
                <div
                    class="flex items-center rounded-2xl p-1"
                    :class="
                        secureMinimumCrewMembers >= 8
                            ? 'bg-secondary-container text-onsecondary-container'
                            : 'bg-error-container/50 text-onerror-container'
                    "
                >
                    <span class="px-2 whitespace-nowrap">{{ $t('views.events.edit.secure-crew') }}</span>
                    <span
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-white/25 px-1 pt-0.5 text-center text-xs whitespace-nowrap"
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
                    <span class="flex h-5 w-5 items-center justify-center rounded-full bg-white/25 px-1 pt-0.5 text-center text-xs">
                        {{ summary[pos.key] || '0' }}
                    </span>
                </div>
            </div>
        </div>
        <div class="-mx-4 flex-1 gap-16 md:flex">
            <div class="crew mb-8 px-4 pb-4 md:w-1/2 lg:mb-0">
                <h2 class="text-secondary mb-4 font-bold">{{ $t('domain.event.crew') }}</h2>
                <!-- slot list-admin dropzone -->
                <div class="sticky top-24">
                    <div class="absolute z-10 w-full" :class="{ hidden: dragSource !== DragSource.FROM_WAITING_LIST }">
                        <VDropzone class="h-96" @drop="addToTeam($event as ResolvedRegistrationSlot)">
                            <div class="dropzone-add">
                                <i class="fa-regular fa-calendar-plus text-3xl opacity-75"></i>
                                <span>{{ $t('views.events.edit.actions.add-to-crew') }}</span>
                            </div>
                        </VDropzone>
                    </div>
                </div>
                <div class="-mx-4" :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_WAITING_LIST }">
                    <!-- empty slot list placeholder -->
                    <div v-if="team.length === 0" class="bg-secondary-container text-onsecondary-container rounded-xl">
                        <div class="flex items-center py-8 pr-8 pl-4">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-list-check opacity-75"></i>
                                    {{ $t('views.events.edit.empty-crew.title') }}
                                </h3>
                                <p class="text-sm">
                                    {{ $t('views.events.edit.empty-crew.desc') }}
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8 opacity-10">
                            <li v-for="i in 5" :key="i" class="mr-4 flex items-center rounded-xl px-4 py-2 md:space-x-4">
                                <i class="fa-solid fa-grip-vertical hidden text-sm lg:inline"></i>
                                <i class="fa-regular fa-circle"></i>
                                <span class="bg-onsecondary-container mx-2 inline-block h-4 w-64 rounded-full"> </span>
                                <span class="grow"></span>
                                <span class="tag neutral h-4 w-20"></span>
                            </li>
                        </ul>
                    </div>
                    <!-- slot list -->
                    <ul v-else>
                        <RegistrationRow
                            v-for="it in team"
                            :key="`${it.slot?.key}_${it.registration?.key}_${it.user?.key}_${it.position.key}`"
                            :value="it"
                            @cancel-registration="cancelRegistration(it)"
                            @edit-registration="editSlotRegistration(it)"
                            @edit-slot="editSlot(it)"
                            @delete-slot="deleteSlot(it)"
                            @add-to-team="addToTeam(it)"
                            @remove-from-team="removeFromTeam(it)"
                            @dragend="dragSource = null"
                            @dragstart="dragSource = DragSource.FROM_TEAM"
                        />
                    </ul>
                </div>
            </div>

            <div class="waiting-list px-4 md:w-1/2">
                <h2 class="text-secondary mb-4 font-bold">{{ $t('domain.event.waiting-list') }}</h2>
                <!-- waitinglist dropzone -->
                <div class="sticky top-24">
                    <div class="absolute w-full space-y-8" :class="{ hidden: dragSource !== DragSource.FROM_TEAM }">
                        <VDropzone class="h-44" @drop="removeFromTeam($event as ResolvedRegistrationSlot)">
                            <div class="dropzone-remove">
                                <i class="fa-regular fa-calendar-minus text-3xl opacity-75"></i>
                                <span>{{ $t('views.events.edit.actions.move-to-waiting-list') }}</span>
                            </div>
                        </VDropzone>
                        <VDropzone class="h-44" @drop="cancelRegistration($event as ResolvedRegistrationSlot)">
                            <div class="dropzone-delete">
                                <i class="fa-regular fa-calendar-xmark text-3xl opacity-75"></i>
                                <span>{{ $t('views.events.edit.actions.delete-registration') }}</span>
                            </div>
                        </VDropzone>
                    </div>
                </div>
                <div class="-mx-4" :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_TEAM }">
                    <!-- empty waitinglist placeholder -->
                    <div v-if="registrations.length === 0" class="bg-surface-container-low text-onsurface rounded-xl">
                        <div class="flex items-center py-8 pr-8 pl-4">
                            <div class="mr-4">
                                <h3 class="mb-4 text-base">
                                    <i class="fa-solid fa-list-check opacity-75"></i>
                                    {{ $t('views.events.edit.empty-waitinglist.title') }}
                                </h3>
                                <p class="text-sm">
                                    {{ $t('views.events.edit.empty-waitinglist.desc') }}
                                </p>
                            </div>
                            <div></div>
                        </div>
                        <ul class="pb-8 opacity-10">
                            <li v-for="i in 5" :key="i" class="mr-4 flex items-center rounded-xl px-4 py-2 md:space-x-4">
                                <i class="fa-solid fa-grip-vertical hidden text-sm lg:inline"></i>
                                <span class="bg-onsurface mx-2 inline-block h-4 w-64 rounded-full"> </span>
                                <span class="grow"></span>
                                <span class="tag neutral block h-4 w-20"> </span>
                            </li>
                        </ul>
                    </div>
                    <!-- waitinglist -->
                    <ul>
                        <RegistrationRow
                            v-for="it in registrations"
                            :key="`${it.slot?.key}_${it.registration?.key}_${it.user?.key}_${it.position.key}`"
                            :value="it"
                            @cancel-registration="cancelRegistration(it)"
                            @edit-registration="editSlotRegistration(it)"
                            @edit-slot="editSlot(it)"
                            @delete-slot="deleteSlot(it)"
                            @add-to-team="addToTeam(it)"
                            @remove-from-team="removeFromTeam(it)"
                            @dragend="dragSource = null"
                            @dragstart="dragSource = DragSource.FROM_WAITING_LIST"
                        />
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
import { useI18n } from 'vue-i18n';
import { useErrorHandlingService, useEventAdministrationUseCase, useEventUseCase } from '@/application';
import type { Event, PositionKey, Registration, Slot } from '@/domain';
import { SlotCriticality } from '@/domain';
import { useEventService } from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot';
import type { Dialog } from '@/ui/components/common';
import { VDropzone } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions';
import RegistrationRow from '@/ui/views/events/edit/components/RegistrationRow.vue';
import SlotEditDlg from '@/ui/views/events/edit/components/SlotEditDlg.vue';
import { v4 as uuid } from 'uuid';
import RegistrationEditDlg from './RegistrationEditDlg.vue';

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

const { t } = useI18n();
const eventUseCase = useEventUseCase();
const eventAdminUseCase = useEventAdministrationUseCase();
const eventService = useEventService();
const errorHandler = useErrorHandlingService();
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
            title: t('domain.event.no-slot-for-position-error.title'),
            message: t('domain.event.no-slot-for-position-error.message', { name: aggregate.name, position: aggregate.position.name }),
            cancelText: t('generic.cancel'),
            retryText: t('domain.event.no-slot-for-position-error.retry'),
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
        emit('update:event', await eventAdminUseCase.assignUserToSlot(props.event, aggregate.user, slot.key));
        await fetchTeam();
    } else {
        emit('update:event', await eventAdminUseCase.assignGuestToSlot(props.event, aggregate.name, slot.key));
        await fetchTeam();
    }
}

async function removeFromTeam(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (aggregate.slot) {
        emit('update:event', await eventAdminUseCase.unassignSlot(props.event, aggregate.slot.key));
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

async function fetchTeam(): Promise<void> {
    const all = await eventUseCase.resolveRegistrations(props.event);
    team.value = eventAdminUseCase.filterForCrew(all);
    registrations.value = eventAdminUseCase.filterForWaitingList(all);
}

init();
</script>

<style>
@reference "tailwindcss";

.dropzone-add,
.dropzone-remove,
.dropzone-delete {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    border-width: 2px;
    border-style: dashed;
    @apply space-y-8;
    border-radius: var(--radius-xl);
    border-color: var(--color-outline);
}

.dropzone span {
    font-size: var(--text-sm);
    font-weight: var(--font-weight-black);
    text-transform: uppercase;
}

.dropzone.hover .dropzone-add {
    border: 2px dashed;
    background-color: var(--color-primary-container);
    color: var(--color-onprimary-container);
    border-color: var(--color-onprimary-container);
}

.dropzone.hover .dropzone-remove {
    border: 2px dashed;
    background-color: var(--color-primary-container);
    color: var(--color-onprimary-container);
    border-color: var(--color-onprimary-container);
}

.dropzone.hover .dropzone-delete {
    border: 2px dashed;
    background-color: var(--color-error-container);
    color: var(--color-onerror-container);
    border-color: var(--color-onerror-container);
}
</style>
