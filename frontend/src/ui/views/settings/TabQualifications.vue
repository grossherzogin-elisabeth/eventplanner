<template>
    <div class="flex h-full flex-col">
        <div class="scrollbar-invisible mb-4 flex items-center gap-2 overflow-x-auto px-4 md:px-16 xl:min-h-8 xl:px-20">
            <FilterMultiselect
                v-model="filterPositions"
                data-test-id="filter-position"
                :placeholder="$t('views.settings.filter.positions')"
                :options="positions.options.value"
            />
            <FilterToggle v-model="filterExpires" data-test-id="filter-expires" :label="$t('views.settings.filter.expires')" />
            <div class="flex-grow"></div>
            <div class="hidden lg:block">
                <VSearchButton v-model="filter" />
            </div>
        </div>

        <div class="flex-1">
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
                        <p class="mb-1 line-clamp-2 font-semibold" data-test-id="qualification-name">{{ item.name }}</p>
                        <p class="line-clamp-2 text-sm">
                            {{ item.description }}
                        </p>
                    </td>
                    <td class="w-96">
                        <span
                            v-for="positionKey in item.grantsPositions"
                            :key="positionKey"
                            class="tag custom my-1 mr-2"
                            :style="{ '--color': positions.get(positionKey).color }"
                        >
                            {{ positions.get(positionKey).name }}
                        </span>
                    </td>
                    <td class="w-80">
                        <div class="flex justify-end">
                            <div v-if="item.expires" class="status-badge warning">
                                <i class="fa-solid fa-clock"></i>
                                <span>{{ $t('views.settings.qualifications.status-expires') }}</span>
                            </div>
                            <div v-else class="status-badge success">
                                <i class="fa-solid fa-check-circle"></i>
                                <span>{{ $t('views.settings.qualifications.status-no-expires') }}</span>
                            </div>
                        </div>
                    </td>
                </template>
                <template #context-menu="{ item }">
                    <li class="context-menu-item" data-test-id="context-menu-edit" @click="editQualification(item)">
                        <i class="fa-solid fa-edit" />
                        <span>{{ $t('generic.edit') }}</span>
                    </li>
                    <li class="context-menu-item text-error" data-test-id="context-menu-delete" @click="deleteQualification(item)">
                        <i class="fa-solid fa-trash-alt" />
                        <span>{{ $t('generic.delete') }}</span>
                    </li>
                </template>
            </VTable>
        </div>
        <div
            class="permission-write-positions pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12"
        >
            <button class="btn-floating pointer-events-auto" @click="createQualification()">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>{{ $t('views.settings.qualifications.add-new') }}</span>
            </button>
        </div>
        <QualificationEditDlg ref="qualificationDetailsDialog" data-test-id="edit-dialog" />
        <VConfirmationDialog ref="deleteQualificationDialog" data-test-id="delete-confirm-dialog" />
    </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useQualificationsAdministrationUseCase } from '@/application';
import type { Qualification } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VConfirmationDialog, VSearchButton, VTable } from '@/ui/components/common';
import { FilterMultiselect, FilterToggle } from '@/ui/components/filters';
import { usePositions } from '@/ui/composables/Positions';
import { useQuery } from '@/ui/composables/QueryState';
import QualificationEditDlg from './components/QualificationDetailsDlg.vue';

const positions = usePositions();
const qualificationAdministrationUseCase = useQualificationsAdministrationUseCase();

const qualifications = ref<Qualification[] | undefined>(undefined);
const filter = useQuery('filter', '').parameter;
const filterPositions = useQuery('positions', []).parameter;
const filterExpires = useQuery('expires', false).parameter;

const qualificationDetailsDialog = ref<Dialog<Qualification | undefined, Qualification | undefined> | null>(null);
const deleteQualificationDialog = ref<ConfirmationDialog | null>(null);

const { t } = useI18n();

function init(): void {
    fetchQualifications();
    watch(filter, fetchQualifications);
    watch(filterExpires, fetchQualifications);
    watch(filterPositions, fetchQualifications, { deep: true });
}

async function fetchQualifications(): Promise<void> {
    qualifications.value = await qualificationAdministrationUseCase.getQualifications({
        text: filter.value,
        expires: filterExpires.value,
        grantsPosition: filterPositions.value,
    });
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
        title: t('views.settings.qualifications.delete-title'),
        message: t('views.settings.qualifications.delete-message'),
        submit: t('generic.delete'),
        danger: true,
    });
    if (confirmed) {
        await qualificationAdministrationUseCase.deleteQualification(qualification);
        await fetchQualifications();
    }
}

init();
</script>
