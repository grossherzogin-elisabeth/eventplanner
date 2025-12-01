<template>
    <div class="xl:overflow-auto">
        <div class="xs:px-8 px-4 py-8 md:px-16 xl:px-20">
            <div class="max-w-2xl">
                <h2 class="mb-4 hidden xl:block">Feedback</h2>
                <p class="mb-8">
                    Du hast einen Bug gefunden oder möchtest uns generelles Feedback geben? Schicke uns gerne eine Mail an
                    <a class="link" :href="`mailto:${config.technicalSupportEmail}`">
                        {{ config.technicalSupportEmail }}
                        <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i> </a
                    >. Je detailierter du einen Fehler dabei beschreibst, desto leichter können wir ihn nachstellen und beheben. Wenn du
                    einen GitHub Account hast, kannst du zum Melden von Bugs auch direkt ein Ticket in unserem
                    <a class="link" href="https://github.com/grossherzogin-elisabeth/eventplanner/issues" target="_blank">
                        Issue Tracker
                        <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i>
                    </a>
                    anlegen.
                </p>
                <VInfo class="xs:-mx-4 mb-8">
                    Viele Fehler lassen sich schon dadurch beheben, die Seite einmal neu zu laden und es noch einmal zu probieren. Klicke
                    dazu auf den Neu Laden Button <i class="fa-solid fa-rotate-right"></i>, den du meistens oben links in deinem
                    Browserfenster findest oder verwende die Tastenkombination <code>STRG</code> + <code>R</code> (Windows) bzw.
                    <code>CMD</code> + <code>R</code> (MacOS). Auf mobilen Geräten kannst du die Seite nach unten ziehen, um sie neu zu
                    laden.
                </VInfo>

                <h2 class="mb-4">Beta Features</h2>
                <div class="mb-8">
                    <VInputCheckBox
                        label="Dark Mode ausprobieren"
                        :model-value="settings?.theme === Theme.Dark"
                        @update:model-value="enableDarkMode($event)"
                    />
                </div>

                <h2 class="mb-4">System Status</h2>
                <table class="mb-8">
                    <tbody>
                        <tr>
                            <td class="py-1 pr-4">Build:</td>
                            <td v-if="systemInfo.buildDate" class="py-1 font-bold">
                                {{ $d(systemInfo.buildDate, DateTimeFormat.DD_MM_YYYY) }}
                                {{ $d(systemInfo.buildDate, DateTimeFormat.hh_mm) }}
                            </td>
                            <td v-else class="py-1 font-bold">-</td>
                        </tr>
                        <tr>
                            <td class="py-1 pr-4">Branch:</td>
                            <td class="py-1 font-bold">
                                <a
                                    :href="`https://github.com/grossherzogin-elisabeth/eventplanner/tree/${systemInfo.buildBranch}`"
                                    class="link"
                                    target="_blank"
                                >
                                    {{ systemInfo.buildBranch }}
                                    <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i>
                                </a>
                            </td>
                        </tr>
                        <tr>
                            <td class="py-1 pr-4">Commit:</td>
                            <td class="py-1 font-bold">
                                <a
                                    :href="`https://github.com/grossherzogin-elisabeth/eventplanner/commit/${systemInfo.buildCommit}`"
                                    class="link"
                                    target="_blank"
                                >
                                    #{{ systemInfo.buildCommit }}
                                    <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i>
                                </a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useConfigService, useUsersUseCase } from '@/application';
import { DateTimeFormat } from '@/common/date';
import type { UserSettings } from '@/domain';
import { Theme } from '@/domain';
import { VInfo, VInputCheckBox } from '@/ui/components/common';

interface SystemInfo {
    buildDate?: Date;
    buildBranch?: string;
    buildCommit?: string;
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const config = useConfigService().getConfig();
const usersUseCase = useUsersUseCase();

const systemInfo = ref<SystemInfo>({});
const settings = ref<UserSettings | undefined>(undefined);

function init(): void {
    emit('update:tab-title', 'Feedback');
    fetchStatus();
    fetchSettings();
}

async function fetchStatus(): Promise<void> {
    const response = await fetch('api/v1/status');
    systemInfo.value = await response.clone().json();
}

async function fetchSettings(): Promise<void> {
    settings.value = await usersUseCase.getUserSettings();
}

async function enableDarkMode(enabled: boolean): Promise<void> {
    if (settings.value) {
        settings.value.theme = enabled ? Theme.Dark : Theme.Light;
        settings.value = await usersUseCase.saveUserSettings(settings.value);
    }
}

init();
</script>
