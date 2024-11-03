<template>
    <div class="xl:overflow-auto">
        <div class="px-8 py-8 md:px-16 xl:px-20">
            <div class="max-w-2xl">
                <h2 class="mb-4">Feedback</h2>
                <p class="mb-8">
                    Du hast einen Bug gefunden oder möchtest uns generelles Feedback geben? Schicke uns gerne eine Mail
                    an
                    <a class="link" :href="`mailto:${config.technicalSupportEmail}`">
                        {{ config.technicalSupportEmail }} </a
                    >. Wenn du einen GitHub Account hast, kannst du zum Melden von Bugs auch direkt ein Ticket in
                    unserem
                    <a
                        class="link"
                        href="https://github.com/grossherzogin-elisabeth/eventplanner/issues"
                        target="_blank"
                    >
                        Issue Tracker
                        <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i>
                    </a>
                    anlegen.
                </p>
                <h2 class="mb-4">System Status</h2>
                <table class="mb-8">
                    <tbody>
                        <tr>
                            <td class="py-1 pr-4">Uptime:</td>
                            <td class="py-1 font-bold">? Tage</td>
                        </tr>
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
import { DateTimeFormat } from '@/common/date';
import { useConfig } from '@/ui/composables/Application';

interface SystemInfo {
    buildDate?: Date;
    buildBranch?: string;
    buildCommit?: string;
}

const config = useConfig();
const systemInfo = ref<SystemInfo>({});

async function fetchStatus(): Promise<void> {
    const response = await fetch('api/v1/status');
    systemInfo.value = await response.clone().json();
}
fetchStatus();
</script>
