<template>
    <div
        class="pointer-events-none fixed top-12 right-0 left-0 z-40 flex w-full flex-col items-stretch px-4 py-2 sm:left-auto sm:w-96 xl:top-2 xl:right-16"
    >
        <template v-for="(notification, index) in notifications" :key="index">
            <div v-if="!notification.hidden">
                <div class="notification-wrapper" :class="notification.class">
                    <div class="notification">
                        <i class="fa-solid text-xl" :class="notification.icon" />
                        <p class="text-sm font-bold">{{ notification.text }}</p>
                        <div class="w-0 grow"></div>
                        <button class="self-stretch" @click="hideNotification(index)">
                            <i class="fa-solid fa-xmark text-xl"></i>
                        </button>
                    </div>
                </div>
            </div>
        </template>
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { wait } from '@/common';
import { useNotifications } from '@/ui/composables/Application';

interface Notification {
    icon: string;
    text: string;
    class: string;
    hidden: boolean;
}

const notificationService = useNotifications();
const notifications = ref<Notification[]>([]);

async function showNotification(message: string, type: 'success' | 'warning' | 'error' | 'info' = 'success'): Promise<void> {
    let icon = '';
    switch (type) {
        case 'success':
            icon = 'fa-check';
            break;
        case 'info':
            icon = 'fa-info';
            break;
        case 'warning':
            icon = 'fa-warning';
            break;
        case 'error':
            icon = 'fa-exclamation';
            break;
        default:
            icon = 'fa-check';
            break;
    }
    const notification: Notification = {
        icon: icon,
        text: message,
        class: `${type}`,
        hidden: false,
    };
    const index = notifications.value.push(notification) - 1;
    await wait(5000);
    notifications.value[index].class = `${type} animate-disappear`;
    await wait(5000);
    notifications.value[index].hidden = true;
}

async function hideNotification(index: number): Promise<void> {
    notifications.value[index].hidden = true;
}

function init(): void {
    notificationService.registerNotificationHandler((message, type) => showNotification(message, type));
}

init();
</script>

<style>
@reference "tailwindcss";

.notification-wrapper {
    overflow: hidden;
    @apply mb-4;
    @apply h-20;
    border-radius: var(--radius-xl);
    background-color: var(--color-surface);
    @apply shadow-xl;
}

.notification {
    display: flex;
    align-items: center;
    overflow: hidden;
    height: 100%;
    padding-top: 0;
    padding-bottom: 0;
    @apply gap-4;
    @apply px-6;
}

.notification button {
    pointer-events: auto;
}

.success .notification {
    background-color: --alpha(var(--color-success-container) / 50%);
    color: var(--color-onsuccess-container);
}

.success .notification p {
    color: var(--color-onsuccess-container);
}

.info .notification {
    background-color: --alpha(var(--color-info-container) / 50%);
    color: var(--color-oninfo-container);
}

.info .notification p {
    color: var(--color-oninfo-container);
}

.warning .notification {
    background-color: --alpha(var(--color-warning-container) / 50%);
    color: var(--color-onwarning-container);
}

.warning .notification p {
    color: var(--color-onwarning-container);
}

.error .notification {
    background-color: --alpha(var(--color-error-container) / 50%);
    color: var(--color-onerror-container);
}

.error .notification p {
    color: var(--color-onerror-container);
}

.animate-disappear {
    animation: anim-disappear 1s ease;
    opacity: 0;
    overflow: hidden;
    height: 0;
    padding: 0;
}

@keyframes anim-disappear {
    0% {
        opacity: 1;
        height: 5rem;
        padding-bottom: 1rem;
    }
    90% {
        opacity: 0;
        height: 5rem;
        padding-bottom: 1rem;
    }
    100% {
        opacity: 0;
        height: 0;
    }
}
</style>
