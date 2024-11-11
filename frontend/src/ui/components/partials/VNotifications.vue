<template>
    <div
        class="pointer-events-none fixed left-0 right-0 top-12 z-30 flex w-full flex-col items-stretch px-4 py-2 sm:left-auto sm:w-96 xl:right-16 xl:top-2"
    >
        <template v-for="(notification, index) in notifications" :key="index">
            <div v-if="!notification.hidden" :class="notification.class">
                <div class="notification">
                    <i class="fa-solid text-xl" :class="notification.icon" />
                    <p class="text-sm font-bold">{{ notification.text }}</p>
                    <div class="w-0 flex-grow"></div>
                    <button class="self-stretch" @click="hideNotification(index)">
                        <i class="fa-solid fa-xmark text-xl"></i>
                    </button>
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

async function showNotification(
    message: string,
    type: 'success' | 'warning' | 'error' | 'info' = 'success'
): Promise<void> {
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
        class: `h-20 pb-4 ${type}`,
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
.notification {
    @apply flex h-full items-center gap-4 overflow-hidden px-6 py-0;
}

.fixed .notification {
    @apply rounded-xl shadow-xl;
}

.notification button {
    @apply pointer-events-auto;
}

.success .notification {
    @apply bg-green-100 text-green-800;
}

.success .notification p {
    @apply text-green-900;
}

.info .notification {
    @apply bg-blue-100 text-blue-800;
}

.info .notification p {
    @apply text-blue-900;
}

.warning .notification {
    @apply bg-yellow-100 text-yellow-800;
}

.warning .notification p {
    @apply text-yellow-900;
}

.error .notification {
    @apply bg-error-container text-onerror-container;
}

.error .notification p {
    @apply text-onerror-container;
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
