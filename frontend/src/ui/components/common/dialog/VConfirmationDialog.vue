<template>
    <VDialog ref="dlg" height="max-h-screen h-auto error-dialog modal">
        <template #title>
            <h1 class="text-red-600">
                <slot name="title"></slot>
            </h1>
        </template>
        <template #content>
            <div class="px-8 py-8 lg:px-16">
                <p>
                    <slot name="message"></slot>
                </p>
            </div>
        </template>
        <template #buttons>
            <button class="btn-secondary" @click="cancel()">
                <slot name="cancel">Abbrechen</slot>
            </button>
            <button class="btn-danger" @click="submit()">
                <slot name="submit">Ja</slot>
            </button>
        </template>
    </VDialog>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import type { Dialog } from '@/ui/components/common';
import { VDialog } from '@/ui/components/common';

const dlg = ref<Dialog | null>(null);

function submit() {
    dlg.value?.submit();
}

async function open(): Promise<void> {
    await dlg.value?.open().catch();
}

function cancel(): void {
    dlg.value?.close();
}

defineExpose<Dialog>({
    open: async () => open(),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(),
    reject: (reason?: void) => dlg.value?.reject(reason),
});
</script>

<style #scoped>
.error-dialog .dialog-header {
    @apply border-red-600 bg-red-100;
}

.dialog.error-dialog {
    @apply bg-gradient-to-br from-red-50 from-50% to-red-50;
}

.error-dialog .dialog-close-button {
    @apply hover:bg-red-200 hover:text-red-700;
}
</style>
