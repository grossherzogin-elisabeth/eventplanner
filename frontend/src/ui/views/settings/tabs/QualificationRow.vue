<template>
    <td :key="props.qualification?.icon" class="pr-4 text-xl">
        <i class="fa-solid" :class="props.qualification?.icon ?? 'fa-circle text-surface-container-high'" />
    </td>
    <td class="w-full sm:min-w-80">
        <p class="mb-1 line-clamp-2 font-semibold" data-test-id="qualification-name">{{ props.qualification?.name }}</p>
        <p class="line-clamp-2 text-sm">{{ props.qualification?.description }}</p>
        <p class="mt-2 flex flex-wrap items-center justify-start text-sm">
            <span
                v-for="positionKey in props.qualification?.grantsPositions"
                :key="positionKey"
                class="tag custom my-1 mr-2"
                :style="{ '--color': positions.get(positionKey).color }"
            >
                {{ positions.get(positionKey).name }}
            </span>
        </p>
    </td>
    <td class="hidden w-80 md:table-cell">
        <div class="flex justify-end">
            <div v-if="props.qualification?.expires" class="status-badge warning">
                <i class="fa-solid fa-clock"></i>
                <span>{{ $t('views.settings.qualifications.status-expires') }}</span>
            </div>
            <div v-else class="status-badge success">
                <i class="fa-solid fa-check-circle"></i>
                <span>{{ $t('views.settings.qualifications.status-no-expires') }}</span>
            </div>
        </div>
    </td>
</template>
<script lang="ts" setup>
import type { Qualification } from '@/domain';
import { usePositions } from '@/ui/composables/Positions.ts';

interface Props {
    qualification?: Qualification;
}
const props = defineProps<Props>();

const positions = usePositions();
</script>
