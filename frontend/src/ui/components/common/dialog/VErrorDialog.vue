<template>
    <VDialog ref="dlg" height="max-h-screen h-auto" class="error-dialog modal">
        <template #title>
            <h1 class="text-red-600">
                {{ error.title || 'Unwerwarteter Fehler' }}
            </h1>
        </template>
        <template #content>
            <div class="px-8 py-8 lg:px-16">
                <p v-if="error.message">
                    {{ error.message }}
                </p>
                <p v-else>
                    Ein unerwarteter Fehler ist aufgetreten. Bitte versuche es erneut. Sollte der Fehler wiederholt
                    auftreten, melde ihn gerne an admin@grossherzogin-elisabeth.de.
                </p>
                <div v-if="error.error" class="mt-8">
                    <h2 class="mb-4 text-red-600">Details</h2>
                    <p>
                        {{ details }}
                    </p>
                </div>
            </div>
        </template>
        <template #buttons>
            <template v-if="error.retry">
                <button class="btn-secondary" @click="submit">Schließen</button>
                <button class="btn-danger" @click="retry()">Erneut versuchen</button>
            </template>
            <button v-else class="btn-danger" @click="submit">Schließen</button>
        </template>
    </VDialog>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { Dialog } from '@/ui/components/common';
import { VDialog } from '@/ui/components/common';
import { useErrorHandling } from '@/ui/composables/Application';
import type { ErrorDialogMessage } from './ErrorDialog';

const errorHandlingUseCase = useErrorHandling();
const dlg = ref<Dialog<ErrorDialogMessage, void> | null>(null);
const error = ref<ErrorDialogMessage>({});

const details = computed<string>(() => {
    const e = error.value.error;
    console.log(e);
    if (e instanceof Response) {
        return e.status + ': ' + e.statusText;
    }
    if (e instanceof Error) {
        console.log('error');
    }
    return '';
});

function init() {
    errorHandlingUseCase.registerErrorHandler((err) => open(err));
}

function submit() {
    dlg.value?.submit();
}

async function open(message?: ErrorDialogMessage): Promise<void> {
    if (dlg.value && message) {
        error.value = message;
        await dlg.value?.open(message).catch();
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
