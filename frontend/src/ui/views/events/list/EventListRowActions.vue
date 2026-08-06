<template>
    <li class="permission-read-events">
        <RouterLink
            :to="{
                name: Routes.EventDetails,
                params: { year: props.event.start.getFullYear(), key: props.event.key },
            }"
            class="context-menu-item"
        >
            <i class="fa-solid fa-search" />
            <span>{{ $t('views.event-list.action.link-event-details') }}</span>
        </RouterLink>
    </li>
    <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntry(props.event)">
        <i class="fa-solid fa-calendar-alt" />
        <span>{{ $t('views.event-list.action.create-calendar-entry') }}</span>
    </li>
    <li
        v-for="template in eventExports.templates.value"
        :key="template"
        class="permission-export-events context-menu-item"
        data-test-id="action-export"
        @click="eventExports.exportEvent(props.event, template)"
    >
        <i class="fa-solid fa-file-excel" />
        <span>{{ $t('domain.event.actions.export-to-template', { template }) }}</span>
    </li>
    <template v-if="!props.event.signedInUserRegistration">
        <li
            class="permission-write-own-registrations context-menu-item"
            :class="{ disabled: !props.event.canSignedInUserJoin }"
            @click="emit('join', props.event)"
        >
            <i class="fa-solid fa-user-plus" />
            <span>{{ $t('views.event-list.action.signup') }}</span>
        </li>
    </template>
    <li
        v-if="props.event.isSignedInUserAssigned"
        class="permission-write-own-registrations context-menu-item text-error"
        :class="{ disabled: isPastEvent }"
        @click="emit('leave', props.event)"
    >
        <i class="fa-solid fa-ban" />
        <span>{{ $t('views.event-list.action.cancel') }}</span>
    </li>
    <li
        v-else-if="props.event.signedInUserRegistration"
        class="permission-write-own-registrations context-menu-item"
        :class="{ disabled: isPastEvent }"
        @click="emit('leave', props.event)"
    >
        <i class="fa-solid fa-user-minus" />
        <span>{{ $t('views.event-list.action.leave-waitinglist') }}</span>
    </li>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import type { Event } from '@/domain';
import { useEventUseCase } from '@/application';
import { useEventExports } from '@/ui/composables/EventExports';
import { Routes } from '@/ui/views/Routes';

interface Emits {
    (e: 'join', value: Event): void;
    (e: 'leave', value: Event): void;
}

interface Props {
    event: Event;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const eventExports = useEventExports();
const eventUseCase = useEventUseCase();

const isPastEvent = computed<boolean>(() => {
    return props.event.start.getTime() < Date.now();
});
</script>
