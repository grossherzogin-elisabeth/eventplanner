<template>
    <div class="full-width-scrollable">
        <VTable
            :items="props.event?.locations"
            class="scrollbar-invisible interactive-table no-header"
            :class="$attrs.class"
            :sortable="hasPermission(Permission.UPDATE_EVENT_DETAILS)"
            @reordered="updateOrders"
            @click="editLocation($event.item)"
        >
            <template #row="{ item }">
                <td :key="item?.icon" class="pr-4 text-xl">
                    <i class="fa-solid" :class="item?.icon" />
                </td>
                <td class="w-full sm:w-2/3 lg:w-1/2">
                    <p class="font-semibold">
                        <span>{{ item?.name }}</span>
                    </p>
                    <div class="flex opacity-75">
                        <p class="mt-1 w-0 grow truncate text-sm font-light">
                            <span v-if="item?.address">
                                {{ item.address }}
                            </span>
                            <template v-if="item?.eta || item?.etd">
                                <span v-if="item.address" class="sm:hidden"> / </span>
                                <span v-if="item.eta" class="sm:hidden">
                                    {{ $t('domain.location.eta') }}:
                                    {{ $d(item.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}
                                </span>
                                <span v-if="item.eta && item.etd" class="sm:hidden"> / </span>
                                <span v-if="item.etd" class="sm:hidden">
                                    {{ $t('domain.location.etd') }}:
                                    {{ $d(item.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}
                                </span>
                            </template>
                        </p>
                    </div>
                </td>
                <td class="hidden w-1/3 whitespace-nowrap sm:table-cell lg:hidden">
                    <p class="">
                        <span v-if="item" class="inline-block w-12 opacity-70">{{ $t('domain.location.eta') }}: </span>
                        <template v-if="item?.eta">{{ $d(item.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}</template>
                        <span v-else-if="item" class="italic opacity-25">{{ $t('generic.no-information') }}</span>
                    </p>
                    <p class="">
                        <span v-if="item" class="inline-block w-12 opacity-70">{{ $t('domain.location.etd') }}: </span>
                        <template v-if="item?.etd">{{ $d(item.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}</template>
                        <span v-else-if="item" class="italic opacity-25">{{ $t('generic.no-information') }}</span>
                    </p>
                </td>
                <td class="hidden w-1/4 whitespace-nowrap lg:table-cell">
                    <p class="mb-1 font-semibold">
                        <template v-if="item?.eta">{{ $d(item.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}</template>
                        <span v-else-if="item" class="italic opacity-25">{{ $t('generic.no-information') }}</span>
                    </p>
                    <p class="text-sm opacity-75">
                        <template v-if="item">{{ $t('domain.location.eta') }}</template>
                    </p>
                </td>
                <td class="hidden w-1/4 whitespace-nowrap lg:table-cell">
                    <p class="mb-1 font-semibold">
                        <template v-if="item?.etd">{{ $d(item.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}</template>
                        <span v-else-if="item" class="italic opacity-25">{{ $t('generic.no-information') }}</span>
                    </p>
                    <p class="text-sm opacity-75">
                        <template v-if="item">{{ $t('domain.location.etd') }}</template>
                    </p>
                </td>
            </template>
            <template v-if="hasPermission(Permission.UPDATE_EVENT_DETAILS)" #context-menu="{ item }">
                <li class="context-menu-item" data-test-id="action-edit" @click="editLocation(item)">
                    <i class="fa-solid fa-edit" />
                    <span>{{ $t('domain.location.actions.edit') }}</span>
                </li>
                <li class="context-menu-item" data-test-id="action-move-up" @click="moveLocationUp(item)">
                    <i class="fa-solid fa-arrow-up" />
                    <span>{{ $t('generic.move-up') }}</span>
                </li>
                <li class="context-menu-item" data-test-id="action-move-down" @click="moveLocationDown(item)">
                    <i class="fa-solid fa-arrow-down" />
                    <span>{{ $t('generic.move-down') }}</span>
                </li>
                <li class="context-menu-item text-error" data-test-id="action-delete" @click="deleteLocation(item)">
                    <i class="fa-solid fa-trash-alt" />
                    <span>{{ $t('domain.location.actions.delete') }}</span>
                </li>
            </template>
        </VTable>
        <LocationEditDlg ref="editLocationDialog" :event="props.event" />
    </div>
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
    event?: Event;
}

type Emit = (e: 'update:event', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const eventService = useEventService();
const { hasPermission } = useSession();

const editLocationDialog = ref<Dialog<Location, Location | undefined> | null>(null);

async function editLocation(location: Location): Promise<void> {
    if (props.event && hasPermission(Permission.UPDATE_EVENT_DETAILS)) {
        const editedLocation = await editLocationDialog.value?.open(location);
        if (editedLocation) {
            const updatedEvent = eventService.updateLocation(deepCopy(props.event), editedLocation);
            emit('update:event', updatedEvent);
        }
    }
}

async function moveLocationUp(location: Location): Promise<void> {
    if (props.event && hasPermission(Permission.UPDATE_EVENT_DETAILS)) {
        const updatedEvent = eventService.moveLocation(deepCopy(props.event), location, -1);
        emit('update:event', updatedEvent);
    }
}

async function moveLocationDown(location: Location): Promise<void> {
    if (props.event && hasPermission(Permission.UPDATE_EVENT_DETAILS)) {
        const updatedEvent = eventService.moveLocation(deepCopy(props.event), location, 1);
        emit('update:event', updatedEvent);
    }
}

async function updateOrders(): Promise<void> {
    if (props.event && hasPermission(Permission.UPDATE_EVENT_DETAILS)) {
        const updatedEvent = deepCopy(props.event);
        updatedEvent.locations.forEach((location, index) => (location.order = index + 1));
        emit('update:event', updatedEvent);
    }
}

function deleteLocation(location: Location): void {
    if (props.event && hasPermission(Permission.UPDATE_EVENT_DETAILS)) {
        const updatedEvent = eventService.removeLocation(deepCopy(props.event), location);
        emit('update:event', updatedEvent);
    }
}
</script>
