<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1>Reise absagen</h1>
        </template>
        <template #default>
            <div v-if="event" class="flex flex-1 flex-col px-8 pt-4 lg:px-10">
                <section>
                    <p class="mb-8 max-w-lg">
                        Wenn du die Reise absagst wird vom System automatisch eine Benachrichtigung an die Crew gesendet. Du kannst die zu
                        versendene Nachricht hier anpassen.
                    </p>
                    <div class="mb-4">
                        <VInputLabel>Nachricht an die Crew</VInputLabel>
                        <VInputTextArea v-model="message" class="min-h-64" />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-danger" @click="submit">
                <span>Reise absagen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputLabel, VInputTextArea } from '@/ui/components/common';

const i18n = useI18n();

const dlg = ref<Dialog<Event, string | undefined> | null>(null);
const event = ref<Event | null>(null);
const message = ref<string>('');

async function open(evt: Event): Promise<string | undefined> {
    event.value = evt;
    message.value = `
            Moin liebe Lissi Crew, \n

            ihr seid für die Reise
            vom ${i18n.d(evt.start, DateTimeFormat.DDD_DD_MM_YYYY)}
            bis zum ${i18n.d(evt.end, DateTimeFormat.DDD_DD_MM_YYYY)}
            als Crew eingeplant. Leider müssen wir die Reise hiermit absagen. \n

            Viele Grüße, \n
            Euer Büroteam`
        .split('\n')
        .map((l) => l.trim())
        .map((l) => (l.length === 0 ? '\n' : `${l} `))
        .join('')
        .trim();
    return await dlg.value?.open().catch(() => undefined);
}

function submit(): void {
    dlg.value?.submit(message.value);
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Event, string | undefined>>({
    open: (evt: Event) => open(evt),
    close: () => dlg.value?.reject(),
    submit: (msg?: string) => dlg.value?.submit(msg),
    reject: () => dlg.value?.reject(),
});
</script>
