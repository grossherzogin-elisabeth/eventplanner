<template>
    <div
        class="xl:bg-primary xl:dark:bg-surface-container-low flex min-h-screen flex-col"
        :class="{ impersonated: signedInUser?.impersonated }"
    >
        <div id="navbar" class="xl:hidden">
            <AppNavbar :title="title" />
        </div>
        <VNotifications id="notifications" />
        <div class="flex flex-1 items-stretch">
            <div class="relative hidden h-screen w-104 flex-col xl:flex">
                <AppMenu class="text-onprimary dark:text-onsurface-variant relative z-10" />
            </div>
            <main class="bg-surface relative flex w-0 grow flex-col xl:h-screen xl:overflow-hidden xl:rounded-l-3xl xl:shadow-2xl">
                <RouterView v-show="!loading" id="router-view" v-model:tab-title="title" class="flex flex-1 flex-col" />
                <div v-if="loading" class="bg-surface absolute top-0 right-0 bottom-0 left-0 z-20 flex items-center justify-center">
                    <VLoadingSpinner />
                </div>
            </main>
        </div>
    </div>
    <div class="ruler"></div>
    <VErrorDialog />
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useConfigService } from '@/application';
import { Permission } from '@/domain';
import { VErrorDialog, VLoadingSpinner } from '@/ui/components/common';
import AppMenu from '@/ui/components/partials/AppMenu.vue';
import AppNavbar from '@/ui/components/partials/AppNavbar.vue';
import VNotifications from '@/ui/components/partials/VNotifications.vue';
import { useSession } from '@/ui/composables/Session.ts';
import { useViewportSize } from '@/ui/composables/ViewportSize';
import { useRouter } from 'vue-router';

useViewportSize();
const router = useRouter();
const configService = useConfigService();
const { signedInUser } = useSession();

const title = ref<string>('');
const loading = ref<boolean>(false);

function init(): void {
    console.info('🚀 Mounting app');
    setTitle();
    watch(title, setTitle);
    watch(signedInUser, () => {
        Object.values(Permission).forEach((permission) => document.body.classList.remove(permission));
        signedInUser.value?.permissions.forEach((permission) => document.body.classList.add(permission));
    });
    router.beforeEach(() => (loading.value = true));
    router.afterEach(() => (loading.value = false));
}

function setTitle(): void {
    const tabTitle = configService.getConfig().tabTitle;
    if (title.value) {
        document.title = `${tabTitle} | ${title.value}`;
    } else {
        document.title = tabTitle;
    }
}

init();
</script>
