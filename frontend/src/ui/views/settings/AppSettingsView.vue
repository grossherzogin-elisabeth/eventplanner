<template>
    <div class="xl:overflow-y-auto xl:overflow-x-hidden">
        <DetailsPage>
            <template #content>
                <VTabs
                    v-if="settings"
                    v-model="tab"
                    :tabs="tabs"
                    class="sticky top-12 z-20 bg-surface pt-4 xl:top-0 xl:pt-8"
                >
                    <template #[Tab.GENERAL_SETTINGS]>
                        <div class="items-start gap-32 2xl:flex">
                            <section class="max-w-2xl">
                                <div class="mb-4">
                                    <VInputLabel>Menü Titel</VInputLabel>
                                    <VInputText v-model="settings.ui.menuTitle" required />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>Tab Titel Präfix</VInputLabel>
                                    <VInputText v-model="settings.ui.tabTitle" required />
                                </div>
                                <div class="mb-4 mt-12">
                                    <VInputLabel>Technischer Support Email</VInputLabel>
                                    <VInputText v-model="settings.ui.technicalSupportEmail" required />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>Fachlicher Support Email</VInputLabel>
                                    <VInputText v-model="settings.ui.supportEmail" required />
                                </div>
                            </section>
                        </div>
                    </template>
                    <template #[Tab.EMAIL]>
                        <div class="items-start gap-32">
                            <section class="max-w-2xl">
                                <div class="mb-4">
                                    <VInputLabel>From</VInputLabel>
                                    <VInputText v-model="settings.email.from" required />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>From Anzeigename</VInputLabel>
                                    <VInputText v-model="settings.email.fromDisplayName" required />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>Reply to</VInputLabel>
                                    <VInputText v-model="settings.email.replyTo" required />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>Reply to Anzeigename</VInputLabel>
                                    <VInputText v-model="settings.email.replyToDisplayName" required />
                                </div>
                                <div class="mb-4 mt-12">
                                    <VInputLabel>Hostname</VInputLabel>
                                    <VInputText v-model="settings.email.host" required />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>Port</VInputLabel>
                                    <VInputNumber v-model="settings.email.port" required />
                                </div>
                                <div class="mb-4">
                                    <VInputCheckBox v-model="settings.email.enableStartTls" label="Start TLS" />
                                </div>
                                <div class="mb-4">
                                    <VInputCheckBox v-model="settings.email.enableSSL" label="SSL" />
                                </div>
                                <div class="mb-4 mt-12">
                                    <VInputLabel>Authentifizierungsmethode</VInputLabel>
                                    <VInputText model-value="Benutzername / Passwort" required disabled />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>Benutzername</VInputLabel>
                                    <VInputText v-model="settings.email.username" required />
                                </div>
                                <div class="mb-4">
                                    <VInputLabel>Password</VInputLabel>
                                    <VInputText
                                        v-model="settings.email.password"
                                        type="password"
                                        placeholder="****************"
                                        required
                                    />
                                </div>
                            </section>
                        </div>
                    </template>
                </VTabs>
            </template>
            <template #primary-button>
                <AsyncButton :action="save">
                    <template #icon>
                        <i class="fa-solid fa-save"></i>
                    </template>
                    <template #label>
                        <span>Speichern</span>
                    </template>
                </AsyncButton>
            </template>
        </DetailsPage>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import type { AppSettings } from '@/domain';
import { AsyncButton, VInputCheckBox, VInputLabel, VInputNumber, VInputText, VTabs } from '@/ui/components/common';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useAppSettingsUseCase } from '@/ui/composables/Application.ts';

enum Tab {
    GENERAL_SETTINGS = 'Allgemein',
    EMAIL = 'Email',
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const appSettingsUseCase = useAppSettingsUseCase();

const tabs = [Tab.GENERAL_SETTINGS, Tab.EMAIL];
const tab = ref<Tab>(tabs[0]);
const settings = ref<AppSettings | null>(null);

function init(): void {
    emit('update:title', 'Einstellungen');
    fetchSettings();
}

async function save(): Promise<void> {
    if (settings.value) {
        await appSettingsUseCase.updateSettings(settings.value);
    }
}

async function fetchSettings(): Promise<void> {
    settings.value = await appSettingsUseCase.getAdminSettings();
}

init();
</script>
