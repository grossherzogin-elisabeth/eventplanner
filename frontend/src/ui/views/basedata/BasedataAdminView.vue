<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-auto xl:overflow-x-hidden">
        <teleport to="#nav-right">
            <NavbarFilter
                v-if="tab === Tab.QUALIFICATIONS"
                v-model="qualificationFilter"
                placeholder="Qualifikationen durchsuchen"
            />
            <NavbarFilter
                v-else-if="tab === Tab.POSITIONS"
                v-model="positionsFilter"
                placeholder="Positionen durchsuchen"
            />
        </teleport>
        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div v-if="tab === Tab.QUALIFICATIONS" class="flex items-stretch gap-2 pb-2">
                    <VSearchButton v-model="qualificationFilter" placeholder="Einträge filtern" class="w-48" />
                    <div class="hidden flex-grow md:block"></div>
                    <button class="btn-primary" @click="createQualification()">
                        <i class="fa-solid fa-file-circle-plus"></i>
                        <span>Qualifikation hinzufügen</span>
                    </button>
                </div>
                <div v-else-if="tab === Tab.POSITIONS" class="flex items-stretch gap-2 pb-2">
                    <VSearchButton v-model="positionsFilter" placeholder="Einträge filtern" class="w-48" />
                    <div class="hidden flex-grow md:block"></div>
                    <button class="btn-primary" @click="createPosition()">
                        <i class="fa-solid fa-file-circle-plus"></i>
                        <span>Position hinzufügen</span>
                    </button>
                </div>
            </template>
            <template #[Tab.QUALIFICATIONS]>
                <div class="-mx-8 md:-mx-16 xl:-mx-20">
                    <QualificationsTable ref="qualificationsTable" :filter="qualificationFilter" />
                </div>
            </template>
            <template #[Tab.POSITIONS]>
                <div class="-mx-8 md:-mx-16 xl:-mx-20">
                    <PositionsTable ref="positionsTable" :filter="positionsFilter" />
                </div>
            </template>
        </VTabs>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import PositionsTable from './PositionsTable.vue';
import QualificationsTable from './QualificationsTable.vue';

enum Tab {
    QUALIFICATIONS = 'Qualifikationen',
    POSITIONS = 'Positionen',
}

const tabs = [Tab.QUALIFICATIONS, Tab.POSITIONS];
const tab = ref<string>(tabs[0]);
const qualificationFilter = ref<string>('');
const positionsFilter = ref<string>('');
const qualificationsTable = ref<{ createQualification(): void } | null>(null);
const positionsTable = ref<{ createPosition(): void } | null>(null);

function createQualification(): void {
    if (qualificationsTable.value) {
        qualificationsTable.value.createQualification();
    }
}

function createPosition(): void {
    if (positionsTable.value) {
        positionsTable.value.createPosition();
    }
}
</script>
