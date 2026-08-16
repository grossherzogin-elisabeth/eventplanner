<template>
    <div>
        <div v-if="props.value?.isExpired" class="status-badge error font-size-inherit">
            <i class="fa-solid fa-ban"></i>
            <span>{{ $t('domain.user-qualification.expired') }}</span>
        </div>
        <div v-else-if="props.value?.willExpireSoon" class="status-badge warning font-size-inherit">
            <i class="fa-solid fa-warning"></i>
            <span> {{ $t('domain.user-qualification.expiring-soon') }}</span>
        </div>
        <div v-else-if="props.value" class="status-badge success font-size-inherit">
            <i class="fa-solid fa-check-circle"></i>
            <span>{{ $t('domain.user-qualification.valid') }}</span>
        </div>
        <div v-else class="status-badge neutral font-size-inherit">
            <i class="fa-circle text-surface-container-high"></i>
            <span></span>
        </div>
    </div>
</template>
<script setup lang="ts">
import type { ResolvedUserQualification } from '@/domain';

interface Props {
    value?: ResolvedUserQualification;
}

const props = defineProps<Props>();
</script>
