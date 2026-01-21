<template>
    <VSheet v-if="event" ref="sheet">
        <template #title>{{ event.name }}</template>
        <template #content>
            <div class="xs:px-8 space-y-8 px-4 pb-4 sm:w-120 lg:px-10">
                <VSuccess
                    v-if="event.signedInUserRegistration && event.isSignedInUserAssigned"
                    class="xs:-mx-4 my-4 text-sm"
                    icon="fa-check"
                >
                    <i18n-t tag="span" keypath="components.event-details-sheet.note-assigned">
                        <b>{{ positions.get(event.signedInUserRegistration.positionKey).name }}</b>
                    </i18n-t>
                </VSuccess>
                <VInfo v-else-if="event.signedInUserRegistration" class="xs:-mx-4 my-4 text-sm" icon="fa-hourglass-half">
                    <i18n-t tag="span" keypath="components.event-details-sheet.note-on-waiting-list">
                        <b>{{ positions.get(event.signedInUserRegistration.positionKey).name }}</b>
                    </i18n-t>
                </VInfo>
                <VWarning v-else-if="openPositions.length > 0 && event.state === EventState.Planned" class="xs:-mx-4 my-4 text-sm">
                    {{ $t('components.event-details-sheet.note-missing-crew') }}
                    {{ openPositions.map((it) => it.name).join(', ') }}
                </VWarning>

                <EventDetailsCard :event="event" class="" />
                <EventLocationsCard v-if="event.locations.length > 0" :event="event" class="" />
            </div>
        </template>
        <template #bottom>
            <div class="lg:px-10-lg flex justify-end gap-2 px-8 py-4">
                <RouterLink
                    :to="{ name: Routes.EventDetails, params: { key: event.key } }"
                    class="btn-primary"
                    :title="$t('components.event-details-sheet.show-details')"
                >
                    <span>{{ $t('components.event-details-sheet.show-details') }}</span>
                </RouterLink>
            </div>
        </template>
    </VSheet>
</template>
<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import type { Event, Position } from '@/domain';
import { EventState } from '@/domain';
import { useEventService } from '@/domain/services';
import type { Sheet } from '@/ui/components/common';
import { VInfo, VSheet, VSuccess, VWarning } from '@/ui/components/common';
import EventDetailsCard from '@/ui/components/events/EventDetailsCard.vue';
import EventLocationsCard from '@/ui/components/events/EventLocationsCard.vue';
import { usePositions } from '@/ui/composables/Positions.ts';
import { Routes } from '@/ui/views/Routes.ts';

const route = useRoute();
const positions = usePositions();
const eventService = useEventService();

const event = ref<Event | null>(null);
const sheet = ref<Sheet<Event, Event> | null>(null);

const openPositions = computed<Position[]>(() => {
    if (!event.value) {
        return [];
    }
    const openRequiredSlots = eventService.getOpenSlots(event.value);
    return positions.all.value
        .map((position) => ({
            position: position,
            count: openRequiredSlots.filter((slot) => slot.positionKeys[0] === position.key).length,
        }))
        .filter((pos) => pos.count > 0)
        .map((it) => it.position);
});

function init(): void {
    watch(
        () => route.fullPath,
        () => sheet.value?.submit()
    );
}

async function open(evt: Event): Promise<Event> {
    event.value = evt;
    await nextTick();
    await sheet.value?.open().catch(() => undefined);
    return event.value;
}

defineExpose<Sheet<Event, Event | undefined>>({
    open: (evt: Event) => open(evt),
    close: () => sheet.value?.reject(),
    submit: () => sheet.value?.submit(),
    reject: () => sheet.value?.reject(),
});

init();
</script>
