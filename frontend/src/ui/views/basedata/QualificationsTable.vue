<template>
    <VTable
        :items="qualifications"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        @click="editQualification($event.item)"
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
                    v-for="positionKey in item.grantsPositions"
                    :key="positionKey"
                    class="position my-0.5 mr-1 bg-surface-container-high text-xs opacity-75"
                    :style="{ background: positions.get(positionKey).color }"
                >
                    {{ positions.get(positionKey).name }}
                </span>
            </td>
            <td class="w-80">
                <div class="flex justify-end">
                    <div v-if="item.expires" class="status-panel bg-yellow-container text-onyellow-container">
                        <i class="fa-solid fa-clock"></i>
                        <span class="whitespace-nowrap font-semibold">{{ $t('views.basedata.tab.qualifications.status-expires') }}</span>
                    </div>
                    <div v-else class="status-panel bg-green-container text-ongreen-container">
                        <i class="fa-solid fa-check-circle"></i>
                        <span class="whitespace-nowrap font-semibold">{{ $t('views.basedata.tab.qualifications.status-no-expires') }}</span>
                    </div>
                </div>
            </td>
        </template>
        <template #context-menu="{ item }">
            <li class="context-menu-item" @click="editQualification(item)">
                <i class="fa-solid fa-edit" />
                <span>{{ $t('generic.edit') }}</span>
            </li>
            <li class="context-menu-item text-error" @click="deleteQualification(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>{{ $t('generic.delete') }}</span>
            </li>
        </template>
    </VTable>
    <QualificationEditDlg ref="qualificationDetailsDialog" />
    <VConfirmationDialog ref="deleteQualificationDialog" />
</template>
<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { Qualification } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import { useQualificationsAdministrationUseCase } from '@/ui/composables/Application.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import QualificationEditDlg from './QualificationDetailsDlg.vue';

interface Props {
    filter?: string;
}

const props = defineProps<Props>();

const positions = usePositions();
const qualificationAdministrationUseCase = useQualificationsAdministrationUseCase();

const qualifications = ref<Qualification[] | undefined>(undefined);

const qualificationDetailsDialog = ref<Dialog<Qualification | undefined, Qualification | undefined> | null>(null);
const deleteQualificationDialog = ref<ConfirmationDialog | null>(null);

const { t } = useI18n();

function init(): void {
    fetchQualifications();
    watch(() => props.filter, fetchQualifications);
}

async function fetchQualifications(): Promise<void> {
    qualifications.value = await qualificationAdministrationUseCase.getQualifications(props.filter);
}

async function createQualification(): Promise<void> {
    const newQualification = await qualificationDetailsDialog.value?.open();
    if (newQualification) {
        await qualificationAdministrationUseCase.createQualification(newQualification);
        await fetchQualifications();
    }
}

async function editQualification(qualification: Qualification): Promise<void> {
    const editedQualification = await qualificationDetailsDialog.value?.open(qualification);
    if (editedQualification) {
        await qualificationAdministrationUseCase.updateQualification(editedQualification);
        await fetchQualifications();
    }
}

async function deleteQualification(qualification: Qualification): Promise<void> {
    const confirmed = await deleteQualificationDialog.value?.open({
        title: t('views.basedata.tab.qualifications.delete-title'),
        message: t('views.basedata.tab.qualifications.delete-message'),
        submit: t('generic.delete'),
        danger: true,
    });
    if (confirmed) {
        await qualificationAdministrationUseCase.deleteQualification(qualification);
        await fetchQualifications();
    }
}

defineExpose({
    createQualification: () => createQualification(),
});

init();
</script>
