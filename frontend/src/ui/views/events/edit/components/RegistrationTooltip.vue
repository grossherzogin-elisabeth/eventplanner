<template>
    <div class="bg-surface-container-high text-onsurface rounded-xl p-4 text-sm shadow-xl">
        <h3 class="text-secondary mb-4 flex items-center gap-2 text-base">
            <i class="fa-solid fa-user-circle"></i>
            <span>
                {{ props.registration.name }}
            </span>
        </h3>
        <p v-if="props.registration.registration?.note" class="mb-4 line-clamp-6 text-sm italic">
            "{{ props.registration.registration.note }}"
        </p>
        <template v-if="props.registration.user">
            <h4 class="mb-2 text-sm font-bold">{{ $t('views.events.edit.tooltip.information') }}</h4>
            <div class="mb-4 flex flex-wrap items-center gap-1">
                <span v-if="props.registration?.state === RegistrationSlotState.CONFIRMED" class="tag success">
                    <i class="fa-solid fa-check" />
                    {{ $t('domain.registration.confirmed') }}
                </span>
                <span v-if="props.registration?.state === RegistrationSlotState.ASSIGNED" class="tag error">
                    <i class="fa-solid fa-clock" />
                    {{ $t('domain.registration.confirmation-pending') }}
                </span>
                <span v-if="!props.registration?.registration?.userKey" class="tag info">
                    <i class="fa-solid fa-info-circle" />
                    {{ $t('domain.registration.guest') }}
                </span>
                <span v-if="props.registration?.registration?.overnightStay" class="tag info">
                    <i class="fa-solid fa-bed" />
                    {{ $t('domain.registration.overnight-stay') }}
                </span>
                <span v-if="props.registration?.registration?.arrival" class="tag info">
                    <i class="fa-solid fa-calendar-day" />
                    {{ $t('domain.registration.arrival-on-day-before') }}
                </span>
            </div>
            <h4 class="mb-2 text-sm font-bold">{{ $t('views.events.edit.tooltip.positions') }}</h4>
            <div class="mb-4 flex flex-wrap items-center gap-1">
                <span
                    v-for="p in props.registration.user.positionKeys"
                    :key="p"
                    :style="{ '--color': positions.get(p).color }"
                    class="tag custom"
                >
                    {{ positions.get(p).name }}
                </span>
                <span v-if="props.registration.hasOverwrittenPosition" class="tag error line-through">
                    <i class="fa-solid fa-warning"></i>
                    {{ positions.get(props.registration.registration?.positionKey).name }}
                </span>
            </div>
            <h4 class="mb-2 text-sm font-bold">{{ $t('views.events.edit.tooltip.qualifications') }}</h4>
            <div class="flex flex-wrap items-center gap-1">
                <span
                    v-for="q in props.registration.user.qualifications"
                    :key="q.qualificationKey"
                    class="tag"
                    :class="props.registration.expiredQualifications.includes(q.qualificationKey) ? 'error' : 'info'"
                >
                    <i v-if="props.registration.expiredQualifications.includes(q.qualificationKey)" class="fa-solid fa-warning"></i>
                    <i v-else class="fa-solid fa-check"></i>
                    {{ qualifications.get(q.qualificationKey).name }}
                </span>
            </div>
        </template>
        <p v-else>
            {{ $t('views.events.edit.tooltip.guest-info', { name: props.registration.name }) }}
        </p>
    </div>
</template>
<script lang="ts" setup>
import type { ResolvedRegistrationSlot } from '@/domain';
import { RegistrationSlotState } from '@/domain';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQualifications } from '@/ui/composables/Qualifications.ts';

interface Props {
    registration: ResolvedRegistrationSlot;
}

const props = defineProps<Props>();

const positions = usePositions();
const qualifications = useQualifications();
</script>
