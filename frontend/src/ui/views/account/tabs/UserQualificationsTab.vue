<template>
    <VTable :items="userQualifications" class="scrollbar-invisible no-header xs:px-8 overflow-x-auto px-4 md:px-16 xl:px-20">
        <template #row="{ item }">
            <UserQualificationRow :value="item" />
        </template>
    </VTable>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { ResolvedUserQualification, UserDetails } from '@/domain';
import { useUserService } from '@/domain/services';
import { VTable } from '@/ui/components/common';
import { useQualifications } from '@/ui/composables/Qualifications.ts';
import UserQualificationRow from '@/ui/components/users/UserQualificationRow.vue';

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
