<template>
    <div ref="element" @mouseenter="onMouseEnter" @mouseleave="onMouseLeave" @mousemove="onMouseEnter">
        <slot />
    </div>
    <VDropdownWrapper
        v-if="showTooltip"
        :anchor="element"
        class="pointer-events-none pt-4"
        min-width="0rem"
        max-width="min(26rem, 90vw)"
        anchor-align-x="center"
        anchor-align-y="center"
        dropdown-position-x="center"
        dropdown-position-y="bottom"
        :prefer-anchor-width="false"
        background-class="pointer-events-none"
    >
        <template #default>
            <slot name="tooltip">
                <div
                    class="bg-surface-container-high text-onsurface pointer-events-none rounded-xl p-2 text-sm shadow-xl"
                    :class="$attrs.class"
                >
                    <slot name="text" />
                </div>
            </slot>
        </template>
    </VDropdownWrapper>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { isTouchDevice } from '@/common';
import { VDropdownWrapper } from '@/ui/components/common';

const showTooltip = ref<boolean>(false);
const element = ref<HTMLElement | null>(null);
let timeout: ReturnType<typeof setTimeout> | undefined = undefined;

interface Props {
    disabled?: boolean;
    delay?: number;
}

const props = defineProps<Props>();

function onMouseEnter(): void {
    if (isTouchDevice()) {
        return;
    }
    if (timeout) {
        clearTimeout(timeout);
    }
    if (!props.disabled) {
        timeout = setTimeout(() => (showTooltip.value = true), props.delay || 1000);
    }
}

function onMouseLeave(): void {
    if (isTouchDevice()) {
        return;
    }
    if (timeout) {
        clearTimeout(timeout);
    }
    timeout = setTimeout(() => (showTooltip.value = false), 100);
}
</script>
