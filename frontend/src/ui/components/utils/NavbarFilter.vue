<template>
    <button class="flex h-full items-center" @click="openSearch()">
        <i class="fa-solid fa-search"></i>
    </button>
    <div
        class="absolute right-0 top-0 z-10 flex h-full items-center bg-primary px-8 transition-all md:px-16 xl:px-20"
        :class="showSearch ? 'left-0 md:left-auto' : 'left-full lg:left-auto'"
    >
        <div
            class="-mx-4 flex flex-grow items-center space-x-8 rounded-full bg-p-500 px-4 py-1.5 focus-within:bg-p-500"
        >
            <i class="fa-solid fa-magnifying-glass text-white" />
            <input
                ref="input"
                :value="props.modelValue"
                class="flex-grow bg-transparent placeholder-white placeholder-opacity-50"
                :placeholder="placeholder || 'Einträge filtern'"
                @input="onInput($event)"
            />
            <button class="lg:hidden" @click="cancel()">
                <i class="fa-solid fa-xmark text-white" />
            </button>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { wait } from '@/common';

interface Props {
    modelValue?: string;
    placeholder?: string;
}

type Emits = (e: 'update:modelValue', value: string) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const showSearch = ref<boolean>(false);
const input = ref<HTMLInputElement | null>(null);

async function openSearch(): Promise<void> {
    showSearch.value = true;
    await wait(200);
    input.value?.focus();
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onInput(event: any): void {
    emit('update:modelValue', event.target?.value);
}

function cancel(): void {
    if (props.modelValue) {
        emit('update:modelValue', '');
    } else {
        showSearch.value = false;
    }
}
</script>
