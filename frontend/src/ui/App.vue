<template>
    <div
        class="flex min-h-screen flex-col bg-primary-50 from-primary-900 to-primary-800 to-50% xl:bg-gradient-to-r"
        :class="{ impersonated: signedInUser?.impersonated }"
    >
        <div v-if="initialized && signedInUser" class="xl:hidden">
            <AppNavbar :title="title" />
        </div>
        <VNotifications />
        <div v-if="initialized" class="flex flex-1 items-stretch">
            <div class="relative hidden h-screen w-96 flex-col pt-4 text-white xl:flex">
                <AppMenu v-if="signedInUser" class="relative z-10" />
            </div>
            <div
                class="relative flex h-full w-0 flex-grow flex-col bg-primary-50 xl:h-screen xl:overflow-hidden xl:rounded-l-3xl xl:shadow-2xl"
            >
                <RouterView id="router-view" v-model:title="title" class="flex flex-1 flex-col" />
            </div>
        </div>
        <div
            v-else
            class="flex min-h-screen flex-col items-center justify-center bg-gradient-to-t from-primary-900 to-primary-800 to-50% p-8"
        >
            <WorldMap class="mb-16 w-full max-w-xl animate-pulse text-white text-opacity-25" />
            <span class="text-xl font-light text-white">Anwendung wird geladen...</span>
        </div>
    </div>
    <VErrorDialog />
    <AppFooter />
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { SignedInUser } from '@/domain';
import WorldMap from '@/ui/assets/images/worldmap.svg?component';
import { VErrorDialog } from '@/ui/components/common';
import AppFooter from '@/ui/components/partials/AppFooter.vue';
import AppMenu from '@/ui/components/partials/AppMenu.vue';
import AppNavbar from '@/ui/components/partials/AppNavbar.vue';
import VNotifications from '@/ui/components/partials/VNotifications.vue';
import { useAuthUseCase } from '@/ui/composables/Application';
import { useViewportSize } from '@/ui/composables/ViewportSize';

useViewportSize();
const authUseCase = useAuthUseCase();

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const initialized = ref<boolean>(false);
const signedInUser = ref<SignedInUser | null>(null);
const title = ref<string>('');

async function init(): Promise<void> {
    console.info('🚀 Mounting app');
    setTitle();
    watch(title, setTitle);
    initialized.value = true;
    authUseCase.onLogin().then(() => (signedInUser.value = authUseCase.getSignedInUser()));
    authUseCase.onLogout().then(() => (signedInUser.value = null));
}

function setTitle(): void {
    if (title.value) {
        document.title = `Lissi App | ${title.value}`;
    } else {
        document.title = 'Lissi App';
    }
}

init();
</script>
<style>
.europe {
    @apply text-primary-600;
}
</style>
