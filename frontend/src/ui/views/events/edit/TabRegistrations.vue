<template>
    <div
        class="scrollbar-invisible xs:-mx-8 xs:px-8 -mx-4 mb-4 flex items-center gap-2 overflow-x-auto px-4 md:-mx-16 md:-mt-4 md:px-16 xl:-mx-20 xl:px-20"
    >
        <FilterMultiselect
            v-model="filterPositions"
            :placeholder="$t('views.events.edit.filter.all-positions')"
            :options="positions.options.value"
        />
        <FilterToggle
            v-if="props.event.signupType === EventSignupType.Assignment"
            v-model="filterFreeSlots"
            :label="$t('views.events.edit.filter.free-slots')"
        />
        <FilterToggle v-model="filterValidQualifications" :label="$t('views.events.edit.filter.valid-qualifications')" />
        <FilterToggle v-model="filterUnconfirmed" :label="$t('views.events.edit.filter.pending-confirmation')" />
    </div>
    <template v-if="props.event.signupType === EventSignupType.Assignment">
        <h2 class="text-secondary mb-4 font-bold">
            {{ $t('domain.event.crew-count', { count: filteredCrew.length }) }}
        </h2>
    </template>
    <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
        <RegistrationsTable
            :event="props.event"
            :registrations="filteredCrew"
            @delete-registration="deleteRegistration($event)"
            @edit-registration="editRegistration($event)"
            @edit-slot="editSlot($event)"
            @delete-slot="deleteSlot($event)"
            @add-to-crew="addToCrew($event)"
            @remove-from-crew="removeFromCrew($event)"
        />
    </div>
    <template v-if="props.event.signupType === EventSignupType.Assignment">
        <h2 class="text-secondary mb-4 font-bold">
            {{ $t('domain.event.registration-count', { count: filteredRegistrations.length }) }}
        </h2>
        <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
            <RegistrationsTable
                :event="props.event"
                :registrations="filteredRegistrations"
                @delete-registration="deleteRegistration($event)"
                @edit-registration="editRegistration($event)"
                @edit-slot="editSlot($event)"
                @delete-slot="deleteSlot($event)"
                @add-to-crew="addToCrew($event)"
                @remove-from-crew="removeFromCrew($event)"
            />
        </div>
    </template>
    <RegistrationEditDlg ref="editRegistrationDialog" :event="props.event" />
    <SlotEditDlg ref="editSlotDialog" />
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useErrorHandlingService, useEventAdministrationUseCase } from '@/application';
import { deepCopy, hasAnyOverlap } from '@/common';
import type { Event, PositionKey, Registration, ResolvedRegistrationSlot, Slot } from '@/domain';
import { EventSignupType, RegistrationSlotState, SlotCriticality, useEventService } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { FilterMultiselect, FilterToggle } from '@/ui/components/filters';
import { usePositions } from '@/ui/composables/Positions';
import { useQuery } from '@/ui/composables/QueryState';
import RegistrationEditDlg from '@/ui/views/events/edit/components/RegistrationEditDlg.vue';
import RegistrationsTable from '@/ui/views/events/edit/components/RegistrationsTable.vue';
import SlotEditDlg from '@/ui/views/events/edit/components/SlotEditDlg.vue';
import { v4 as uuid } from 'uuid';

interface Props {
    event: Event;
    waitinglist: ResolvedRegistrationSlot[];
    crew: ResolvedRegistrationSlot[];
}

type Emit = (e: 'update:event', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const { t } = useI18n();
const eventAdminUseCase = useEventAdministrationUseCase();
const eventService = useEventService();
const errorHandler = useErrorHandlingService();
const positions = usePositions();

const filterPositions = useQuery<PositionKey[]>('positions', []).parameter;
const filterFreeSlots = useQuery<boolean>('free-slots', false).parameter;
const filterValidQualifications = useQuery<boolean>('valid-qualifications', false).parameter;
const filterUnconfirmed = useQuery<boolean>('pending-confirmation', false).parameter;

const editRegistrationDialog = ref<Dialog<Registration, Registration | undefined> | null>(null);
const editSlotDialog = ref<Dialog<Slot, Slot | undefined> | null>(null);

const filteredCrew = computed(() =>
    props.crew
        .filter(matchesFreeSlotsFilter)
        .filter(matchesUnconfirmedFilter)
        .filter(matchesValidQualificationsFilter)
        .filter(matchesPositionsFilter)
);
const filteredRegistrations = computed(() =>
    props.waitinglist.filter(matchesUnconfirmedFilter).filter(matchesValidQualificationsFilter).filter(matchesPositionsFilter)
);

function matchesFreeSlotsFilter(value: ResolvedRegistrationSlot): boolean {
    return filterFreeSlots.value !== true || value.registration === undefined;
}

function matchesPositionsFilter(value: ResolvedRegistrationSlot): boolean {
    if (filterPositions.value.length === 0) {
        return true;
    }
    if (value.registration !== undefined) {
        return filterPositions.value.includes(value.registration.positionKey);
    }
    return hasAnyOverlap(value.slot?.positionKeys ?? [], filterPositions.value);
}

function matchesValidQualificationsFilter(value: ResolvedRegistrationSlot): boolean {
    return (
        filterValidQualifications.value !== true ||
        (value.registration !== undefined && value.expiredQualifications.length === 0) ||
        (value.registration === undefined && filterFreeSlots.value === true)
    );
}

function matchesUnconfirmedFilter(value: ResolvedRegistrationSlot): boolean {
    return filterUnconfirmed.value !== true || value.state !== RegistrationSlotState.CONFIRMED;
}

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

async function deleteRegistration(aggregate: ResolvedRegistrationSlot): Promise<void> {
    await removeFromCrew(aggregate);
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

async function editRegistration(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (aggregate.user || aggregate.name) {
        const updatedEvent = deepCopy(props.event);
        const registration = eventService.findRegistration(updatedEvent, aggregate.user?.key, aggregate.name);
        if (registration) {
            const updatedRegistration = await editRegistrationDialog.value?.open(registration);
            if (updatedRegistration) {
                registration.positionKey = updatedRegistration.positionKey;
                registration.note = updatedRegistration.note;
                registration.name = updatedRegistration.name;
                emit('update:event', updatedEvent);
            }
        }
    }
}

async function deleteSlot(aggregate: ResolvedRegistrationSlot): Promise<void> {
    if (!aggregate.slot || aggregate.user) {
        return;
    }
    const updatedEvent = eventService.removeSlot(deepCopy(props.event), aggregate.slot);
    emit('update:event', updatedEvent);
}
</script>
