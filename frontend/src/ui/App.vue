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
            <div class="relative hidden h-screen w-md flex-col xl:flex">
                <AppMenu class="text-onprimary dark:text-onsurface-variant relative z-10" />
            </div>
            <div class="bg-surface relative flex w-0 grow flex-col xl:h-screen xl:overflow-hidden xl:rounded-l-3xl xl:shadow-2xl">
                <RouterView id="router-view" v-model:tab-title="title" class="flex flex-1 flex-col" />
            </div>
        </div>
    </div>
    <div class="ruler"></div>
    <VErrorDialog />
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useConfigService } from '@/application';
import { Permission } from '@/domain';
import { VErrorDialog } from '@/ui/components/common';
import AppMenu from '@/ui/components/partials/AppMenu.vue';
import AppNavbar from '@/ui/components/partials/AppNavbar.vue';
import VNotifications from '@/ui/components/partials/VNotifications.vue';
import { useSession } from '@/ui/composables/Session.ts';
import { useViewportSize } from '@/ui/composables/ViewportSize';

useViewportSize();
const configService = useConfigService();
const { signedInUser } = useSession();

const title = ref<string>('');

async function init(): Promise<void> {
    console.info('🚀 Mounting app');
    setTitle();
    watch(title, setTitle);
    watch(signedInUser, () => {
        Object.values(Permission).forEach((permission) => document.body.classList.remove(permission));
        signedInUser.value?.permissions.forEach((permission) => document.body.classList.add(permission));
    });
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
