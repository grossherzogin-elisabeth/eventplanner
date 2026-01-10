<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-x-hidden xl:overflow-y-auto">
        <teleport to="#nav-right">
            <div class="h-full lg:hidden">
                <NavbarFilter v-if="tab === Tab.QUALIFICATIONS" v-model="filter" />
                <NavbarFilter v-else-if="tab === Tab.POSITIONS" v-model="filter" />
            </div>
        </teleport>
        <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-0 xl:pt-8">
            <template #[Tab.QUALIFICATIONS]>
                <div class="xs:-mx-8 -mx-4 h-full md:-mx-16 xl:-mx-20">
                    <TabQualifications />
                </div>
            </template>
            <template #[Tab.POSITIONS]>
                <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                    <TabPositions />
                </div>
            </template>
            <template #[Tab.GENERAL_SETTINGS]>
                <div class="lg:max-w-xl">
                    <TabUi v-if="settings" :model-value="settings" @update:model-value="saveSettings($event)" />
                </div>
            </template>
            <template #[Tab.NOTIFICATIONS]>
                <div class="lg:max-w-xl">
                    <TabNotifications v-if="settings" :model-value="settings" @update:model-value="saveSettings($event)" />
                </div>
            </template>
        </VTabs>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useAppSettingsUseCase } from '@/application';
import type { AppSettings } from '@/domain';
import { VTabs } from '@/ui/components/common';
import NavbarFilter from '@/ui/components/utils/NavbarFilter.vue';
import { useQuery } from '@/ui/composables/QueryState.ts';
import TabNotifications from './TabNotifications.vue';
import TabPositions from './TabPositions.vue';
import TabQualifications from './TabQualifications.vue';
import TabUi from './TabUi.vue';

enum Tab {
    GENERAL_SETTINGS = 'general',
    NOTIFICATIONS = 'notifications',
    POSITIONS = 'positions',
    QUALIFICATIONS = 'qualifications',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const appSettingsUseCase = useAppSettingsUseCase();

const tabs = [Tab.GENERAL_SETTINGS, Tab.NOTIFICATIONS, Tab.POSITIONS, Tab.QUALIFICATIONS].map((it) => ({
    value: it,
    label: t(`views.settings.tab.${it}`),
}));
const tab = ref<Tab>(tabs[0].value);
const settings = ref<AppSettings | null>(null);
const filter = useQuery<string>('filter', '').parameter;

function init(): void {
    emit('update:tab-title', 'Einstellungen');
    fetchSettings();
}

async function saveSettings(updatedSettings: AppSettings): Promise<void> {
    settings.value = await appSettingsUseCase.updateSettings(updatedSettings);
}

async function fetchSettings(): Promise<void> {
    settings.value = await appSettingsUseCase.getAdminSettings();
}

init();
</script>
