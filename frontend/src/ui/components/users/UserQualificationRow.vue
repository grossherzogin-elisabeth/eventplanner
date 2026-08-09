<template>
    <td :key="props.value?.icon" class="xs:table-cell hidden pr-4 text-xl">
        <i class="fa-solid" :class="props.value?.icon ?? 'fa-circle text-surface-container-highest text-2xl'" />
    </td>
    <td class="w-full sm:w-2/3">
        <div class="mb-2 flex items-center justify-between gap-2">
            <p class="w-0 grow truncate font-semibold" :title="props.value?.name">{{ props.value?.name }}</p>
            <UserQualificationStatus :value="props.value" class="text-xs sm:hidden" />
        </div>
        <p class="line-clamp-3 text-xs">
            {{ props.value?.description }}
        </p>
        <div v-if="props.value?.expiresAt" class="mt-2 flex items-center justify-end font-semibold sm:hidden">
            <p class="mb-1 text-sm">
                {{ $t('domain.user-qualification.expires-at', { date: $d(props.value.expiresAt, DateTimeFormat.DD_MM_YYYY) }) }}
            </p>
        </div>
    </td>

    <td class="hidden w-1/6 sm:table-cell">
        <div class="flex flex-wrap items-center justify-end">
            <span
                v-for="positionKey in props.value?.grantsPositions"
                :key="positionKey"
                class="tag custom"
                :style="{ '--color': positions.get(positionKey).color }"
            >
                {{ positions.get(positionKey).name }}
            </span>
        </div>
    </td>

    <td class="hidden w-1/6 sm:table-cell">
        <template v-if="!props.value || props.value?.expires">
            <p class="mb-1 font-semibold">
                <template v-if="props.value">
                    {{ props.value.expiresAt ? $d(props.value.expiresAt, DateTimeFormat.DD_MM_YYYY) : $t('generic.no-information') }}
                </template>
            </p>
            <p class="text-sm">
                <template v-if="props.value">
                    {{ $t('views.account.qualifications.status-expires-on') }}
                </template>
            </p>
        </template>
        <p v-else class="text-sm">
            {{ $t('views.account.qualifications.status-no-expires') }}
        </p>
    </td>
    <td class="hidden sm:table-cell">
        <UserQualificationStatus :value="props.value" class="text-sm" />
    </td>
</template>
<script setup lang="ts">
import UserQualificationStatus from '@/ui/components/users/UserQualificationStatus.vue';
import type { ResolvedUserQualification } from '@/domain';
import { DateTimeFormat } from '@/common/date';
import { usePositions } from '@/ui/composables/Positions.ts';

interface Props {
    value?: ResolvedUserQualification;
}

const props = defineProps<Props>();

const positions = usePositions();
</script>
