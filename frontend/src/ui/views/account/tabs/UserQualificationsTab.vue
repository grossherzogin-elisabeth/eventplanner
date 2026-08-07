<template>
    <VTable :items="userQualifications" class="scrollbar-invisible no-header xs:px-8 overflow-x-auto px-4 md:px-16 xl:px-20">
        <template #row="{ item }">
            <td :key="item?.icon" class="pr-4 text-xl">
                <i class="fa-solid" :class="item?.icon" />
            </td>
            <td class="w-full sm:w-2/3">
                <div class="mb-2 flex items-center justify-between gap-2">
                    <p class="w-0 grow truncate font-semibold" :title="item?.name">{{ item?.name }}</p>
                    <UserQualificationStatus :value="item" class="text-xs sm:hidden" />
                </div>
                <p class="line-clamp-3 text-xs">
                    {{ item?.description }}
                </p>
                <div v-if="item?.expiresAt" class="mt-2 flex items-center justify-end font-semibold sm:hidden">
                    <p class="mb-1 text-sm">
                        {{ $t('domain.user-qualification.expires-at', { date: $d(item.expiresAt, DateTimeFormat.DD_MM_YYYY) }) }}
                    </p>
                </div>
            </td>
            <td class="hidden w-1/6 sm:table-cell">
                <template v-if="item?.expires">
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
            <td class="hidden sm:table-cell">
                <div class="flex items-center justify-end">
                    <UserQualificationStatus :value="item" />
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
import UserQualificationStatus from '@/ui/components/users/UserQualificationStatus.vue';

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
