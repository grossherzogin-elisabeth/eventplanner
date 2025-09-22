<template>
    <VTable
        :items="props.event.slots"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        :class="$attrs.class"
        :page-size="-1"
        sortable
        @click="editSlot($event.item)"
        @reordered="updatePrios"
    >
        <template #row="{ item, index }">
            <td class="w-0">
                <span class="inline-block rounded-full bg-surface-container-highest px-2 py-1 text-sm font-semibold">
                    #{{ index + 1 }}
                </span>
            </td>
            <td class="w-1/3">
                <p class="whitespace-nowrap font-semibold">
                    {{ item.positionName || positions.get(item.positionKeys[0]).name }}
                </p>
                <p class="truncate text-sm">
                    {{ $t('views.events.edit.slots-table.registration-id') }}:
                    {{ item.assignedRegistrationKey?.substring(0, 8) || $t('views.events.edit.slots-table.free') }}
                </p>
            </td>
            <td class="w-2/3 min-w-96">
                <div class="flex flex-wrap items-center">
                    <div
                        v-for="positionKey in item.positionKeys"
                        :key="positionKey"
                        :style="{ background: positions.get(positionKey).color }"
                        class="position mb-1 mr-1 text-xs"
                    >
                        <span>{{ positions.get(positionKey).name }}</span>
                    </div>
                </div>
            </td>
            <td class="w-64">
                <div class="flex items-center justify-end">
                    <span v-if="item.criticality === 2" class="status-panel bg-red-container text-onred-container">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-2 whitespace-nowrap font-semibold">{{
                            $t('views.events.edit.slot-edit-dlg.criticality.values.required')
                        }}</span>
                    </span>
                    <span v-else-if="item.criticality === 1" class="status-panel bg-yellow-container text-onyellow-container">
                        <i class="fa-solid fa-circle-info"></i>
                        <span class="ml-2 whitespace-nowrap font-semibold">{{
                            $t('views.events.edit.slot-edit-dlg.criticality.values.important')
                        }}</span>
                    </span>
                    <span v-else class="status-panel bg-surface-container-highest text-onsurface">
                        <i class="fa-solid fa-circle-question"></i>
                        <span class="ml-2 whitespace-nowrap font-semibold">{{
                            $t('views.events.edit.slot-edit-dlg.criticality.values.optional')
                        }}</span>
                    </span>
                </div>
            </td>
        </template>
        <template #context-menu="{ item }">
            <li class="context-menu-item" @click="editSlot(item)">
                <i class="fa-solid fa-edit" />
                <span>{{ $t('views.events.edit.slots-table.edit-slot') }}</span>
            </li>
            <li class="context-menu-item" @click="duplicateSlot(item)">
                <i class="fa-solid fa-clone" />
                <span>{{ $t('views.events.edit.slots-table.duplicate-slot') }}</span>
            </li>
            <li class="context-menu-item" @click="moveSlotUp(item)">
                <i class="fa-solid fa-arrow-up" />
                <span>{{ $t('views.events.edit.slots-table.move-up') }}</span>
            </li>
            <li class="context-menu-item" @click="moveSlotDown(item)">
                <i class="fa-solid fa-arrow-down" />
                <span>{{ $t('views.events.edit.slots-table.move-down') }}</span>
            </li>
            <li class="context-menu-item text-error" @click="deleteSlot(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>{{ $t('views.events.edit.slots-table.delete-slot') }}</span>
            </li>
        </template>
    </VTable>
    <SlotEditDlg ref="editSlotDialog" />
</template>
<script setup lang="ts">
import { ref } from 'vue';
import type { Event, Slot } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { useEventService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import SlotEditDlg from '@/ui/views/events/edit/SlotEditDlg.vue';

interface Props {
    event: Event;
}

type Emit = (e: 'update:modelValue', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const positions = usePositions();
const eventService = useEventService();

const editSlotDialog = ref<Dialog<Slot, Slot | undefined> | null>(null);

async function editSlot(slot: Slot): Promise<void> {
    const editedSlot = await editSlotDialog.value?.open(slot);
    if (editedSlot) {
        const updatedEvent = eventService.updateSlot(props.event, editedSlot);
        emit('update:modelValue', updatedEvent);
    }
}

function updatePrios(): void {
    const updatedEvent = props.event;
    updatedEvent.slots.forEach((slot, index) => (slot.order = index));
    emit('update:modelValue', updatedEvent);
}

async function moveSlotUp(slot: Slot): Promise<void> {
    const updatedEvent = eventService.moveSlot(props.event, slot, -1);
    emit('update:modelValue', updatedEvent);
}

async function moveSlotDown(slot: Slot): Promise<void> {
    const updatedEvent = eventService.moveSlot(props.event, slot, 1);
    emit('update:modelValue', updatedEvent);
}

function deleteSlot(slot: Slot): void {
    const updatedEvent = eventService.removeSlot(props.event, slot);
    emit('update:modelValue', updatedEvent);
}

function duplicateSlot(slot: Slot): void {
    const updatedEvent = eventService.duplicateSlot(props.event, slot);
    emit('update:modelValue', updatedEvent);
}
</script>
