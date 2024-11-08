<template>
    <div
        class="flex items-center overflow-hidden rounded-xl bg-blue-100 text-blue-900"
        :class="{ hidden: dismissed }"
        @click="clampActive = !clampActive"
    >
        <i class="fa-solid fa-info-circle my-3 ml-4 self-start text-xl"></i>
        <span class="mx-4 my-2 w-0 flex-grow text-sm" :class="{ 'line-clamp-3': clamp && clampActive }">
            <slot></slot>
        </span>
        <button
            v-if="props.dismissable !== false"
            class="m-1 h-9 w-9 self-start rounded-full hover:bg-blue-200 hover:bg-opacity-75"
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
    clamp?: boolean;
}

type Emits = (e: 'dismiss') => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */
const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const clampActive = ref<boolean>(true);
const dismissed = ref<boolean>(false);

function dismiss(): void {
    dismissed.value = true;
    emit('dismiss');
}
</script>
