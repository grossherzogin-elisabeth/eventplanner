<template>
    <VDialog ref="dlg">
        <template #title>
            <template v-if="position.key">{{ $t('views.basedata.tab.positions.edit') }}</template>
            <template v-else>{{ $t('views.basedata.tab.positions.add-new') }}</template>
        </template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <div v-if="position.key" class="mb-4">
                        <VInputText v-model.trim="position.key" :label="$t('views.basedata.tab.positions.id')" required disabled />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="position.name"
                            :label="$t('views.basedata.tab.positions.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="position.imoListRank"
                            :label="$t('views.basedata.tab.positions.imoListRank')"
                            :errors="validation.errors.value['imoListRank']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="position.color"
                            :label="$t('views.basedata.tab.positions.color')"
                            :errors="validation.errors.value['color']"
                            :errors-visible="validation.showErrors.value"
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
                            :label="$t('views.basedata.tab.positions.prio')"
                            :errors="validation.errors.value['prio']"
                            :errors-visible="validation.showErrors.value"
                            required
                        />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-ghost" name="save" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ $t('generic.save') }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy } from '@/common';
import type { Position } from '@/domain';
import { usePositionService } from '@/domain/services.ts';
import type { Dialog } from '@/ui/components/common';
import { VInputNumber } from '@/ui/components/common';
import { VDialog, VInputText } from '@/ui/components/common';
import { useValidation } from '@/ui/composables/Validation.ts';

const positionService = usePositionService();

const dlg = ref<Dialog<Position | undefined, Position | undefined> | null>(null);
const position = ref<Position>({
    key: '',
    name: '',
    imoListRank: '',
    color: '',
    prio: 0,
});
const validation = useValidation(position, (position) => positionService.validate(position));

async function open(value?: Position): Promise<Position | undefined> {
    validation.reset();
    position.value = value
        ? deepCopy(value)
        : {
              key: '',
              name: '',
              imoListRank: '',
              color: '',
              prio: 0,
          };
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
