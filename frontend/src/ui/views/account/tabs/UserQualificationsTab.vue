<template>
    <VTable :items="userQualifications" class="scrollbar-invisible no-header overflow-x-auto px-8 md:px-16 xl:px-20">
        <template #row="{ item }">
            <td :key="item.icon" class="text-xl">
                <i class="fa-solid" :class="item.icon" />
            </td>
            <td class="w-2/3 min-w-80">
                <p class="mb-1 font-semibold">{{ item.name }}</p>
                <p class="text-sm">
                    {{ item.description }}
                </p>
            </td>
            <td class="w-1/6">
                <template v-if="item.expires">
                    <p v-if="item.expiresAt" class="mb-1 font-semibold">
                        {{ $d(item.expiresAt, DateTimeFormat.DD_MM_YYYY) }}
                    </p>
                    <p v-else class="mb-1 font-semibold">{{ $t('generic.no-information') }}</p>
                    <p class="text-sm">{{ $t('views.account.qualifications.status-expires-on') }}</p>
                </template>
                <p v-else class="text-sm">
                    {{ $t('views.account.qualifications.status-no-expires') }}
                </p>
            </td>
            <td>
                <div class="flex items-center justify-end">
                    <div v-if="item.isExpired" class="status-badge error">
                        <i class="fa-solid fa-ban"></i>
                        <span>{{ $t('views.account.qualifications.status-expired') }}</span>
                    </div>
                    <div v-else-if="item.willExpireSoon" class="status-badge warning">
                        <i class="fa-solid fa-warning"></i>
                        <span> {{ $t('views.account.qualifications.status-expiring-soon') }}</span>
                    </div>
                    <div v-else class="status-badge success">
                        <i class="fa-solid fa-check-circle"></i>
                        <span>{{ $t('views.account.qualifications.status-valid') }}</span>
                    </div>
                </div>
            </td>
        </template>
    </VTable>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { ResolvedUserQualification, UserDetails } from '@/domain';
import { useUserService } from '@/domain/services';
import { VTable } from '@/ui/components/common';
import { useQualifications } from '@/ui/composables/Qualifications.ts';

interface Props {
    user: UserDetails;
}

const props = defineProps<Props>();

const usersService = useUserService();
const qualifications = useQualifications();

const userQualifications = computed<ResolvedUserQualification[] | undefined>(() => {
    if (qualifications.map.value === undefined) {
        return undefined;
    }
    return usersService.resolveQualifications(props.user, qualifications.map.value);
});
</script>
