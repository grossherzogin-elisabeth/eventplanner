<template>
    <div v-if="props.open" class="fixed bottom-0 left-0 right-0 top-0 z-50 bg-black/75" @click="close()"></div>
    <div class="menu-wrapper text-white" :class="{ open: props.open }" @click="close()">
        <div class="menu" @click.stop>
            <slot />
        </div>
        <button class="icon-button ml-2 mr-6 text-xl text-white">
            <i class="fa-solid fa-close"></i>
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
    @apply rounded-r-3xl;
    @apply shadow-xl;
    @apply bg-primary;
    @apply text-onprimary;
}

@media (min-width: 30rem) {
    .menu-wrapper {
        --menu-width: min(30rem, 100vw);
    }
}
</style>
