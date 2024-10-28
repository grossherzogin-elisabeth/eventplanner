<template>
    <div class="flex items-center rounded-xl bg-blue-200 p-2 pl-4 text-blue-900" :class="{ hidden: dismissed }">
        <i class="fa-solid fa-info-circle"></i>
        <span class="ml-4 mr-1 w-0 flex-grow">
            <slot></slot>
        </span>
        <button class="h-9 w-9 self-baseline rounded-full hover:bg-yellow-200 hover:bg-opacity-75" @click="dismiss()">
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';

type Emits = (e: 'dismiss') => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */
const emit = defineEmits<Emits>();

const dismissed = ref<boolean>(false);

function dismiss(): void {
    dismissed.value = true;
    emit('dismiss');
}
</script>
