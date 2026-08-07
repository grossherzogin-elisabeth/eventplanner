<template>
    <button ref="button" v-bind="$attrs" class="cursor-pointer" @click.stop="open = true">
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
        <div class="context-menu" @click="open = false">
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
