<template>
    <VTable
        :items="userQualifications"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        @click="editUserQualification($event)"
    >
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
                <div class="flex flex-wrap items-center justify-end">
                    <span
                        v-for="positionKey in item.grantsPositions"
                        :key="positionKey"
                        class="position bg-surface-container-highest my-0.5 mr-1 text-xs opacity-75"
                        :style="{ background: positions.get(positionKey).color }"
                    >
                        {{ positions.get(positionKey).name }}
                    </span>
                </div>
            </td>
            <td class="w-1/6">
                <template v-if="item.expires">
                    <p v-if="item.expiresAt" class="mb-1 font-semibold">
                        {{ $d(item.expiresAt, DateTimeFormat.DD_MM_YYYY) }}
                    </p>
                    <p v-else class="mb-1 font-semibold">k.A.</p>
                    <p class="text-sm">Gültig bis</p>
                </template>
                <p v-else class="text-sm">
                    Ohne<br />
                    Ablaufdatum
                </p>
            </td>
            <td>
                <div class="flex items-center justify-end">
                    <div v-if="item.isExpired" class="bg-red-container text-onred-container status-panel">
                        <i class="fa-solid fa-ban"></i>
                        <span class="whitespace-nowrap font-semibold">Abgelaufen</span>
                    </div>
                    <div
                        v-else-if="item.willExpireSoon"
                        class="status-panel bg-yellow-container text-onyellow-container"
                    >
                        <i class="fa-solid fa-warning"></i>
                        <span class="whitespace-nowrap font-semibold"> Läuft bald ab</span>
                    </div>
                    <div v-else class="status-panel bg-green-container text-ongreen-container">
                        <i class="fa-solid fa-check-circle"></i>
                        <span class="whitespace-nowrap font-semibold">Gültig</span>
                    </div>
                </div>
            </td>
        </template>
        <template #context-menu="{ item }">
            <li class="context-menu-item" @click="editUserQualification(item)">
                <i class="fa-solid fa-edit" />
                <span>Qualification bearbeiten</span>
            </li>
            <li class="context-menu-item text-error" @click="deleteUserQualification(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>Qualifikation löschen</span>
            </li>
        </template>
    </VTable>
    <UserQualificationDetailsDlg ref="editUserQualificationDialog" />
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type { ResolvedUserQualification, UserDetails, UserQualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { useUserService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQualifications } from '@/ui/composables/Qualifications.ts';
import UserQualificationDetailsDlg from './UserQualificationDetailsDlg.vue';

interface Props {
    modelValue: UserDetails;
}

type Emit = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const usersService = useUserService();
const positions = usePositions();
const qualifications = useQualifications();

const editUserQualificationDialog = ref<Dialog<UserQualification, UserQualification | undefined> | null>(null);

const userQualifications = computed<ResolvedUserQualification[] | undefined>(() => {
    return usersService.resolveQualifications(props.modelValue, qualifications.map.value);
});

function deleteUserQualification(userQualification: ResolvedUserQualification): void {
    const user = props.modelValue;
    user.qualifications = user.qualifications.filter((it) => it.qualificationKey !== userQualification.key);
    emit('update:modelValue', user);
}

async function editUserQualification(userQualification: ResolvedUserQualification): Promise<void> {
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
</script>
