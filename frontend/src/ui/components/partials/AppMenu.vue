<template>
    <div v-if="signedInUser.key" class="menu flex-1 overflow-y-auto">
        <h1 class="mb-8 mt-4 px-8 text-2xl font-thin xl:pl-14">{{ config.menuTitle }}</h1>

        <div v-if="signedInUser.impersonated" class="mx-4 rounded-2xl bg-red-100 pl-4 text-red-800 xl:mx-8 xl:pl-6">
            <div class="flex items-center">
                <p class="mr-2 w-0 flex-grow py-4 text-sm font-bold">
                    Du siehst die Anwendung aus Sicht von
                    <span class="italic">{{ signedInUser.firstname }} {{ signedInUser.lastname }}</span>
                </p>
                <button
                    class="mr-2 block h-10 w-10 rounded-full hover:bg-red-200"
                    title="Impersonate Modus beenden"
                    @click="authUseCase.impersonateUser(null)"
                >
                    <i class="fa-solid fa-arrow-right-from-bracket"></i>
                </button>
            </div>
        </div>

        <ul class="menu-list my-4">
            <li v-if="signedInUser.permissions.includes(Permission.READ_EVENTS)" class="menu-item">
                <RouterLink :to="{ name: Routes.Home }">
                    <i class="fa-solid fa-home"></i>
                    <span>Meine nächsten Reisen</span>
                </RouterLink>
            </li>
            <li v-else class="menu-item">
                <RouterLink :to="{ name: Routes.Onboarding }">
                    <i class="fa-solid fa-home"></i>
                    <span>Start</span>
                </RouterLink>
            </li>
            <li
                v-if="signedInUser.permissions.includes(Permission.READ_EVENTS)"
                :class="{ expanded: eventsExpanded }"
                class="menu-item"
            >
                <button @click="eventsExpanded = !eventsExpanded">
                    <i class="fa-solid fa-calendar-days"></i>
                    <span>Alle Reisen</span>
                    <i class="menu-chevron fa-solid fa-chevron-right"></i>
                </button>
                <ul v-if="eventsExpanded" class="space-y-1 pb-4">
                    <li v-for="y in years" :key="y" class="menu-item level-2">
                        <RouterLink :to="{ name: Routes.Events, params: { year: y } }">
                            <span>{{ y }}</span>
                        </RouterLink>
                    </li>
                </ul>
            </li>
            <li v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)" class="menu-item">
                <RouterLink :to="{ name: Routes.EventsAdmin }">
                    <i class="fa-solid fa-compass"></i>
                    <span>Reisen verwalten</span>
                </RouterLink>
            </li>
            <li v-if="signedInUser.permissions.includes(Permission.READ_USER_DETAILS)" class="menu-item">
                <RouterLink :to="{ name: Routes.UsersList }">
                    <i class="fa-solid fa-users"></i>
                    <span>Nutzer verwalten</span>
                </RouterLink>
            </li>
            <li v-if="signedInUser.permissions.includes(Permission.WRITE_QUALIFICATIONS)" class="menu-item">
                <RouterLink :to="{ name: Routes.Basedata }">
                    <i class="fa-solid fa-database"></i>
                    <span>Stammdaten</span>
                </RouterLink>
            </li>
            <li v-if="signedInUser.permissions.includes(Permission.WRITE_SETTINGS)" class="menu-item">
                <RouterLink :to="{ name: Routes.AppSettings }">
                    <i class="fa-solid fa-gear"></i>
                    <span>Einstellungen</span>
                </RouterLink>
            </li>
            <li class="menu-item hidden">
                <RouterLink :to="{ name: Routes.Wiki }">
                    <i class="fa-solid fa-book"></i>
                    <span>Wiki</span>
                </RouterLink>
            </li>
            <li class="menu-item">
                <RouterLink :to="{ name: Routes.Account }">
                    <i class="fa-solid fa-user-circle"></i>
                    <span>Meine Daten</span>
                </RouterLink>
            </li>
            <li class="menu-item">
                <a type="button" @click="authUseCase.logout()">
                    <i class="fa-solid fa-sign-out"></i>
                    <span>Abmelden</span>
                </a>
            </li>
        </ul>
        <h2 class="menu-subheading hidden">Rechtliches</h2>
        <ul class="menu-list my-4 hidden">
            <li class="menu-item">
                <RouterLink :to="{ name: Routes.Privacy }">
                    <i class="fa-solid fa-user-shield"></i>
                    <span>Datenschutzerklärung</span>
                </RouterLink>
            </li>
            <li class="menu-item">
                <RouterLink :to="{ name: Routes.Imprint }">
                    <i class="fa-solid fa-section"></i>
                    <span>Impressum</span>
                </RouterLink>
            </li>
        </ul>
    </div>
    <div v-else class="menu animate-pulse">
        <div class="mx-12 my-4 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-12 h-6 w-1/2 rounded-full bg-current opacity-10"></div>

        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import type { SignedInUser } from '@/domain';
import { Permission } from '@/domain';
import { useAuthUseCase, useConfig } from '@/ui/composables/Application';
import { Routes } from '@/ui/views/Routes';

const config = useConfig();
const authUseCase = useAuthUseCase();

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const years: number[] = [new Date().getFullYear() - 1, new Date().getFullYear(), new Date().getFullYear() + 1];
const eventsExpanded = ref<boolean>(false);

authUseCase.onLogin().then(() => (signedInUser.value = authUseCase.getSignedInUser()));
</script>

<style>
.menu-list {
    @apply space-y-1 px-4 xl:px-8;
}

.menu-list * {
    @apply text-left;
    @apply hover:no-underline;
}

.menu-item {
    @apply overflow-hidden rounded-2xl;
    @apply font-semibold md:text-lg;
    @apply cursor-pointer;
}

.text-white .menu-item {
    color: white;
}

.menu-item > a,
.menu-item > button {
    @apply px-4 py-2 xl:px-6;
    @apply flex w-full items-center;
}

/* expand */
.menu-item.expanded {
    @apply bg-primary-800 bg-opacity-5;
}

.text-white .menu-item.expanded {
    @apply bg-primary-50 bg-opacity-5;
}

.menu-item .menu-chevron {
    @apply transition-transform duration-75;
}

.menu-item.expanded .menu-chevron {
    @apply rotate-90;
}

/* hover */
.menu-item:hover:not(.expanded):not(.active) > a:not(.router-link-active),
.menu-item:hover:not(.expanded):not(.active) > button:not(.router-link-active) {
    @apply bg-gray-500 bg-opacity-10;
}

.text-white .menu-item:hover:not(.expanded):not(.active) > a:not(.router-link-active),
.text-white .menu-item:hover:not(.expanded):not(.active) > button:not(.router-link-active) {
    @apply bg-white bg-opacity-10;
}

/* active */
.menu-item > a.router-link-active,
.menu-item.active > a,
.menu-item.active > button {
    @apply relative bg-primary-800 bg-opacity-15;
}

.menu-item > a.router-link-active:before,
.menu-item.active > a:before {
    @apply absolute bottom-auto left-0 top-auto;
    @apply h-5 w-1;
    @apply rounded-full bg-current;
}

.text-white .menu-item > a.router-link-active,
.text-white .menu-item.active > a,
.text-white .menu-item.active > button {
    @apply bg-primary-50 text-primary-800;
    @apply font-bold;
}

/* 2nd level */
.menu-item.level-2 {
    @apply mx-2 rounded-xl text-base;
}

.menu-item.level-2 > a,
.menu-item.level-2 > button {
    @apply pl-8 xl:pl-8;
}

.menu-item span {
    @apply ml-4 flex-grow md:ml-8;
}

.menu-item svg {
    @apply inline-block h-4 w-4;
}

.menu-subheading {
    @apply mt-8 pl-8 text-sm font-semibold opacity-50 xl:pl-14;
}

.text-white .menu-subheading {
    color: white;
}
</style>
