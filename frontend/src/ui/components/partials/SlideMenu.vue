<template>
    <div v-if="props.open" class="bg-scrim/50 fixed top-0 right-0 bottom-0 left-0 z-50" @click="close()"></div>
    <div class="menu-wrapper" :class="{ open: props.open }" @click="close()">
        <div class="menu" @click.stop>
            <slot />
        </div>
        <button class="btn-icon mr-6 ml-2 text-xl">
            <i class="fa-solid fa-close text-white"></i>
        </button>
    </div>
</template>
<script setup lang="ts">
import { watch } from 'vue';
import { useRoute } from 'vue-router';
import { disableScrolling, enableScrolling } from '@/common';

interface Props {
    open: boolean;
}

type Emits = (e: 'update:open', value: boolean) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const route = useRoute();
const props = defineProps<Props>();
const emit = defineEmits<Emits>();

watch(
    () => props.open,
    (open) => {
        if (open) disableScrolling();
        else enableScrolling();
    }
);

watch(
    () => route.fullPath,
    () => {
        close();
    }
);

function close(): void {
    emit('update:open', false);
}
</script>

<style scoped>
@reference "tailwindcss";

.menu-wrapper {
    --duration: 0.25s;
    --menu-width: 100vw;
    position: fixed;
    bottom: 0;
    z-index: 50;
    overflow: hidden;
    opacity: 0;
    display: flex;
    align-items: start;
    top: 0;
    width: var(--menu-width);
    left: calc(var(--menu-width) * -1);
    transition:
        left var(--duration) ease,
        opacity var(--duration) step-end;
}

.menu-wrapper.open {
    left: 0;
    opacity: 1;
    transition: left var(--duration) ease;
}

.menu {
    width: 0;
    flex-grow: 1;
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    border-top-right-radius: var(--radius-3xl);
    border-bottom-right-radius: var(--radius-3xl);
    @apply shadow-xl;
    background-color: var(--color-primary);
    color: var(--color-onprimary);
}

html.dark .menu {
    background-color: var(--color-surface-container);
    color: var(--color-onsurface-variant);
}

@media (min-width: 30rem) {
    .menu-wrapper {
        --menu-width: min(30rem, 100vw);
    }
}
</style>
