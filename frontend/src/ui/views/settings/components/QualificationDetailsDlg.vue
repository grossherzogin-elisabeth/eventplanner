<template>
    <VDialog ref="dlg" data-test-id="qualification-details-dialog">
        <template #title>
            <template v-if="mode === Mode.CREATE">{{ $t('views.settings.qualifications.add-new') }}</template>
            <template v-else>{{ $t('views.settings.qualifications.edit') }}</template>
        </template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 pb-8 lg:w-xl lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputText
                            v-model="qualification.key"
                            data-test-id="input-key"
                            :label="$t('views.settings.qualifications.key')"
                            :errors="validation.errors.value['key']"
                            :errors-visible="validation.showErrors.value"
                            required
                            :disabled="mode === Mode.EDIT"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="qualification.name"
                            data-test-id="input-name"
                            :label="$t('views.settings.qualifications.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="qualification.icon"
                            data-test-id="input-icon"
                            :label="$t('views.settings.qualifications.icon')"
                            :placeholder="$t('views.settings.qualifications.icon-placeholder')"
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
                        <VInputTextArea
                            v-model="qualification.description"
                            data-test-id="input-description"
                            :label="$t('views.settings.qualifications.description')"
                            :errors="validation.errors.value['description']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                </section>
                <div class="mb-4">
                    <VInputCheckBox v-model="qualification.expires" :label="$t('views.settings.qualifications.expires')" />
                </div>
                <div data-test-id="input-positions" class="bg-surface-container-highest xs:-mx-4 mt-8 rounded-xl p-4 pr-8 text-sm">
                    <h2 class="mb-4 text-xs font-bold">{{ $t('views.settings.qualifications.positions') }}</h2>
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
            <button class="btn-ghost" data-test-id="button-cancel" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-ghost" data-test-id="button-submit" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ $t('generic.save') }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { deepCopy } from '@/common';
import type { PositionKey, Qualification } from '@/domain';
import { useQualificationService } from '@/domain/services';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCheckBox, VInputText, VInputTextArea } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions';
import { useQualifications } from '@/ui/composables/Qualifications';
import { useValidation } from '@/ui/composables/Validation';

enum Mode {
    CREATE = 'CREATE',
    EDIT = 'EDIT',
}

const qualificationService = useQualificationService();
const positions = usePositions();

const dlg = ref<Dialog<Qualification | undefined, Qualification | undefined> | null>(null);
const mode = ref<Mode>(Mode.CREATE);
const qualification = ref<Qualification>({ key: '', icon: 'fa-id-card', expires: false, name: '', description: '', grantsPositions: [] });
const qualifications = useQualifications();
const others = computed(() => {
    if (mode.value === Mode.CREATE) return qualifications.all.value;
    return qualifications.all.value.filter((it) => it.key !== qualification.value.key);
});
const validation = useValidation(qualification, (qualification) => qualificationService.validate(qualification, others.value));

async function open(value?: Qualification): Promise<Qualification | undefined> {
    validation.reset();
    if (value) {
        mode.value = Mode.EDIT;
        validation.showErrors.value = true;
        qualification.value = deepCopy(value);
    } else {
        mode.value = Mode.CREATE;
        validation.showErrors.value = false;
        qualification.value = { key: '', icon: 'fa-id-card', expires: false, name: '', description: '', grantsPositions: [] };
    }
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
