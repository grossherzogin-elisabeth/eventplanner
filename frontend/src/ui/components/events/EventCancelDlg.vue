<template>
    <VDialog ref="dlg" height="max-h-screen h-auto" type="modal-danger" data-test-id="cancel-event-dialog">
        <template #title>
            {{ $t('components.event-cancel-dialog.title', { count: events?.length ?? 1 }) }}
        </template>
        <template #default>
            <div v-if="events" class="flex flex-1 flex-col px-8 pt-4 lg:px-10">
                <section>
                    <p class="mb-8 max-w-lg">
                        {{ $t('components.event-cancel-dialog.message', { count: events.length }) }}
                    </p>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost-danger" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-ghost-danger" @click="submit">
                <span>{{ $t('components.event-cancel-dialog.submit') }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import type { Event } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog } from '@/ui/components/common';

const dlg = ref<Dialog<Event | Event[], boolean> | null>(null);
const events = ref<Event[] | null>(null);

async function open(evt: Event | Event[]): Promise<boolean> {
    events.value = Array.isArray(evt) ? evt : [evt];
    return (await dlg.value?.open().catch(() => false)) ?? false;
}

function submit(): void {
    dlg.value?.submit(true);
}

function cancel(): void {
    dlg.value?.submit(false);
}

defineExpose<Dialog<Event | Event[], boolean>>({
    open: (evt: Event | Event[]) => open(evt),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(true),
    reject: () => dlg.value?.reject(),
});
</script>
