<template>
    <VDialog ref="dlg" data-test-id="position-details-dialog">
        <template #title>
            <template v-if="mode === Mode.EDIT">{{ $t('views.settings.positions.edit') }}</template>
            <template v-else>{{ $t('views.settings.positions.add-new') }}</template>
        </template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputText
                            v-model.trim="position.key"
                            data-test-id="input-key"
                            :label="$t('views.settings.positions.id')"
                            :errors="validation.errors.value['key']"
                            :errors-visible="validation.showErrors.value"
                            required
                            :disabled="mode === Mode.EDIT || !hasWritePermission"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="position.name"
                            data-test-id="input-name"
                            :label="$t('views.settings.positions.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            :disabled="!hasWritePermission"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="position.imoListRank"
                            data-test-id="input-imo-list-rank"
                            :label="$t('views.settings.positions.imo-list-rank')"
                            :errors="validation.errors.value['imoListRank']"
                            :errors-visible="validation.showErrors.value"
                            :disabled="!hasWritePermission"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="position.color"
                            data-test-id="input-color"
                            :label="$t('views.settings.positions.color')"
                            :errors="validation.errors.value['color']"
                            :errors-visible="validation.showErrors.value"
                            :disabled="!hasWritePermission"
                            required
                        >
                            <template #after>
                                <div class="mr-4 h-8 w-8 rounded-lg" :style="{ background: position.color }"></div>
                            </template>
                        </VInputText>
                    </div>
                    <div class="mb-4">
                        <VInputNumber
                            v-model="position.prio"
                            data-test-id="input-prio"
                            :label="$t('views.settings.positions.prio')"
                            :errors="validation.errors.value['prio']"
                            :errors-visible="validation.showErrors.value"
                            :disabled="!hasWritePermission"
                            required
                        />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <template v-if="hasWritePermission">
                <button class="btn-ghost" data-test-id="button-cancel" @click="cancel">
                    <span>{{ $t('generic.cancel') }}</span>
                </button>
                <button class="btn-ghost" data-test-id="button-submit" :disabled="validation.disableSubmit.value" @click="submit">
                    <span>{{ $t('generic.save') }}</span>
                </button>
            </template>
            <template v-else>
                <button class="btn-ghost" data-test-id="button-submit" :disabled="validation.disableSubmit.value" @click="cancel">
                    <span>{{ $t('generic.close') }}</span>
                </button>
            </template>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useAuthUseCase } from '@/application';
import { deepCopy } from '@/common';
import type { Position } from '@/domain';
import { Permission } from '@/domain';
import { usePositionService } from '@/domain/services';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputNumber, VInputText } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions';
import { useValidation } from '@/ui/composables/Validation';

enum Mode {
    CREATE = 'CREATE',
    EDIT = 'EDIT',
}

const authUseCase = useAuthUseCase();
const positionService = usePositionService();

const dlg = ref<Dialog<Position | undefined, Position | undefined> | null>(null);
const mode = ref<Mode>(Mode.CREATE);
const position = ref<Position>({ key: '', name: '', imoListRank: '', color: '', prio: 0 });
const positions = usePositions();
const hasWritePermission = computed<boolean>(() => authUseCase.getSignedInUser().permissions.includes(Permission.WRITE_POSITIONS));
const others = computed(() => {
    if (mode.value === Mode.CREATE) return positions.all.value;
    return positions.all.value.filter((it) => it.key !== position.value.key);
});
const validation = useValidation(position, (position) => positionService.validate(position, others.value));

async function open(value?: Position): Promise<Position | undefined> {
    validation.reset();
    if (value) {
        mode.value = Mode.EDIT;
        validation.showErrors.value = true;
        position.value = deepCopy(value);
    } else {
        mode.value = Mode.CREATE;
        validation.showErrors.value = false;
        position.value = { key: '', name: '', imoListRank: '', color: '', prio: 0 };
    }
    return await dlg.value?.open().catch(() => undefined);
}

function submit(): void {
    if (validation.isValid.value) {
        dlg.value?.submit(position.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Position | undefined, Position | undefined>>({
    open: (value?: Position) => open(value),
    close: () => dlg.value?.reject(),
    submit: (result?: Position) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});
</script>
