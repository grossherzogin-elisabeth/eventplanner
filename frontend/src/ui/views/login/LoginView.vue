<template>
    <div v-if="enableLoginView">
        <div
            class="fixed bottom-0 left-0 right-0 top-0 overflow-y-auto bg-gradient-to-t from-primary-800 to-primary-700"
        >
            <div class="self-stretch p-8 pt-16 text-white sm:hidden">
                <h1 class="mb-8 text-2xl font-thin">
                    Segelschulschiff<br />
                    Großherzogin Elisabeth
                </h1>
                <p class="mb-8 text-lg">
                    Diese Seite ist nur für angemeldete Stammcrew Mitglieder sichtbar. Bitte melde dich mit deinem Crew
                    Account an um die App zu nutzen.
                </p>
            </div>
        </div>
        <div class="fixed bottom-0 left-0 right-0 top-0 overflow-y-auto">
            <div class="mx-auto flex min-h-full flex-col justify-end sm:p-8">
                <div class="h-96 sm:hidden"></div>
                <div
                    class="mx-auto flex w-full flex-col rounded-t-3xl bg-primary-50 sm:max-w-xl sm:rounded-3xl md:flex-row lg:max-w-5xl"
                >
                    <div class="relative hidden w-0 flex-grow overflow-hidden rounded-l-3xl lg:block">
                        <img
                            class="h-full w-full object-cover object-left opacity-50"
                            src="@/ui/assets/images/login-1.jpg"
                        />
                        <div
                            class="absolute bottom-0 left-0 right-0 top-0 bg-gradient-to-l from-primary-50 to-transparent"
                        ></div>
                    </div>
                    <div class="flex flex-col p-8 sm:p-16 lg:mx-auto lg:max-w-xl">
                        <div class="mb-8 h-1 w-8 self-center rounded-full bg-gray-800 opacity-25 sm:hidden"></div>

                        <div class="hidden sm:block">
                            <h1 class="mb-8 text-2xl font-thin">
                                Segelschulschiff<br />
                                Großherzogin Elisabeth
                            </h1>
                            <p class="mb-8 text-lg">
                                Diese Seite ist nur für angemeldete Stammcrew Mitglieder sichtbar. Bitte melde dich mit
                                deinem Crew Account an um die App zu nutzen.
                            </p>
                        </div>

                        <div v-if="enableDirectLogin">
                            <h2 class="mb-4 px-4">Bei deinem Lissi Account anmelden</h2>
                            <div class="mb-4">
                                <VInputLabel>Email ode Benutzername</VInputLabel>
                                <VInputText v-model="username" placeholder="max.mustermensch@email.de" />
                            </div>
                            <div class="mb-8">
                                <VInputLabel>Passwort</VInputLabel>
                                <VInputText v-model="password" type="password" placeholder="**********" />
                            </div>
                            <div class="flex items-center justify-between sm:flex-row">
                                <button class="btn-ghost">
                                    <span class="mx-auto"> Passwort vergessen? </span>
                                </button>
                                <button class="btn-primary sm:mb-0" @click="authUseCase.login()">
                                    <span class="mx-auto py-2"> Anmelden </span>
                                </button>
                            </div>
                        </div>
                        <button v-else class="btn-primary mb-4" @click="authUseCase.login()">
                            <i class="fa-solid fa-user text-xl sm:mx-4" />
                            <span class="mx-auto py-2"> Anmelden mit Lissi Account </span>
                        </button>

                        <div class="flex justify-center py-8">
                            <p>oder</p>
                        </div>
                        <button class="btn-secondary mb-4" @click="authUseCase.login()">
                            <i class="fa-brands fa-google text-xl sm:mx-4" />
                            <span class="mx-auto py-2"> Anmelden mit Google </span>
                        </button>
                        <button class="btn-secondary mb-4" @click="authUseCase.login()">
                            <i class="fa-brands fa-apple text-xl sm:mx-4" />
                            <span class="mx-auto py-2"> Anmelden mit Apple </span>
                        </button>
                        <button class="btn-secondary mb-4" @click="authUseCase.login()">
                            <i class="fa-brands fa-microsoft text-xl sm:mx-4" />
                            <span class="mx-auto py-2"> Anmelden mit Microsoft </span>
                        </button>
                    </div>
                </div>
                <div class="h-0 flex-grow bg-primary-50 sm:hidden"></div>
            </div>
        </div>
    </div>
    <div v-else class="h-full overflow-y-auto px-8 pb-8 pt-8 md:px-16 xl:px-20">
        <div class="-mx-4 flex max-w-2xl items-center gap-4 rounded-2xl bg-blue-200 p-4 text-blue-700">
            <i class="fa-solid fa-info-circle"></i>
            <span class="font-bold">
                Diese Seite ist nur für angemeldete Nutzer sichtbar. Du wirst in Kürze zum Login weitergeleitet...
            </span>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { VInputLabel, VInputText } from '@/ui/components/common';
import { useAuthUseCase } from '@/ui/composables/Application';

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const authUseCase = useAuthUseCase();
const router = useRouter();

const enableLoginView = localStorage.getItem('flag.login-show-any') === 'true';
const enableDirectLogin = localStorage.getItem('flag.login-show-fields') === 'true';
const username = ref<string>('');
const password = ref<string>('');

async function init(): Promise<void> {
    emit('update:title', 'Login');
    if (!enableLoginView) {
        await authUseCase.login();
    } else {
        if (!authUseCase.isLoggedIn()) {
            await authUseCase.onLogin();
        }
        await router.push('/');
    }
}

init();
</script>
