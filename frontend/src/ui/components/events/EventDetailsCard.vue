<template>
    <section>
        <h2 class="mb-2 font-bold text-secondary">
            {{ $t('components.event-details-card.title') }}
        </h2>
        <div class="space-y-1 rounded-2xl bg-surface-container bg-opacity-50 p-4 shadow xs:-mx-4">
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
            <p v-if="props.event.description" class="flex items-baseline space-x-4">
                <i class="fa-solid fa-info-circle w-4" />
                <span>{{ props.event.description }} </span>
            </p>
        </div>
    </section>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { DateTimeFormat } from '@/common/date';
import { type Event, EventState } from '@/domain';

interface Props {
    event: Event;
}
const props = defineProps<Props>();

const waitingListCount = computed<number>(() => {
    if (!props.event) return 0;
    return props.event.registrations.length - props.event.assignedUserCount;
});
</script>
