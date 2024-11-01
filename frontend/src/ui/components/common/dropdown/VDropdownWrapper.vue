<template>
    <teleport to="#app">
        <div
            class="dropdown-wrapper-background"
            :class="{ 'pointer-events-none': blockAllInput }"
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
    // max height of the dropdown, use a css value like `50px`
    maxHeight?: string;
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
    'maxHeight': '10rem',
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
            'maxHeight': props.maxHeight || 'unset',
            '--max-height': props.maxHeight || 'unset',
        };
    }
    await nextTick();
    applyDropdownAlignment();

    // render dropdown and then move it up, so everything is visible
    await nextTick();
    moveIntoVisibleArea();
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
    dropdownStyle.value = {
        'top': `${top}px`,
        'left': `${left}px`,
        'width': `clamp(${props.minWidth || '20rem'}, ${rect.width}px,${props.maxWidth || '100vw'})`,
        'maxHeight': props.maxHeight || 'unset',
        '--max-height': props.maxHeight || 'unset',
    };
}

function applyDropdownAlignment(): void {
    if (!dropdown.value) {
        return;
    }
    const dropdownRect = dropdown.value.getBoundingClientRect();
    switch (props.dropdownPositionX) {
        case 'left':
            dropdownStyle.value.left = `${dropdownRect.left - dropdownRect.width}px`;
            break;
        case 'center':
            dropdownStyle.value.left = `${dropdownRect.left - dropdownRect.width / 2}px`;
            break;
        case 'right':
        default:
    }

    switch (props.dropdownPositionY) {
        case 'top':
            dropdownStyle.value.top = `${dropdownRect.top - dropdownRect.height}px`;
            break;
        case 'center':
            dropdownStyle.value.top = `${dropdownRect.top - dropdownRect.height / 2}px`;
            break;
        case 'bottom':
        default:
    }
}

function moveIntoVisibleArea(): void {
    if (!dropdown.value) {
        return;
    }
    const windowPadding = 5;
    const dropdownRect = dropdown.value.getBoundingClientRect();
    if (dropdownRect.y + dropdownRect.height + windowPadding > window.innerHeight) {
        dropdownStyle.value = {
            ...dropdownStyle.value,
            top: `${window.innerHeight - dropdownRect.height - windowPadding}px`,
        };
    }
    if (dropdownRect.x + dropdownRect.width + windowPadding > window.innerWidth) {
        dropdownStyle.value = {
            ...dropdownStyle.value,
            left: `${window.innerWidth - dropdownRect.width - windowPadding}px`,
        };
    }
}

function close() {
    setTimeout(() => {
        emit('close');
        console.log('close dropdown');
    }, 10);
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
}, 150);
</script>
