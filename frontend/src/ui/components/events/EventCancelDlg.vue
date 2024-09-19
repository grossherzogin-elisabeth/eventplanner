<template>
    <VDialog ref="dlg" height="max-h-screen h-auto">
        <template #title>
            <h1>Reise absagen</h1>
        </template>
        <template #default>
            <div v-if="event" class="flex flex-1 flex-col p-8 lg:px-16">
                <p class="mb-8 max-w-lg">
                    Wenn du die Reise absagst wird vom System automatisch eine Benachrichtigung an die Crew gesendet. Du
                    kannst die zu versendene Nachricht hier anpassen.
                </p>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Nachricht an die Crew</VInputLabel>
                    <VInputTextArea v-model="message" class="min-h-64" />
                </div>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <i class="fa-solid fa-xmark"></i>
                <span>Nicht absagen</span>
            </button>
            <button class="btn-danger" @click="submit">
                <i class="fa-solid fa-ban"></i>
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

const dlg = ref<Dialog<Event, string> | null>(null);
const event = ref<Event | null>(null);
const message = ref<string>('');

async function open(evt: Event): Promise<string> {
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
    await dlg.value?.open();
    return message.value;
}

defineExpose<Dialog<Event, string>>({
    open: (evt: Event) => open(evt),
    close: () => dlg.value?.reject(),
    submit: (msg: string) => dlg.value?.submit(msg),
    reject: (reason?: void) => dlg.value?.reject(reason),
});
</script>
