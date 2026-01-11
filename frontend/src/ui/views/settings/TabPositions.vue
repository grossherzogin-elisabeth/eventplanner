<template>
    <div class="flex h-full flex-col">
        <div class="flex-1">
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
                        <p class="truncate font-semibold" data-test-id="position-name">{{ item.name }}</p>
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
                <template v-if="signedInUser.permissions.includes(Permission.WRITE_POSITIONS)" #context-menu="{ item }">
                    <li class="context-menu-item" data-test-id="context-menu-edit" @click="editPosition(item)">
                        <i class="fa-solid fa-edit" />
                        <span>{{ $t('generic.edit') }}</span>
                    </li>
                    <li class="context-menu-item text-error" data-test-id="context-menu-delete" @click="deletePosition(item)">
                        <i class="fa-solid fa-trash-alt" />
                        <span>{{ $t('generic.delete') }}</span>
                    </li>
                </template>
            </VTable>
        </div>

        <div
            v-if="signedInUser.permissions.includes(Permission.WRITE_POSITIONS)"
            class="pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12"
        >
            <button class="btn-floating pointer-events-auto" data-test-id="button-create" @click="createPosition()">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>{{ $t('views.settings.positions.add-new') }}</span>
            </button>
        </div>

        <PositionDetailsDlg ref="positionDetailsDialog" data-test-id="details-dialog" />
        <VConfirmationDialog ref="deletePositionDialog" data-test-id="delete-confirm-dialog" />
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useAuthUseCase, usePositionAdministrationUseCase } from '@/application';
import type { Position } from '@/domain';
import { Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VConfirmationDialog, VTable } from '@/ui/components/common';
import PositionDetailsDlg from './components/PositionDetailsDlg.vue';

const signedInUser = useAuthUseCase().getSignedInUser();
const positionAdministrationUseCase = usePositionAdministrationUseCase();

const positions = ref<Position[] | undefined>(undefined);

const positionDetailsDialog = ref<Dialog<Position | undefined, Position | undefined> | null>(null);
const deletePositionDialog = ref<ConfirmationDialog | null>(null);

const { t } = useI18n();

function init(): void {
    fetchPositions();
}

async function fetchPositions(): Promise<void> {
    positions.value = await positionAdministrationUseCase.getPositions();
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
        title: t('views.settings.positions.delete-title'),
        message: t('views.settings.positions.delete-message'),
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
