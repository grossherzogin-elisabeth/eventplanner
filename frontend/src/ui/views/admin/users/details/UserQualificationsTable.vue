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
                        class="position my-0.5 mr-1 bg-gray-500 text-xs opacity-75"
                        :style="{ background: positions.get(positionKey).color }"
                    >
                        {{ positions.get(positionKey).name }}
                    </span>
                </div>
            </td>
            <td class="w-1/6">
                <template v-if="item.expiresAt">
                    <p v-if="item.expiresAt" class="mb-1 font-semibold">
                        {{ $d(item.expiresAt, DateTimeFormat.DD_MM_YYYY) }}
                    </p>
                    <p v-else class="mb-1 font-semibold">??.??.????</p>
                    <p class="text-sm">Gültig bis</p>
                </template>
                <p v-else class="text-sm">
                    Ohne<br />
                    Ablaufdatum
                </p>
            </td>
            <td>
                <div
                    v-if="item.isExpired"
                    class="inline-flex w-auto items-center space-x-2 rounded-full bg-red-100 py-1 pl-3 pr-4 text-red-700"
                >
                    <i class="fa-solid fa-ban"></i>
                    <span class="whitespace-nowrap font-semibold">Abgelaufen</span>
                </div>
                <div
                    v-else-if="item.willExpireSoon"
                    class="inline-flex w-auto items-center space-x-2 rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                >
                    <i class="fa-solid fa-warning"></i>
                    <span class="whitespace-nowrap font-semibold"> Läuft bald ab</span>
                </div>
                <div
                    v-else
                    class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-200 py-1 pl-3 pr-4 text-green-700"
                >
                    <i class="fa-solid fa-check-circle"></i>
                    <span class="whitespace-nowrap font-semibold">Gültig</span>
                </div>
            </td>
            <td class="w-0">
                <ContextMenuButton class="px-4 py-2">
                    <ul>
                        <li class="context-menu-item" @click="editUserQualification(item)">
                            <i class="fa-solid fa-edit" />
                            <span>Qualification bearbeiten</span>
                        </li>
                        <li class="context-menu-item text-red-700" @click="deleteUserQualification(item)">
                            <i class="fa-solid fa-trash-alt" />
                            <span>Qualifikation löschen</span>
                        </li>
                    </ul>
                </ContextMenuButton>
            </td>
        </template>
    </VTable>
    <QualificationEditDlg ref="editQualificationDialog" />
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { DateTimeFormat } from '@/common/date';
import type {
    Qualification,
    QualificationKey,
    ResolvedUserQualification,
    UserDetails,
    UserQualification,
} from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { ContextMenuButton, VTable } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { useUserService } from '@/ui/composables/Domain';
import { usePositions } from '@/ui/composables/Positions.ts';
import QualificationEditDlg from '@/ui/views/admin/users/details/QualificationEditDlg.vue';

interface Props {
    modelValue: UserDetails;
}

type Emit = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const positions = usePositions();

const qualifications = ref<Map<QualificationKey, Qualification> | undefined>(undefined);
const editQualificationDialog = ref<Dialog<UserQualification, UserQualification> | null>(null);

const userQualifications = computed<ResolvedUserQualification[] | undefined>(() => {
    if (qualifications.value === undefined) {
        return undefined;
    }
    return usersService.resolveQualifications(props.modelValue, qualifications.value);
});

function init(): void {
    fetchQualifications();
}

function deleteUserQualification(userQualification: ResolvedUserQualification): void {
    const user = props.modelValue;
    user.qualifications = user.qualifications.filter((it) => it.qualificationKey !== userQualification.key);
    emit('update:modelValue', user);
}

async function editUserQualification(userQualification: ResolvedUserQualification): Promise<void> {
    const user = props.modelValue;
    const editedQualification = await editQualificationDialog.value?.open({
        qualificationKey: userQualification.key,
        note: userQualification.note,
        expiresAt: userQualification.expiresAt,
    });
    if (editedQualification) {
        user.qualifications = user.qualifications.map((oldQualification) => {
            return oldQualification.qualificationKey === editedQualification?.qualificationKey
                ? {
                      qualificationKey: editedQualification.qualificationKey,
                      note: editedQualification.note,
                      expiresAt: editedQualification.expiresAt,
                  }
                : oldQualification;
        });
        emit('update:modelValue', user);
    }
}

async function fetchQualifications(): Promise<void> {
    qualifications.value = await usersUseCase.resolveQualifications();
}

init();
</script>
