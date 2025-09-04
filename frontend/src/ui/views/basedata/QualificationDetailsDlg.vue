<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 v-if="!qualification.key">{{ $t('views.basedata.tab.qualifications.add-new') }}</h1>
            <h1 v-else>{{ $t('views.basedata.tab.qualifications.edit') }}</h1>
        </template>
        <template #default>
            <div class="px-4 pt-4 xs:px-8 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputLabel>{{ $t('views.basedata.tab.qualifications.name') }}</VInputLabel>
                        <VInputText
                            v-model="qualification.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>{{ $t('views.basedata.tab.qualifications.icon') }}</VInputLabel>
                        <VInputText
                            v-model="qualification.icon"
                            :placeholder="$t('views.basedata.tab.qualifications.icon-placeholder')"
                            :errors="validation.errors.value['icon']"
                            :errors-visible="validation.showErrors.value"
                            required
                        >
                            <template #after>
                                <span :key="qualification.icon">
                                    <i class="fa-solid mr-4" :class="qualification.icon"></i>
                                </span>
                            </template>
                        </VInputText>
                    </div>
                    <div class="mb-4">
                        <VInputLabel>{{ $t('views.basedata.tab.qualifications.description') }}</VInputLabel>
                        <VInputTextArea
                            v-model="qualification.description"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                </section>
                <div class="mb-4">
                    <VInputCheckBox v-model="qualification.expires" :label="$t('domain.qualifications.expires')" />
                </div>
                <div class="mt-8 rounded-xl bg-surface-container-low p-4 pr-8 text-sm xs:-mx-4">
                    <h2 class="mb-4 text-xs font-bold">{{ $t('views.basedata.tab.positions.title') }}</h2>
                    <div class="grid gap-x-8 gap-y-2 sm:grid-cols-2">
                        <div v-for="position in positions.all.value" :key="position.key">
                            <VInputCheckBox
                                :model-value="qualification.grantsPositions?.includes(position.key)"
                                :label="position.name"
                                @update:model-value="togglePosition(position.key, $event)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ $t('generic.save') }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy } from '@/common';
import type { PositionKey, Qualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VDialog, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useQualificationService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

const qualificationService = useQualificationService();
const positions = usePositions();

const dlg = ref<Dialog<Qualification | undefined, Qualification | undefined> | null>(null);
const qualification = ref<Qualification>({
    key: '',
    icon: 'fa-id-card',
    expires: false,
    name: '',
    description: '',
    grantsPositions: [],
});
const validation = useValidation(qualification, (qualification) => qualificationService.validate(qualification));

async function open(value?: Qualification): Promise<Qualification | undefined> {
    validation.reset();
    qualification.value = value
        ? deepCopy(value)
        : {
              key: '',
              icon: 'fa-id-card',
              expires: false,
              name: '',
              description: '',
              grantsPositions: [],
          };
    // wait until user submits
    return await dlg.value?.open().catch(() => undefined);
}

function togglePosition(position: PositionKey, enabled: boolean): void {
    if (!enabled) {
        qualification.value.grantsPositions = qualification.value.grantsPositions.filter((it) => it !== position);
    } else if (!qualification.value.grantsPositions.includes(position)) {
        qualification.value.grantsPositions.push(position);
    }
}

function submit(): void {
    if (validation.isValid.value) {
        dlg.value?.submit(qualification.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Qualification | undefined, Qualification | undefined>>({
    open: (value?: Qualification) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result?: Qualification) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
