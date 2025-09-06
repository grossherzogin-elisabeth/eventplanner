<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <VTabs v-model="tab" :tabs="tabs" class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
            <template #[Tab.PERSONAL_DATA]>
                <div class="xl:max-w-xl">
                    <AccountData v-if="userDetails" :model-value="userDetails" @update:model-value="update($event)" />
                </div>
            </template>
            <template #[Tab.QUALIFICATIONS]>
                <div class="xl:max-w-5xl">
                    <div class="-mx-4 xs:-mx-8 md:-mx-16 xl:-mx-20">
                        <UserQualificationsTable v-if="userDetails" :user="userDetails" />
                    </div>
                </div>
            </template>
        </VTabs>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { InputSelectOption, UserDetails } from '@/domain';
import { VTabs } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import AccountData from '@/ui/views/account/AccountData.vue';
import UserQualificationsTable from './UserQualificationsTable.vue';

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

const tabs: InputSelectOption[] = [Tab.PERSONAL_DATA, Tab.QUALIFICATIONS, Tab.SETTINGS].map((it) => ({
    value: it,
    label: t(`views.account.tab.${it}`),
}));
const tab = ref<string>(tabs[0].value);

async function fetchUserDetails(): Promise<void> {
    userDetails.value = await usersUseCase.getUserDetailsForSignedInUser();
}

async function update(changes: UserDetails): Promise<void> {
    if (userDetails.value) {
        userDetails.value = await usersUseCase.updateUserDetailsForSignedInUser(userDetails.value, changes);
    }
}

function init(): void {
    emit('update:tab-title', t('views.account.title'));
    fetchUserDetails();
}

init();
</script>
