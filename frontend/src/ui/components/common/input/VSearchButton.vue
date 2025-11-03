<template>
    <div class="btn-search" :class="modelValue ? 'active' : ''" @click="input?.focus()">
        <i class="fa-solid fa-search" />
        <div class="relative">
            <input
                ref="input"
                :placeholder="placeholder ?? 'Einträge filtern'"
                class="absolute"
                name="search"
                :value="props.modelValue"
                @input="onInput"
                @keydown.esc="input?.blur()"
            />
            <div class="placeholder pointer-events-none opacity-0">{{ placeholder ?? 'Einträge filtern' }}</div>
            <button v-if="props.modelValue !== ''" class="absolute top-0 right-0 bottom-0" @click="emit('update:modelValue', '')">
                <i class="fa-solid fa-xmark"></i>
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
@reference "tailwindcss";

.btn-search {
    display: none;
    align-items: center;
    cursor: pointer;
    @apply lg:flex;
    @apply gap-4;
    border-radius: var(--radius-xl);
    @apply px-4;
    @apply py-2;
    color: var(--color-primary);
}

.btn-search .placeholder {
    min-width: 0;
    transition-property: min-width;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 100ms;
}

.btn-search:hover {
    background-color: --alpha(var(--color-primary) / 10%);
    color: var(--color-primary);
}

.btn-search:hover input::placeholder {
    color: var(--color-primary);
}

.btn-search.active,
.btn-search:focus-within {
    cursor: text;
    background-color: var(--color-surface-container);
    color: var(--color-onsurface);
}

.btn-search.active .placeholder,
.btn-search:focus-within .placeholder {
    @apply min-w-64;
}

.btn-search input {
    display: inline-block;
    width: auto;
    flex-grow: 1;
    cursor: pointer;
    background-color: transparent;
    color: var(--color-secondary);
}

.btn-search input::placeholder {
    color: var(--color-secondary);
}

.btn-search input:focus-within {
    cursor: text;
}

.btn-search input:focus-within::placeholder {
    color: --alpha(var(--color-secondary) / 25%);
}
</style>
