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
        <div class="top-0 hidden bg-primary-50 px-4 pb-8 pl-16 pr-20 pt-8 xl:block">
            <div v-if="tab === Tab.QUALIFICATIONS" class="flex items-center space-x-4">
                <VInputText
                    v-model="qualificationFilter"
                    class="input-search w-96"
                    placeholder="Qualifikationen durchsuchen"
                >
                    <template #before>
                        <i class="fa-solid fa-magnifying-glass ml-4 text-primary-900 text-opacity-25" />
                    </template>
                </VInputText>
                <div class="hidden flex-grow md:block"></div>
                <button class="btn-primary" @click="createQualification()">
                    <i class="fa-solid fa-file-circle-plus"></i>
                    <span>Qualifikation hinzufügen</span>
                </button>
            </div>
            <div v-else-if="tab === Tab.POSITIONS" class="flex items-center space-x-4">
                <VInputText v-model="positionsFilter" class="input-search w-96" placeholder="Positionen durchsuchen">
                    <template #before>
                        <i class="fa-solid fa-magnifying-glass ml-4 text-primary-900 text-opacity-25" />
                    </template>
                </VInputText>
                <div class="hidden flex-grow md:block"></div>
                <button class="btn-primary" @click="createPosition()">
                    <i class="fa-solid fa-file-circle-plus"></i>
                    <span>Position hinzufügen</span>
                </button>
            </div>
        </div>
        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8">
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
import { VInputText, VTabs } from '@/ui/components/common';
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

function createQualification() {
    if (qualificationsTable.value) {
        qualificationsTable.value.createQualification();
    }
}

function createPosition() {
    if (positionsTable.value) {
        positionsTable.value.createPosition();
    }
}
</script>
