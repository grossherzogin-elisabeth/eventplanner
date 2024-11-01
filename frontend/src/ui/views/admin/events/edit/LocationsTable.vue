<template>
    <VTable
        :items="props.event.locations"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        :class="$attrs.class"
        @click="editLocation($event)"
    >
        <template #row="{ item, first, last }">
            <td class="py-0">
                <div class="flex flex-col items-center text-primary-900 text-opacity-20">
                    <div
                        class="min-h-4 flex-grow border-r-2 border-dashed border-current"
                        :class="{ 'min-h-2 opacity-0': first, 'min-h-6': last }"
                    ></div>
                    <div class="my-1 h-4 w-4 rounded-full border-2 border-current"></div>
                    <div
                        class="min-h-4 flex-grow border-r-2 border-dashed border-current"
                        :class="{ 'min-h-2 opacity-0': last, 'min-h-6': first }"
                    ></div>
                </div>
            </td>
            <td :key="item.icon" class="text-xl">
                <i class="fa-solid" :class="item.icon" />
            </td>
            <td class="w-full lg:w-2/3">
                <p class="mb-1 font-semibold">
                    <span>{{ item.name }}</span>
                </p>
                <p class="mb-1 truncate text-sm font-light">
                    <span v-if="item.address && item.country">
                        {{ item.address }}, {{ countries.getName(item.country) }}
                    </span>
                    <span v-else-if="item.address">{{ item.address }}</span>
                    <span v-else-if="item.country">{{ countries.getName(item.country) }}</span>
                </p>
            </td>
            <td class="hidden w-1/3 lg:table-cell"></td>
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
    <LocationEditDlg ref="editLocationDialog" />
</template>
<script setup lang="ts">
import { ref } from 'vue';
import type { Event, Location } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { useCountries } from '@/ui/composables/Countries.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import LocationEditDlg from '@/ui/views/admin/events/edit/LocationEditDlg.vue';

interface Props {
    event: Event;
}

type Emit = (e: 'update:modelValue', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const countries = useCountries();
const eventService = useEventService();
const editLocationDialog = ref<Dialog<Location, Location | undefined> | null>(null);

async function editLocation(location: Location): Promise<void> {
    const editedLocation = await editLocationDialog.value?.open(location);
    if (editedLocation) {
        const updatedEvent = eventService.updateLocation(props.event, editedLocation);
        emit('update:modelValue', updatedEvent);
    }
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
