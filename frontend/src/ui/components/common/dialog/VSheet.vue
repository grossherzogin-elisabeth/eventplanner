<template>
    <div v-if="renderContent">
        <teleport to="body">
            <div
                ref="background"
                :class="`${sheetOpen ? 'open' : 'closed'}`"
                class="sheet-background scrollbar-invisible fixed bottom-0 left-0 right-0 top-0 z-40 overflow-y-auto overscroll-contain sm:flex"
                :style="{
                    '--open-close-animation-duration': `${animationDuration}ms`,
                }"
                @scroll.passive="detectScrollClose($event)"
                @click="reject()"
            >
                <div
                    ref="wrapper"
                    class="sheet-wrapper overflow-clip sm:ml-auto sm:mt-0 sm:h-full sm:w-auto sm:p-4"
                    @click.stop=""
                    @mousedown.stop=""
                >
                    <div class="h-screen w-screen sm:hidden" @click="reject()" @pointerdown="reject()"></div>
                    <div
                        :class="$attrs.class"
                        class="flex max-h-screen flex-col overflow-clip rounded-t-3xl bg-surface-container-lowest pt-8 shadow-lg sm:h-full sm:max-h-full sm:rounded-3xl sm:pb-0 sm:pt-4"
                        :style="{ minHeight: `${sheetMinHeight}` }"
                    >
                        <div class="handle mx-auto mb-4 h-1 min-h-1 w-8 rounded-full bg-onsurface-variant sm:hidden"></div>
                        <div class="flex flex-1 flex-col overflow-clip" style="max-height: calc(100vh - 3.25rem)">
                            <div class="flex h-16 w-full items-center justify-between pl-8 lg:pl-10 lg:pr-2">
                                <div v-if="props.showBackButton" class="-ml-4 mr-4">
                                    <button class="icon-button" @click="back()">
                                        <i class="fa-solid fa-arrow-left"></i>
                                    </button>
                                </div>
                                <div class="flex w-0 flex-grow items-center overflow-hidden">
                                    <slot name="title"></slot>
                                </div>
                                <div class="sheet-close-button-wrapper hidden w-20 items-center justify-center sm:flex lg:w-16">
                                    <button class="sheet-close-button icon-button" @click="reject()">
                                        <i class="fa-solid fa-close"></i>
                                    </button>
                                </div>
                            </div>
                            <div class="flex flex-1 flex-col" :class="{ 'overflow-y-auto': isFullScreen }">
                                <slot name="content"></slot>
                                <slot name="default"></slot>
                            </div>
                            <div class="sticky bottom-0 z-50 bg-surface-container-lowest">
                                <slot name="bottom"></slot>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </teleport>
    </div>
</template>

<script lang="ts" setup>
import type { Ref } from 'vue';
import { computed } from 'vue';
import { nextTick, ref } from 'vue';
import { wait } from '@/common';
import type { Dialog } from './Dialog';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type T = any; // Result type
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type E = any; // Error type

interface Props {
    minHeight?: string;
    showBackButton?: boolean;
}

interface Emits {
    // The sheet starts the opening animation
    (e: 'opening'): void;

    // The sheets open animation finished
    (e: 'opened'): void;

    // The sheet starts the closing animation
    (e: 'closing'): void;

    // The sheets close animation finished
    (e: 'closed'): void;

    // The back button of the sheet has been clicked
    (e: 'back'): void;
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
const sheetOpen: Ref<boolean> = ref(false);
const sheetOpening: Ref<boolean> = ref(false);
const background: Ref<HTMLElement | null> = ref(null);
const wrapper: Ref<HTMLElement | null> = ref(null);
const renderContent: Ref<boolean> = ref(false);
const isFullScreen: Ref<boolean> = ref(false);
let promiseResolve: ((result: T) => void) | null = null;
let promiseReject: ((reason: T) => void) | null = null;
let closeTimeout: ReturnType<typeof setTimeout> | undefined = undefined;
const sheetMinHeight = computed<string>(() => props.minHeight || '10rem');

async function open(): Promise<T> {
    sheetOpening.value = true;
    clearTimeout(closeTimeout);
    emit('opening');
    renderContent.value = true;
    await nextTick(() => (sheetOpen.value = true));
    background.value?.scrollTo({ top: 400 });
    await wait(animationDuration);
    sheetOpening.value = false;
    emit('opened');
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

function back(): void {
    emit('back');
}

async function close(): Promise<void> {
    if (sheetOpening.value || !sheetOpen.value) {
        return;
    }
    sheetOpen.value = false;
    emit('closing');

    await nextTick();
    closeTimeout = setTimeout(() => {
        renderContent.value = false;
        emit('closed');
    }, animationDuration + 100);
}

function detectScrollClose(event: Event): void {
    const element = event.target as HTMLElement;
    if (element.scrollTop > window.innerHeight) {
        isFullScreen.value = true;
    } else {
        isFullScreen.value = false;
    }
    if (element.scrollTop < 25) {
        reject();
    } else if (element.scrollTop < 100 && !element.contains(document.activeElement)) {
        reject();
    }
}
</script>
<style scoped>
.sheet-background {
    --open-close-animation-duration: 250ms;
    --open-close-animation-diff-x: 0;
    --open-close-animation-diff-y: 100vh;
    background-color: rgba(0, 0, 0, 0);
}

.sheet-background.open {
    transition-property: background-color;
    transition-duration: var(--open-close-animation-duration);
    transition-timing-function: ease-in;
    background-color: rgba(0, 0, 0, 0.6);
    pointer-events: auto;
}

.sheet-background.open .sheet-wrapper {
    animation: anim-slide-in var(--open-close-animation-duration) ease;
    opacity: 1;
}

.sheet-wrapper {
    --anim-slide-diff-x: var(--open-close-animation-diff-x);
    --anim-slide-diff-y: var(--open-close-animation-diff-y);
    animation: anim-slide-out var(--open-close-animation-duration) ease;
    opacity: 0;
}

@media (min-width: 640px) {
    .sheet-background {
        --open-close-animation-diff-x: 100vw;
        --open-close-animation-diff-y: 0;
    }
}
</style>
