<template>
    <VTable
        :items="positions"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        @click="editPosition($event.item)"
    >
        <template #row="{ item }">
            <td>
                <div class="bg-surface-container-high flex w-12 items-center justify-center rounded-full px-2 py-1">
                    <i class="fa-solid fa-arrow-up text-xs"></i>
                    <span class="ml-2 text-center text-sm font-semibold">
                        {{ item.prio }}
                    </span>
                </div>
            </td>
            <td class="w-3/4 min-w-80">
                <p class="truncate font-semibold">{{ item.name }}</p>
            </td>
            <td class="w-1/4">
                <div class="flex items-center space-x-2">
                    <div class="mb-0.5 h-6 w-6 rounded-lg" :style="{ background: item.color }"></div>
                    <p class="truncate font-bold" :style="{ color: item.color }">
                        {{ item.color }}
                    </p>
                </div>
            </td>
            <td class="w-96">
                <span class="tag custom" :style="{ '--color': item.color }">
                    {{ item.name }}
                </span>
            </td>
        </template>
        <template #context-menu="{ item }">
            <li class="context-menu-item" @click="editPosition(item)">
                <i class="fa-solid fa-edit" />
                <span>{{ $t('generic.edit') }}</span>
            </li>
            <li class="context-menu-item text-error" @click="deletePosition(item)">
                <i class="fa-solid fa-trash-alt" />
                <span>{{ $t('generic.delete') }}</span>
            </li>
        </template>
    </VTable>
    <PositionDetailsDlg ref="positionDetailsDialog" />
    <VConfirmationDialog ref="deletePositionDialog" />
</template>
<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { usePositionAdministrationUseCase } from '@/application';
import type { Position } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';
import { VTable } from '@/ui/components/common';
import PositionDetailsDlg from './PositionDetailsDlg.vue';

interface Props {
    filter?: string;
}

const props = defineProps<Props>();

const positionAdministrationUseCase = usePositionAdministrationUseCase();

const positions = ref<Position[] | undefined>(undefined);

const positionDetailsDialog = ref<Dialog<Position | undefined, Position | undefined> | null>(null);
const deletePositionDialog = ref<ConfirmationDialog | null>(null);

const { t } = useI18n();

function init(): void {
    fetchPositions();
    watch(() => props.filter, fetchPositions);
}

async function fetchPositions(): Promise<void> {
    positions.value = await positionAdministrationUseCase.getPositions(props.filter);
}

async function createPosition(): Promise<void> {
    const newPosition = await positionDetailsDialog.value?.open();
    if (newPosition) {
        await positionAdministrationUseCase.createPosition(newPosition);
        await fetchPositions();
    }
}

async function editPosition(position: Position): Promise<void> {
    const editedPosition = await positionDetailsDialog.value?.open(position);
    if (editedPosition) {
        await positionAdministrationUseCase.updatePosition(editedPosition);
        await fetchPositions();
    }
}

async function deletePosition(position: Position): Promise<void> {
    const confirmed = await deletePositionDialog.value?.open({
        title: t('views.basedata.tab.positions.delete-title'),
        message: t('views.basedata.tab.positions.delete-message'),
        submit: t('generic.delete'),
        danger: true,
    });
    if (confirmed) {
        await positionAdministrationUseCase.deletePosition(position);
        await fetchPositions();
    }
}

defineExpose({
    createPosition: () => createPosition(),
});

init();
</script>
