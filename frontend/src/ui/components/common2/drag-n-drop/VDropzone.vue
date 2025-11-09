<template>
    <div
        class="dropzone"
        :class="{ hover: isHovered }"
        @dragover="handleDragOver($event)"
        @dragleave="handleDragLeave()"
        @drop="handleDragStop($event)"
    >
        <slot></slot>
    </div>
</template>

<script lang="ts" setup generic="T extends object">
import { ref } from 'vue';

type Emits = (e: 'drop', t: T) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */
const emit = defineEmits<Emits>();

const isHovered = ref<boolean>(false);

function handleDragOver(dragEvent: DragEvent): void {
    isHovered.value = true;
    if (dragEvent.dataTransfer) {
        dragEvent.preventDefault();
        dragEvent.dataTransfer.dropEffect = 'link';
    }
}

function handleDragLeave(): void {
    isHovered.value = false;
}

function handleDragStop(dragEvent: DragEvent): void {
    if (dragEvent.dataTransfer) {
        dragEvent.preventDefault();

        const payload: T = JSON.parse(dragEvent.dataTransfer.getData('application/json'));
        if (payload) {
            emit('drop', payload);
        }
    }
}
</script>

<style>
.dropzone {
    padding: 0;
    transition-property: all;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 200ms;
    --anim-distance: 1rem;
    animation: anim-dropzone 0.5s;
}

.dropzone * {
    --anim-distance: 1rem;
    animation: anim-dropzone 0.5s;
}

.dropzone.hover {
    padding: 0.25rem;
}

@keyframes anim-dropzone {
    0% {
        transform: translateY(var(--anim-distance));
    }
    100% {
        transform: translateY(0);
    }
}
</style>
