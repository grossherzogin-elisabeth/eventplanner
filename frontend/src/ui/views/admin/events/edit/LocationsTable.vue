<template>
    <VTable
        :items="props.event.locations"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        @click="editLocation($event)"
    >
        <template #row="{ item }">
            <td :key="item.icon" class="text-xl">
                <i class="fa-solid" :class="item.icon" />
            </td>
            <td class="w-1/3">
                <p class="mb-1 font-semibold">{{ item.name }} {{ item.order }}</p>
            </td>
            <td class="w-1/3">
                <p class="mb-1 text-sm font-light">{{ item.address }}</p>
            </td>
            <td class="w-1/3">
                <CountryFlag v-if="item.country" :country="item.country" />
            </td>
        </template>
        <template #context-menu="{ item }">
            <li class="context-menu-item" @click="editLocation(item)">
                <i class="fa-solid fa-edit" />
                <span>Reisestop bearbeiten</span>
            </li>
            <li class="context-menu-item" @click="moveLocationUp(item)">
                <i class="fa-solid fa-arrow-up" />
                <span>Nach oben verschieben</span>
            </li>
            <li class="context-menu-item" @click="moveLocationDown(item)">
                <i class="fa-solid fa-arrow-down" />
                <span>Nach unten verschieben</span>
            </li>
            <li class="context-menu-item text-red-700" @click="deleteLocation(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>Reisestop Löschen</span>
            </li>
        </template>
    </VTable>
</template>
<script setup lang="ts">
import type { Event, Location } from '@/domain';
import { VTable } from '@/ui/components/common';
import CountryFlag from '@/ui/components/utils/CountryFlag.vue';
import { useEventService } from '@/ui/composables/Domain.ts';

interface Props {
    event: Event;
}

type Emit = (e: 'update:modelValue', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const eventService = useEventService();

async function editLocation(location: Location): Promise<void> {
    alert('TODO Location ' + location.name + ' bearbeiten');
    // const editedLocation = await editLocationDialog.value?.open(location);
    // if (editedLocation) {
    //     const updatedEvent = eventService.updateLocation(props.event, editedLocation);
    //     emit('update:modelValue', updatedEvent);
    // }
}

async function moveLocationUp(location: Location): Promise<void> {
    const updatedEvent = eventService.moveLocation(props.event, location, -1);
    emit('update:modelValue', updatedEvent);
}

async function moveLocationDown(location: Location): Promise<void> {
    const updatedEvent = eventService.moveLocation(props.event, location, 1);
    emit('update:modelValue', updatedEvent);
}

function deleteLocation(location: Location): void {
    const updatedEvent = eventService.removeLocation(props.event, location);
    emit('update:modelValue', updatedEvent);
}
</script>
