<template>
    <VTable
        :items="props.event.locations"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        :class="$attrs.class"
        :sortable="hasPermission(Permission.WRITE_EVENT_DETAILS)"
        @reordered="updateOrders"
        @click="editLocation($event.item)"
    >
        <template #row="{ item, first, last }">
            <td class="hidden py-0">
                <div class="text-secondary flex flex-col items-center">
                    <div
                        class="min-h-4 grow border-r-2 border-dashed border-current"
                        :class="{ 'min-h-2 opacity-0': first, 'min-h-6': last }"
                    ></div>
                    <div class="my-1 h-4 w-4 rounded-full border-2 border-current"></div>
                    <div
                        class="min-h-4 grow border-r-2 border-dashed border-current"
                        :class="{ 'min-h-2 opacity-0': last, 'min-h-6': first }"
                    ></div>
                </div>
            </td>
            <td :key="item.icon" class="text-xl">
                <i class="fa-solid" :class="item.icon" />
            </td>
            <td class="w-full max-w-[50vw] sm:w-1/2 sm:max-w-full">
                <p class="font-semibold">
                    <span>{{ item.name }}</span>
                </p>
                <p v-if="item.address" class="mt-1 truncate text-sm font-light">
                    <span>{{ item.address }}</span>
                </p>
            </td>
            <td class="hidden w-1/2 whitespace-nowrap sm:table-cell">
                <p class="mb-2 text-sm">
                    <span class="mr-2 inline-block w-10 opacity-50">{{ $t('domain.location.eta') }}:</span>
                    <span v-if="item.eta" class="font-semibold">{{ $d(item.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}</span>
                    <span v-else>-</span>
                </p>
                <p class="text-sm">
                    <span class="mr-2 inline-block w-10 opacity-50">{{ $t('domain.location.etd') }}: </span>
                    <span v-if="item.etd" class="font-semibold">{{ $d(item.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}</span>
                    <span v-else>-</span>
                </p>
            </td>
        </template>
        <template v-if="hasPermission(Permission.WRITE_EVENT_DETAILS)" #context-menu="{ item }">
            <li class="context-menu-item" @click="editLocation(item)">
                <i class="fa-solid fa-edit" />
                <span>{{ $t('views.events.edit.actions.edit-location') }}</span>
            </li>
            <li class="context-menu-item" @click="moveLocationUp(item)">
                <i class="fa-solid fa-arrow-up" />
                <span>{{ $t('generic.move-up') }}</span>
            </li>
            <li class="context-menu-item" @click="moveLocationDown(item)">
                <i class="fa-solid fa-arrow-down" />
                <span>{{ $t('generic.move-down') }}</span>
            </li>
            <li class="context-menu-item text-error" @click="deleteLocation(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>{{ $t('views.events.edit.actions.delete-location') }}</span>
            </li>
        </template>
    </VTable>
    <LocationEditDlg ref="editLocationDialog" />
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { deepCopy } from '@/common';
import { DateTimeFormat } from '@/common/date';
import type { Event, Location } from '@/domain';
import { Permission } from '@/domain';
import { useEventService } from '@/domain/services';
import type { Dialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { useSession } from '@/ui/composables/Session.ts';
import LocationEditDlg from '@/ui/views/events/edit/components/LocationEditDlg.vue';

interface Props {
    event: Event;
}

type Emit = (e: 'update:event', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const eventService = useEventService();
const { hasPermission } = useSession();

const editLocationDialog = ref<Dialog<Location, Location | undefined> | null>(null);

async function editLocation(location: Location): Promise<void> {
    if (!hasPermission(Permission.WRITE_EVENT_DETAILS)) {
        console.warn('User has no permission to edit event details');
        return;
    }
    const editedLocation = await editLocationDialog.value?.open(location);
    if (editedLocation) {
        const updatedEvent = eventService.updateLocation(deepCopy(props.event), editedLocation);
        emit('update:event', updatedEvent);
    }
}

async function moveLocationUp(location: Location): Promise<void> {
    const updatedEvent = eventService.moveLocation(deepCopy(props.event), location, -1);
    emit('update:event', updatedEvent);
}

async function moveLocationDown(location: Location): Promise<void> {
    const updatedEvent = eventService.moveLocation(deepCopy(props.event), location, 1);
    emit('update:event', updatedEvent);
}

async function updateOrders(): Promise<void> {
    const updatedEvent = deepCopy(props.event);
    updatedEvent.locations.forEach((location, index) => (location.order = index + 1));
    emit('update:event', updatedEvent);
}

function deleteLocation(location: Location): void {
    const updatedEvent = eventService.removeLocation(deepCopy(props.event), location);
    emit('update:event', updatedEvent);
}
</script>
