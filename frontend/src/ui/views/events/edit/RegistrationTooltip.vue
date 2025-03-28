<template>
    <div class="rounded-xl bg-surface-container-high p-4 text-sm text-onsurface shadow-xl">
        <h3 class="mb-4 flex items-center gap-2 text-base text-secondary">
            <i class="fa-solid fa-user-circle"></i>
            <span>
                {{ props.registration.name }}
            </span>
        </h3>
        <p v-if="props.registration.registration?.note" class="mb-4 line-clamp-6 text-sm italic">
            "{{ props.registration.registration.note }}"
        </p>
        <template v-if="props.registration.user">
            <h4 class="mb-2 text-sm font-bold">Positionen</h4>
            <div class="mb-4 flex flex-wrap items-center gap-1">
                <span
                    v-for="p in props.registration.user.positionKeys"
                    :key="p"
                    :style="{ background: positions.get(p).color }"
                    class="position cursor-pointer text-xs"
                >
                    {{ positions.get(p).name }}
                </span>
                <span
                    v-if="props.registration.hasOverwrittenPosition"
                    class="position cursor-pointer bg-error-container text-xs text-onerror-container line-through"
                >
                    <i class="fa-solid fa-warning"></i>
                    {{ positions.get(props.registration.registration?.positionKey).name }}
                </span>
            </div>
            <h4 class="mb-2 text-sm font-bold">Qualifikationen</h4>
            <div class="flex flex-wrap items-center gap-1">
                <span
                    v-for="q in props.registration.user.qualifications"
                    :key="q.qualificationKey"
                    class="truncate whitespace-nowrap rounded-lg px-2 py-1 text-xs font-bold"
                    :class="
                        props.registration.expiredQualifications.includes(q.qualificationKey)
                            ? 'bg-error-container text-onerror-container'
                            : 'bg-secondary-container text-onsecondary-container'
                    "
                >
                    <i v-if="props.registration.expiredQualifications.includes(q.qualificationKey)" class="fa-solid fa-warning"></i>
                    <i v-else class="fa-solid fa-check"></i>
                    {{ qualifications.get(q.qualificationKey).name }}
                </span>
            </div>
        </template>
        <p v-else>{{ props.registration.name }} ist Gastcrew. Positionen und Qualifikationen sind daher unbekannt.</p>
    </div>
</template>
<script lang="ts" setup>
import type { ResolvedRegistrationSlot } from '@/domain';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQualifications } from '@/ui/composables/Qualifications.ts';

interface Props {
    registration: ResolvedRegistrationSlot;
}

const props = defineProps<Props>();

const positions = usePositions();
const qualifications = useQualifications();
</script>
