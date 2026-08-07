<template>
    <td class="w-1/3 font-semibold whitespace-nowrap">
        <p class="mb-2">
            {{ props.user?.nickName || props.user?.firstName }} {{ props.user?.lastName }}
            <span v-if="props.user?.verified" class="bg-success-container/50 inline-flex h-5 w-5 items-center justify-center rounded-full">
                <i class="fa-solid fa-check text-onsuccess-container text-xs"></i>
            </span>
        </p>
        <p v-if="userRoles" class="max-w-64 truncate text-sm" :title="userRoles">
            {{ userRoles }}
        </p>
        <p v-else class="max-w-64 truncate text-sm italic">Keine Rolle zugewiesen</p>
    </td>
    <td class="w-1/3">
        <div class="flex max-w-64 flex-wrap gap-1">
            <span v-for="position in userPositions" :key="position.key" class="tag custom" :style="{ '--color': position.color }">
                {{ position.name }}
            </span>
        </div>
    </td>
    <td class="w-1/5">
        <div class="flex space-x-8">
            <!--            <div :class="{ 'opacity-25': !props.user?.singleDayEventsCount }">-->
            <!--                <p class="mb-1 font-semibold">{{ props.user?.singleDayEventsCount || '-' }}</p>-->
            <!--                <p class="text-sm" title="Tagesfahrten">TF</p>-->
            <!--            </div>-->
            <!--            <div :class="{ 'opacity-25': !props.user?.weekendEventsCount }">-->
            <!--                <p class="mb-1 font-semibold">{{ props.user?.weekendEventsCount || '-' }}</p>-->
            <!--                <p class="text-sm" title="Wochenendfahrten">WE</p>-->
            <!--            </div>-->
            <!--            <div :class="{ 'opacity-25': !props.user?.multiDayEventsCount }">-->
            <!--                <p class="mb-1 font-semibold">{{ props.user?.multiDayEventsCount || '-' }}</p>-->
            <!--                <p class="text-sm" title="Sommerreisen und mehrtägige Fahrten">SR</p>-->
            <!--            </div>-->
            <!--            <div :class="{ 'opacity-25': !props.user?.waitingListCount }">-->
            <!--                <p class="mb-1 font-semibold">{{ props.user?.waitingListCount || '-' }}</p>-->
            <!--                <p class="text-sm" title="Warteliste">WL</p>-->
            <!--            </div>-->
        </div>
    </td>
    <td class="w-1/12">
        <div class="flex items-center justify-end">
            <div v-if="props.user?.qualifications?.length === 0" class="status-badge neutral">
                <i class="fa-solid fa-question-circle"></i>
                <span>Keine Angaben</span>
            </div>
            <div v-else-if="expiredQualifications.length > 0" class="status-badge error" :title="expiredQualifications.join(', ')">
                <i class="fa-solid fa-ban"></i>
                <span> {{ expiredQualifications.length }} abgelaufen </span>
            </div>
            <div
                v-else-if="soonExpiringQualifications.length > 0"
                class="status-badge warning"
                :title="soonExpiringQualifications.join(', ')"
            >
                <i class="fa-solid fa-warning"></i>
                <span>
                    <template v-if="soonExpiringQualifications.length === 1"> 1 läuft bald ab </template>
                    <template v-else> {{ soonExpiringQualifications.length }} laufen bald ab </template>
                </span>
            </div>
            <div v-else-if="props.user" class="status-badge success">
                <i class="fa-solid fa-check-circle"></i>
                <span>Alle gültig</span>
            </div>
            <div v-else class="status-badge neutral">
                <i class="fa-solid fa-check-circle text-surface-container-high"></i>
                <span></span>
            </div>
        </div>
    </td>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { User, Position, QualificationKey } from '@/domain';
import { useUserService } from '@/domain';
import { filterUndefined } from '@/common';
import { usePositions } from '@/ui/composables/Positions.ts';

interface Props {
    user?: User;
}
const props = defineProps<Props>();

const { t } = useI18n();
const positions = usePositions();
const userService = useUserService();

const userRoles = computed<string>(() => {
    return props.user?.roles?.map((k) => t(`domain.role.${k}`)).join(', ') ?? '';
});

const userPositions = computed<Position[]>(() => {
    return props.user?.positionKeys?.map((key) => positions.get(key)).filter(filterUndefined) ?? [];
});

const expiredQualifications = computed<QualificationKey[]>(() => {
    return userService.getExpiredQualifications(props.user);
});

const soonExpiringQualifications = computed<QualificationKey[]>(() => {
    return userService.getSoonExpiringQualifications(props.user);
});
</script>
