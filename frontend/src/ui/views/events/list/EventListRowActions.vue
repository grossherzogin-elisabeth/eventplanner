<template>
    <hr />
    <template v-if="props.events">
        <template v-if="props.events?.length === 1">
            <li class="permission-read-events block">
                <RouterLink
                    :to="{
                        name: Routes.EventDetails,
                        params: { year: props.events[0].start.getFullYear(), key: props.events[0].key },
                    }"
                    class="context-menu-item"
                >
                    <i class="fa-solid fa-search" />
                    <span>{{ $t('domain.event.actions.view') }}</span>
                </RouterLink>
            </li>
            <li class="permission-write-events block">
                <RouterLink
                    :to="{
                        name: Routes.EventEdit,
                        params: { year: props.events[0].start.getFullYear(), key: props.events[0].key },
                    }"
                    class="context-menu-item"
                >
                    <i class="fa-solid fa-compass-drafting" />
                    <span>{{ $t('domain.event.actions.edit') }}</span>
                </RouterLink>
            </li>
        </template>
        <li class="context-menu-item" @click="eventUseCase.downloadCalendarEntries(props.events)">
            <i class="fa-solid fa-calendar-alt" />
            <span>{{ $t('domain.event.actions.create-calendar-entry') }}</span>
        </li>
        <li
            class="permission-write-own-registrations context-menu-item"
            :class="{ disabled: joinableEvents === 0 }"
            @click="emit('join', props.events)"
        >
            <i class="fa-solid fa-user-plus" />
            <span>{{ $t('domain.event.actions.sign-up') }}</span>
        </li>
        <li
            class="permission-write-own-registrations context-menu-item"
            :class="{ disabled: eventsWithUserOnWaitingList === 0 }"
            @click="emit('leave-waiting-list', props.events)"
        >
            <i class="fa-solid fa-user-minus" />
            <span>{{ $t('domain.event.actions.leave-waiting-list') }}</span>
        </li>
        <li
            class="permission-write-own-registrations context-menu-item text-error"
            :class="{ disabled: eventsWithUserInCrew === 0 }"
            @click="emit('leave-all', props.events)"
        >
            <i class="fa-solid fa-ban" />
            <span>{{ $t('domain.event.actions.cancel') }}</span>
        </li>
        <template v-if="props.events?.length === 1 && eventExports.templates.value.length > 0">
            <hr />
            <li
                v-for="template in eventExports.templates.value"
                :key="template"
                class="permission-export-events context-menu-item"
                data-test-id="action-export"
                @click="eventExports.exportEvent(props.events[0], template)"
            >
                <i class="fa-solid fa-file-excel" />
                <span>{{ $t('domain.event.actions.export-to-template', { template }) }}</span>
            </li>
        </template>
    </template>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import type { Event } from '@/domain';
import { useEventUseCase } from '@/application';
import { useEventExports } from '@/ui/composables/EventExports';
import { Routes } from '@/ui/views/Routes';

interface Emits {
    (e: 'join', events: Event[]): void;
    (e: 'leave-all', events: Event[]): void;
    (e: 'leave-waiting-list', events: Event[]): void;
}

interface Props {
    events?: Event[];
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const eventExports = useEventExports();
const eventUseCase = useEventUseCase();

const futureEvents = computed<Event[]>(() => {
    const now = Date.now();
    return props.events?.filter((it) => it.start.getTime() > now) ?? [];
});

const joinableEvents = computed<number>(() => {
    return futureEvents.value.filter((it) => it.canSignedInUserJoin).length;
});

const eventsWithUserOnWaitingList = computed<number>(() => {
    return futureEvents.value.filter((it) => it.signedInUserRegistration && !it.isSignedInUserAssigned).length;
});

const eventsWithUserInCrew = computed<number>(() => {
    return futureEvents.value.filter((it) => it.signedInUserRegistration && it.isSignedInUserAssigned).length;
});
</script>
