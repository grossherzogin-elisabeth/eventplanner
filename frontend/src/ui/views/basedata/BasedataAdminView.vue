<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-x-hidden xl:overflow-y-auto">
        <teleport to="#nav-right">
            <NavbarFilter
                v-if="tab === Tab.QUALIFICATIONS"
                v-model="qualificationFilter"
                :placeholder="$t('views.basedata.tab.qualifications.search-placeholder')"
            />
            <NavbarFilter
                v-else-if="tab === Tab.POSITIONS"
                v-model="positionsFilter"
                :placeholder="$t('views.basedata.tab.positions.search-placeholder')"
            />
        </teleport>
        <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-0 xl:pt-8">
            <template #end>
                <div v-if="tab === Tab.QUALIFICATIONS" class="flex items-stretch gap-2 pb-2">
                    <div class="hidden 2xl:block">
                        <button class="btn-primary" name="create" @click="createQualification()">
                            <i class="fa-solid fa-file-circle-plus"></i>
                            <span>{{ $t('views.basedata.tab.qualifications.add-new') }}</span>
                        </button>
                    </div>
                    <VSearchButton v-model="qualificationFilter" :placeholder="$t('generic.filter-entries')" class="w-48" />
                </div>
                <div v-else-if="tab === Tab.POSITIONS" class="flex items-stretch gap-2 pb-2">
                    <div class="hidden 2xl:block">
                        <button class="btn-primary" name="create" @click="createPosition()">
                            <i class="fa-solid fa-file-circle-plus"></i>
                            <span>{{ $t('views.basedata.tab.positions.add-new') }}</span>
                        </button>
                    </div>
                    <VSearchButton v-model="positionsFilter" :placeholder="$t('generic.filter-entries')" class="w-48" />
                </div>
            </template>
            <template #[Tab.QUALIFICATIONS]>
                <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                    <QualificationsTable ref="qualificationsTable" :filter="qualificationFilter" />
                </div>
            </template>
            <template #[Tab.POSITIONS]>
                <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                    <PositionsTable ref="positionsTable" :filter="positionsFilter" />
                </div>
            </template>
        </VTabs>

        <!-- the floating action button would overlap with the multiselect actions, so only show one of those two -->
        <div
            v-if="tab === Tab.POSITIONS"
            class="permission-write-positions pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" @click="createPosition()">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>{{ $t('views.basedata.tab.positions.add-new') }}</span>
            </button>
        </div>
        <div
            v-else-if="tab === Tab.QUALIFICATIONS"
            class="permission-write-positions pointer-events-none sticky right-0 bottom-0 z-10 mt-4 flex justify-end pr-3 pb-4 md:pr-7 xl:pr-12 2xl:hidden"
        >
            <button class="btn-floating pointer-events-auto" @click="createQualification()">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>{{ $t('views.basedata.tab.qualifications.add-new') }}</span>
            </button>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { VTabs } from '@/ui/components/common';
import VSearchButton from '@/ui/components/common/input/VSearchButton.vue';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import PositionsTable from './PositionsTable.vue';
import QualificationsTable from './QualificationsTable.vue';

enum Tab {
    QUALIFICATIONS = 'qualifications',
    POSITIONS = 'positions',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();

const tabs = [Tab.QUALIFICATIONS, Tab.POSITIONS].map((it) => ({
    value: it,
    label: t(`views.basedata.tab.${it}.title`),
}));
const tab = ref<string>(tabs[0].value);
const qualificationFilter = ref<string>('');
const positionsFilter = ref<string>('');
const qualificationsTable = ref<{ createQualification(): void } | null>(null);
const positionsTable = ref<{ createPosition(): void } | null>(null);

function init(): void {
    emit('update:tab-title', t('views.basedata.title'));
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
