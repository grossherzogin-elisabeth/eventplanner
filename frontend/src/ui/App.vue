<template>
    <div
        class="flex min-h-screen flex-col from-p-700 to-p-600 to-50% md:bg-gradient-to-r"
        :class="{ impersonated: signedInUser?.impersonated }"
    >
        <div id="navbar" class="md:hidden">
            <AppNavbar :title="title" />
        </div>
        <VNotifications id="notifications" />
        <div class="fixed bottom-0 left-0 top-0 z-50 hidden text-onprimary md:block xl:hidden">
            <AppMenu class="relative z-10 h-screen" :collapsed="true" />
        </div>
        <div class="flex flex-1 items-stretch">
            <div class="relative hidden h-screen w-96 flex-col xl:flex">
                <AppMenu class="relative z-10" :collapsed="false" />
            </div>
            <div class="relative flex w-0 flex-grow flex-col md:ml-16 md:h-screen xl:ml-0">
                <div class="flex flex-1 flex-col bg-surface md:overflow-hidden md:rounded-l-3xl md:shadow-2xl">
                    <RouterView id="router-view" v-model:title="title" class="flex flex-1 flex-col md:overflow-y-auto" />
                </div>
            </div>
        </div>
    </div>
    <VErrorDialog />
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { SignedInUser } from '@/domain';
import { Permission } from '@/domain';
import { VErrorDialog } from '@/ui/components/common';
import AppMenu from '@/ui/components/partials/AppMenu.vue';
import AppNavbar from '@/ui/components/partials/AppNavbar.vue';
import VNotifications from '@/ui/components/partials/VNotifications.vue';
import { useAuthUseCase, useConfig } from '@/ui/composables/Application';
import { useViewportSize } from '@/ui/composables/ViewportSize';

useViewportSize();
const config = useConfig();
const authUseCase = useAuthUseCase();

const signedInUser = ref<SignedInUser | null>(null);
const title = ref<string>('');

async function init(): Promise<void> {
    console.info('🚀 Mounting app');
    setTitle();
    watch(title, setTitle);
    authUseCase.onLogin().then(() => (signedInUser.value = authUseCase.getSignedInUser()));
    authUseCase.onLogout().then(() => (signedInUser.value = null));
    watch(signedInUser, () => {
        Object.values(Permission).forEach((permission) => document.body.classList.remove(permission));
        signedInUser.value?.permissions.forEach((permission) => document.body.classList.add(permission));
    });
}

function setTitle(): void {
    if (title.value) {
        document.title = `${config.tabTitle} | ${title.value}`;
    } else {
        document.title = config.tabTitle;
    }
}

init();
</script>
