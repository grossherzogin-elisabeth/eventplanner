<template>
    <VDialog ref="dlg" height="max-h-screen h-auto modal" :class="{ 'error-dialog': content?.danger }">
        <template #title>
            <h1 class="">
                <slot name="title">{{ content?.title }}</slot>
            </h1>
        </template>
        <template #content>
            <div class="px-8 py-8 lg:px-16">
                <p>
                    <slot name="message">{{ content?.message }}</slot>
                </p>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel()">
                <slot name="cancel">{{ content?.cancel || 'Abbrechen' }}</slot>
            </button>
            <button :class="content?.danger ? 'btn-danger' : 'btn-primary'" @click="submit()">
                <slot name="submit">{{ content?.submit || 'Ja' }}</slot>
            </button>
        </template>
    </VDialog>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import type { ConfirmationDialog, ConfirmationDialogContent } from './ConfirmationDialog.ts';
import VDialog from './VDialog.vue';

const content = ref<ConfirmationDialogContent | undefined>(undefined);
const dlg = ref<ConfirmationDialog | null>(null);

async function open(dialogContent?: ConfirmationDialogContent): Promise<boolean | undefined> {
    content.value = dialogContent;
    if (!dlg.value) {
        return undefined;
    }
    return await dlg.value.open().catch(() => undefined);
}

function submit(): void {
    dlg.value?.submit(true);
}

function cancel(): void {
    dlg.value?.submit(false);
}

defineExpose<ConfirmationDialog>({
    open: async (dialogContent: ConfirmationDialogContent) => open(dialogContent),
    close: () => dlg.value?.reject(),
    submit: (confirm?: boolean) => dlg.value?.submit(confirm),
    reject: () => dlg.value?.reject(),
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

.error-dialog h1 {
    @apply text-red-600;
}
</style>
