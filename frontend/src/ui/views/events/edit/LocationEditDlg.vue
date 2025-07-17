<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 v-if="location.order === 1">Starthafen bearbeiten</h1>
            <h1 v-else-if="location.order === -1">Reiseabschnitt hinzufügen</h1>
            <h1 v-else>Reiseabschnitt bearbeiten</h1>
        </template>
        <template #default>
            <div class="px-4 pt-4 xs:px-8 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputLabel>Name</VInputLabel>
                        <VInputText
                            v-model.trim="location.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
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
                    <div class="mb-2 flex space-x-4">
                        <div class="w-3/5">
                            <VInputLabel>EDA</VInputLabel>
                            <VInputDate
                                :model-value="location.eta"
                                :errors="validation.errors.value['start']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.eta = updateDate(location.eta, $event)"
                            />
                        </div>
                        <div class="w-2/5">
                            <VInputLabel>ETA</VInputLabel>
                            <VInputTime
                                :model-value="location.eta"
                                :errors="validation.errors.value['start']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.eta = updateTime(location.eta, $event, 'minutes')"
                            />
                        </div>
                    </div>
                    <div class="mb-4 flex justify-end">
                        <button class="link text-sm" @click="location.eta = undefined">ETA löschen</button>
                    </div>
                    <div class="mb-2 flex space-x-4">
                        <div class="w-3/5">
                            <VInputLabel>EDD</VInputLabel>
                            <VInputDate
                                :model-value="location.etd"
                                :errors="validation.errors.value['etd']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.etd = updateDate(location.etd, $event)"
                            />
                        </div>
                        <div class="w-2/5">
                            <VInputLabel>ETD</VInputLabel>
                            <VInputTime
                                :model-value="location.etd"
                                :errors="validation.errors.value['etd']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.etd = updateTime(location.etd, $event, 'minutes')"
                            />
                        </div>
                    </div>
                    <div class="mb-4 flex justify-end">
                        <button class="link text-sm" @click="location.etd = undefined">ETD löschen</button>
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Liegeplatz</VInputLabel>
                        <VInputTextArea
                            v-model.trim="location.address"
                            class="h-24"
                            :errors="validation.errors.value['address']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Liegeplatz Link</VInputLabel>
                        <VInputText
                            v-model.trim="location.addressLink"
                            :errors="validation.errors.value['addressLink']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Weitere Informationen</VInputLabel>
                        <VInputTextArea
                            v-model.trim="location.information"
                            class="h-24"
                            :errors="validation.errors.value['information']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Weitere Informationen Link</VInputLabel>
                        <VInputText
                            v-model.trim="location.informationLink"
                            :errors="validation.errors.value['informationLink']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                </section>
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
import { deepCopy, updateDate, updateTime } from '@/common';
import type { Location, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputDate, VInputLabel, VInputSelect, VInputText, VInputTextArea, VInputTime } from '@/ui/components/common';
import { useValidation } from '@/ui/composables/Validation.ts';

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
