<template>
    <div>
        <teleport to="#app">
            <div
                v-if="renderContent"
                ref="background"
                :class="`${sheetOpen ? 'open' : 'closed'}`"
                class="sheet-background scrollbar-invisible fixed top-0 right-0 bottom-0 left-0 z-40 overflow-y-auto overscroll-contain sm:flex"
                :style="{
                    '--open-close-animation-duration': `${animationDuration}ms`,
                }"
                @scroll.passive="onScroll()"
                @click="reject()"
            >
                <div
                    ref="wrapper"
                    class="sheet-wrapper overflow-clip sm:mt-0 sm:ml-auto sm:h-full sm:w-auto sm:p-4"
                    @click.stop=""
                    @mousedown.stop=""
                >
                    <div class="h-screen w-screen sm:hidden" @click="reject()" @pointerdown="reject()"></div>
                    <div
                        class="flex max-h-screen flex-col overflow-clip rounded-t-3xl shadow-lg transition-all duration-200 sm:h-full sm:max-h-full sm:rounded-3xl sm:pt-4 sm:pb-0"
                        :class="
                            isFullyExpanded ? `${$attrs.class} bg-surface-container h-screen` : `${$attrs.class} bg-surface-container-low`
                        "
                    >
                        <div class="handle flex items-center justify-center sm:hidden">
                            <div class="bg-onsurface-variant mt-4 mb-4 h-1 min-h-1 w-8 rounded-full"></div>
                        </div>
                        <div
                            class="flex h-screen flex-1 flex-col overflow-hidden"
                            style="max-height: calc(var(--viewport-height, 100vh) - 2.25rem)"
                        >
                            <div
                                class="sheet-header xs:pl-8 z-10 flex h-12 w-full items-center justify-between pl-4 sm:pr-8 lg:pr-10 lg:pl-10"
                                :class="{ 'shadow-sm': isFullyExpanded }"
                            >
                                <div v-if="props.showBackButton" class="mr-4 -ml-4">
                                    <button class="btn-icon" @click="back()">
                                        <i class="fa-solid fa-arrow-left"></i>
                                    </button>
                                </div>
                                <div class="flex h-16 w-0 grow items-center overflow-hidden font-bold">
                                    <slot name="title"></slot>
                                </div>
                                <div class="-mr-4 hidden sm:block">
                                    <button class="btn-icon" @click="reject()">
                                        <i class="fa-solid fa-close"></i>
                                    </button>
                                </div>
                            </div>
                            <div
                                class="sheet-content bg-surface-container-low mb-0 flex flex-1 flex-col pt-4 md:mb-0 md:overflow-y-auto"
                                :class="{ 'overflow-y-auto': isFullyExpanded }"
                            >
                                <slot name="content"></slot>
                                <slot name="default"></slot>
                            </div>
                            <div class="bg-surface-container-low sticky bottom-0 z-50 sm:bottom-4">
                                <slot name="bottom"></slot>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </teleport>
    </div>
</template>

<script lang="ts" setup generic="T, E">
import type { Ref } from 'vue';
import { onUnmounted } from 'vue';
import { nextTick, ref } from 'vue';
import { disableScrolling, enableScrolling, wait } from '@/common';
import type { Sheet } from '@/ui/components/common';

interface Props {
    showBackButton?: boolean;
}

interface Emits {
    /**
     * The sheet starts the opening animation
     */
    (e: 'opening'): void;
    /**
     * The sheets open animation finished
     */
    (e: 'opened'): void;
    /**
     * The sheet starts the closing animation
     */
    (e: 'closing'): void;
    /**
     * The sheets close animation finished
     */
    (e: 'closed'): void;
    /**
     * The back button of the sheet has been clicked
     */
    (e: 'back'): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
defineExpose<Sheet<void, T | undefined, E>>({
    open: () => open(),
    close: () => reject(),
    submit: (result?: T) => submit(result),
    reject: (reason?: E) => reject(reason),
});

const animationDuration = 250;

const background: Ref<HTMLElement | null> = ref(null);
const wrapper: Ref<HTMLElement | null> = ref(null);

const sheetOpen: Ref<boolean> = ref(false);
const sheetOpening: Ref<boolean> = ref(false);
const renderContent: Ref<boolean> = ref(false);
const isFullyExpanded: Ref<boolean> = ref(false);
const scrollTop: Ref<number> = ref(0);

let promiseResolve: ((result: T | undefined) => void) | null = null;
let promiseReject: ((reason: E | undefined) => void) | null = null;
let closeTimeout: ReturnType<typeof setTimeout> | undefined = undefined;

function init(): void {
    window.addEventListener('resize', onWindowResize, { passive: true });
    onUnmounted(() => enableScrolling());
}

async function open(): Promise<T | undefined> {
    // disable background scrolling to prevent scroll glitches
    disableScrolling();
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
    return new Promise<T | undefined>((resolve, reject) => {
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
        enableScrolling();
        renderContent.value = false;
        emit('closed');
    }, animationDuration + 100);
}

function onWindowResize(): void {
    if (!background.value) {
        return;
    }
    if (background.value.scrollTop < scrollTop.value) {
        background.value.scrollTop = scrollTop.value;
    }
    onScroll();
}

async function onScroll(): Promise<void> {
    // give it a tick to render
    await nextTick();

    calcIsFullyExpanded();
    calcScrollTop();
    detectScrollClose();
}

function calcIsFullyExpanded(): void {
    if (background.value) {
        isFullyExpanded.value = background.value.scrollTop >= getViewportHeight();
    }
}

function calcScrollTop(): void {
    if (background.value) {
        scrollTop.value = background.value.scrollTop;
    }
}

function detectScrollClose(): void {
    if (background.value) {
        // close the sheet when the users scrolls it to the bottom
        const threshold = background.value.contains(document.activeElement) ? 25 : 100;
        if (background.value.scrollTop < threshold) {
            reject();
        }
    }
}

function getViewportHeight(): number {
    return window.visualViewport?.height ?? window.innerHeight;
}

init();
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

@media (prefers-color-scheme: dark) {
    .sheet-background {
        background-color: rgba(0, 0, 0, 0);
    }

    .sheet-background.open {
        background-color: rgba(0, 0, 0, 0.8);
    }
}
</style>
