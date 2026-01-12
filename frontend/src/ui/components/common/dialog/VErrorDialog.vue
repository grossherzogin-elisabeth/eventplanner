<template>
    <VDialog ref="dlg" height="max-h-screen h-auto" type="modal-danger">
        <template #title>{{ error.title || $t('components.error-dialog.default-title') }}</template>
        <template #content>
            <div class="xs:px-8 px-4 pt-8 pb-8 lg:px-10">
                <p>
                    {{ error.message || $t('components.error-dialog.default-text') }}
                </p>
            </div>
            <div v-if="showDetails" class="max-h-64 overflow-auto pb-8">
                <pre class="xs:pl-8 text-onerror-container pl-4 text-xs lg:pl-10">{{ details }}</pre>
            </div>
        </template>
        <template #buttons>
            <button v-if="error.error && !showDetails" class="btn-ghost-danger" @click="showDetails = true">
                {{ $t('components.error-dialog.show-details') }}
            </button>
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
const showDetails = ref<boolean>(false);

const details = computed<string>(() => {
    const e = error.value.error;
    if (e instanceof Response) {
        return e.status + ': ' + e.statusText;
    }
    if (e instanceof Error) {
        return e.stack ?? e.message;
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
        showDetails.value = false;
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
