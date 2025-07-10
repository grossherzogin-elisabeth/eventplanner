<template>
    <section>
        <h2 class="mb-2 font-bold text-secondary">
            {{ $t('app.event-details.title') }}
        </h2>
        <div class="space-y-1 rounded-2xl bg-surface-container-low p-4">
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
                <span>{{ props.event.days }} Tage</span>
            </p>
            <p class="flex items-center space-x-4">
                <i class="fa-solid fa-users w-4" />
                <span v-if="props.event.state === EventState.OpenForSignup"> {{ props.event.registrations.length }} Anmeldungen </span>
                <span v-else-if="props.event.assignedUserCount && waitingListCount">
                    {{ props.event.assignedUserCount }} Crew, {{ waitingListCount }} Warteliste
                </span>
                <span v-else-if="props.event.assignedUserCount"> {{ props.event.assignedUserCount }} Crew </span>
                <span v-else> {{ props.event.registrations.length }} Anmeldungen </span>
            </p>
            <p v-if="props.event.description" class="flex items-center space-x-4">
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
