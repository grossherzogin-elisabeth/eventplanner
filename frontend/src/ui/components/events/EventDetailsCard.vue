<template>
    <section>
        <h2 class="text-secondary mb-2 font-bold">
            {{ $t('components.event-details-card.title') }}
        </h2>
        <VInteractiveList>
            <VListItem
                icon="fa-calendar-day"
                :label="$t('domain.event.time')"
                :content="formatTimeRange(props.event.start, props.event.end)"
            />
            <VListItem
                icon="fa-users"
                :label="$t('domain.event.crew')"
                :content="$t('components.event-details-card.registrations', { count: props.event.registrations.length })"
            >
                <template #default>
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
                </template>
            </VListItem>
            <VListItem icon="fa-shapes" :label="$t('domain.event.type')" :content="eventTypes.getName(props.event.type)" />
            <VListItem
                icon="fa-user-check"
                :label="$t('domain.event.signup-type')"
                :content="eventSignupTypes.getName(props.event.signupType)"
            />
            <VListItem v-if="props.event.description" icon="fa-file-lines" :label="$t('domain.event.description')">
                <template #default>
                    <VMarkdown :value="props.event.description" />
                </template>
            </VListItem>
        </VInteractiveList>
    </section>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { type Event, EventState } from '@/domain';
import { VInteractiveList, VListItem, VMarkdown } from '@/ui/components/common';
import { formatTimeRange } from '@/ui/composables/DateRangeFormatter.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { useEventSignupTypes } from '@/ui/composables/EventSignupTypes.ts';

interface Props {
    event: Event;
}
const props = defineProps<Props>();

const eventTypes = useEventTypes();
const eventSignupTypes = useEventSignupTypes();

const waitingListCount = computed<number>(() => {
    if (!props.event) return 0;
    return props.event.registrations.length - props.event.assignedUserCount;
});
</script>

<style>
.no-header .header {
    display: none;
}
</style>
