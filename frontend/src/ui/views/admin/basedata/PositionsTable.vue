<template>
    <VTable
        :items="positions"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        @click="editPosition($event)"
    >
        <template #row="{ item }">
            <!--            <td :key="item.icon" class="text-xl">-->
            <!--                <i class="fa-solid" :class="item.icon || 'fa-anchor'" />-->
            <!--            </td>-->
            <td>
                <div class="flex w-12 items-center justify-center rounded-full bg-gray-200 px-2 py-1 text-gray-700">
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
                <span class="position text-sm" :style="{ background: item.color }">
                    {{ item.name }}
                </span>
            </td>
            <td class="w-0">
                <ContextMenuButton class="px-4 py-2">
                    <ul>
                        <li class="context-menu-item" @click="editPosition(item)">
                            <i class="fa-solid fa-pencil" />
                            <span>Bearbeiten</span>
                        </li>
                        <li class="context-menu-item text-red-700" @click="deletePosition(item)">
                            <i class="fa-solid fa-trash-alt" />
                            <span>Löschen</span>
                        </li>
                    </ul>
                </ContextMenuButton>
            </td>
        </template>
    </VTable>
    <PositionCreateDlg ref="createPositionDialog" />
    <PositionEditDlg ref="editPositionDialog" />
    <VConfirmationDialog ref="deletePositionDialog">
        <template #title>Position löschen?</template>
        <template #message>
            Bist du sicher, das du die Position löschen möchtest? Die Position wird bei allen Nutzern, bei denen sie
            assoziert ist, entfernt.
        </template>
        <template #submit>Löschen</template>
    </VConfirmationDialog>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue';
import type { Position } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';
import { ContextMenuButton, VTable } from '@/ui/components/common';
import { usePositionAdministrationUseCase } from '@/ui/composables/Application';
import PositionCreateDlg from '@/ui/views/admin/basedata/PositionCreateDlg.vue';
import PositionEditDlg from '@/ui/views/admin/basedata/PositionEditDlg.vue';

interface Props {
    filter?: string;
}

const props = defineProps<Props>();

const positionAdministrationUseCase = usePositionAdministrationUseCase();

const positions = ref<Position[] | undefined>(undefined);

const createPositionDialog = ref<Dialog<void, Position> | null>(null);
const editPositionDialog = ref<Dialog<Position, Position> | null>(null);
const deletePositionDialog = ref<Dialog<Position> | null>(null);

function init(): void {
    fetchPositions();
    watch(() => props.filter, fetchPositions);
}

async function fetchPositions(): Promise<void> {
    positions.value = await positionAdministrationUseCase.getPositions(props.filter);
}

function createPosition(): void {
    if (createPositionDialog.value) {
        createPositionDialog.value
            .open()
            .then((it) => positionAdministrationUseCase.createPosition(it))
            .then(() => fetchPositions())
            .catch(() => {});
    }
}

function editPosition(position: Position): void {
    if (editPositionDialog.value) {
        editPositionDialog.value
            .open(position)
            .then((it) => positionAdministrationUseCase.updatePosition(it))
            .then(() => fetchPositions())
            .catch(() => {});
    }
}

function deletePosition(position: Position): void {
    if (deletePositionDialog.value) {
        deletePositionDialog.value
            .open(position)
            .then(() => positionAdministrationUseCase.deletePosition(position))
            .then(() => fetchPositions())
            .catch(() => {});
    }
}

defineExpose({
    createPosition: () => createPosition(),
});

init();
</script>
