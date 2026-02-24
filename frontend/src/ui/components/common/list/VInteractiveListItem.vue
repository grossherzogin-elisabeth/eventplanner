<template>
    <div class="group" :class="props.disabled ? '' : 'cursor-pointer'" @click="edit()">
        <div class="relative flex items-start space-x-6">
            <i class="fa-solid text-onsurface-variant mt-2 w-4 sm:w-8 sm:text-xl" :class="props.icon" />
            <div class="w-0 grow">
                <h4 class="text-onsurface/50 mb-1 text-xs font-bold">{{ props.label }}</h4>
                <slot name="default">
                    <p class="line-clamp-3">{{ props.content }}</p>
                </slot>
            </div>
            <button v-if="!props.disabled" class="btn-icon hidden group-hover:inline">
                <i class="fa-solid fa-pen" />
            </button>
        </div>
        <VDialog v-if="!props.disabled" ref="editSheet" :type="props.dialogType ?? 'fullscreen'">
            <template #title> {{ props.label }} </template>
            <template #content>
                <div class="xs:px-8 px-4 py-4 lg:px-10">
                    <slot name="edit" :value="mutableCopy" :errors="validation.errors.value" />
                </div>
            </template>
            <template #buttons>
                <div v-if="props.direct" class="flex justify-end gap-2">
                    <button class="btn-ghost" name="save" data-test-id="button-cancel" @click="submit()">
                        <span>{{ $t('generic.close') }}</span>
                    </button>
                </div>
                <div v-else class="flex justify-end gap-2">
                    <button class="btn-ghost" name="save" data-test-id="button-cancel" @click="cancel()">
                        <span>{{ $t('generic.cancel') }}</span>
                    </button>
                    <AsyncButton class="btn-ghost" :disabled="validation.disableSubmit.value" :action="submit" data-test-id="button-submit">
                        <template #label>
                            <span>{{ $t('generic.save') }}</span>
                        </template>
                    </AsyncButton>
                </div>
            </template>
        </VDialog>
    </div>
</template>
<script lang="ts" setup generic="T">
import type { Ref } from 'vue';
import { ref, watch } from 'vue';
import { deepCopy, wait } from '@/common';
import { AsyncButton, type Sheet, VDialog } from '@/ui/components/common';
import { useValidation } from '@/ui/composables/Validation';

interface Props {
    modelValue: T;
    icon: string;
    label: string;
    content?: string;
    disabled?: boolean;
    direct?: boolean;
    dialogType?: 'fullscreen' | 'modal';
    validate?: (t: T) => Record<string, string[]>;
}

type Emits = (e: 'update:modelValue', updated: T) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const mutableCopy = ref<T>(deepCopy(props.modelValue)) as Ref<T>;
const editSheet = ref<Sheet<T, T | undefined> | null>(null);

const validation = useValidation(mutableCopy, props.validate ?? defaultValidation);
validation.showErrors.value = true;

function init(): void {
    if (props.direct) {
        watch(
            () => mutableCopy.value,
            () => update(),
            { deep: true }
        );
    }
}

function defaultValidation(): Record<string, string[]> {
    return {};
}

function edit(): void {
    if (!props.disabled) {
        mutableCopy.value = deepCopy(props.modelValue);
        validation.reset();
        editSheet.value?.open(mutableCopy.value).catch(() => {
            // ignore
        });
    }
}

function cancel(): void {
    mutableCopy.value = deepCopy(props.modelValue);
    editSheet.value?.close();
}

async function submit(): Promise<void> {
    emit('update:modelValue', mutableCopy.value);
    if (!props.direct) {
        // TODO replace fake delay with actual request await
        await wait(1000);
    }
    editSheet.value?.close();
}

function update(): void {
    emit('update:modelValue', mutableCopy.value);
}

init();
</script>
