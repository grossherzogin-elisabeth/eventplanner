<template>
    <div v-if="signedInUser.key" class="menu flex-1 overflow-y-auto">
        <h1 class="mb-8 mt-8 px-8 text-2xl font-thin xl:pl-14">{{ config.menuTitle }}</h1>

        <div v-if="signedInUser.impersonated" class="mx-4 rounded-2xl bg-error-container pl-4 text-onerror-container xl:mx-8 xl:pl-6">
            <div class="flex items-center">
                <p class="mr-2 w-0 flex-grow py-4 text-sm font-bold">
                    Du siehst die Anwendung aus Sicht von
                    <span class="italic">{{ signedInUser.firstName }} {{ signedInUser.lastName }}</span>
                </p>
                <button class="icon-button mr-2" title="Impersonate Modus beenden" @click="authUseCase.impersonateUser(null)">
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
            <li :class="{ expanded: eventsExpanded }" class="permission-read-events menu-item">
                <button @click="eventsExpanded = !eventsExpanded">
                    <i class="fa-solid fa-calendar-days"></i>
                    <span>Kalender</span>
                    <i class="menu-chevron fa-solid fa-chevron-right"></i>
                </button>
                <ul v-if="eventsExpanded" class="space-y-1 pb-4">
                    <li
                        v-for="y in years"
                        :key="y"
                        class="menu-item level-2"
                        :class="{ active: eventRouteActive && eventRoute === `${Routes.EventsCalendar}:${y}` }"
                    >
                        <RouterLink :to="{ name: Routes.EventsCalendar, params: { year: y } }">
                            <span>{{ y }}</span>
                        </RouterLink>
                    </li>
                </ul>
            </li>
            <li class="permission-read-events menu-item" :class="{ active: eventRouteActive && eventRoute === Routes.EventsList }">
                <RouterLink :to="{ name: Routes.EventsList }">
                    <i class="fa-solid fa-compass"></i>
                    <span>Alle Reisen</span>
                </RouterLink>
            </li>
            <li class="permission-write-events menu-item" :class="{ active: eventRouteActive && eventRoute === Routes.EventsListAdmin }">
                <RouterLink :to="{ name: Routes.EventsListAdmin }">
                    <i class="fa-solid fa-compass-drafting"></i>
                    <span>Reisen verwalten</span>
                </RouterLink>
            </li>
            <li class="permission-read-user-details menu-item">
                <RouterLink :to="{ name: Routes.UsersList }">
                    <i class="fa-solid fa-users"></i>
                    <span>Nutzer verwalten</span>
                </RouterLink>
            </li>
            <li
                v-if="
                    signedInUser.permissions.includes(Permission.WRITE_QUALIFICATIONS) ||
                    signedInUser.permissions.includes(Permission.WRITE_POSITIONS)
                "
                class="menu-item"
            >
                <RouterLink :to="{ name: Routes.Basedata }">
                    <i class="fa-solid fa-database"></i>
                    <span>Stammdaten verwalten</span>
                </RouterLink>
            </li>
            <li class="permission-write-application-settings menu-item">
                <RouterLink :to="{ name: Routes.AppSettings }">
                    <i class="fa-solid fa-gear"></i>
                    <span>Einstellungen</span>
                </RouterLink>
            </li>
            <li class="menu-item">
                <RouterLink :to="{ name: Routes.Wiki }">
                    <i class="fa-solid fa-graduation-cap"></i>
                    <span>Wiki</span>
                </RouterLink>
            </li>
            <li class="menu-item">
                <RouterLink :to="{ name: Routes.SystemInfo }">
                    <i class="fa-solid fa-comment"></i>
                    <span>Feedback</span>
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
    <div v-else-if="loading || route.name === Routes.Login" class="menu animate-pulse">
        <div class="mx-12 my-4 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-12 h-6 w-1/2 rounded-full bg-current opacity-10"></div>

        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
    </div>
    <div v-else>
        <h1 class="mb-8 mt-4 px-8 text-2xl font-thin xl:pl-14">{{ config.menuTitle }}</h1>
        <VInfo class="mx-8">
            <h2 class="mb-2">Noch kein Account?</h2>
            <p class="mb-2">
                Mit einem Lissi Account kannst du jederzeit den Status deiner nächsten Reisen einsehen und dich zu Reisen an und abmelden.
            </p>
        </VInfo>
        <ul class="menu-list my-4">
            <li class="menu-item">
                <RouterLink :to="{ name: Routes.Login }">
                    <i class="fa-solid fa-sign-in-alt"></i>
                    <span>Zum Login</span>
                </RouterLink>
            </li>
            <li class="menu-item">
                <RouterLink :to="{ name: Routes.Login }">
                    <i class="fa-solid fa-user-circle"></i>
                    <span>Jetzt registrieren</span>
                </RouterLink>
            </li>
        </ul>
    </div>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import type { SignedInUser } from '@/domain';
import { Permission } from '@/domain';
import { VInfo } from '@/ui/components/common';
import { useAuthUseCase, useConfig } from '@/ui/composables/Application';
import { Routes } from '@/ui/views/Routes';

const config = useConfig();
const authUseCase = useAuthUseCase();
const route = useRoute();

const eventRoute = ref<string | undefined>(undefined);
const eventRouteActive = ref<boolean>(false);

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const loading = ref<boolean>(true);
const years: number[] = [new Date().getFullYear() - 1, new Date().getFullYear(), new Date().getFullYear() + 1];
const eventsExpanded = ref<boolean>(false);

authUseCase.onLogin().then(() => (signedInUser.value = authUseCase.getSignedInUser()));
setTimeout(() => (loading.value = false), 1000);
watch(route, () => {
    eventRouteActive.value = route.matched.find((it) => it.name === 'app_event-parent') !== undefined;
    if (route.name === Routes.EventsCalendar) {
        eventRoute.value = Routes.EventsCalendar + ':' + route.params.year;
    } else if (route.name === Routes.EventsListAdmin) {
        eventRoute.value = Routes.EventsListAdmin;
    } else if (route.name === Routes.EventsList || eventRoute.value === undefined) {
        eventRoute.value = Routes.EventsList;
    }
});
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

.menu-item > a,
.menu-item > button {
    @apply px-4 py-2 xl:px-6;
    @apply flex w-full items-center;
}

/* expand */
.menu-item.expanded {
    @apply bg-onprimary bg-opacity-5;
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
    @apply bg-onprimary bg-opacity-10;
}

/* active */
.menu-item > a.router-link-active,
.menu-item.active > a,
.menu-item.active > button {
    @apply relative bg-onprimary font-bold text-primary;
}

.menu-item > a.router-link-active:before,
.menu-item.active > a:before {
    @apply absolute bottom-auto left-0 top-auto;
    @apply h-5 w-1;
    @apply rounded-full bg-current;
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
</style>
