<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 v-if="location.order === 1">{{ $t('views.events.edit.actions.edit-start-location') }}</h1>
            <h1 v-else-if="location.order === -1">{{ $t('views.events.edit.actions.add-location') }}</h1>
            <h1 v-else>{{ $t('views.events.edit.actions.edit-location') }}</h1>
        </template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputText
                            v-model.trim="location.name"
                            :label="$t('domain.location.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="location.icon"
                            :label="$t('domain.location.icon')"
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
                                <span class="grow">{{ item.label }}</span>
                            </template>
                            <template #before>
                                <span :key="location.icon" class="pt-5">
                                    <i class="fa-solid" :class="location.icon"></i>
                                </span>
                            </template>
                        </VInputSelect>
                    </div>
                    <div class="mb-2 flex space-x-4">
                        <div class="w-3/5">
                            <VInputDate
                                :label="$t('domain.location.eda')"
                                :model-value="location.eta"
                                :errors="validation.errors.value['start']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.eta = updateDate(location.eta, $event)"
                            />
                        </div>
                        <div class="w-2/5">
                            <VInputTime
                                :label="$t('domain.location.eta')"
                                :model-value="location.eta"
                                :errors="validation.errors.value['start']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.eta = updateTime(location.eta, $event, 'minutes')"
                            />
                        </div>
                    </div>
                    <div class="mb-4 flex justify-end">
                        <button class="link text-sm" @click="location.eta = undefined">
                            {{ $t('views.events.edit.actions.delete-eta') }}
                        </button>
                    </div>
                    <div class="mb-2 flex space-x-4">
                        <div class="w-3/5">
                            <VInputDate
                                :label="$t('domain.location.edd')"
                                :model-value="location.etd"
                                :errors="validation.errors.value['etd']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.etd = updateDate(location.etd, $event)"
                            />
                        </div>
                        <div class="w-2/5">
                            <VInputTime
                                :label="$t('domain.location.etd')"
                                :model-value="location.etd"
                                :errors="validation.errors.value['etd']"
                                :errors-visible="validation.showErrors.value"
                                @update:model-value="location.etd = updateTime(location.etd, $event, 'minutes')"
                            />
                        </div>
                    </div>
                    <div class="mb-4 flex justify-end">
                        <button class="link text-sm" @click="location.etd = undefined">
                            {{ $t('views.events.edit.actions.delete-etd') }}
                        </button>
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model.trim="location.address"
                            :label="$t('domain.location.address')"
                            class="h-24"
                            :errors="validation.errors.value['address']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model.trim="location.addressLink"
                            :label="$t('domain.location.address-link')"
                            :errors="validation.errors.value['addressLink']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model.trim="location.information"
                            :label="$t('domain.location.information')"
                            class="h-24"
                            :errors="validation.errors.value['information']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model.trim="location.informationLink"
                            :label="$t('domain.location.information-link')"
                            :errors="validation.errors.value['informationLink']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-ghost" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ $t('generic.apply') }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy, updateDate, updateTime } from '@/common';
import { Validator, notEmpty } from '@/common/validation';
import type { Location } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputDate, VInputSelect, VInputText, VInputTextArea, VInputTime } from '@/ui/components/common';
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
    return Validator.validate('name', value.name, notEmpty()).validate('icon', value.icon, notEmpty()).getErrors();
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
