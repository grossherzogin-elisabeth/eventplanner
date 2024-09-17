<template>
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
            <div class="flex flex-grow">
                <h1 v-if="!meta.hasTransparentHeader && props.title" class="block w-0 flex-grow truncate xl:hidden">
                    {{ props.title }}
                </h1>
            </div>
            <div class="flex h-full justify-end">
                <div id="nav-right" class="h-full px-8 md:pr-16 xl:pr-20"></div>
            </div>
        </div>
    </nav>
    <!-- Push the content away from the navbar -->
    <div class="h-nav"></div>
    <div
        v-if="signedInUser && signedInUser.impersonated"
        class="h-16 bg-red-100 px-8 text-red-800 shadow-inner md:px-16"
    >
        <div class="flex h-full items-center">
            <i class="fa-solid fa-warning" />
            <p class="ml-4 mr-2 line-clamp-3 flex-grow py-4 text-sm font-bold">
                Du siehst die Anwendung aus Sicht von
                <span class="italic">{{ signedInUser.firstname }} {{ signedInUser.lastname }}</span>
            </p>
            <button
                class="mr-2 hidden h-10 w-10 rounded-full hover:bg-red-200 sm:block"
                @click="authUseCase.impersonateUser(null)"
            >
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
            </button>
        </div>
    </div>

    <SlideMenu v-model:open="menuOpen">
        <AppMenu />
    </SlideMenu>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRoute, useRouter } from 'vue-router';
import type { SignedInUser } from '@/domain';
import { useAuthUseCase } from '@/ui/composables/Application';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import AppMenu from './AppMenu.vue';
import SlideMenu from './SlideMenu.vue';

interface Props {
    title: string;
}

const props = defineProps<Props>();

const router = useRouter();
const route = useRoute();

const authUseCase = useAuthUseCase();

const menuOpen = ref<boolean>(false);
const signedInUser = ref<SignedInUser | null>(null);
const backTo = ref<RouteLocationRaw>('');

const meta = computed<RouteMetaData>(() => route.meta as RouteMetaData);

async function init(): Promise<void> {
    authUseCase.onLogin().then(() => (signedInUser.value = authUseCase.getSignedInUser()));
    watch(router.currentRoute, onRouteChanged);
}

function onRouteChanged() {
    const meta = route.meta as RouteMetaData;
    if (meta.backTo) {
        backTo.value = { name: meta.backTo };
    } else {
        backTo.value = '';
    }
}

init();
</script>
