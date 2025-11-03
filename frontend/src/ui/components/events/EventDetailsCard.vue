<template>
    <section>
        <h2 class="text-secondary mb-2 font-bold">
            {{ $t('components.event-details-card.title') }}
        </h2>
        <div class="bg-surface-container/50 xs:-mx-4 space-y-1 rounded-2xl p-4 shadow-sm">
            <p class="flex items-center space-x-4">
                <i class="fa-solid fa-calendar-day w-4" />
                <span>{{ $d(props.event.start, DateTimeFormat.DDD_DD_MM_hh_mm) }}</span>
            </p>
            <p class="flex items-center space-x-4">
                <i class="fa-solid fa-flag-checkered w-4" />
                <span>{{ $d(props.event.end, DateTimeFormat.DDD_DD_MM_hh_mm) }}</span>
            </p>
            <p class="flex items-center space-x-4">
                <i class="fa-solid fa-route w-4" />
                <span>
                    {{ $t('components.event-details-card.days', { count: props.event.days }) }}
                </span>
            </p>
            <p class="flex items-center space-x-4">
                <i class="fa-solid fa-users w-4" />
                <span v-if="props.event.state === EventState.OpenForSignup">
                    {{ $t('components.event-details-card.registrations', { count: props.event.registrations.length }) }}
                </span>
                <span v-else-if="props.event.assignedUserCount && waitingListCount">
                    {{ $t('components.event-details-card.assigned', { count: props.event.assignedUserCount }) }},
                    {{ $t('components.event-details-card.waitinglist', { count: waitingListCount }) }}
                </span>
                <span v-else-if="props.event.assignedUserCount">
                    {{ $t('components.event-details-card.assigned', { count: props.event.assignedUserCount }) }}
                </span>
                <span v-else>
                    {{ $t('components.event-details-card.registrations', { count: props.event.registrations.length }) }}
                </span>
            </p>
            <VMarkdown v-if="props.event.description" :value="props.event.description" class="pt-4 text-sm" />
        </div>
    </section>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { DateTimeFormat } from '@/common/date';
import { type Event, EventState } from '@/domain';
import { VMarkdown } from '@/ui/components/common';

interface Props {
    event: Event;
}
const props = defineProps<Props>();

const waitingListCount = computed<number>(() => {
    if (!props.event) return 0;
    return props.event.registrations.length - props.event.assignedUserCount;
});
</script>
