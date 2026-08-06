<template>
    <div class="flex h-full flex-col xl:max-w-5xl">
        <div class="filter-panel scrollbar-invisible">
            <FilterMultiselect
                v-model="filterPositions"
                data-test-id="filter-position"
                :placeholder="$t('views.settings.filter.positions')"
                :options="positions.options.value"
            />
            <FilterToggle v-model="filterExpires" data-test-id="filter-expires" :label="$t('views.settings.filter.expires')" />
            <div class="grow"></div>
            <div class="hidden lg:block">
                <VSearchButton v-model="filter" />
            </div>
        </div>

        <div class="flex-1">
            <VTable
                :items="qualifications"
                class="scrollbar-invisible interactive-table no-header xs:px-8 overflow-x-auto px-4 pt-4 md:px-16 xl:px-20"
                @click="editQualification($event.item)"
            >
                <template #row="{ item }">
                    <QualificationRow :qualification="item" />
                </template>
                <template v-if="hasPermission(Permission.WRITE_QUALIFICATIONS)" #context-menu="{ item }">
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
            v-if="hasPermission(Permission.WRITE_QUALIFICATIONS)"
            class="pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12"
        >
            <button class="btn-floating pointer-events-auto" data-test-id="button-create" @click="createQualification()">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>{{ $t('views.settings.qualifications.add-new') }}</span>
            </button>
        </div>
        <QualificationEditDlg ref="qualificationDetailsDialog" data-test-id="details-dialog" />
        <VConfirmationDialog ref="deleteQualificationDialog" data-test-id="delete-confirm-dialog" />
    </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useQualificationsAdministrationUseCase } from '@/application';
import type { Qualification } from '@/domain';
import { Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VConfirmationDialog, VSearchButton, VTable } from '@/ui/components/common';
import { FilterMultiselect, FilterToggle } from '@/ui/components/filters';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQuery } from '@/ui/composables/QueryState.ts';
import { useSession } from '@/ui/composables/Session.ts';
import QualificationEditDlg from '../components/QualificationDetailsDlg.vue';
import QualificationRow from '@/ui/views/settings/tabs/QualificationRow.vue';

const positions = usePositions();
const qualificationAdministrationUseCase = useQualificationsAdministrationUseCase();
const { hasPermission } = useSession();

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
