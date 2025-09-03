<template>
    <VTable
        :items="props.event.locations"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        :class="$attrs.class"
        :sortable="signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
        @reordered="updateOrders"
        @click="editLocation($event.item)"
    >
        <template #row="{ item, first, last }">
            <td class="hidden py-0">
                <div class="flex flex-col items-center text-secondary">
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
            <td class="w-1/2 max-w-[50vw] sm:max-w-full">
                <p class="font-semibold">
                    <span>{{ item.name }}</span>
                </p>
                <p v-if="item.address" class="mt-1 truncate text-sm font-light">
                    <span>{{ item.address }}</span>
                </p>
            </td>
            <td class="w-1/2 whitespace-nowrap">
                <p class="mb-2 text-sm">
                    <span class="mr-2 inline-block w-10 opacity-50">{{ $t('views.events.edit.locations-table.eta') }} </span>
                    <span v-if="item.eta" class="font-semibold">{{ $d(item.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}</span>
                    <span v-else>-</span>
                </p>
                <p class="text-sm">
                    <span class="mr-2 inline-block w-10 opacity-50">{{ $t('views.events.edit.locations-table.etd') }} </span>
                    <span v-if="item.etd" class="font-semibold">{{ $d(item.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}</span>
                    <span v-else>-</span>
                </p>
            </td>
        </template>
        <template v-if="signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)" #context-menu="{ item }">
            <li class="context-menu-item" @click="editLocation(item)">
                <i class="fa-solid fa-edit" />
                <span>{{ $t('views.events.edit.locations-table.edit') }}</span>
            </li>
            <li class="context-menu-item" @click="moveLocationUp(item)">
                <i class="fa-solid fa-arrow-up" />
                <span>{{ $t('views.events.edit.locations-table.move-up') }}</span>
            </li>
            <li class="context-menu-item" @click="moveLocationDown(item)">
                <i class="fa-solid fa-arrow-down" />
                <span>{{ $t('views.events.edit.locations-table.move-down') }}</span>
            </li>
            <li class="context-menu-item text-error" @click="deleteLocation(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>{{ $t('views.events.edit.locations-table.delete') }}</span>
            </li>
        </template>
    </VTable>
    <LocationEditDlg ref="editLocationDialog" />
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { Event, Location } from '@/domain';
import { Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { useAuthUseCase } from '@/ui/composables/Application.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import LocationEditDlg from '@/ui/views/events/edit/LocationEditDlg.vue';

interface Props {
    event: Event;
}

type Emit = (e: 'update:modelValue', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const signedInUser = useAuthUseCase().getSignedInUser();
const eventService = useEventService();
const editLocationDialog = ref<Dialog<Location, Location | undefined> | null>(null);

async function editLocation(location: Location): Promise<void> {
    if (!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)) {
        console.warn('User has no permission to edit event details');
        return;
    }
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

async function updateOrders(): Promise<void> {
    props.event.locations.forEach((location, index) => (location.order = index + 1));
}

function deleteLocation(location: Location): void {
    const updatedEvent = eventService.removeLocation(props.event, location);
    emit('update:modelValue', updatedEvent);
}
</script>
