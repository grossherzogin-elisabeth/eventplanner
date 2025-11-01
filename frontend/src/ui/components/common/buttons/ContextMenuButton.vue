<template>
    <button ref="button" :class="$attrs.class" class="cursor-pointer" @click.stop="open = true">
        <slot name="icon">
            <i class="fa-solid fa-ellipsis-vertical mx-1" />
        </slot>
    </button>
    <VDropdownWrapper
        v-if="open"
        :anchor="$refs.button as HTMLElement"
        :anchor-align-x="props.anchorAlignX || 'right'"
        :anchor-align-y="props.anchorAlignY || 'top'"
        :dropdown-position-x="props.dropdownPositionX || 'left'"
        :dropdown-position-y="props.dropdownPositionY || 'bottom'"
        :min-width="props.minWidth || '20rem'"
        :max-width="props.maxWidth || '20rem'"
        :max-height="props.maxHeight"
        @close="open = false"
    >
        <div class="rounded-xl bg-surface-container-high p-4 shadow-xl" @click="open = false">
            <slot />
        </div>
    </VDropdownWrapper>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import VDropdownWrapper from '@/ui/components/common/dropdown/VDropdownWrapper.vue';

interface Props {
    // html element this dropdown is relative to
    anchorAlignY?: 'top' | 'center' | 'bottom';
    anchorAlignX?: 'left' | 'center' | 'right';
    dropdownPositionX?: 'left' | 'center' | 'right';
    dropdownPositionY?: 'top' | 'center' | 'bottom';
    // max width of the dropdown, use a css value like `50px`
    maxWidth?: string;
    // min width of the dropdown, use a css value like `50px`
    minWidth?: string;
    // max height of the dropdown, use a css value like `50px`
    maxHeight?: string;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();

const open = ref<boolean>(false);
</script>

<style>
.context-menu-item {
    display: flex;
    align-items: center;
    cursor: pointer;
    @apply -mx-4;
    @apply px-4;
    @apply space-x-4;
    @apply py-3;
    @apply -mx-3;
    @apply rounded-lg;

    @apply md:py-2;
}

.context-menu-item:hover {
    @apply bg-surface-container-highest;
    text-decoration-line: none;
}

.context-menu-item.disabled {
    pointer-events: none;
    opacity: 0.5;
}

.context-menu-item > i,
.context-menu-item > svg:first-child {
    display: block;
    @apply w-6;
}
</style>
