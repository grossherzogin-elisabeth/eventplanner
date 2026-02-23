<template>
    <div class="xl:overflow-x-hidden xl:overflow-y-auto">
        <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-0 xl:pt-8">
            <template #[Tab.PERSONAL_DATA]>
                <div class="lg:max-w-xl">
                    <AccountDataTab v-if="userDetails" :model-value="userDetails" @update:model-value="updateUserDetails($event)" />
                </div>
            </template>
            <template #[Tab.SETTINGS]>
                <div class="lg:max-w-xl">
                    <UserSettingsTab
                        v-if="userDetails && userSettings"
                        :user="userDetails"
                        :model-value="userSettings"
                        @update:model-value="updateUserSettings($event)"
                    />
                </div>
            </template>
            <template #[Tab.QUALIFICATIONS]>
                <div class="xl:max-w-5xl">
                    <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                        <UserQualificationsTab v-if="userDetails" :user="userDetails" />
                    </div>
                </div>
            </template>
        </VTabs>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useUsersUseCase } from '@/application';
import type { InputSelectOption, UserDetails, UserSettings } from '@/domain';
import { VTabs } from '@/ui/components/common';
import AccountDataTab from '@/ui/views/account/tabs/AccountDataTab.vue';
import UserSettingsTab from '@/ui/views/account/tabs/UserSettingsTab.vue';
import UserQualificationsTab from './tabs/UserQualificationsTab.vue';

enum Tab {
    PERSONAL_DATA = 'data',
    SETTINGS = 'settings',
    QUALIFICATIONS = 'qualifications',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const usersUseCase = useUsersUseCase();
const userDetails = ref<UserDetails | null>(null);
const userSettings = ref<UserSettings | null>(null);

const tabs: InputSelectOption[] = [Tab.PERSONAL_DATA, Tab.SETTINGS, Tab.QUALIFICATIONS].map((it) => ({
    value: it,
    label: t(`views.account.tab.${it}`),
}));
const tab = ref<string>(tabs[0].value);

function init(): void {
    emit('update:tab-title', t('views.account.title'));
    fetchUserDetails();
    fetchUserSettings();
}
async function fetchUserDetails(): Promise<void> {
    userDetails.value = await usersUseCase.getUserDetailsForSignedInUser();
}

async function updateUserDetails(changes: UserDetails): Promise<void> {
    if (userDetails.value) {
        userDetails.value = await usersUseCase.updateUserDetailsForSignedInUser(userDetails.value, changes);
    }
}

async function fetchUserSettings(): Promise<void> {
    userSettings.value = await usersUseCase.getUserSettings();
}

async function updateUserSettings(settings: UserSettings): Promise<void> {
    userSettings.value = await usersUseCase.saveUserSettings(settings);
}

init();
</script>
