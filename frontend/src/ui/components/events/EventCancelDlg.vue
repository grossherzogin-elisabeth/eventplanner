<template>
    <VDialog ref="dlg" height="max-h-screen h-auto" type="modal-danger" data-test-id="cancel-event-dialog">
        <template #title>{{ $t('components.event-cancel-dialog.title', { count: events?.length }) }}</template>
        <template #default>
            <div v-if="events" class="flex flex-1 flex-col px-8 pt-4 lg:px-10">
                <section>
                    <p class="mb-8 max-w-lg">
                        {{ $t('components.event-cancel-dialog.message', { count: events?.length }) }}
                    </p>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost-danger" type="button" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-ghost-danger" type="button" @click="submit">
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

const dlg = ref<Dialog<Event[], boolean> | null>(null);
const events = ref<Event[] | null>(null);

async function open(evts: Event[]): Promise<boolean> {
    events.value = evts;
    return (await dlg.value?.open().catch(() => false)) ?? false;
}

function submit(): void {
    dlg.value?.submit(true);
}

function cancel(): void {
    dlg.value?.submit(false);
}

defineExpose<Dialog<Event[], boolean>>({
    open: (evts: Event[]) => open(evts),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(true),
    reject: () => dlg.value?.reject(),
});
</script>
