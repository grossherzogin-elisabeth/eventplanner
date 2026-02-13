<template>
    <VSheet v-if="event" ref="sheet">
        <template #title>{{ event.name }}</template>
        <template #content>
            <div class="xs:px-8 space-y-8 px-4 pb-4 sm:w-120 lg:px-10">
                <EventStateBanner :event="event" />
                <EventDetailsCard :event="event" />
                <EventLocationsCard v-if="event.locations.length > 0" :event="event" />
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
import { nextTick, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import type { Event } from '@/domain';
import type { Sheet } from '@/ui/components/common';
import { VSheet } from '@/ui/components/common';
import EventDetailsCard from '@/ui/components/events/EventDetailsCard.vue';
import EventLocationsCard from '@/ui/components/events/EventLocationsCard.vue';
import EventStateBanner from '@/ui/components/events/EventStateBanner.vue';
import { Routes } from '@/ui/views/Routes.ts';

const route = useRoute();

const event = ref<Event | null>(null);
const sheet = ref<Sheet<Event, Event> | null>(null);

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
