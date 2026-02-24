<template>
    <VTable
        :items="props.event.slots"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        :class="$attrs.class"
        :page-size="-1"
        sortable
        @click="editSlot($event.item)"
        @reordered="updateOrders"
    >
        <template #row="{ item, index }">
            <td class="w-0">
                <span class="bg-surface-container-highest inline-block rounded-full px-2 py-1 text-sm font-semibold">
                    #{{ index + 1 }}
                </span>
            </td>
            <td class="w-1/3">
                <p class="font-semibold whitespace-nowrap">
                    {{ item.positionName || positions.get(item.positionKeys[0]).name }}
                </p>
                <p v-if="item.assignedRegistrationKey" class="truncate text-sm">
                    {{ props.registrations.find((r) => r.registration?.key === item.assignedRegistrationKey)?.name }}
                </p>
                <p v-else class="truncate text-sm">
                    {{ $t('views.events.edit.free') }}
                </p>
            </td>
            <td class="w-2/3 min-w-96">
                <div class="flex flex-wrap items-center gap-1">
                    <div
                        v-for="positionKey in item.positionKeys"
                        :key="positionKey"
                        :style="{ '--color': positions.get(positionKey).color }"
                        class="tag custom"
                    >
                        <span>{{ positions.get(positionKey).name }}</span>
                    </div>
                </div>
            </td>
            <td class="w-64">
                <div class="flex items-center justify-end">
                    <span v-if="item.criticality === 2" class="status-badge error">
                        <i class="fa-solid fa-warning"></i>
                        <span>{{ $t('domain.event-slot.required') }}</span>
                    </span>
                    <span v-else-if="item.criticality === 1" class="status-badge warning">
                        <i class="fa-solid fa-circle-info"></i>
                        <span>{{ $t('domain.event-slot.important') }}</span>
                    </span>
                    <span v-else class="status-badge neutral">
                        <i class="fa-solid fa-circle-question"></i>
                        <span>{{ $t('domain.event-slot.optional') }}</span>
                    </span>
                </div>
            </td>
        </template>
        <template v-if="hasPermission(Permission.WRITE_EVENT_DETAILS)" #context-menu="{ item }">
            <li class="context-menu-item" data-test-id="action-edit" @click="editSlot(item)">
                <i class="fa-solid fa-edit" />
                <span>{{ $t('views.events.edit.actions.edit-slot') }}</span>
            </li>
            <li class="context-menu-item" data-test-id="action-duplicate" @click="duplicateSlot(item)">
                <i class="fa-solid fa-clone" />
                <span>{{ $t('views.events.edit.actions.duplicate-slot') }}</span>
            </li>
            <li class="context-menu-item" data-test-id="action-move-up" @click="moveSlotUp(item)">
                <i class="fa-solid fa-arrow-up" />
                <span>{{ $t('generic.move-up') }}</span>
            </li>
            <li class="context-menu-item" data-test-id="action-move-down" @click="moveSlotDown(item)">
                <i class="fa-solid fa-arrow-down" />
                <span>{{ $t('generic.move-down') }}</span>
            </li>
            <li class="context-menu-item text-error" data-test-id="action-delete" @click="deleteSlot(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>{{ $t('views.events.edit.actions.delete-slot') }}</span>
            </li>
        </template>
    </VTable>
    <SlotEditDlg ref="editSlotDialog" />
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { deepCopy } from '@/common';
import type { Event, ResolvedRegistrationSlot, Slot } from '@/domain';
import { Permission, useEventService } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useSession } from '@/ui/composables/Session.ts';
import SlotEditDlg from '@/ui/views/events/edit/components/SlotEditDlg.vue';

interface Props {
    event: Event;
    registrations: ResolvedRegistrationSlot[];
    loading?: boolean;
}

type Emit = (e: 'update:event', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const { hasPermission } = useSession();
const positions = usePositions();
const eventService = useEventService();

const editSlotDialog = ref<Dialog<Slot, Slot | undefined> | null>(null);

async function editSlot(slot: Slot): Promise<void> {
    if (hasPermission(Permission.WRITE_EVENT_DETAILS)) {
        const editedSlot = await editSlotDialog.value?.open(slot);
        if (editedSlot) {
            const updatedEvent = eventService.updateSlot(deepCopy(props.event), editedSlot);
            emit('update:event', updatedEvent);
        }
    }
}

function updateOrders(): void {
    if (hasPermission(Permission.WRITE_EVENT_DETAILS)) {
        const updatedEvent = deepCopy(props.event);
        updatedEvent.slots.forEach((slot, index) => (slot.order = index));
        emit('update:event', updatedEvent);
    }
}

async function moveSlotUp(slot: Slot): Promise<void> {
    if (hasPermission(Permission.WRITE_EVENT_DETAILS)) {
        const updatedEvent = eventService.moveSlot(deepCopy(props.event), slot, -1);
        emit('update:event', updatedEvent);
    }
}

async function moveSlotDown(slot: Slot): Promise<void> {
    if (hasPermission(Permission.WRITE_EVENT_DETAILS)) {
        const updatedEvent = eventService.moveSlot(deepCopy(props.event), slot, 1);
        emit('update:event', updatedEvent);
    }
}

function deleteSlot(slot: Slot): void {
    if (hasPermission(Permission.WRITE_EVENT_DETAILS)) {
        const updatedEvent = eventService.removeSlot(deepCopy(props.event), slot);
        emit('update:event', updatedEvent);
    }
}

function duplicateSlot(slot: Slot): void {
    if (hasPermission(Permission.WRITE_EVENT_DETAILS)) {
        const updatedEvent = eventService.duplicateSlot(deepCopy(props.event), slot);
        emit('update:event', updatedEvent);
    }
}
</script>
