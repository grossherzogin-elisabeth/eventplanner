<template>
    <div class="group cursor-pointer" @click="edit()">
        <div class="relative flex items-start space-x-6">
            <i class="fa-solid mt-2 w-4 text-onsurface-variant sm:w-8 sm:text-xl" :class="props.icon" />
            <div class="w-0 flex-grow">
                <h4 class="mb-1 text-xs font-bold text-onsurface/50">{{ props.label }}</h4>
                <slot name="default">
                    {{ props.content }}
                </slot>
            </div>
            <button class="icon-button">
                <i class="fa-solid fa-pen hidden group-hover:inline" />
            </button>
        </div>
        <VDialog ref="editSheet">
            <template #title> {{ props.label }} </template>
            <template #content>
                <div class="px-4 py-4 xs:px-8 lg:px-10">
                    <slot name="edit" :value="mutableCopy" :errors="validation.errors.value" />
                </div>
            </template>
            <template #buttons>
                <div class="flex justify-end gap-2">
                    <button class="btn-ghost" name="save" @click="cancel()">
                        <span>{{ $t('generic.cancel') }}</span>
                    </button>
                    <AsyncButton class="btn-ghost" :disabled="validation.disableSubmit.value" :action="submit">
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
import { ref } from 'vue';
import { deepCopy, wait } from '@/common';
import { AsyncButton, type Sheet, VDialog } from '@/ui/components/common';
import { useValidation } from '@/ui/composables/Validation';

interface Props {
    modelValue: T;
    icon: string;
    label: string;
    content?: string;
    validate?: (t: T) => Record<string, string[]>;
}

type Emits = (e: 'update:modelValue', updated: T) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const mutableCopy = ref<T>(deepCopy(props.modelValue)) as Ref<T>;
const editSheet = ref<Sheet<T, T | undefined> | null>(null);

const validation = useValidation(mutableCopy, props.validate ?? defaultValidation);
validation.showErrors.value = true;

function defaultValidation(): Record<string, string[]> {
    return {};
}

function edit(): void {
    mutableCopy.value = deepCopy(props.modelValue);
    validation.reset();
    editSheet.value?.open(mutableCopy.value).catch(() => {
        // ignore
    });
}

function cancel(): void {
    mutableCopy.value = deepCopy(props.modelValue);
    editSheet.value?.close();
}

async function submit(): Promise<void> {
    emit('update:modelValue', mutableCopy.value);
    // TODO replace fake delay with actual request await
    await wait(1000);
    editSheet.value?.close();
}
</script>
