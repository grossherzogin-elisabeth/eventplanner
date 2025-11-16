<template>
    <nav class="fixed top-0 z-40 w-screen print:hidden">
        <div
            :class="meta.hasTransparentHeader ? 'sm:bg-primary sm:dark:bg-surface-container' : 'bg-primary dark:bg-surface-container'"
            class="h-nav text-onprimary dark:text-onsurface flex items-center shadow-sm"
        >
            <div class="h-full">
                <div class="bg-primary dark:bg-surface-container flex h-full items-center xl:hidden">
                    <!--                    <RouterLink v-if="backTo" :to="backTo" class="btn-icon mx-4 md:ml-12">-->
                    <!--                        <i class="fa-solid fa-arrow-left"></i>-->
                    <!--                    </RouterLink>-->
                    <button class="btn-icon xs:ml-4 mr-1 md:ml-12" @click="menuOpen = true">
                        <i class="fa-solid fa-bars"></i>
                    </button>
                </div>
            </div>
            <div class="flex grow">
                <h1 v-if="props.title" class="w-0 grow truncate xl:hidden" :class="meta.hasTransparentHeader ? 'hidden sm:block' : 'block'">
                    {{ props.title }}
                </h1>
            </div>
            <div class="flex h-full justify-end">
                <div id="nav-right" class="xs:mx-8 mx-4 h-full md:mr-16 xl:mr-20"></div>
            </div>
        </div>
    </nav>
    <!-- Push the content away from the navbar -->
    <div class="h-nav"></div>
    <div
        v-if="signedInUser && signedInUser.impersonated"
        class="bg-error-container text-onerror-container h-16 pr-4 pl-8 shadow-inner md:pr-12 md:pl-16"
    >
        <div class="flex h-full items-center">
            <i class="fa-solid fa-warning" />
            <p class="mr-2 ml-4 line-clamp-3 w-0 grow py-4 text-sm font-bold">
                Du siehst die Anwendung aus Sicht von
                <span class="italic">{{ signedInUser.firstName }} {{ signedInUser.lastName }}</span>
            </p>
            <button class="btn-icon" @click="authUseCase.impersonateUser(null)">
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
import { useAuthUseCase } from '@/application';
import type { SignedInUser } from '@/domain';
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
