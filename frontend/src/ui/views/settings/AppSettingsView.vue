<template>
    <div class="xl:overflow-x-hidden xl:overflow-y-auto">
        <DetailsPage>
            <template #header> Einstellungen </template>
            <template #content>
                <VTabs v-if="settings" v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 xl:top-20 xl:pt-8">
                    <template #[Tab.GENERAL_SETTINGS]>
                        <div class="items-start gap-32">
                            <section class="max-w-2xl">
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.ui.menuTitle" label="Menü Titel" required />
                                </div>
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.ui.tabTitle" label="Tab Titel Präfix" required />
                                </div>
                                <div class="mt-12 mb-4">
                                    <VInputText
                                        v-model.trim="settings.ui.technicalSupportEmail"
                                        label="Technischer Support Email"
                                        required
                                    />
                                </div>
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.ui.supportEmail" label="Fachlicher Support Email" required />
                                </div>
                            </section>
                        </div>
                    </template>
                    <template #[Tab.EMAIL]>
                        <div class="items-start gap-32">
                            <section class="max-w-2xl">
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.email.from" label="From" required />
                                </div>
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.email.fromDisplayName" label="From Anzeigename" required />
                                </div>
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.email.replyTo" label="Reply to" required />
                                </div>
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.email.replyToDisplayName" label="Reply to Anzeigename" required />
                                </div>
                                <div class="mt-12 mb-4">
                                    <VInputText v-model.trim="settings.email.host" label="Hostname" required />
                                </div>
                                <div class="mb-4">
                                    <VInputNumber v-model="settings.email.port" label="Port" required />
                                </div>
                                <div class="mb-4">
                                    <VInputCheckBox v-model="settings.email.enableStartTls" label="Start TLS" />
                                </div>
                                <div class="mb-4">
                                    <VInputCheckBox v-model="settings.email.enableSSL" label="SSL" />
                                </div>
                                <div class="mt-12 mb-4">
                                    <VInputText label="Authentifizierungsmethode" model-value="Benutzername / Passwort" required disabled />
                                </div>
                                <div class="mb-4">
                                    <VInputText v-model.trim="settings.email.username" label="Benutzername" required />
                                </div>
                                <div class="mb-4">
                                    <VInputText
                                        v-model.trim="settings.email.password"
                                        label="Password"
                                        type="password"
                                        placeholder="****************"
                                        required
                                    />
                                </div>
                            </section>
                        </div>
                    </template>
                    <template #[Tab.NOTIFICATIONS]>
                        <div class="items-start gap-32">
                            <section class="max-w-2xl">
                                <div class="mb-4">
                                    <VInputTextArea v-model.trim="settings.notifications.teamsWebhookUrl" label="MS Teams Webhook" />
                                </div>
                            </section>
                        </div>
                    </template>
                </VTabs>
            </template>
            <template #primary-button>
                <AsyncButton :action="save" name="save">
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
import { useI18n } from 'vue-i18n';
import type { AppSettings } from '@/domain';
import { AsyncButton, VInputCheckBox, VInputNumber, VInputText, VInputTextArea, VTabs } from '@/ui/components/common';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useAppSettingsUseCase } from '@/ui/composables/Application.ts';

enum Tab {
    GENERAL_SETTINGS = 'general',
    EMAIL = 'email',
    NOTIFICATIONS = 'notifications',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const appSettingsUseCase = useAppSettingsUseCase();

const tabs = [Tab.GENERAL_SETTINGS, Tab.EMAIL, Tab.NOTIFICATIONS].map((it) => ({
    value: it,
    label: t(`views.settings.tab.${it}`),
}));
const tab = ref<Tab>(tabs[0].value);
const settings = ref<AppSettings | null>(null);

function init(): void {
    emit('update:tab-title', 'Einstellungen');
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
