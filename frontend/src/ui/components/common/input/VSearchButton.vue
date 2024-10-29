<template>
    <div
        class="hidden w-44 cursor-pointer items-center gap-4 rounded-lg px-4 py-2 text-primary-700 transition-all duration-100 focus-within:w-64 focus-within:cursor-text focus-within:bg-primary-100 focus-within:text-primary-300 hover:bg-primary-100 lg:flex xxl:focus-within:w-80"
        @click="$refs.input.focus()"
    >
        <i class="fa-solid fa-search text-inherit" />
        <input
            ref="input"
            :value="props.modelValue"
            class="w-0 flex-grow cursor-pointer bg-transparent text-primary-700 placeholder-primary-700 focus-within:cursor-text focus-within:placeholder-primary-300"
            :placeholder="placeholder"
            @input="onInput"
        />
        <button v-if="props.modelValue !== ''" @click="emit('update:modelValue', '')">
            <i class="fa-solid fa-xmark text-primary-700"></i>
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

function onInput(event: Event) {
    const element = event.target as HTMLInputElement;
    emit('update:modelValue', element.value);
}
</script>
