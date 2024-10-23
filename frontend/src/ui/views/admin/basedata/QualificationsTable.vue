<template>
    <VTable
        :items="qualifications"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        @click="editQualification($event)"
    >
        <template #row="{ item }">
            <td :key="item.icon" class="text-xl">
                <i class="fa-solid" :class="item.icon" />
            </td>
            <td class="w-full min-w-80">
                <p class="mb-1 line-clamp-2 font-semibold">{{ item.name }}</p>
                <p class="line-clamp-2 text-sm">
                    {{ item.description }}
                </p>
            </td>
            <td class="w-96">
                <span
                    v-if="item.grantsPosition"
                    class="position text-sm"
                    :style="{ background: positions.get(item.grantsPosition).color }"
                >
                    {{ positions.get(item.grantsPosition).name }}
                </span>
            </td>
            <td class="w-80">
                <div class="flex justify-end">
                    <div
                        v-if="item.expires"
                        class="inline-flex w-auto items-center space-x-2 rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                    >
                        <i class="fa-solid fa-clock"></i>
                        <span class="whitespace-nowrap font-semibold">Mit Ablaufdatum</span>
                    </div>
                    <div
                        v-else
                        class="inline-flex w-auto items-center space-x-2 rounded-full bg-green-200 py-1 pl-3 pr-4 text-green-700"
                    >
                        <i class="fa-solid fa-check-circle"></i>
                        <span class="whitespace-nowrap font-semibold">Ohne Ablaufdatum</span>
                    </div>
                </div>
            </td>
            <td class="w-0">
                <ContextMenuButton class="px-4 py-2">
                    <ul>
                        <li class="context-menu-item" @click="editQualification(item)">
                            <i class="fa-solid fa-pencil" />
                            <span>Bearbeiten</span>
                        </li>
                        <li class="context-menu-item text-red-700" @click="deleteQualification(item)">
                            <i class="fa-solid fa-trash-alt" />
                            <span>Löschen</span>
                        </li>
                    </ul>
                </ContextMenuButton>
            </td>
        </template>
    </VTable>
    <QualificationCreateDlg ref="createQualificationDialog" />
    <QualificationEditDlg ref="editQualificationDialog" />
    <VConfirmationDialog ref="deleteQualificationDialog">
        <template #title>Qualifikation löschen?</template>
        <template #message>
            Bist du sicher, das du die Qualifikation löschen möchtest? Die Qualifikation wird bei allen Nutzern, bei
            denen sie assoziert ist, entfernt.
        </template>
        <template #submit>Löschen</template>
    </VConfirmationDialog>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue';
import type { Qualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';
import { ContextMenuButton, VTable } from '@/ui/components/common';
import { useQualificationsAdministrationUseCase } from '@/ui/composables/Application';
import { usePositions } from '@/ui/composables/Positions';
import QualificationCreateDlg from '@/ui/views/admin/basedata/QualificationCreateDlg.vue';
import QualificationEditDlg from '@/ui/views/admin/basedata/QualificationEditDlg.vue';

interface Props {
    filter?: string;
}

const props = defineProps<Props>();

const positions = usePositions();
const qualificationAdministrationUseCase = useQualificationsAdministrationUseCase();

const qualifications = ref<Qualification[] | undefined>(undefined);

const createQualificationDialog = ref<Dialog<void, Qualification> | null>(null);
const editQualificationDialog = ref<Dialog<Qualification, Qualification> | null>(null);
const deleteQualificationDialog = ref<Dialog<Qualification> | null>(null);

function init(): void {
    fetchQualifications();
    watch(() => props.filter, fetchQualifications);
}

async function fetchQualifications(): Promise<void> {
    qualifications.value = await qualificationAdministrationUseCase.getQualifications(props.filter);
}

function createQualification(): void {
    if (createQualificationDialog.value) {
        createQualificationDialog.value
            .open()
            .then((it) => qualificationAdministrationUseCase.createQualification(it))
            .then(() => fetchQualifications())
            .catch(() => {});
    }
}

function editQualification(qualification: Qualification): void {
    if (editQualificationDialog.value) {
        editQualificationDialog.value
            .open(qualification)
            .then((it) => qualificationAdministrationUseCase.updateQualification(it))
            .then(() => fetchQualifications())
            .catch(() => {});
    }
}

function deleteQualification(qualification: Qualification): void {
    if (deleteQualificationDialog.value) {
        deleteQualificationDialog.value
            .open(qualification)
            .then(() => qualificationAdministrationUseCase.deleteQualification(qualification))
            .then(() => fetchQualifications())
            .catch(() => {});
    }
}

defineExpose({
    createQualification: () => createQualification(),
});

init();
</script>
