<template>
    <div class="full-width-scrollable">
        <VTable
            :items="renderedEvents"
            :page-size="-1"
            class="scrollbar-invisible interactive-table no-header"
            @click="openEvent($event.item, $event.event)"
        >
            <template #icon="{ item }">
                <span v-if="item?.isAssigned" class="bg-success-container/25 flex h-full w-full items-center justify-center rounded-full">
                    <i class="fa-solid fa-user-check text-onsuccess-container/75"></i>
                </span>
                <span v-else class="bg-warning-container flex h-full w-full items-center justify-center rounded-full">
                    <i class="fa-solid fa-user-clock text-onwarning-container"></i>
                </span>
            </template>
            <template #row="{ item }">
                <UserEventsTableRow
                    :event="item?.event"
                    :registration="item?.registration"
                    :position="item?.position"
                    :position-name="item?.positionName"
                    :is-assigned="item?.isAssigned"
                />
            </template>
            <template v-if="hasPermission(Permission.UPDATE_USERS)" #context-menu="{ item }">
                <li>
                    <RouterLink
                        :to="{
                            name: Routes.EventDetails,
                            params: { year: item.event.start.getFullYear(), key: item.event.key },
                        }"
                        data-test-id="action-view-event"
                        class="context-menu-item"
                    >
                        <i class="fa-solid fa-search" />
                        <span>{{ $t('domain.event.actions.view') }}</span>
                    </RouterLink>
                </li>
                <li v-if="hasPermission(Permission.UPDATE_EVENTS)">
                    <RouterLink
                        :to="{
                            name: Routes.EventEdit,
                            params: { year: item.event.start.getFullYear(), key: item.event.key },
                        }"
                        data-test-id="action-edit-event"
                        class="context-menu-item"
                    >
                        <i class="fa-solid fa-drafting-compass" />
                        <span>{{ $t('domain.event.actions.edit') }}</span>
                    </RouterLink>
                </li>
                <li
                    v-if="!item.isAssigned"
                    data-test-id="action-add-to-crew"
                    class="context-menu-item"
                    :class="{ disabled: item.event.isInPast }"
                    @click="addUserToCrew(item)"
                >
                    <i class="fa-solid fa-user-plus" />
                    <span>{{ $t('domain.registration.actions.assign') }}</span>
                </li>
                <li
                    class="context-menu-item text-error"
                    data-test-id="action-delete-registration"
                    :class="{ disabled: item.event.isInPast }"
                    @click="deleteRegistration(item)"
                >
                    <i class="fa-solid fa-trash-alt" />
                    <span>{{ $t('domain.registration.actions.delete') }}</span>
                </li>
            </template>
        </VTable>
    </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRouter } from 'vue-router';
import { useErrorHandlingService, useEventAdministrationUseCase } from '@/application';
import { filterUndefined } from '@/common';
import type { Event, Position, Registration, UserDetails } from '@/domain';
import { EventSignupType, Permission, useEventService } from '@/domain';
import { VTable } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions';
import { useSession } from '@/ui/composables/Session';
import { Routes } from '@/ui/views/Routes';
import UserEventsTableRow from '@/ui/views/users/details/components/UserEventsTableRow.vue';

export interface EventTableViewItem {
    event: Event;
    registration: Registration;
    position: Position;
    positionName: string;
    isAssigned: boolean;
}

interface Props {
    events?: Event[];
    user: UserDetails;
}

type Emits = (e: 'update:events', value: Event[]) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const router = useRouter();
const { hasPermission } = useSession();
const eventService = useEventService();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const errorHandling = useErrorHandlingService();
const positions = usePositions();

const renderedEvents = computed<EventTableViewItem[] | undefined>(() => {
    return props.events
        ?.map((evt) => {
            const registration = evt.registrations.find((it) => it.userKey === props.user.key);
            const slot = evt.slots.find((it) => it.assignedRegistrationKey === registration?.key);
            const position = positions.get(registration?.positionKey || '');
            if (registration && position) {
                return {
                    event: evt,
                    registration: registration,
                    positionName: slot?.positionName || position.name,
                    position: position,
                    isAssigned: slot != undefined || evt.signupType === EventSignupType.Open,
                };
            }
            console.warn('Failed to get users position');
            return undefined;
        })
        .filter(filterUndefined);
});

async function addUserToCrew(item: EventTableViewItem): Promise<void> {
    try {
        let event = props.events?.find((it) => it.key === item.event.key);
        if (!event) {
            throw new Error('Veranstaltung konnte nicht gefunden werden');
        }
        const slot = eventService.getOpenSlots(event).find((it) => it.positionKeys.includes(item.position.key));
        if (slot) {
            event = await eventAdministrationUseCase.assignUserToSlot(event, props.user, slot.key);
            await eventAdministrationUseCase.updateEvent(event.key, event);
            item.event.assignedUserCount = item.event.assignedUserCount + 1;
            item.event.waitingListCount = item.event.waitingListCount - 1;
        } else {
            throw new Error(`Die Veranstaltung hat keinen passenden freien Slot für die Position ${item.positionName}`);
        }
    } catch (e) {
        errorHandling.handleRawError(e);
    }
}

async function deleteRegistration(item: EventTableViewItem): Promise<void> {
    try {
        let event = props.events?.find((it) => it.key === item.event.key);
        if (!event) {
            throw new Error('Veranstaltung konnte nicht gefunden werden');
        }
        event = eventService.cancelUserRegistration(event, props.user.key);
        await eventAdministrationUseCase.updateEvent(event.key, event);
        const updatedEvents = props.events || [];
        const index = updatedEvents.findIndex((it) => it.key === item.event.key);
        updatedEvents.splice(index, 1);
        emit('update:events', updatedEvents);
    } catch (e) {
        errorHandling.handleRawError(e);
    }
}

async function openEvent(item: EventTableViewItem, evt: MouseEvent): Promise<void> {
    let to: RouteLocationRaw = {
        name: Routes.EventDetails,
        params: { year: item.event.start.getFullYear(), key: item.event.key },
    };
    if (hasPermission(Permission.UPDATE_EVENTS)) {
        to = {
            name: Routes.EventEdit,
            params: { year: item.event.start.getFullYear(), key: item.event.key },
        };
    }
    if (evt.metaKey || evt.ctrlKey) {
        window.open(router.resolve(to).href, '_blank');
    } else {
        await router.push(to);
    }
}
</script>
