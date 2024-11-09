<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 v-if="location.order === 1">Starthafen bearbeiten</h1>
            <h1 v-else-if="location.order === -1">Reiseabschnitt hinzufügen</h1>
            <h1 v-else>Reiseabschnitt bearbeiten</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <div class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText
                        v-model="location.name"
                        :errors="validation.errors.value['name']"
                        :errors-visible="validation.showErrors.value"
                        required
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Icon</VInputLabel>
                    <VInputSelect
                        v-model="location.icon"
                        placeholder="fa-anchor"
                        :errors="validation.errors.value['icon']"
                        :errors-visible="validation.showErrors.value"
                        required
                        :options="[
                            { value: 'fa-anchor', label: 'fa-anchor' },
                            { value: 'fa-water', label: 'fa-water' },
                            { value: 'fa-house', label: 'fa-house' },
                            { value: 'fa-city', label: 'fa-city' },
                            { value: 'fa-sailboat', label: 'fa-sailboat' },
                            { value: 'fa-screwdriver-wrench', label: 'fa-screwdriver-wrench' },
                        ]"
                    >
                        <template #item="{ item }">
                            <i class="fa-solid mr-4" :class="item.value"></i>
                            <span class="flex-grow">{{ item.label }}</span>
                        </template>
                        <template #before>
                            <span :key="location.icon">
                                <i class="fa-solid ml-4 mt-5" :class="location.icon"></i>
                            </span>
                        </template>
                    </VInputSelect>
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Adresse</VInputLabel>
                    <VInputTextArea
                        v-model="location.address"
                        :errors="validation.errors.value['address']"
                        :errors-visible="validation.showErrors.value"
                    />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Land</VInputLabel>
                    <VInputCombobox
                        v-model="location.country"
                        :errors="validation.errors.value['country']"
                        :errors-visible="validation.showErrors.value"
                        :options="countries.options"
                    />
                </div>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Übernehmen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy } from '@/common';
import type { Location, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputTextArea } from '@/ui/components/common';
import { VInputCombobox } from '@/ui/components/common';
import { VInputSelect } from '@/ui/components/common';
import { VDialog, VInputLabel, VInputText } from '@/ui/components/common';
import { useCountries } from '@/ui/composables/Countries.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

const countries = useCountries();

const dlg = ref<Dialog<Location | undefined, Location | undefined> | null>(null);
const location = ref<Location>({
    name: '',
    address: '',
    icon: 'fa-anchor',
    country: '',
    order: -1,
});

const validation = useValidation(location, (value) => {
    // TODO extract to service
    const errors: Record<string, ValidationHint[]> = {};
    if (!value.name) {
        errors.name = errors.name || [];
        errors.name.push({ key: 'Bitte gib einen Namen an', params: {} });
    }
    if (!value.icon) {
        errors.icon = errors.icon || [];
        errors.icon.push({ key: 'Bitte wähle ein Icon', params: {} });
    }
    return errors;
});

async function open(value?: Location): Promise<Location | undefined> {
    validation.reset();
    location.value = value
        ? deepCopy(value)
        : {
              name: '',
              address: '',
              icon: 'fa-anchor',
              country: '',
              order: -1,
          };
    return await dlg.value?.open().catch(() => undefined);
}

function submit(): void {
    if (validation.isValid.value) {
        dlg.value?.submit(location.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Location | undefined, Location | undefined>>({
    open: (value?: Location) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result?: Location) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
