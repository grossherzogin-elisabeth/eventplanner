<template>
    <div class="flex items-baseline rounded-xl bg-yellow-100 p-2 pl-4 text-yellow-900" :class="{ hidden: dismissed }">
        <i class="fa-solid fa-warning"></i>
        <span class="ml-4 mr-1 w-0 flex-grow">
            <slot></slot>
        </span>
        <button
            v-if="props.dismissable === true"
            class="h-9 w-9 rounded-full hover:bg-yellow-200 hover:bg-opacity-75"
            @click="dismiss()"
        >
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';

interface Props {
    dismissable?: boolean;
}

type Emits = (e: 'dismiss') => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */
const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const dismissed = ref<boolean>(false);

function dismiss(): void {
    dismissed.value = true;
    emit('dismiss');
}
</script>
