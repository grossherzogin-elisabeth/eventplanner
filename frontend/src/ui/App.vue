<template>
    <div class="flex min-h-screen flex-col bg-primary-50 from-primary-900 to-primary-800 to-50% xl:bg-gradient-to-r">
        <div v-if="initialized && loggedIn" class="xl:hidden">
            <AppNavbar />
        </div>
        <div v-if="initialized && loggedIn" class="flex flex-1 items-stretch">
            <div class="relative hidden h-screen w-96 flex-col pt-4 text-white xl:flex">
                <AppMenu class="relative z-10" />
            </div>
            <div
                class="relative flex h-full w-0 flex-grow flex-col bg-primary-50 xl:h-screen xl:overflow-hidden xl:rounded-l-3xl xl:shadow-2xl"
            >
                <RouterView class="flex flex-1 flex-col" />
                <div
                    v-if="routeHasDialog"
                    class="fixed bottom-0 left-0 right-0 top-0 z-30 bg-gray-700 bg-opacity-80 pt-12 lg:px-4 lg:pb-4 lg:pt-16 xl:p-8"
                    @click="closeDialog()"
                >
                    <div class="mx-auto flex h-full max-w-screen-lg items-start">
                        <div class="dialog h-full w-full max-w-screen-lg" @click.stop>
                            <!-- dialog header -->
                            <div class="dialog-header">
                                <div class="flex w-20 items-center justify-center lg:w-16">
                                    <button
                                        v-if="dialogStack.length > 1"
                                        class="h-10 w-10 rounded-full text-gray-700 hover:bg-primary-200 hover:text-gray-950"
                                        @click="router.back()"
                                    >
                                        <i class="fa-solid fa-arrow-left"></i>
                                    </button>
                                </div>
                                <div class="flex w-20 items-center justify-center lg:w-16">
                                    <button
                                        class="h-10 w-10 rounded-full text-gray-700 hover:bg-primary-200 hover:text-gray-950"
                                        @click="closeDialog()"
                                    >
                                        <i class="fa-solid fa-close"></i>
                                    </button>
                                </div>
                            </div>
                            <!-- dialog content -->
                            <div class="flex h-0 flex-1 flex-col">
                                <RouterView class="flex flex-1 flex-col" name="dialog" />
                            </div>
                        </div>
                        <!--                        <div class="hidden lg:block">-->
                        <!--                            <button class="rounded-full w-10 h-10 ml-2 text-white hover:text-primary-600 hover:bg-primary-50 flex items-center justify-center">-->
                        <!--                                <i class="fa-solid fa-close"></i>-->
                        <!--                            </button>-->
                        <!--                        </div>-->
                    </div>
                </div>
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
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import WorldMap from '@/ui/assets/images/worldmap.svg?component';
import { VErrorDialog } from '@/ui/components/common';
import AppFooter from '@/ui/components/partials/AppFooter.vue';
import AppMenu from '@/ui/components/partials/AppMenu.vue';
import AppNavbar from '@/ui/components/partials/AppNavbar.vue';
import { useAuthUseCase } from '@/ui/composables/Application';
import { useViewportSize } from '@/ui/composables/ViewportSize';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
useViewportSize();
const authUseCase = useAuthUseCase();

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const cachedMainView = ref<any>(undefined);
const initialized = ref<boolean>(false);
const loggedIn = ref<boolean>(false);

const routeHasDialog = computed<boolean>(
    () => route.matched[route.matched.length - 1]?.components?.dialog !== undefined
);

let mainRoute: string | null = null;
let dialogStack: string[] = [];

function closeDialog(): void {
    dialogStack = [];
    if (mainRoute) {
        router.push(mainRoute);
    } else {
        router.push('/');
    }
}

async function init(): Promise<void> {
    console.info('🚀 Mounting app');
    initialized.value = true;
    const redirect = await authUseCase.login();
    loggedIn.value = true;
    if (redirect) {
        await router.push(redirect);
    }

    /**
     * This hook sets the tab title based on the title set in your route meta
     */
    router.afterEach((to) => {
        const meta = to.meta as RouteMetaData | undefined;
        if (typeof meta?.title === 'string') {
            document.title = `Lissi App | ${i18n.t(meta.title)}`;
        } else if (typeof meta?.title === 'function') {
            document.title = `Lissi App | ${meta.title(to)}`;
        } else {
            document.title = 'Lissi App';
        }
    });

    router.afterEach((to, from) => {
        const route = router.getRoutes().find((it) => it.name === to.name);
        if (route?.components?.dialog) {
            if (dialogStack.length === 0) {
                mainRoute = from?.fullPath || '/users';
            }
            dialogStack.push(to.fullPath);
        } else {
            dialogStack = [];
        }
        if (route?.components?.default) {
            cachedMainView.value = route?.components?.default;
        }
    });
}

init();
</script>
<style>
.europe {
    @apply text-primary-600;
}
</style>
