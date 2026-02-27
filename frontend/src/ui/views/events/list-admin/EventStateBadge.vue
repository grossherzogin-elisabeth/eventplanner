<template>
    <div class="flex items-center justify-end">
        <VTooltip v-if="unassignedRequiredPositions.length > 0 || unassignedOptionalPositions.length > 0" :delay="10">
            <template #default>
                <div :key="state.icon" class="status-badge" :class="state.color">
                    <i class="fa-solid w-4" :class="state.icon"></i>
                    <span class="font-semibold whitespace-nowrap">{{ state.name }}</span>
                </div>
            </template>
            <template #tooltip>
                <div class="bg-surface-container-high text-onsurface flex flex-col gap-8 rounded-xl p-4 text-sm shadow-xl">
                    <div v-if="unassignedRequiredPositions.length > 0">
                        <h4 class="mb-2 font-bold">{{ $t('views.event-admin-list.state.missing-crew') }}</h4>
                        <div class="flex flex-wrap gap-2">
                            <span
                                v-for="position in unassignedRequiredPositions"
                                :key="position.key"
                                class="tag custom flex gap-1"
                                :style="{ '--color': position.color }"
                            >
                                {{ position.name }}
                                <span
                                    class="flex h-4 w-4 items-center justify-center rounded-full bg-white/10 px-1 text-center text-xs whitespace-nowrap"
                                >
                                    {{ position.count }}
                                </span>
                            </span>
                        </div>
                    </div>
                    <div v-if="unassignedOptionalPositions.length > 0">
                        <h4 class="mb-2 font-bold">{{ $t('views.event-admin-list.state.free-slots-for') }}</h4>
                        <div class="flex flex-wrap gap-2">
                            <span
                                v-for="position in unassignedOptionalPositions"
                                :key="position.key"
                                class="tag custom flex gap-1"
                                :style="{ '--color': position.color }"
                            >
                                {{ position.name }}
                                <span
                                    class="flex h-4 w-4 items-center justify-center rounded-full bg-white/10 px-1 pt-0.5 text-center text-xs whitespace-nowrap"
                                >
                                    {{ position.count }}
                                </span>
                            </span>
                        </div>
                    </div>
                </div>
            </template>
        </VTooltip>
        <div v-else :key="state.icon" class="status-badge" :class="state.color">
            <i class="fa-solid" :class="state.icon"></i>
            <span>{{ state.name }}</span>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { Event, Position, Slot } from '@/domain';
import { EventState, SlotCriticality } from '@/domain';
import { useEventService } from '@/domain/services';
import { VTooltip } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';

interface OpenPositionsCounter extends Position {
    count: number;
}

interface StateDetails {
    name: string;
    color: string;
    icon: string;
    iconMobile?: string;
}

interface Props {
    event: Event;
}
const props = defineProps<Props>();

const eventService = useEventService();
const positions = usePositions();
const { t } = useI18n();

const openSlots = computed<Slot[]>(() => eventService.getOpenSlots(props.event));
const openRequiredSlots = computed<Slot[]>(() => openSlots.value.filter((slot) => slot.criticality !== SlotCriticality.Optional));
const openOptionalSlots = computed<Slot[]>(() => openSlots.value.filter((slot) => slot.criticality === SlotCriticality.Optional));

const unassignedRequiredPositions = computed<OpenPositionsCounter[]>(() =>
    positions.all.value
        .map((pos) => ({ ...pos, count: openRequiredSlots.value.filter((slot) => slot.positionKeys[0] === pos.key).length }))
        .filter((pos) => pos.count > 0)
);
const unassignedOptionalPositions = computed<OpenPositionsCounter[]>(() =>
    positions.all.value
        .map((pos) => ({ ...pos, count: openOptionalSlots.value.filter((slot) => slot.positionKeys[0] === pos.key).length }))
        .filter((pos) => pos.count > 0)
);

const state = computed<StateDetails>(() => {
    switch (props.event.state) {
        case EventState.Draft:
            return { name: t('domain.event-state.draft'), icon: 'fa-compass-drafting', color: 'neutral' };
        case EventState.OpenForSignup:
            return { name: t('domain.event-state.open-for-signup'), icon: 'fa-people-group', color: 'neutral' };
        case EventState.Canceled:
            return { name: t('domain.event-state.canceled'), icon: 'fa-ban', color: 'error' };
    }
    if (openRequiredSlots.value.length > 0) {
        return { name: t('views.event-admin-list.state.missing-crew'), icon: 'fa-warning', color: 'warning' };
    }
    if (openSlots.value.length > 0) {
        return {
            name: t('domain.event-state.open-slots'),
            icon: 'fa-info-circle',
            iconMobile: 'fa-info',
            color: 'info',
        };
    }
    return {
        name: t('domain.event-state.full'),
        icon: 'fa-check-circle',
        iconMobile: 'fa-check',
        color: 'success',
    };
});
</script>
