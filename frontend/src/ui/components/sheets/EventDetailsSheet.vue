<template>
    <VSheet v-if="event" ref="sheet">
        <template #title>
            <h1 class="truncate">
                {{ event.name }}
            </h1>
        </template>
        <template #content>
            <div class="space-y-8 px-8 sm:w-[30rem] lg:px-10">
                <VWarning v-if="openPositions.length > 0" class="-mx-4 my-4 text-sm">
                    Für diese Reise wird noch Crew für die folgenden Positionen gesucht:
                    {{ openPositions.map((it) => it.name).join(', ') }}
                </VWarning>
                <VSuccess
                    v-if="event.signedInUserRegistration && event.signedInUserAssignedSlot"
                    class="-mx-4 my-4 text-sm"
                    icon="fa-check"
                >
                    Du bist für diese Reise als
                    <b>{{ positions.get(event.signedInUserRegistration.positionKey).name }}</b>
                    eingeplant
                </VSuccess>
                <VInfo v-else-if="event.signedInUserRegistration" class="-mx-4 my-4 text-sm" icon="fa-hourglass-half">
                    Du stehst für diese Reise als
                    <b>{{ positions.get(event.signedInUserRegistration.positionKey).name }}</b>
                    auf der Warteliste
                </VInfo>

                <EventDetails :event="event" class="-mx-4" />
                <EventRoute :event="event" class="-mx-4" />
            </div>
        </template>
        <template #bottom>
            <div class="lg:px-10-lg flex justify-end gap-2 px-8 py-4">
                <RouterLink :to="{ name: Routes.EventDetails, params: { key: event.key } }" class="btn-primary" title="Detailansicht">
                    <span>Details anzeigen</span>
                </RouterLink>
            </div>
        </template>
    </VSheet>
</template>
<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import type { Event, Position } from '@/domain';
import type { Sheet } from '@/ui/components/common';
import { VWarning } from '@/ui/components/common';
import { VInfo, VSheet, VSuccess } from '@/ui/components/common';
import EventDetails from '@/ui/components/events/EventDetails.vue';
import EventRoute from '@/ui/components/events/EventRoute.vue';
import { useEventService } from '@/ui/composables/Domain.ts';
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
