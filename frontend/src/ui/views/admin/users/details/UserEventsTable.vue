<template>
    <div class="scrollbar-invisible -mx-8 overflow-x-auto px-8 md:-mx-16 md:px-16 xl:-mx-20 xl:px-20">
        <VTable :items="renderedEvents" :page-size="-1" class="interactive-table no-header" @click="openEvent($event)">
            <template #row="{ item }">
                <td class="w-16 text-xl opacity-50">
                    <i v-if="!item.waitingList" class="fa-solid fa-check-circle text-green-700"></i>
                    <i v-else class="fa-solid fa-clock text-gray-500"></i>
                </td>
                <td class="w-1/2 max-w-[65vw] border-none font-semibold">
                    <p class="mb-1 truncate">{{ item.name }}</p>
                    <p class="text-sm font-light">{{ item.locations }}</p>
                </td>
                <td class="w-1/6 whitespace-nowrap text-center">
                    <p class="mb-1 font-semibold">
                        {{ item.crewCount }}
                        <span v-if="item.waitingListCount" class="opacity-40"> +{{ item.waitingListCount }} </span>
                    </p>
                    <p class="text-sm">Crew</p>
                </td>
                <td class="w-2/6 whitespace-nowrap">
                    <p class="mb-1 font-semibold">{{ formatDateRange(item.start, item.end) }}</p>
                    <p class="text-sm">{{ item.duration }} Tage</p>
                </td>
                <td class="w-1/6">
                    <div :style="{ background: item.position.color }" class="position inline-flex">
                        <span class="px-2 text-sm">{{ item.positionName }}</span>
                    </div>
                </td>
                <td class="">
                    <ContextMenuButton>
                        <ul>
                            <li>
                                <RouterLink
                                    :to="{
                                        name: Routes.EventDetails,
                                        params: { year: item.start.getFullYear(), key: item.eventKey },
                                    }"
                                    class="context-menu-item"
                                >
                                    <i class="fa-solid fa-search" />
                                    <span>Reise anzeigen</span>
                                </RouterLink>
                            </li>
                            <li v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)">
                                <RouterLink
                                    :to="{
                                        name: Routes.EventEdit,
                                        params: { year: item.start.getFullYear(), key: item.eventKey },
                                    }"
                                    class="context-menu-item"
                                >
                                    <i class="fa-solid fa-edit" />
                                    <span>Reise bearbeiten</span>
                                </RouterLink>
                            </li>
                            <li
                                class="context-menu-item"
                                :class="{ disabled: item.inPast || !item.waitingList }"
                                @click="addUserToCrew(item)"
                            >
                                <i class="fa-solid fa-user-plus" />
                                <span>Zur Crew hinzufügen</span>
                            </li>
                            <li
                                class="context-menu-item text-red-700"
                                :class="{ disabled: item.inPast }"
                                @click="deleteRegistration(item)"
                            >
                                <i class="fa-solid fa-trash" />
                                <span>Anmeldung löschen</span>
                            </li>
                        </ul>
                    </ContextMenuButton>
                </td>
            </template>
        </VTable>
    </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { ArrayUtils } from '@/common';
import type { Event, Position, PositionKey, UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { ContextMenuButton, VTable } from '@/ui/components/common';
import { useAuthUseCase, useErrorHandling, useEventAdministrationUseCase } from '@/ui/composables/Application';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter';
import { useEventService } from '@/ui/composables/Domain';
import { Routes } from '@/ui/views/Routes';

export interface EventTableViewItem {
    eventKey: string;
    name: string;
    locations: string;
    start: Date;
    end: Date;
    duration: number;
    position: Position;
    positionName?: string;
    waitingList: boolean;
    inPast: boolean;
    crewCount: number;
    waitingListCount: number;
}

interface Props {
    events: Event[];
    positions: Map<PositionKey, Position>;
    user: UserDetails;
}

const props = defineProps<Props>();

const router = useRouter();
const authUseCase = useAuthUseCase();
const eventService = useEventService();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const errorHandling = useErrorHandling();
const signedInUser = authUseCase.getSignedInUser();

const renderedEvents = computed<EventTableViewItem[]>(() => {
    return props.events
        .map((evt) => {
            const registration = evt.registrations.find((it) => it.userKey === props.user.key);
            const slot = evt.slots.find((it) => it.key === registration?.slotKey);
            const position = props.positions.get(registration?.positionKey || '');
            if (position) {
                return {
                    eventKey: evt.key,
                    name: evt.name,
                    start: evt.start,
                    end: evt.end,
                    duration: new Date(evt.end.getTime() - evt.start.getTime()).getDate(),
                    position: position,
                    positionName: slot?.positionName || position.name,
                    waitingList: slot === undefined,
                    locations: evt.locations.map((it) => it.name).join(' - '),
                    crewCount: evt.assignedUserCount,
                    inPast: evt.start.getTime() < new Date().getTime(),
                    waitingListCount: evt.registrations.length - evt.assignedUserCount,
                };
            }
            console.warn('Failed to get users position');
            return undefined;
        })
        .filter(ArrayUtils.filterUndefined);
});

async function addUserToCrew(item: EventTableViewItem): Promise<void> {
    try {
        let event = props.events.find((it) => it.key === item.eventKey);
        if (!event) {
            throw new Error('Reise konnte nicht gefunden werden');
        }
        const slot = eventService.getOpenSlots(event).find((it) => it.positionKeys.includes(item.position.key));
        if (slot) {
            event = eventService.assignUserToSlot(event, props.user, slot.key);
            await eventAdministrationUseCase.updateEvent(event.key, event);
            item.crewCount = item.crewCount + 1;
            item.waitingListCount = item.waitingListCount - 1;
        } else {
            throw new Error(`Die Reise hat keinen passenden freien Slot für die Position ${item.positionName}`);
        }
    } catch (e) {
        errorHandling.handleRawError(e);
    }
}

async function deleteRegistration(item: EventTableViewItem): Promise<void> {
    try {
        let event = props.events.find((it) => it.key === item.eventKey);
        if (!event) {
            throw new Error('Reise konnte nicht gefunden werden');
        }
        event = eventService.cancelUserRegistration(event, props.user.key);
        await eventAdministrationUseCase.updateEvent(event.key, event);
        if (item.waitingListCount) {
            item.waitingListCount = item.waitingListCount - 1;
        } else {
            item.crewCount = item.crewCount - 1;
        }
    } catch (e) {
        errorHandling.handleRawError(e);
    }
}

async function openEvent(event: EventTableViewItem): Promise<void> {
    if (signedInUser.permissions.includes(Permission.WRITE_EVENTS)) {
        await router.push({
            name: Routes.EventEdit,
            params: { year: event.start.getFullYear(), key: event.eventKey },
        });
    } else {
        await router.push({
            name: Routes.EventDetails,
            params: { year: event.start.getFullYear(), key: event.eventKey },
        });
    }
}
</script>
