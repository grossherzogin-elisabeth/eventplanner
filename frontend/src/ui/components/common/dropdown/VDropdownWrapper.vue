<template>
    <teleport to="body">
        <div
            class="dropdown-wrapper-background"
            :class="blockAllInput ? `pointer-events-none ${props.backgroundClass}` : props.backgroundClass"
            @click.stop="close"
            @keydown.esc="close"
        ></div>
        <div
            ref="dropdown"
            class="dropdown-wrapper"
            :style="dropdownStyle"
            :class="blockAllInput ? `pointer-events-none ${$attrs.class}` : $attrs.class"
            @click.stop="close"
            @mouseup="close"
        >
            <slot></slot>
        </div>
    </teleport>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue';

interface Props {
    // html element this dropdown is relative to
    anchor?: HTMLElement | null;
    anchorAlignY?: 'top' | 'center' | 'bottom';
    anchorAlignX?: 'left' | 'center' | 'right';
    dropdownPositionX?: 'left' | 'center' | 'right';
    dropdownPositionY?: 'top' | 'center' | 'bottom';

    // x coordinate
    x?: number;
    // y coordinate
    y?: number;
    // max width of the dropdown, use a css value like `50px`
    maxWidth?: string;
    // min width of the dropdown, use a css value like `50px`
    minWidth?: string;
    // use the width of the anchor element as preferred width
    preferAnchorWidth?: boolean;
    // max height of the dropdown, use a css value like `50px`
    maxHeight?: string;
    backgroundClass?: string;
}

// dropdown is closed
type Emits = (e: 'close') => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const dropdown = ref<HTMLElement | null>(null);
const dropdownStyle = ref({
    'top': '0px',
    'left': '0px',
    'width': '10rem',
    'minWidth': '10rem',
    'maxWidth': '100vw',
    'maxHeight': 'min(10rem, var(--viewport-height))',
    '--max-height': 'unset',
});
const resizeObserver = new ResizeObserver(() => moveIntoVisibleArea());
// block all input events from having any effect until the open animation is finished
const blockAllInput = ref<boolean>(true);

async function updatePosition(): Promise<void> {
    if (props.anchor) {
        updatePositionToAnchor();
    } else if (props.x !== undefined && props.y !== undefined) {
        dropdownStyle.value = {
            'top': `${props.y}px`,
            'left': `${props.x}px`,
            'width': `min(${props.minWidth || '10rem'}, ${props.maxWidth || '100vw'})`,
            'minWidth': props.minWidth || '10rem',
            'maxWidth': props.maxWidth || '100vw',
            'maxHeight': props.maxHeight || 'var(--viewport-height)',
            '--max-height': props.maxHeight || 'unset',
        };
    }
    await nextTick();
    applyDropdownAlignment();

    // render dropdown and then move it up, so everything is visible
    await nextTick();
    moveIntoVisibleArea();
    await nextTick();
}

function updatePositionToAnchor(): void {
    if (!props.anchor) {
        return;
    }
    const rect = props.anchor.getBoundingClientRect();
    let top: number;
    switch (props.anchorAlignY) {
        case 'top':
            top = rect.top;
            break;
        case 'center':
            top = rect.top + rect.height / 2;
            break;
        case 'bottom':
            top = rect.bottom;
            break;
        default:
            top = rect.bottom;
            break;
    }
    let left: number;
    switch (props.anchorAlignX) {
        case 'right':
            left = rect.x + rect.width;
            break;
        case 'center':
            left = rect.x + rect.width / 2;
            break;
        case 'left':
        default:
            left = rect.x;
    }
    if (props.preferAnchorWidth === false) {
        dropdownStyle.value = {
            'top': `${top}px`,
            'left': `${left}px`,
            'width': 'auto',
            'minWidth': props.minWidth || '20rem',
            'maxWidth': props.maxWidth || '100vw',
            'maxHeight': props.maxHeight || 'unset',
            '--max-height': props.maxHeight || 'unset',
        };
    } else {
        dropdownStyle.value = {
            'top': `${top}px`,
            'left': `${left}px`,
            'width': `clamp(${props.minWidth || '20rem'}, ${rect.width}px, ${props.maxWidth || '100vw'})`,
            'minWidth': props.minWidth || '20rem',
            'maxWidth': props.maxWidth || '100vw',
            'maxHeight': props.maxHeight || 'unset',
            '--max-height': props.maxHeight || 'unset',
        };
    }
}

function applyDropdownAlignment(): void {
    if (!dropdown.value) {
        return;
    }
    const anchorRect = props.anchor?.getBoundingClientRect();
    const dropdownRect = dropdown.value.getBoundingClientRect();
    switch (props.dropdownPositionX) {
        case 'left':
            dropdownStyle.value.left = `${dropdownRect.left - dropdownRect.width}px`;
            break;
        case 'center':
            dropdownStyle.value.left = `${dropdownRect.left - dropdownRect.width / 2}px`;
            break;
        case 'right': // default
        default:
    }

    switch (props.dropdownPositionY) {
        case 'top':
            if (anchorRect) {
                switch (props.anchorAlignY) {
                    case 'top':
                        dropdownStyle.value.top = `${anchorRect.top - dropdownRect.height}px`;
                        break;
                    case 'center':
                        dropdownStyle.value.top = `${anchorRect.top + anchorRect.height / 2 - dropdownRect.height}px`;
                        break;
                    case 'bottom':
                        dropdownStyle.value.top = `${anchorRect.bottom - dropdownRect.height}px`;
                        break;
                }
            } else {
                dropdownStyle.value.top = `${dropdownRect.top - dropdownRect.height}px`;
            }
            break;
        case 'center':
            dropdownStyle.value.top = `${dropdownRect.top - dropdownRect.height / 2}px`;
            break;
        case 'bottom': // default
        default:
    }
}

function moveIntoVisibleArea(): void {
    if (!dropdown.value) {
        return;
    }
    const windowPadding = 10;
    const dropdownRect = dropdown.value.getBoundingClientRect();
    const viewPortHeight = window.visualViewport?.height || window.innerHeight;
    if (dropdownRect.height > viewPortHeight) {
        dropdownStyle.value = {
            ...dropdownStyle.value,
            maxHeight: `${viewPortHeight - windowPadding * 2}px`,
        };
    }
    if (dropdownRect.top < windowPadding) {
        dropdownStyle.value = {
            ...dropdownStyle.value,
            top: `${windowPadding}px`,
        };
    } else if (dropdownRect.bottom + windowPadding > window.innerHeight) {
        dropdownStyle.value = {
            ...dropdownStyle.value,
            top: `${window.innerHeight - dropdownRect.height - windowPadding}px`,
        };
    }
    if (dropdownRect.left < windowPadding) {
        dropdownStyle.value = {
            ...dropdownStyle.value,
            left: `${windowPadding}px`,
        };
    } else if (dropdownRect.right + windowPadding > window.innerWidth) {
        dropdownStyle.value = {
            ...dropdownStyle.value,
            left: `${window.innerWidth - dropdownRect.width - windowPadding}px`,
        };
    }
}

function close(): void {
    setTimeout(() => emit('close'), 10);
}

onMounted(() => {
    window.addEventListener('scroll', updatePosition, { passive: true });
    window.addEventListener('resize', updatePosition, { passive: true });
    updatePosition();
    if (dropdown.value) {
        resizeObserver.observe(dropdown.value);
    }
});

onUnmounted(() => {
    window.removeEventListener('scroll', updatePosition);
    window.removeEventListener('resize', updatePosition);
});

setTimeout(() => {
    blockAllInput.value = false;
}, 200);
</script>
