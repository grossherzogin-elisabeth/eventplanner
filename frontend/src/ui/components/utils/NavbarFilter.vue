<template>
    <button class="flex h-full items-center" @click="openSearch()">
        <i class="fa-solid fa-search"></i>
    </button>
    <div
        class="absolute right-0 top-0 z-10 flex h-full items-center bg-navbar px-8 transition-all md:px-16 xl:px-20"
        :class="showSearch ? 'left-0 md:left-auto' : 'left-full'"
    >
        <div class="-mx-4 flex flex-grow items-center space-x-8 rounded-full px-4 py-1.5 focus-within:bg-primary-700">
            <i class="fa-solid fa-magnifying-glass text-white" />
            <input
                ref="input"
                :value="props.modelValue"
                class="flex-grow bg-transparent placeholder-white placeholder-opacity-50"
                :placeholder="placeholder || 'Einträge filtern'"
                @input="onInput($event)"
            />
            <button @click="showSearch = false">
                <i class="fa-solid fa-xmark text-white" />
            </button>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';

interface Props {
    modelValue?: string;
    placeholder?: string;
}

interface Emits {
    (e: 'update:modelValue', value: string): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const showSearch = ref<boolean>(false);
const input = ref<HTMLInputElement | null>(null);

function openSearch() {
    showSearch.value = true;
    input.value?.focus();
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onInput(event: any) {
    emit('update:modelValue', event.target?.value);
}
</script>
