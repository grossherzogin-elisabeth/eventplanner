<template>
    <div v-if="loading || route.name === Routes.Login" class="menu animate-pulse" data-test-id="menu-loading">
        <div class="mx-12 my-4 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-12 h-6 w-1/2 rounded-full bg-current opacity-10"></div>

        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
        <div class="mx-12 mb-6 h-6 rounded-full bg-current opacity-10"></div>
    </div>
    <div v-else-if="signedInUser" class="menu flex-1 overflow-y-auto">
        <h1 class="mt-8 mb-8 px-8 text-2xl font-thin xl:pl-14">{{ menuTitle }}</h1>

        <div v-if="signedInUser.impersonated" class="bg-error-container text-onerror-container mx-4 rounded-2xl pl-4 xl:mx-8 xl:pl-6">
            <div class="flex items-center">
                <i18n-t tag="p" keypath="navigation.impersonate" class="mr-2 w-0 grow py-4 text-sm font-bold">
                    <span class="italic">{{ signedInUser.firstName }} {{ signedInUser.lastName }}</span>
                </i18n-t>
                <button class="btn-icon mr-2" title="Impersonate Modus beenden" @click="authUseCase.impersonateUser(null)">
                    <i class="fa-solid fa-arrow-right-from-bracket"></i>
                </button>
            </div>
        </div>

        <ul class="menu-list my-4">
            <li v-if="hasPermission(Permission.READ_EVENTS)" class="menu-item" data-test-id="menu-item-home">
                <RouterLink :to="{ name: Routes.Home }">
                    <i class="fa-solid fa-home"></i>
                    <span>{{ $t('navigation.myNextEvents') }}</span>
                </RouterLink>
            </li>
            <li v-else class="menu-item" data-test-id="menu-item-onboarding">
                <RouterLink :to="{ name: Routes.Onboarding }">
                    <i class="fa-solid fa-home"></i>
                    <span>{{ $t('navigation.start') }}</span>
                </RouterLink>
            </li>
            <li
                v-if="hasPermission(Permission.READ_EVENTS)"
                :class="{ expanded: eventsExpanded }"
                class="menu-item"
                data-test-id="menu-item-calendar"
            >
                <button @click="eventsExpanded = !eventsExpanded">
                    <i class="fa-solid fa-calendar-days"></i>
                    <span>{{ $t('navigation.calendar') }}</span>
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
            <li
                v-if="hasPermission(Permission.READ_EVENTS)"
                class="menu-item"
                data-test-id="menu-item-event-list"
                :class="{ active: eventRouteActive && eventRoute === Routes.EventsList }"
            >
                <RouterLink :to="{ name: Routes.EventsList }">
                    <i class="fa-solid fa-compass"></i>
                    <span>{{ $t('navigation.allEvents') }}</span>
                </RouterLink>
            </li>
            <li
                v-if="hasPermission(Permission.WRITE_EVENTS)"
                class="menu-item"
                data-test-id="menu-item-event-admin"
                :class="{ active: eventRouteActive && eventRoute === Routes.EventsListAdmin }"
            >
                <RouterLink :to="{ name: Routes.EventsListAdmin }">
                    <i class="fa-solid fa-compass-drafting"></i>
                    <span>{{ $t('navigation.manageEvents') }}</span>
                </RouterLink>
            </li>
            <li v-if="hasPermission(Permission.READ_USER_DETAILS)" class="menu-item" data-test-id="menu-item-user-list">
                <RouterLink :to="{ name: Routes.UsersList }">
                    <i class="fa-solid fa-users"></i>
                    <span v-if="hasPermission(Permission.WRITE_USERS)">{{ $t('navigation.manageUsers') }}</span>
                    <span>{{ $t('navigation.listUsers') }}</span>
                </RouterLink>
            </li>
            <li v-if="hasPermission(Permission.WRITE_SETTINGS)" class="menu-item" data-test-id="menu-item-admin-settings">
                <RouterLink :to="{ name: Routes.AppSettings }">
                    <i class="fa-solid fa-gear"></i>
                    <span>{{ $t('navigation.manageSettings') }}</span>
                </RouterLink>
            </li>
            <li class="menu-item" data-test-id="menu-item-account">
                <RouterLink :to="{ name: Routes.Account }">
                    <i class="fa-solid fa-user-circle"></i>
                    <span>{{ $t('navigation.account') }}</span>
                </RouterLink>
            </li>
            <li class="menu-item" data-test-id="menu-item-logout">
                <a type="button" @click="authUseCase.logout()">
                    <i class="fa-solid fa-sign-out"></i>
                    <span>{{ $t('navigation.signOut') }}</span>
                </a>
            </li>
        </ul>
    </div>
    <div v-else>
        <h1 class="mt-4 mb-8 px-8 text-2xl font-thin xl:pl-14">{{ menuTitle }}</h1>
        <VInfo class="mx-8" data-test-id="create-account-hint">
            <h2 class="mb-2">Noch kein Account?</h2>
            <p class="mb-2">
                Mit einem Lissi Account kannst du jederzeit den Status deiner nächsten Veranstaltungen einsehen und dich zu Veranstaltungen
                an und abmelden.
            </p>
        </VInfo>
        <ul class="menu-list my-4">
            <li class="menu-item" data-test-id="menu-item-login">
                <RouterLink :to="{ name: Routes.Login }">
                    <i class="fa-solid fa-sign-in-alt"></i>
                    <span>Zum Login</span>
                </RouterLink>
            </li>
            <li class="menu-item" data-test-id="menu-item-register">
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
import { useAuthUseCase, useConfigService } from '@/application';
import { Permission } from '@/domain';
import { VInfo } from '@/ui/components/common';
import { useSession } from '@/ui/composables/Session.ts';
import { Routes } from '@/ui/views/Routes';

const menuTitle = useConfigService().getConfig().menuTitle;
const authUseCase = useAuthUseCase();
const route = useRoute();

const eventRoute = ref<string | undefined>(undefined);
const eventRouteActive = ref<boolean>(false);

const { signedInUser, hasPermission } = useSession();
const loading = ref<boolean>(true);
const years: number[] = [new Date().getFullYear() - 1, new Date().getFullYear(), new Date().getFullYear() + 1];
const eventsExpanded = ref<boolean>(false);

function init(): void {
    authUseCase
        .authenticate()
        .then(() => (loading.value = false))
        .catch(() => (loading.value = false));
    watch(route, () => {
        eventRouteActive.value = route.matched.some((it) => it.name === 'app_event-parent');
        if (route.name === Routes.EventsCalendar) {
            eventRoute.value = Routes.EventsCalendar + ':' + route.params.year;
        } else if (route.name === Routes.EventsListAdmin) {
            eventRoute.value = Routes.EventsListAdmin;
        } else if (route.name === Routes.EventsList || eventRoute.value === undefined) {
            eventRoute.value = Routes.EventsList;
        }
    });
}

init();
</script>

<style>
@reference "tailwindcss";

.menu-list {
    @apply space-y-1;
    @apply px-4;
    @apply xl:px-8;
}

.menu-list * {
    text-align: left;
}

.menu-list *:hover {
    text-decoration-line: none;
}

.menu-item {
    overflow: hidden;
    cursor: pointer;
    border-radius: var(--radius-2xl);
    font-weight: var(--font-weight-semibold);
    text-overflow: ellipsis;
    white-space: nowrap;
    @apply md:text-lg;
}

.menu-item > a,
.menu-item > button {
    width: 100%;
    display: flex;
    align-items: center;
    @apply px-4;
    @apply py-2;
    @apply xl:px-6;
}

/* expand */
.menu-item.expanded {
    background-color: --alpha(var(--color-secondary-container) / 10%);
}

.menu-item .menu-chevron {
    transition-property: transform;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 75ms;
}

.menu-item.expanded .menu-chevron {
    @apply rotate-90;
}

/* hover */
.menu-item:hover:not(.expanded):not(.active) > a:not(.router-link-active),
.menu-item:hover:not(.expanded):not(.active) > button:not(.router-link-active) {
    background-color: --alpha(var(--color-secondary-container) / 50%);
}

/* active */
.menu-item > a.router-link-active,
.menu-item.active > a,
.menu-item.active > button {
    position: relative;
    background-color: var(--color-secondary-container);
    color: var(--color-onsecondary-container);
    font-weight: var(--font-weight-bold);
}

.menu-item > a.router-link-active:before,
.menu-item.active > a:before {
    position: absolute;
    top: auto;
    bottom: auto;
    left: 0;
    background-color: currentColor;
    @apply h-5;
    @apply w-1;
    border-radius: calc(infinity * 1px);
}

/* 2nd level */
.menu-item.level-2 {
    @apply mx-2;
    border-radius: var(--radius-xl);
    font-size: var(--text-base);
}

.menu-item.level-2 > a,
.menu-item.level-2 > button {
    @apply pl-8;
}

.menu-item span {
    flex-grow: 1;
    @apply ml-4;
    @apply md:ml-8;
}

.menu-item svg {
    display: inline-block;
    @apply h-4;
    @apply w-4;
}

.menu-subheading {
    @apply mt-8;
    @apply pl-8;
    font-size: var(--text-sm);
    font-weight: var(--font-weight-semibold);
    @apply opacity-50;
    @apply xl:pl-14;
}
</style>
