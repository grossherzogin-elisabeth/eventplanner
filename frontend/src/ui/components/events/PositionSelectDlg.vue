<template>
    <VDialog ref="dlg" height="h-auto max-h-screen" class="modal" :class="$attrs.class">
        <template #title>
            <h1>Position auswählen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <p class="mb-4 text-sm">
                    Du hast die Qualifikation für mehrere Positionen. Mit welcher Position möchtest du dich für diese
                    Reise anmelden?
                </p>
                <div class="-mx-4 mb-8">
                    <VInputLabel>Position</VInputLabel>
                    <VInputSelect v-model="position" :options="availablePositions" required />
                </div>
                <p class="mb-4 text-sm">
                    Du kannst die ausgewählte Position als Standard festlegen, um dich für weitere Reisen noch einfacher
                    anmelden zu können.
                </p>
                <div class="mb-4">
                    <VInputCheckBox v-model="saveAsDefaultPosition" label="Als Standardposition merken" />
                </div>
            </div>
        </template>
        <template #buttons="{ submit, close }">
            <button class="btn-secondary" @click="close">
                <i class="fa-solid fa-xmark"></i>
                <span>Abbrechen</span>
            </button>
            <AsyncButton class="btn-primary" :action="() => submit()">
                <template #icon><i class="fa-solid fa-check"></i></template>
                <template #label>Anmelden</template>
            </AsyncButton>
        </template>
    </VDialog>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { InputSelectOption, PositionKey, UserDetails } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton } from '@/ui/components/common';
import { VInputSelect } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VDialog, VInputLabel } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { usePositions } from '@/ui/composables/Positions';

const positions = usePositions();
const usersUseCase = useUsersUseCase();

const user = ref<UserDetails | null>(null);

const dlg = ref<Dialog<void, PositionKey> | null>(null);
const position = ref<PositionKey | undefined>(undefined);
const saveAsDefaultPosition = ref<boolean>(false);

const availablePositions = computed<InputSelectOption<PositionKey>[]>(() => {
    return positions.options.value
        .filter((it) => it.value)
        .map((it) => ({ ...it, value: it.value || '' }))
        .filter((it) => user.value?.positionKeys.includes(it.value));
});

async function open(): Promise<PositionKey> {
    user.value = await usersUseCase.getUserDetailsForSignedInUser();
    position.value = user.value.positionKeys[0];

    await dlg.value?.open();

    if (saveAsDefaultPosition.value) {
        await usersUseCase.saveUserSettings({ preferredPosition: position.value });
    }
    return position.value;
}

defineExpose<Dialog<void, PositionKey>>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: (result: PositionKey) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
