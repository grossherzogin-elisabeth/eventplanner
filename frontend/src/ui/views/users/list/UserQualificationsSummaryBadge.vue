<template>
    <div v-if="props.user?.qualifications?.length === 0" data-test-id="qualification-summary-no-information" class="status-badge neutral">
        <i class="fa-solid fa-question-circle"></i>
        <span>{{ $t('generic.no-information') }}</span>
    </div>
    <div
        v-else-if="expiredQualifications.length > 0"
        data-test-id="qualification-summary-expired"
        class="status-badge error"
        :title="expiredQualifications.join(', ')"
    >
        <i class="fa-solid fa-ban"></i>
        <span>{{ $t('views.user-list.qualification-summary.expired', { count: expiredQualifications.length }) }}</span>
    </div>
    <div
        v-else-if="soonExpiringQualifications.length > 0"
        data-test-id="qualification-summary-expiring-soon"
        class="status-badge warning"
        :title="soonExpiringQualifications.join(', ')"
    >
        <i class="fa-solid fa-warning"></i>
        <span>{{ $t('views.user-list.qualification-summary.expiring-soon', { count: soonExpiringQualifications.length }) }}</span>
    </div>
    <div v-else-if="props.user" data-test-id="qualification-summary-all-valid" class="status-badge success">
        <i class="fa-solid fa-check-circle"></i>
        <span>{{ $t('views.user-list.qualification-summary.all-valid') }}</span>
    </div>
    <div v-else data-test-id="qualification-summary-placeholder" class="status-badge neutral">
        <i class="fa-solid fa-check-circle text-surface-container-high"></i>
        <span></span>
    </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { QualificationKey, User } from '@/domain';
import { useUserService } from '@/domain';

interface Props {
    user?: User;
}

const props = defineProps<Props>();

const userService = useUserService();

const expiredQualifications = computed<QualificationKey[]>(() => {
    return userService.getExpiredQualifications(props.user);
});

const soonExpiringQualifications = computed<QualificationKey[]>(() => {
    return userService.getSoonExpiringQualifications(props.user);
});
</script>
