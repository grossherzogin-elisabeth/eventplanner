<template>
    <div
        v-if="props.open"
        class="fixed bottom-0 left-0 right-0 top-0 z-50 bg-black bg-opacity-50"
        @click="close()"
    ></div>
    <div class="menu-wrapper" :class="{ open: props.open }" @click="close()">
        <div class="menu" @click.stop>
            <slot />
        </div>
        <button class="px-6 py-3 text-xl text-white md:px-8">
            <i class="fa-solid fa-close"></i>
        </button>
    </div>
</template>
<script setup lang="ts">
import { watch } from 'vue';
import { useRoute } from 'vue-router';
import { WindowUtils } from '@/common';

interface Props {
    open: boolean;
}

interface Emits {
    (e: 'update:open', value: boolean): void;
}

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
        if (open) WindowUtils.disableScrolling();
        else WindowUtils.enableScrolling();
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
    @apply fixed bottom-0 z-50 overflow-hidden;
    @apply opacity-0;
    @apply flex items-start;
    top: 0;
    width: var(--menu-width);
    left: calc(var(--menu-width) * -1);
    transition:
        left var(--duration) ease,
        opacity var(--duration) step-end;
}

.menu-wrapper.open {
    @apply left-0 opacity-100;
    transition: left var(--duration) ease;
}

.menu {
    @apply flex-grow;
    @apply h-full overflow-hidden rounded-r-xl shadow-xl;
    @apply flex flex-col;
    @apply bg-primary-200 text-primary-950;
}

@media (min-width: 30rem) {
    .menu-wrapper {
        --menu-width: min(30rem, 100vw);
    }
}
</style>
