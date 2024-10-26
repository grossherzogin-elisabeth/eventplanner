<template>
    <div
        class="flex min-h-screen flex-col bg-primary-50 from-primary-900 to-primary-800 to-50% xl:bg-gradient-to-r"
        :class="{ impersonated: signedInUser?.impersonated }"
    >
        <div class="xl:hidden">
            <AppNavbar :title="title" />
        </div>
        <VNotifications />
        <div class="flex flex-1 items-stretch">
            <div class="relative hidden h-screen w-96 flex-col pt-4 text-white xl:flex">
                <AppMenu class="relative z-10" />
            </div>
            <div
                class="relative flex h-full w-0 flex-grow flex-col bg-primary-50 xl:h-screen xl:overflow-hidden xl:rounded-l-3xl xl:shadow-2xl"
            >
                <RouterView id="router-view" v-model:title="title" class="flex flex-1 flex-col" />
            </div>
        </div>
    </div>
    <VErrorDialog />
    <AppFooter />
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import type { SignedInUser } from '@/domain';
import { VErrorDialog } from '@/ui/components/common';
import AppFooter from '@/ui/components/partials/AppFooter.vue';
import AppMenu from '@/ui/components/partials/AppMenu.vue';
import AppNavbar from '@/ui/components/partials/AppNavbar.vue';
import VNotifications from '@/ui/components/partials/VNotifications.vue';
import { useAuthUseCase, useConfig } from '@/ui/composables/Application';
import { useRouterStack } from '@/ui/composables/RouterStack';
import { useViewportSize } from '@/ui/composables/ViewportSize';

useViewportSize();
const config = useConfig();
const routerStack = useRouterStack();
const authUseCase = useAuthUseCase();
const router = useRouter();

const signedInUser = ref<SignedInUser | null>(null);
const title = ref<string>('');

async function init(): Promise<void> {
    console.info('🚀 Mounting app');
    setTitle();
    watch(title, setTitle);
    authUseCase.onLogin().then(() => (signedInUser.value = authUseCase.getSignedInUser()));
    authUseCase.onLogout().then(() => (signedInUser.value = null));
    router.afterEach((to) => routerStack.push(to));
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
<style>
.europe {
    @apply text-primary-600;
}
</style>
