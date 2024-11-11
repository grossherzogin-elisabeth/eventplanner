<template>
    <div
        class="btn-search"
        :class="`${$attrs.class || 'w-44'} ${modelValue ? 'active' : ''}`"
        @click="$refs.input.focus()"
    >
        <i class="fa-solid fa-search" />
        <input ref="input" :value="props.modelValue" :placeholder="placeholder" @input="onInput" />
        <button v-if="props.modelValue !== ''" @click="emit('update:modelValue', '')">
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>
</template>
<script lang="ts" setup>
interface Props {
    modelValue?: string;
    placeholder?: string;
}

type Emits = (e: 'update:modelValue', value: string) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

function onInput(event: Event): void {
    const element = event.target as HTMLInputElement;
    emit('update:modelValue', element.value);
}
</script>
<style scoped>
.btn-search {
    @apply hidden items-center lg:flex;
    @apply cursor-pointer gap-4 rounded-lg px-4 py-2;
    @apply transition-all duration-100;
    @apply text-secondary;
}

.btn-search:hover {
    @apply bg-secondary-container text-onsecondary-container;
}

.btn-search.active,
.btn-search:focus-within {
    @apply w-64 cursor-text bg-surface-container text-onsurface xxl:w-80;
}

.btn-search input {
    @apply w-0 flex-grow;
    @apply cursor-pointer focus-within:cursor-text;
    @apply bg-transparent text-secondary;
    @apply placeholder-secondary placeholder-opacity-100 focus-within:placeholder-opacity-25;
}
</style>
