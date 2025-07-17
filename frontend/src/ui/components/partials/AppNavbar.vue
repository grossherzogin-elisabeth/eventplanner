<template>
    <nav class="fixed top-0 z-40 w-screen print:hidden">
        <div :class="meta.hasTransparentHeader ? 'sm:bg-primary' : 'bg-primary'" class="h-nav flex items-center text-onprimary shadow">
            <div class="h-full">
                <div class="flex h-full items-center bg-primary xl:hidden">
                    <!--                    <RouterLink v-if="backTo" :to="backTo" class="icon-button mx-4 md:ml-12">-->
                    <!--                        <i class="fa-solid fa-arrow-left"></i>-->
                    <!--                    </RouterLink>-->
                    <button class="icon-button ml-4 mr-1 md:ml-12" @click="menuOpen = true">
                        <i class="fa-solid fa-bars"></i>
                    </button>
                </div>
            </div>
            <div class="flex flex-grow">
                <h1
                    v-if="props.title"
                    class="w-0 flex-grow truncate xl:hidden"
                    :class="meta.hasTransparentHeader ? 'hidden sm:block' : 'block'"
                >
                    {{ props.title }}
                </h1>
            </div>
            <div class="flex h-full justify-end">
                <div id="nav-right" class="mx-8 h-full md:mr-16 xl:mr-20"></div>
            </div>
        </div>
    </nav>
    <!-- Push the content away from the navbar -->
    <div class="h-nav"></div>
    <div
        v-if="signedInUser && signedInUser.impersonated"
        class="h-16 bg-error-container pl-8 pr-4 text-onerror-container shadow-inner md:pl-16 md:pr-12"
    >
        <div class="flex h-full items-center">
            <i class="fa-solid fa-warning" />
            <p class="ml-4 mr-2 line-clamp-3 w-0 flex-grow py-4 text-sm font-bold">
                Du siehst die Anwendung aus Sicht von
                <span class="italic">{{ signedInUser.firstName }} {{ signedInUser.lastName }}</span>
            </p>
            <button class="icon-button" @click="authUseCase.impersonateUser(null)">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
            </button>
        </div>
    </div>

    <SlideMenu v-model:open="menuOpen">
        <AppMenu />
    </SlideMenu>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import type { SignedInUser } from '@/domain';
import { useAuthUseCase } from '@/ui/composables/Application';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import AppMenu from './AppMenu.vue';
import SlideMenu from './SlideMenu.vue';

interface Props {
    title: string;
}

const props = defineProps<Props>();

const route = useRoute();

const authUseCase = useAuthUseCase();

const menuOpen = ref<boolean>(false);
const signedInUser = ref<SignedInUser | null>(null);

const meta = computed<RouteMetaData>(() => route.meta as RouteMetaData);

async function init(): Promise<void> {
    authUseCase.onLogin().then(() => (signedInUser.value = authUseCase.getSignedInUser()));
}

init();
</script>
