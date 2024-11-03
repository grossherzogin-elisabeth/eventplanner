<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Reisen importieren</h1>
        </template>
        <template #default>
            <div v-if="errors" class="relative h-96 overflow-auto px-8 lg:px-16">
                <p class="mb-8 max-w-lg">
                    Die Reisen wurden erfolgreich importiert. Beim Verarbeiten der Datei sind allerdings die folgenden
                    Fehler gefunden worden. Einige konnten automatisch korrigiert werden. in jedem Fall sollten diese
                    Fehler aber geprüft werden.
                </p>
                <VTable :items="errors" :page-size="-1">
                    <template #row="{ item }">
                        <td class="w-full">
                            <p class="mb-2 font-semibold">
                                {{ item.eventName }} ({{ formatDateRange(item.start, item.end) }})
                            </p>
                            <ul class="mb-4 ml-4 list-disc text-sm">
                                <li v-for="(message, index) in item.messages" :key="index">
                                    {{ message }}
                                </li>
                            </ul>
                        </td>
                    </template>
                </VTable>
            </div>
            <div v-else class="p-8 lg:px-16">
                <p class="mb-4">
                    Hier kannst du Reisen aus einer Excel Datei importieren. Die Verarbeitung der Datei dauert ein paar
                    Minuten.
                </p>
                <VWarning class="-mx-4">
                    Wenn du Reisen aus einer Excel Datei importierts, werden dadurch alle anderen Reisen für das
                    ausgewählte Jahr überschrieben!
                </VWarning>
                <div class="-mx-4 mb-4 mt-8">
                    <VInputLabel>Jahr</VInputLabel>
                    <VInputSelect v-model="year" :options="yearOptions" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Datei</VInputLabel>
                    <VInputFile v-model="file" />
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button v-if="uploadComplete" class="btn-primary" @click="submit">Schließen</button>
            <template v-else>
                <button class="btn-secondary" @click="reject">
                    <span>Abbrechen</span>
                </button>
                <div class="w-auto">
                    <AsyncButton :action="upload" :disabled="!file">
                        <template #label="{ loading }">
                            <template v-if="loading">Reisen werden importiert</template>
                            <template v-else>Reisen importieren</template>
                        </template>
                    </AsyncButton>
                </div>
            </template>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import type { ImportError, InputSelectOption } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VDialog, VInputFile, VInputLabel, VInputSelect, VTable, VWarning } from '@/ui/components/common';
import { useEventAdministrationUseCase } from '@/ui/composables/Application.ts';
import { formatDateRange } from '@/ui/composables/DateRangeFormatter.ts';

const eventAdministrationUseCase = useEventAdministrationUseCase();

const dlg = ref<Dialog | null>(null);
const yearOptions: InputSelectOption<number>[] = [
    new Date().getFullYear() - 1,
    new Date().getFullYear(),
    new Date().getFullYear() + 1,
    new Date().getFullYear() + 2,
].map((y) => ({ label: String(y), value: y }));

const year = ref<number>(new Date().getFullYear());
const file = ref<Blob | undefined>(undefined);
const errors = ref<ImportError[] | null>(null);
const uploadComplete = ref<boolean>(false);

async function open(): Promise<void> {
    file.value = undefined;
    errors.value = null;
    uploadComplete.value = false;
    await dlg.value?.open();
}

async function upload(): Promise<void> {
    if (file.value) {
        errors.value = await eventAdministrationUseCase.importEvents(year.value, file.value);
        uploadComplete.value = true;
    }
}

defineExpose<Dialog>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(),
    reject: () => dlg.value?.reject(),
});
</script>
