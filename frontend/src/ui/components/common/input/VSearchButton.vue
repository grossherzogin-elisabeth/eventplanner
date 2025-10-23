<template>
    <div class="btn-search" :class="`${$attrs.class || 'w-48'} ${modelValue ? 'active' : ''}`" @click="input?.focus()">
        <i class="fa-solid fa-search" />
        <input
            ref="input"
            name="search"
            :value="props.modelValue"
            :placeholder="placeholder ?? 'Einträge filtern'"
            @input="onInput"
            @keydown.esc="input?.blur()"
        />
        <button v-if="props.modelValue !== ''" @click="emit('update:modelValue', '')">
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';

interface Props {
    modelValue?: string;
    placeholder?: string;
}

type Emits = (e: 'update:modelValue', value: string) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const input = ref<HTMLInputElement | null>(null);

function onInput(event: Event): void {
    const element = event.target as HTMLInputElement;
    emit('update:modelValue', element.value);
}
</script>
<style scoped>
.btn-search {
    display: none;
    align-items: center;
    cursor: pointer;
    transition-property: all;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 100ms;
    @apply lg:flex;
    @apply gap-4;
    @apply rounded-xl;
    @apply px-4;
    @apply py-2;
    @apply text-secondary;
}

.btn-search:hover {
    @apply bg-secondary-container;
    @apply text-onsecondary-container;
}

.btn-search.active,
.btn-search:focus-within {
    cursor: text;
    @apply w-64;
    @apply bg-surface-container;
    @apply text-onsurface;
    @apply 2xl:w-80;
}

.btn-search input {
    width: 0;
    flex-grow: 1;
    cursor: pointer;
    background-color: transparent;
    @apply text-secondary;
}

.btn-search input::placeholder {
    @apply text-secondary/100;
}

.btn-search input:focus-within {
    cursor: text;
}

.btn-search input:focus-within::placeholder {
    @apply text-secondary/25;
}
</style>
