<template>
    <div v-if="renderContent" :class="dialogOpen ? 'open' : 'closed'" class="dialog-background" @click="reject()">
        <div class="dialog-wrapper" @click.stop="">
            <slot name="dialog">
                <div
                    :class="`
                        ${props.width || 'w-screen max-w-xl'}
                        ${props.height || 'h-auto max-h-screen'}
                    `"
                    class="dialog"
                >
                    <div class="dialog-header pl-8 lg:pl-16">
                        <div class="flex flex-grow items-center pt-4">
                            <slot name="title"></slot>
                        </div>
                        <div class="flex w-20 items-center justify-center lg:w-16">
                            <button class="dialog-close-button" @click="reject()">
                                <i class="fa-solid fa-close"></i>
                            </button>
                        </div>
                    </div>
                    <div class="dialog-content flex flex-1 flex-col overflow-y-auto">
                        <slot name="content"></slot>
                        <slot name="default"></slot>
                    </div>
                    <div v-if="$slots.buttons" class="dialog-buttons">
                        <slot :close="reject" :reject="reject" :submit="submit" name="buttons"></slot>
                    </div>
                </div>
            </slot>
        </div>
    </div>
</template>

<script lang="ts" setup>
import type { Ref } from 'vue';
import { nextTick, ref } from 'vue';
import { WindowUtils } from '@/common';
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

const dialogOpen: Ref<boolean> = ref(false);
const renderContent: Ref<boolean> = ref(false);
let promiseResolve: ((result: T) => void) | null = null;
let promiseReject: ((reason: T) => void) | null = null;
let closeTimeout: ReturnType<typeof setTimeout> | undefined = undefined;

async function open(): Promise<T> {
    WindowUtils.disableScrolling();
    clearTimeout(closeTimeout);
    emit('opening');
    renderContent.value = true;
    await nextTick(() => (dialogOpen.value = true));
    emit('opened');

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
        renderContent.value = false;
        emit('closed');
        WindowUtils.enableScrolling();
    }, 400);
}
</script>
