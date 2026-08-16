<template>
    <hr />
    <template v-if="props.events?.length === 1">
        <!-- Open -->
        <li class="permission-read-events block">
            <RouterLink
                :to="{
                    name: Routes.EventDetails,
                    params: { year: props.events[0].start.getFullYear(), key: props.events[0].key },
                }"
                class="context-menu-item"
                data-test-id="action-view"
            >
                <i class="fa-solid fa-search" />
                <span>{{ $t('domain.event.actions.view') }}</span>
            </RouterLink>
        </li>
    </template>
    <template v-if="props.events">
        <!-- Contact crew -->
        <li
            class="permission-write-event-details context-menu-item"
            data-test-id="action-edit"
            @click="emit('update-events:edit', props.events)"
        >
            <i class="fa-solid fa-compass-drafting" />
            <span>{{ $t('domain.event.actions.edit') }}</span>
        </li>

        <!-- Transition event state DRAFT -> OPEN_FOR_SIGNUP -->
        <li
            class="permission-write-event-details context-menu-item"
            data-test-id="action-open-for-signup"
            :class="{ disabled: !hasAnyDraftEvents }"
            @click="emit('update-events:open-for-signup', props.events)"
        >
            <i class="fa-solid fa-people-group" />
            <span>{{ $t('domain.event.actions.start-crew-signup') }}</span>
        </li>
        <!-- Transition event state OPEN_FOR_SIGNUP -> PLANNED -->
        <li
            class="permission-write-event-details context-menu-item"
            data-test-id="action-publish-crew"
            :class="{ disabled: !hasAnyOpenForSignUpEvents }"
            @click="emit('update-events:publish-crew', props.events)"
        >
            <i class="fa-solid fa-earth-europe" />
            <span>{{ $t('domain.event.actions.publish-crew') }}</span>
        </li>
        <!-- Transition event state to CANCELED -->
        <li
            class="permission-delete-events context-menu-item text-error"
            data-test-id="action-cancel"
            @click="emit('update-events:cancel', props.events)"
        >
            <i class="fa-solid fa-ban" />
            <span>{{ $t('domain.event.actions.cancel', props.events.length) }}</span>
        </li>
        <!-- Delete -->
        <li
            class="permission-delete-events context-menu-item text-error"
            data-test-id="action-delete"
            @click="emit('update-events:delete', props.events)"
        >
            <i class="fa-solid fa-trash-alt" />
            <span>{{ $t('domain.event.actions.delete', props.events.length) }}</span>
        </li>

        <hr />

        <!-- Contact crew -->
        <li class="permission-read-user-details context-menu-item" data-test-id="action-contact-crew" @click="contactCrew(props.events)">
            <i class="fa-solid fa-envelope" />
            <span>{{ $t('domain.event.actions.contact-crew') }}</span>
        </li>

        <!-- Add registration -->
        <li
            class="permission-write-registrations context-menu-item"
            data-test-id="action-create-registration"
            @click="emit('update-events:create-registration', props.events)"
        >
            <i class="fa-solid fa-user-plus" />
            <span>{{ $t('domain.registration.actions.create') }}</span>
        </li>

        <!-- Excel exports -->
        <template v-if="eventExports.templates.value.length > 0">
            <hr />
            <li
                v-for="template in eventExports.templates.value"
                :key="template"
                class="permission-export-events context-menu-item"
                data-test-id="action-export"
                @click="exportEvents(props.events, template)"
            >
                <i class="fa-solid fa-file-excel" />
                <span>{{ $t('domain.event.actions.export-to-template', { template }) }}</span>
            </li>
        </template>
    </template>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Event } from '@/domain';
import { useEventService, EventState } from '@/domain';
import { filterUndefined } from '@/common';
import { Routes } from '@/ui/views/Routes';
import { useUserAdministrationUseCase, useUsersUseCase } from '@/application';
import { useEventExports } from '@/ui/composables/EventExports';

interface Props {
    events?: Event[];
}

interface Emits {
    (e: 'update-events:edit', events: Event[]): void;
    (e: 'update-events:open-for-signup', events: Event[]): void;
    (e: 'update-events:publish-crew', events: Event[]): void;
    (e: 'update-events:cancel', events: Event[]): void;
    (e: 'update-events:delete', events: Event[]): void;
    (e: 'update-events:create-registration', events: Event[]): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const eventExports = useEventExports();
const userAdminUseCase = useUserAdministrationUseCase();
const usersUseCase = useUsersUseCase();
const eventService = useEventService();

const hasAnyDraftEvents = computed<boolean>(() => {
    return props.events !== undefined && props.events.some((it) => it.state === EventState.Draft);
});

const hasAnyOpenForSignUpEvents = computed<boolean>(() => {
    return props.events !== undefined && props.events.some((it) => it.state === EventState.OpenForSignup);
});

async function contactCrew(events: Event[]): Promise<void> {
    const userKeys = events
        .flatMap((event) => eventService.getAssignedRegistrations(event))
        .map((it) => it.userKey)
        .filter(filterUndefined);
    const users = await usersUseCase.getUsers(userKeys);
    await userAdminUseCase.contactUsers(users);
}

async function exportEvents(events: Event[], template: string): Promise<void> {
    for (const event of events) {
        await eventExports.exportEvent(event, template);
    }
}
</script>
