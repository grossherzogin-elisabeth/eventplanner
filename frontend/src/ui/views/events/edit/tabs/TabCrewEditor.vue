<template>
    <div class="-mx-4 flex h-full flex-col px-4">
        <!-- position counters -->
        <div class="bg-surface top-32 -mx-8 mb-4 overflow-x-auto pb-4">
            <div class="scrollbar-invisible text-onsurface-variant flex items-start gap-2 px-8 text-sm font-bold md:flex-wrap">
                <div class="tag info flex items-center space-x-1">
                    <span>{{ $t('domain.event.crew') }}</span>
                    <span>({{ props.event?.assignedUserCount }})</span>
                </div>
                <div class="tag flex items-center space-x-1" :class="secureMinimumCrewMembers >= 8 ? 'info' : 'error'">
                    <span>{{ $t('views.events.edit.secure-crew') }}</span>
                    <span>({{ secureMinimumCrewMembers }})</span>
                </div>
                <div
                    v-for="pos in positions.all.value"
                    :key="pos.key"
                    :style="{ '--color': pos.color }"
                    class="tag custom flex items-center space-x-1"
                >
                    <span>{{ pos.name }}</span>
                    <span>({{ summary[pos.key] || '0' }})</span>
                </div>
            </div>
        </div>
        <div class="-mx-4 flex-1 gap-16 md:flex">
            <div class="crew mb-8 px-4 pb-4 md:w-1/2 lg:mb-0">
                <h2 class="text-secondary mb-4 font-bold">{{ $t('domain.event.crew') }}</h2>
                <!-- slot list-admin dropzone -->
                <div class="sticky top-24">
                    <div class="absolute z-10 w-full" :class="{ hidden: dragSource !== DragSource.FROM_WAITING_LIST }">
                        <VDropzone class="h-96" @drop="addToCrew($event as ResolvedRegistrationSlot)">
                            <div class="dropzone-add">
                                <i class="fa-regular fa-calendar-plus text-3xl opacity-75"></i>
                                <span>{{ $t('views.events.edit.actions.add-to-crew') }}</span>
                            </div>
                        </VDropzone>
                    </div>
                </div>
                <div class="-mx-4" :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_WAITING_LIST }">
                    <!-- empty slot list placeholder -->
                    <div v-if="props.crew.length === 0" class="bg-secondary-container text-onsecondary-container rounded-xl">
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
                            v-for="it in props.crew"
                            :key="`${it.slot?.key}_${it.registration?.key}_${it.user?.key}_${it.position.key}`"
                            :value="it"
                            @cancel-registration="cancelRegistration(it)"
                            @edit-registration="editSlotRegistration(it)"
                            @edit-slot="editSlot(it)"
                            @delete-slot="deleteSlot(it)"
                            @add-to-crew="addToCrew(it)"
                            @remove-from-crew="removeFromCrew(it)"
                            @dragend="dragSource = null"
                            @dragstart="dragSource = DragSource.FROM_CREW"
                        />
                    </ul>
                </div>
            </div>

            <div class="waiting-list px-4 md:w-1/2">
                <h2 class="text-secondary mb-4 font-bold">{{ $t('domain.event.waiting-list') }}</h2>
                <!-- waitinglist dropzone -->
                <div class="sticky top-24">
                    <div class="absolute w-full space-y-8" :class="{ hidden: dragSource !== DragSource.FROM_CREW }">
                        <VDropzone class="h-44" @drop="removeFromCrew($event as ResolvedRegistrationSlot)">
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
                <div class="-mx-4" :class="{ 'pointer-events-none opacity-10': dragSource === DragSource.FROM_CREW }">
                    <!-- empty waitinglist placeholder -->
                    <div v-if="props.waitinglist.length === 0" class="bg-surface-container-low text-onsurface rounded-xl">
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
                            v-for="it in props.waitinglist"
                            :key="`${it.slot?.key}_${it.registration?.key}_${it.user?.key}_${it.position.key}`"
                            :value="it"
                            @cancel-registration="cancelRegistration(it)"
                            @edit-registration="editSlotRegistration(it)"
                            @edit-slot="editSlot(it)"
                            @delete-slot="deleteSlot(it)"
                            @add-to-crew="addToCrew(it)"
                            @remove-from-crew="removeFromCrew(it)"
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
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useErrorHandlingService, useEventAdministrationUseCase } from '@/application';
import { deepCopy } from '@/common';
import type { Event, PositionKey, Registration, Slot } from '@/domain';
import { SlotCriticality, useEventService } from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot.ts';
import type { Dialog } from '@/ui/components/common';
import { VDropzone } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';
import RegistrationRow from '@/ui/views/events/edit/components/RegistrationRow.vue';
import SlotEditDlg from '@/ui/views/events/edit/components/SlotEditDlg.vue';
import { v4 as uuid } from 'uuid';
import RegistrationEditDlg from '../components/RegistrationEditDlg.vue';

enum DragSource {
    FROM_CREW = 'crew',
    FROM_WAITING_LIST = 'waitinglist',
}

interface Props {
    event: Event;
    waitinglist: ResolvedRegistrationSlot[];
    crew: ResolvedRegistrationSlot[];
}

type Emits = (e: 'update:event', value: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { t } = useI18n();
const eventAdminUseCase = useEventAdministrationUseCase();
const eventService = useEventService();
const errorHandler = useErrorHandlingService();
const positions = usePositions();

const dragSource = ref<DragSource | null>(null);

const editRegistrationDialog = ref<Dialog<Registration, Registration | undefined> | null>(null);
const editSlotDialog = ref<Dialog<Slot, Slot | undefined> | null>(null);

const summary = computed<Record<PositionKey, number>>(() => {
    const sum: Record<PositionKey, number> = {};
    props.crew
        .filter((it) => it.name)
        .forEach((it) => {
            let count = sum[it.position.key] || 0;
            count++;
            sum[it.position.key] = count;
        });
    return sum;
});

const secureMinimumCrewMembers = computed<number>(() => {
    return props.crew.filter((it) => it.registration).filter((it) => it.expiredQualifications.length === 0).length;
});

async function addToCrew(aggregate: ResolvedRegistrationSlot): Promise<void> {
    const slot = eventService.getOpenSlots(props.event).find((it) => it.positionKeys.includes(aggregate.position.key));
    if (!slot) {
        errorHandler.handleError({
            title: t('domain.event.no-slot-for-position-error.title'),
            message: t('domain.event.no-slot-for-position-error.message', { name: aggregate.name, position: aggregate.position.name }),
            cancelText: t('generic.cancel'),
            retryText: t('domain.event.no-slot-for-position-error.retry'),
            retry: async () => {
                const event = deepCopy(props.event);
                event.slots.push({
                    key: uuid(),
                    positionKeys: [aggregate.position.key],
                    criticality: SlotCriticality.Optional,
                    assignedRegistrationKey: aggregate.registration?.key,
                    order: event.slots.length,
                });
                emit('update:event', event);
            },
        });
    } else if (aggregate.user) {
        emit('update:event', await eventAdminUseCase.assignUserToSlot(deepCopy(props.event), aggregate.user, slot.key));
    } else {
        emit('update:event', await eventAdminUseCase.assignGuestToSlot(deepCopy(props.event), aggregate.name, slot.key));
    }
}

async function removeFromCrew(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (aggregate.slot) {
        emit('update:event', await eventAdminUseCase.unassignSlot(deepCopy(props.event), aggregate.slot.key));
    }
}

async function cancelRegistration(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (aggregate.user) {
        emit('update:event', eventService.cancelUserRegistration(deepCopy(props.event), aggregate.user?.key));
    } else if (aggregate.name) {
        emit('update:event', eventService.cancelGuestRegistration(deepCopy(props.event), aggregate.name));
    }
}

async function editSlot(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (!aggregate.slot) {
        return;
    }
    const editedSlot = await editSlotDialog.value?.open(aggregate.slot);
    if (editedSlot) {
        const updatedEvent = eventService.updateSlot(deepCopy(props.event), editedSlot);
        emit('update:event', updatedEvent);
    }
}

async function deleteSlot(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (!aggregate.slot || aggregate.user) {
        return;
    }
    const updatedEvent = eventService.removeSlot(deepCopy(props.event), aggregate.slot);
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
