<template>
    <button class="btn-toggle" :class="{ active: modelValue }" @click="modelValue = !modelValue">
        <span>{{ props.label }}</span>
        <button v-if="modelValue" class="text-xs opacity-75 transition-colors duration-1000 hover:opacity-100">
            <i class="fa-solid fa-close" />
        </button>
    </button>
</template>
<script setup lang="ts">
interface Props {
    label: string;
}

const props = defineProps<Props>();
const modelValue = defineModel<boolean>({ required: true, default: false });
</script>
