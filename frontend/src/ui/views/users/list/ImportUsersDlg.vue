<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Nutzer importieren</h1>
        </template>
        <template #default>
            <div class="px-8 pt-4 lg:px-10">
                <section>
                    <p class="mb-4">
                        Hier kannst du Nutzer aus einer Excel Datei importieren. Die Verarbeitung der Datei dauert ein
                        paar Minuten.
                    </p>
                    <div class="-mx-4 mb-2">
                        <VInputLabel>Datei</VInputLabel>
                        <VInputFile v-model="file" />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button v-if="uploadComplete" class="btn-primary" @click="submit">Schließen</button>
            <template v-else>
                <button class="btn-ghost" @click="reject">
                    <span>Abbrechen</span>
                </button>
                <div class="w-auto">
                    <AsyncButton :action="submit" :disabled="!file">
                        <template #label="{ loading }">
                            <template v-if="loading">Nutzer werden importiert</template>
                            <template v-else>Nutzer importieren</template>
                        </template>
                    </AsyncButton>
                </div>
            </template>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VDialog, VInputFile, VInputLabel } from '@/ui/components/common';
import { useUserAdministrationUseCase } from '@/ui/composables/Application.ts';

const userAdministrationUseCase = useUserAdministrationUseCase();

const dlg = ref<Dialog | null>(null);

const file = ref<Blob | undefined>(undefined);
const uploadComplete = ref<boolean>(false);

async function open(): Promise<void> {
    if (file.value) {
        file.value = undefined;
    }
    uploadComplete.value = false;
    await dlg.value?.open();
    if (file.value) {
        await userAdministrationUseCase.importUsers(file.value);
    }
    file.value = undefined;
}

defineExpose<Dialog>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(),
    reject: () => dlg.value?.reject(),
});
</script>
