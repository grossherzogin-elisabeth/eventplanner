<template>
    <VDialog ref="dlg" height="h-auto max-h-screen" class="modal" :class="$attrs.class">
        <template #title>
            <h1>Position auswählen</h1>
        </template>
        <template #default>
            <div class="px-8 pt-4 lg:px-10">
                <section>
                    <p class="mb-4 text-sm">
                        Du hast die Qualifikation für mehrere Positionen. Mit welcher Position möchtest du dich für
                        diese Reise anmelden?
                    </p>
                    <div class="mb-8">
                        <VInputLabel>Position</VInputLabel>
                        <VInputSelect v-model="position" :options="availablePositions" required />
                    </div>
                    <p class="mb-4 text-sm">
                        Du kannst die ausgewählte Position als Standard festlegen, um dich für weitere Reisen noch
                        einfacher anmelden zu können.
                    </p>
                </section>
                <div class="mb-4">
                    <VInputCheckBox v-model="saveAsDefaultPosition" label="Als Standardposition merken" />
                </div>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" @click="submit">
                <span>Anmelden</span>
            </button>
        </template>
    </VDialog>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { InputSelectOption, PositionKey, UserDetails } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputSelect } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VDialog, VInputLabel } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { usePositions } from '@/ui/composables/Positions';

const positions = usePositions();
const usersUseCase = useUsersUseCase();

const dlg = ref<Dialog<void, PositionKey | undefined> | null>(null);
const user = ref<UserDetails | null>(null);
const position = ref<PositionKey | undefined>(undefined);
const saveAsDefaultPosition = ref<boolean>(false);

const availablePositions = computed<InputSelectOption<PositionKey>[]>(() => {
    return positions.options.value
        .filter((it) => it.value)
        .map((it) => ({ ...it, value: it.value || '' }))
        .filter((it) => user.value?.positionKeys.includes(it.value));
});

async function open(): Promise<PositionKey | undefined> {
    user.value = await usersUseCase.getUserDetailsForSignedInUser();
    position.value = user.value.positionKeys[0];

    const result = await dlg.value?.open().catch(() => undefined);
    if (!result) {
        return undefined;
    }
    if (saveAsDefaultPosition.value) {
        await usersUseCase.saveUserSettings({ preferredPosition: result });
    }
    return result;
}

function submit(): void {
    dlg.value?.submit(position.value);
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<void, PositionKey | undefined>>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: (result?: PositionKey) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
