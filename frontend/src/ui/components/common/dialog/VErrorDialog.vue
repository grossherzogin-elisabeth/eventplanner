<template>
    <VDialog ref="dlg" height="max-h-screen h-auto" type="modal-danger">
        <template #title>{{ error.title || $t('components.error-dialog.default-title') }}</template>
        <template #content>
            <div class="px-4 px-8 py-8 lg:px-10">
                <p>
                    {{ error.message || $t('components.error-dialog.default-text') }}
                </p>
                <div v-if="error.error" class="mt-8">
                    <h2 class="text-error mb-4">
                        {{ $t('components.error-dialog.details') }}
                    </h2>
                    <p>
                        {{ details }}
                    </p>
                </div>
            </div>
        </template>
        <template #buttons>
            <template v-if="error.retry">
                <button data-test-id="button-close" class="btn-ghost-danger" @click="submit">
                    {{ error.cancelText || $t('generic.close') }}
                </button>
                <button data-test-id="button-retry" class="btn-ghost-danger" @click="retry()">
                    {{ error.retryText || $t('components.error-dialog.retry') }}
                </button>
            </template>
            <button v-else data-test-id="button-close" class="btn-ghost-danger" @click="submit">
                {{ error.cancelText || $t('generic.close') }}
            </button>
        </template>
    </VDialog>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useErrorHandlingService } from '@/application';
import type { Dialog } from '@/ui/components/common';
import { VDialog } from '@/ui/components/common';
import type { ErrorDialogMessage } from './ErrorDialog';

const errorHandlingService = useErrorHandlingService();
const dlg = ref<Dialog<ErrorDialogMessage, void> | null>(null);
const error = ref<ErrorDialogMessage>({});

const details = computed<string>(() => {
    const e = error.value.error;
    console.log(e);
    if (e instanceof Response) {
        return e.status + ': ' + e.statusText;
    }
    if (e instanceof Error) {
        return String(e);
    }
    return '';
});

function init(): void {
    errorHandlingService.registerErrorHandler((err) => open(err));
}

function submit(): void {
    dlg.value?.submit();
}

async function open(message?: ErrorDialogMessage): Promise<void> {
    if (dlg.value && message) {
        error.value = message;
        await dlg.value?.open(message).catch(() => undefined);
    }
}

function retry(): void {
    if (error.value?.retry && dlg.value) {
        error.value.retry();
        dlg.value.close();
    }
}

defineExpose<Dialog<ErrorDialogMessage, void>>({
    open: async (params?: ErrorDialogMessage) => open(params),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(),
    reject: () => dlg.value?.reject(),
});

init();
</script>
