<template>
    <div
        class="flex items-center overflow-hidden rounded-xl bg-yellow-container text-onyellow-container"
        :class="{ hidden: dismissed }"
        @click="clamp = !clamp"
    >
        <i class="fa-solid fa-warning my-5 ml-4 self-start text-xl"></i>
        <span class="my-4 ml-4 mr-1 w-0 flex-grow text-sm" :class="{ 'line-clamp-3': clamp }">
            <slot></slot>
        </span>
        <button v-if="props.dismissable !== false" class="icon-button my-1 mr-1 self-start" @click="dismiss()">
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

const clamp = ref<boolean>(true);
const dismissed = ref<boolean>(false);

function dismiss(): void {
    dismissed.value = true;
    emit('dismiss');
}
</script>
