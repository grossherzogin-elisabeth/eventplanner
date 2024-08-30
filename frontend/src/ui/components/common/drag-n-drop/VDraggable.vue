<template>
    <component
        :is="component || 'div'"
        :draggable="props.value !== undefined"
        :class="{ 'opacity-10': dragging }"
        @dragend="handleDragEnd()"
        @dragstart="handleDragStart($event)"
    >
        <slot></slot>
    </component>
</template>

<script lang="ts" setup generic="T extends object">
import { ref } from 'vue';

interface Props {
    value?: T;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    component?: any;
}

interface Emits {
    (e: 'dragstart', t: T): void;

    (e: 'dragend', t: T): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const dragging = ref<boolean>(false);

function handleDragStart(dragEvent: DragEvent) {
    if (dragEvent.dataTransfer && props.value) {
        dragging.value = true;
        emit('dragstart', props.value);
        dragEvent.dataTransfer.setData('application/json', JSON.stringify(props.value));
        dragEvent.dataTransfer.dropEffect = 'link';
    } else {
        dragEvent.preventDefault();
    }
}

function handleDragEnd() {
    dragging.value = false;
    if (props.value) {
        emit('dragend', props.value);
    }
}
</script>
