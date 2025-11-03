<template>
    <VDialog ref="dlg" height="max-h-screen h-auto" :type="content?.danger ? 'modal-danger' : 'modal'">
        <template #title>
            <h1 class="truncate">
                <slot name="title">{{ content?.title }}</slot>
            </h1>
        </template>
        <template #content>
            <div class="xs:px-8 px-4 py-8 lg:px-10">
                <p>
                    <slot name="message">{{ content?.message }}</slot>
                </p>
            </div>
        </template>
        <template #buttons>
            <button :class="content?.danger ? 'btn-ghost-danger' : 'btn-ghost'" @click="cancel()">
                <slot name="cancel">{{ content?.cancel || 'Abbrechen' }}</slot>
            </button>
            <button :class="content?.danger ? 'btn-ghost-danger' : 'btn-ghost'" @click="submit()">
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
