<template>
    <div class="group cursor-pointer hover:bg-surface-container" @click="edit()">
        <div class="relative flex items-start space-x-6">
            <i class="fa-solid mt-2 w-4 text-onsurface-variant sm:w-8 sm:text-xl" :class="props.icon" />
            <div class="w-0 flex-grow">
                <h4 class="mb-1 text-xs font-bold text-onsurface text-opacity-50">{{ props.label }}</h4>
                <slot name="default">
                    {{ props.content }}
                </slot>
            </div>
            <button class="icon-button">
                <i class="fa-solid fa-pen hidden group-hover:inline" />
            </button>
        </div>
        <VSheet ref="editSheet">
            <template #title> {{ props.label }} </template>
            <template #content>
                <div class="min-h-[10rem] px-4 pb-4 xs:px-8 sm:w-[30rem] lg:px-10">
                    <slot name="edit" :value="mutableCopy" />
                </div>
            </template>
            <template #bottom>
                <div class="lg:px-10-lg flex justify-end gap-2 px-4 py-4 xs:px-8">
                    <button class="btn-ghost" name="save" @click="cancel()">
                        <span>Abbrechen</span>
                    </button>
                    <button class="btn-primary" @click="submit()">
                        <i class="fa-solid fa-save"></i>
                        <span>Speichern</span>
                    </button>
                </div>
            </template>
        </VSheet>
    </div>
</template>
<script lang="ts" setup generic="T">
import { ref } from 'vue';
import { deepCopy } from '@/common';
import { type Sheet, VSheet } from '@/ui/components/common';

interface Props {
    modelValue: T;
    icon: string;
    label: string;
    content?: string;
}

type Emits = (e: 'update:modelValue', updated: T) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const mutableCopy = ref<T>(deepCopy(props.modelValue));
const editSheet = ref<Sheet<T, T | undefined> | null>(null);

function edit(): void {
    editSheet.value?.open(mutableCopy.value).catch(() => {
        // ignore
    });
}

function cancel(): void {
    mutableCopy.value = deepCopy(props.modelValue);
    editSheet.value?.close();
}

function submit(): void {
    emit('update:modelValue', mutableCopy.value);
    editSheet.value?.close();
}
</script>
