<template>
    <div class="full-width-scrollable">
        <VTable
            :items="userQualifications"
            class="scrollbar-invisible no-header"
            :class="{ 'interactive-table': hasPermission(Permission.UPDATE_USERS) }"
            @click="editUserQualification($event.item)"
        >
            <template #row="{ item }">
                <UserQualificationRow :value="item" />
            </template>
            <template v-if="hasPermission(Permission.UPDATE_USERS)" #context-menu="{ item }">
                <li class="context-menu-item" data-test-id="action-edit-qualification" @click="editUserQualification(item)">
                    <i class="fa-solid fa-edit" />
                    <span>{{ $t('generic.edit') }}</span>
                </li>
                <li class="context-menu-item text-error" data-test-id="action-delete-qualification" @click="deleteUserQualification(item)">
                    <i class="fa-solid fa-trash-alt" />
                    <span>{{ $t('generic.delete') }}</span>
                </li>
            </template>
        </VTable>
        <UserQualificationDetailsDlg ref="editUserQualificationDialog" />
    </div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import type { ResolvedUserQualification, UserDetails, UserQualification } from '@/domain';
import { Permission } from '@/domain';
import { useUserService } from '@/domain/services';
import type { Dialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { useQualifications } from '@/ui/composables/Qualifications';
import { useSession } from '@/ui/composables/Session';
import UserQualificationDetailsDlg from './UserQualificationDetailsDlg.vue';
import UserQualificationRow from '@/ui/components/users/UserQualificationRow.vue';
import { usePositions } from '@/ui/composables/Positions.ts';

interface Props {
    modelValue: UserDetails;
}

type Emit = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const { hasPermission } = useSession();
const usersService = useUserService();
const positions = usePositions();
const qualifications = useQualifications();
const loading = ref(true);

const editUserQualificationDialog = ref<Dialog<UserQualification, UserQualification | undefined> | null>(null);

const userQualifications = computed<ResolvedUserQualification[] | undefined>(() => {
    if (loading.value) {
        return undefined;
    }
    return usersService.resolveQualifications(props.modelValue, qualifications.map.value);
});

async function init(): Promise<void> {
    await Promise.all([positions.loading, qualifications.loading]);
    loading.value = false;
}

function deleteUserQualification(userQualification: ResolvedUserQualification): void {
    const user = props.modelValue;
    user.qualifications = user.qualifications.filter((it) => it.qualificationKey !== userQualification.key);
    emit('update:modelValue', user);
}

async function editUserQualification(userQualification: ResolvedUserQualification): Promise<void> {
    if (!hasPermission(Permission.UPDATE_USERS)) {
        return;
    }
    const user = props.modelValue;
    const editedQualification = await editUserQualificationDialog.value?.open({
        qualificationKey: userQualification.key,
        expires: userQualification.expires,
        expiresAt: userQualification.expiresAt,
        note: userQualification.note,
    });
    if (editedQualification) {
        user.qualifications = user.qualifications.map((oldQualification) => {
            return oldQualification.qualificationKey === editedQualification?.qualificationKey
                ? {
                      qualificationKey: editedQualification.qualificationKey,
                      expires: editedQualification.expires,
                      expiresAt: editedQualification.expiresAt,
                      note: editedQualification.note,
                  }
                : oldQualification;
        });
        emit('update:modelValue', user);
    }
}

init();
</script>
