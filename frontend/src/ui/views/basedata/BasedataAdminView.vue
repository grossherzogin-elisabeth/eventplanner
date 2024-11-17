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
                    <div class="hidden 2xl:block">
                        <button class="btn-primary" @click="createQualification()">
                            <i class="fa-solid fa-file-circle-plus"></i>
                            <span>Qualifikation hinzufügen</span>
                        </button>
                    </div>
                </div>
                <div v-else-if="tab === Tab.POSITIONS" class="flex items-stretch gap-2 pb-2">
                    <VSearchButton v-model="positionsFilter" placeholder="Einträge filtern" class="w-48" />
                    <div class="hidden flex-grow md:block"></div>
                    <div class="hidden 2xl:block">
                        <button class="btn-primary" @click="createPosition()">
                            <i class="fa-solid fa-file-circle-plus"></i>
                            <span>Position hinzufügen</span>
                        </button>
                    </div>
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

        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-if="tab === Tab.POSITIONS"
            class="permission-write-positions pointer-events-none sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" @click="createPosition()">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>Position hinzufügen</span>
            </button>
        </div>
        <div
            v-else-if="tab === Tab.QUALIFICATIONS"
            class="permission-write-positions pointer-events-none sticky bottom-0 right-0 z-10 mt-4 flex justify-end pb-4 pr-3 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" @click="createQualification()">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>Qualifikation hinzufügen</span>
            </button>
        </div>
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

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const tabs = [Tab.QUALIFICATIONS, Tab.POSITIONS];
const tab = ref<string>(tabs[0]);
const qualificationFilter = ref<string>('');
const positionsFilter = ref<string>('');
const qualificationsTable = ref<{ createQualification(): void } | null>(null);
const positionsTable = ref<{ createPosition(): void } | null>(null);

function init(): void {
    emit('update:title', 'Stammdaten');
}

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

init();
</script>
