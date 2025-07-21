<template>
    <div class="flex items-center justify-end">
        <VTooltip v-if="unassignedRequiredPositions.length > 0 || unassignedOptionalPositions.length > 0" :delay="10">
            <template #default>
                <div :key="state.icon" class="status-panel" :class="state.color">
                    <i class="fa-solid w-4" :class="state.icon"></i>
                    <span class="whitespace-nowrap font-semibold">{{ state.name }}</span>
                </div>
            </template>
            <template #tooltip>
                <div class="flex flex-col gap-8 rounded-xl bg-surface-container-high p-4 text-sm text-onsurface shadow-xl">
                    <div v-if="unassignedRequiredPositions.length > 0">
                        <h4 class="mb-2 font-bold">Noch zu besetzende Slots</h4>
                        <div class="flex flex-wrap gap-2">
                            <div
                                v-for="position in unassignedRequiredPositions"
                                :key="position.key"
                                class="position flex gap-2"
                                :style="{ backgroundColor: position.color }"
                            >
                                {{ position.name }}
                                <span
                                    class="flex h-5 w-5 items-center justify-center whitespace-nowrap rounded-full bg-white bg-opacity-25 px-1 text-center text-xs"
                                >
                                    {{ position.count }}
                                </span>
                            </div>
                        </div>
                    </div>
                    <div v-if="unassignedOptionalPositions.length > 0">
                        <h4 class="mb-2 font-bold">Freie Plätze für</h4>
                        <div class="flex flex-wrap gap-2">
                            <div
                                v-for="position in unassignedOptionalPositions"
                                :key="position.key"
                                class="position flex gap-2"
                                :style="{ backgroundColor: position.color }"
                            >
                                {{ position.name }}
                                <span
                                    class="flex h-5 w-5 items-center justify-center whitespace-nowrap rounded-full bg-white bg-opacity-25 px-1 pt-0.5 text-center text-xs"
                                >
                                    {{ position.count }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </VTooltip>
        <div v-else :key="state.icon" class="status-panel" :class="state.color">
            <i class="fa-solid w-4" :class="state.icon"></i>
            <span class="whitespace-nowrap font-semibold">{{ state.name }}</span>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import type { Event, Position, Slot } from '@/domain';
import { SlotCriticality } from '@/domain';
import { EventState } from '@/domain';
import { VTooltip } from '@/ui/components/common';
import { useEventService } from '@/ui/composables/Domain.ts';
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
            return {
                name: 'Entwurf',
                icon: 'fa-compass-drafting',
                color: 'bg-surface-container-highest text-onsurface',
            };
        case EventState.OpenForSignup:
            return {
                name: 'Crew Anmeldung',
                icon: 'fa-people-group',
                color: 'bg-surface-container-highest text-onsurface',
            };
        case EventState.Canceled:
            return { name: 'Abgesagt', icon: 'fa-ban', color: 'bg-red-container text-onred-container' };
    }
    if (openRequiredSlots.value.length > 0) {
        return { name: 'Fehlende Crew', icon: 'fa-warning', color: 'bg-yellow-container text-onyellow-container' };
    }
    if (openSlots.value.length > 0) {
        return {
            name: 'Freie Plätze',
            icon: 'fa-info-circle',
            iconMobile: 'fa-info',
            color: 'bg-blue-container text-onblue-container',
        };
    }
    return {
        name: 'Voll belegt',
        icon: 'fa-check-circle',
        iconMobile: 'fa-check',
        color: 'bg-green-container text-ongreen-container',
    };
});
</script>
