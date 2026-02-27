<template>
    <section v-if="content" class="event-state-banner" :class="{ 'sticky md:static': props.sticky }">
        <component :is="content.type">
            <i18n-t tag="span" :keypath="content.message">
                <template #signedInUserPosition>
                    <b>{{ positions.get(props.event.signedInUserRegistration?.positionKey).name }}</b>
                </template>
                <template #openPositions>
                    <b>{{ openPositions.map((it) => it.name).join(', ') }}</b>
                </template>
            </i18n-t>
        </component>
    </section>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { Event, Position } from '@/domain';
import { EventSignupType, EventState, useEventService } from '@/domain';
import { VInfo, VSuccess, VWarning } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';

interface StateBannerContent {
    type: typeof VInfo | typeof VSuccess | typeof VWarning;
    message: string;
}

interface Props {
    event: Event;
    sticky?: boolean;
}
const props = defineProps<Props>();

const positions = usePositions();
const eventService = useEventService();

const openPositions = computed<Position[]>(() => {
    const openRequiredSlots = eventService.getOpenSlots(props.event);
    return positions.all.value
        .map((position) => ({
            position: position,
            count: openRequiredSlots.filter((slot) => slot.positionKeys[0] === position.key).length,
        }))
        .filter((pos) => pos.count > 0)
        .map((it) => it.position);
});

const content = computed<StateBannerContent | undefined>(() => {
    // event is still in planning state
    if (props.event.state === EventState.OpenForSignup && props.event.signupType === EventSignupType.Assignment) {
        return { type: VInfo, message: 'views.event-details.info-planning' };
    }
    // event was canceled
    if (props.event.state === EventState.Canceled) {
        return { type: VWarning, message: 'views.event-details.info-canceled' };
    }
    // user is assigned
    if (props.event.signedInUserRegistration && props.event.isSignedInUserAssigned) {
        return { type: VSuccess, message: 'views.event-details.info-assigned' };
    }
    // user is on waiting list
    if (props.event.signedInUserRegistration) {
        return { type: VInfo, message: 'views.event-details.info-waitinglist' };
    }
    // user has no registration on this event, but crew members are missing
    if (openPositions.value.length > 0) {
        return { type: VWarning, message: 'views.event-details.info-missing-crew' };
    }
    // no banner
    return undefined;
});
</script>
<style>
@reference "tailwindcss";

.event-state-banner {
    margin-inline: calc(var(--spacing) * -2);
    top: calc(var(--spacing) * 14);
    z-index: 10;
}

@media (width >= 25rem /* 400px */) {
    .event-state-banner {
        margin-inline: calc(var(--spacing) * -4);
    }
}
</style>
