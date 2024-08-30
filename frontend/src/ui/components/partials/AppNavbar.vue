<template>
    <!-- Desktop navbar-->
    <nav class="fixed top-0 z-40 w-screen print:hidden">
        <div
            :class="meta.hasTransparentHeader ? 'sm:bg-navbar' : 'bg-navbar'"
            class="h-nav flex items-center text-white shadow"
        >
            <div class="h-full">
                <div class="flex h-full items-stretch space-x-4 bg-navbar xl:hidden">
                    <RouterLink v-if="backTo" :to="backTo" class="flex items-center px-8 md:pl-16">
                        <i class="fa-solid fa-arrow-left"></i>
                    </RouterLink>
                    <button v-else class="px-8 md:pl-16" @click="menuOpen = true">
                        <i class="fa-solid fa-bars"></i>
                    </button>
                </div>
                <div class="hidden h-full items-center space-x-4 xl:flex">
                    <div class="px-8 xl:pl-12">
                        <i class="fa-solid fa-rocket"></i>
                    </div>
                </div>
            </div>
            <div class="flex-grow">
                <h1 v-if="!meta.hasTransparentHeader && title">
                    <span class="truncate xl:hidden">{{ $t(title) }}</span>
                    <span class="hidden xl:inline">Lissi App</span>
                </h1>
            </div>
            <div class="flex h-full justify-end">
                <div id="nav-right" class="h-full px-8 md:pr-16 xl:pr-20"></div>
            </div>
        </div>
    </nav>
    <!-- Push the content away from the navbar -->
    <div class="h-nav"></div>
    <SlideMenu v-model:open="menuOpen">
        <AppMenu />
    </SlideMenu>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRoute, useRouter } from 'vue-router';
import { useAuthUseCase } from '@/ui/composables/Application';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import AppMenu from './AppMenu.vue';
import SlideMenu from './SlideMenu.vue';

const router = useRouter();
const route = useRoute();

const authUseCase = useAuthUseCase();

const menuOpen = ref<boolean>(false);
const loggedIn = ref<boolean>(false);
const backTo = ref<RouteLocationRaw>('');
const title = ref<string>('');

const meta = computed<RouteMetaData>(() => route.meta as RouteMetaData);

async function init(): Promise<void> {
    authUseCase.onLogin().then(() => (loggedIn.value = true));
    watch(router.currentRoute, onRouteChanged);
}

function onRouteChanged() {
    const meta = route.meta as RouteMetaData;
    if (typeof meta.title === 'string') {
        title.value = meta.title;
    } else if (typeof meta.title === 'function') {
        title.value = meta.title(route);
    } else {
        title.value = '';
    }

    if (meta.backTo) {
        backTo.value = { name: meta.backTo };
    } else {
        backTo.value = '';
    }
}

init();
</script>
