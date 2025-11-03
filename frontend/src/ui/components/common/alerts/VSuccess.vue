<template>
    <div class="status-panel success rounded-2xl" :class="{ hidden: dismissed }" @click="clampActive = !clampActive">
        <i class="fa-solid my-5 ml-4 w-4 self-start" :class="props.icon ?? 'fa-info-circle'"></i>
        <span
            class="mx-4 my-4 mr-1 w-0 grow text-sm"
            :class="{
                'line-clamp-3': clamp && clampActive,
                'mr-4': !props.dismissable,
            }"
        >
            <slot></slot>
        </span>
        <button v-if="props.dismissable !== false" class="btn-icon my-1 mr-1 self-start" @click="dismiss()">
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';

interface Props {
    dismissable?: boolean;
    clamp?: boolean;
    icon?: string;
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
