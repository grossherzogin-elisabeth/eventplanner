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
                    <p class="text-sm">{{ $t('views.account.qualifications.status-expires') }}</p>
                </template>
                <p v-else class="text-sm">
                    {{ $t('views.account.qualifications.status-no-expires') }}
                </p>
            </td>
            <td>
                <div class="flex items-center justify-end">
                    <div v-if="item.isExpired" class="status-panel bg-red-container text-onred-container">
                        <i class="fa-solid fa-ban"></i>
                        <span class="whitespace-nowrap font-semibold">{{ $t('views.account.qualifications.status-expired') }}</span>
                    </div>
                    <div v-else-if="item.willExpireSoon" class="status-panel bg-yellow-container text-onyellow-container">
                        <i class="fa-solid fa-warning"></i>
                        <span class="whitespace-nowrap font-semibold"> {{ $t('views.account.qualifications.status-expiring-soon') }}</span>
                    </div>
                    <div v-else class="status-panel bg-green-container text-ongreen-container">
                        <i class="fa-solid fa-check-circle"></i>
                        <span class="whitespace-nowrap font-semibold">{{ $t('views.account.qualifications.status-valid') }}</span>
                    </div>
                </div>
            </td>
        </template>
    </VTable>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { Qualification, QualificationKey, ResolvedUserQualification, UserDetails } from '@/domain';
import { VTable } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { useUserService } from '@/ui/composables/Domain';

interface Props {
    user: UserDetails;
}

const props = defineProps<Props>();

const usersUseCase = useUsersUseCase();
const usersService = useUserService();

const qualifications = ref<Map<QualificationKey, Qualification> | undefined>(undefined);

const userQualifications = computed<ResolvedUserQualification[] | undefined>(() => {
    if (qualifications.value === undefined) {
        return undefined;
    }
    return usersService.resolveQualifications(props.user, qualifications.value);
});

function init(): void {
    fetchQualifications();
}

async function fetchQualifications(): Promise<void> {
    qualifications.value = await usersUseCase.resolveQualifications();
}

init();
</script>
