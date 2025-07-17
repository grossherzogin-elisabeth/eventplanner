<template>
    <div>
        <teleport to="#app">
            <div
                v-if="renderContent"
                :class="`${dialogOpen ? 'open' : 'closed'} ${props.type || 'fullscreen'}`"
                :style="{
                    '--animation-duration': `${animationDuration}ms`,
                }"
                class="dialog-background scrollbar-invisible pointer-events-none fixed bottom-0 left-0 right-0 top-0 z-40 flex items-center justify-center"
                @mousedown="reject()"
            >
                <div ref="wrapper" class="dialog-wrapper xl:ml-20" @click.stop="" @mousedown.stop="">
                    <slot name="dialog">
                        <div
                            :class="` ${props.width || 'w-screen max-w-xl'} ${props.height || 'h-auto max-h-[95vh]'} ${$attrs.class} `"
                            class="dialog flex flex-col overflow-hidden bg-surface sm:rounded-3xl sm:shadow-lg"
                        >
                            <div class="dialog-header flex h-16 w-full items-center justify-between pl-8 lg:pl-10 lg:pr-2">
                                <div class="dialog-back-button-wrapper -ml-4 mr-4">
                                    <button class="icon-button" @click="reject()">
                                        <i class="fa-solid fa-arrow-left"></i>
                                    </button>
                                </div>
                                <div class="flex w-0 flex-grow items-center overflow-hidden">
                                    <slot name="title"></slot>
                                </div>
                                <div class="dialog-close-button-wrapper flex w-20 items-center justify-center lg:w-16">
                                    <button class="dialog-close-button icon-button" @click="reject()">
                                        <i class="fa-solid fa-close"></i>
                                    </button>
                                </div>
                            </div>
                            <div class="dialog-content flex flex-1 flex-col overflow-y-auto">
                                <slot name="content"></slot>
                                <slot name="default"></slot>
                            </div>
                            <div v-if="$slots.buttons" class="dialog-buttons flex justify-end gap-2 px-8 py-4 lg:px-10">
                                <slot :close="reject" :reject="reject" :submit="submit" name="buttons"></slot>
                            </div>
                        </div>
                    </slot>
                </div>
            </div>
        </teleport>
    </div>
</template>

<script lang="ts" setup>
import type { Ref } from 'vue';
import { nextTick, ref } from 'vue';
import { disableScrolling, enableScrolling } from '@/common';
import type { Dialog } from './Dialog';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type T = any; // Result type
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type E = any; // Error type

interface Props {
    // dialog width css class
    width?: string;
    // dialog height css class
    height?: string;
    // how should this dialog behave on mobile screens
    type?: 'fullscreen' | 'modal';
}

interface Emits {
    // Dialog is opening with animation
    (e: 'opening'): void;

    // Dialogs open animation finished
    (e: 'opened'): void;

    // Dialog is closing with animation
    (e: 'closing'): void;

    // Dialogs close animation finished
    (e: 'closed'): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
defineExpose<Dialog>({
    open: () => open(),
    close: () => reject(),
    submit: (result?: T) => submit(result),
    reject: (reason?: E) => reject(reason),
});

const animationDuration = 250;
const dialogOpen: Ref<boolean> = ref(false);
const wrapper: Ref<HTMLElement | null> = ref(null);
const renderContent: Ref<boolean> = ref(false);
let promiseResolve: ((result: T) => void) | null = null;
let promiseReject: ((reason: T) => void) | null = null;
let closeTimeout: ReturnType<typeof setTimeout> | undefined = undefined;

async function open(): Promise<T> {
    disableScrolling();
    clearTimeout(closeTimeout);
    emit('opening');
    renderContent.value = true;
    await nextTick(() => (dialogOpen.value = true));
    setTimeout(() => {
        emit('opened');
        wrapper.value?.querySelector('input')?.focus();
    }, animationDuration);
    window.addEventListener('cancel', close, { once: true });

    // this promise is resolved, when the dialog is closed
    return new Promise<T>((resolve, reject) => {
        promiseResolve = resolve;
        promiseReject = reject;
    });
}

async function submit(result?: T): Promise<void> {
    if (promiseResolve !== null) {
        promiseResolve(result);
    }
    await close();
}

async function reject(reason?: E): Promise<void> {
    if (promiseReject !== null) {
        promiseReject(reason);
    }
    await close();
}

async function close(): Promise<void> {
    dialogOpen.value = false;
    emit('closing');

    await nextTick();
    closeTimeout = setTimeout(() => {
        // renderContent.value = false;
        emit('closed');
        enableScrolling();
    }, animationDuration + 100);
}
</script>
<style scoped>
.dialog-background {
    --animation-duration: 250ms;
    --anim-slide-diff-x: 0;
    --anim-slide-diff-y: 2rem;
    --anim-color-from: rgba(0, 0, 0, 0);
    --anim-color-to: rgba(0, 0, 0, 0.6);
    animation: anim-color var(--animation-duration) ease-in reverse;
    background-color: var(--anim-color-from);
}

.dialog-background.open {
    animation: anim-color var(--animation-duration) ease-in;
    background-color: var(--anim-color-to);
    pointer-events: auto;
}

.dialog-background:has(.error-dialog) {
    @apply z-50;
}

.dialog-wrapper {
    animation: anim-slide-out var(--animation-duration) ease;
    opacity: 0;
}

.dialog-background.open .dialog-wrapper {
    animation: anim-slide-in var(--animation-duration) ease;
    opacity: 1;
}

@media (max-width: 639px) {
    .modal .dialog {
        @apply rounded-3xl shadow-lg;
        max-width: min(calc(100vw - 2rem), 35rem);
    }
}

@media (max-width: 639px) {
    .fullscreen.dialog-background {
        --anim-slide-diff-x: 100vw;
        --anim-slide-diff-y: 0;
    }

    .fullscreen .dialog-wrapper {
        @apply w-full overflow-hidden;
        height: var(--viewport-height);
    }

    .fullscreen .dialog {
        @apply h-full max-h-full w-full max-w-full !important;
    }

    .fullscreen .dialog .dialog-header {
        height: var(--nav-height);
        @apply truncate bg-primary text-onprimary;
    }

    .fullscreen .dialog .dialog-close-button {
        @apply text-white;
    }

    .fullscreen .dialog .dialog-back-button-wrapper {
        @apply block;
    }

    .fullscreen .dialog .dialog-close-button-wrapper {
        @apply hidden;
    }
}

.error-dialog .dialog-header {
    @apply border-onerror-container bg-error-container;
}

.dialog.error-dialog > .dialog-content,
.dialog.error-dialog > .dialog-buttons {
    @apply bg-error-container bg-opacity-50;
}

.error-dialog .dialog-close-button {
    @apply text-onerror-container;
}

.error-dialog h1 {
    @apply text-onerror-container;
}
</style>
