<template>
    <div>
        <div class="collapsible-pane-header">
            <button
                class="collapsible-pane-btn"
                @click="toggleCollapsed()"
                @keydown.up="setCollapsed(true, true)"
                @keydown.down="setCollapsed(false, true)"
            >
                <div>
                    <slot name="title"></slot>
                </div>
                <IconArrowDown
                    :class="isCollapsed ? 'rotate-180' : 'rotate-0'"
                    class="collapsible-pane-btn-icon transition-transform"
                />
            </button>
        </div>
        <div
            v-show="renderContent"
            ref="pane"
            :class="{ open: !isCollapsed, closed: isCollapsed, animate: animate }"
            :style="{ '--preferred-height': preferredHeight }"
            class="collapsible-pane"
        >
            <div class="collapsible-pane-content">
                <slot name="content"></slot>
                <slot></slot>
            </div>
        </div>
        <div v-show="isCollapsed">
            <slot name="preview"></slot>
        </div>
        <slot name="footer"></slot>
    </div>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { IconArrowDown } from '@/ui/icons/bold';

interface Props {
    collapsed?: boolean;
}

interface Emits {
    (e: 'update:collapsed', value: boolean): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const blocked = ref<boolean>(false);
const isCollapsed = ref<boolean>(false);
const renderContent = ref<boolean>(true);
const animate = ref<boolean>(false);
const pane = ref<HTMLDivElement | null>(null);
const preferredHeight = ref<string>('100vh');

function toggleCollapsed(): void {
    setCollapsed(!isCollapsed.value, true);
}

function resetPreferredHeight(): void {
    preferredHeight.value = '100vh';
}

function setCollapsed(collapse: boolean, animateCollapse: boolean = false): void {
    if (isCollapsed.value === collapse || blocked.value === true) {
        return;
    }
    blocked.value = true;
    animate.value = animateCollapse;
    // update preferred height
    if (pane.value) {
        const height = pane.value?.clientHeight || 0;
        if (collapse) {
            // set preferred height now
            preferredHeight.value = `${height + 10}px`;
            setTimeout(() => (renderContent.value = false), 500);
        } else {
            renderContent.value = true;
            // set preferred height after animation
            // add 10px buffer
            setTimeout(() => (preferredHeight.value = `${height + 10}px`), 500);
        }
    }
    nextTick(() => {
        isCollapsed.value = collapse;
        emit('update:collapsed', isCollapsed.value);
    });
    // unblock after 550 ms
    setTimeout(() => (blocked.value = false), 550);
}

onMounted(() => {
    window.addEventListener('resize', resetPreferredHeight, { passive: true });
    if (props.collapsed) {
        setCollapsed(props.collapsed, false);
    }
});

onUnmounted(() => {
    window.removeEventListener('resize', resetPreferredHeight);
});

watch(
    () => props.collapsed,
    () => setCollapsed(props.collapsed || false, true)
);
</script>
