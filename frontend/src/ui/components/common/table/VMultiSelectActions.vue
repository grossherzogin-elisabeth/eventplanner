<template>
    <div class="multi-select-bar sticky bottom-0 z-20">
        <div class="border-t border-outline-variant bg-surface px-2 md:px-12 xl:rounded-bl-3xl xl:pl-16 xl:pr-20">
            <div class="h-18 flex items-stretch gap-2 whitespace-nowrap py-4">
                <button class="btn-ghost" @click="emit('selectNone')">
                    <i class="fa-solid fa-xmark" />
                </button>
                <span class="self-center font-bold">{{ props.count }} ausgewählt</span>
                <div class="flex-grow"></div>
                <slot name="action" />
                <ContextMenuButton v-if="$slots.menu" class="btn-ghost">
                    <template #default>
                        <li class="context-menu-item" @click="emit('selectAll')">
                            <i class="fa-solid fa-list-check" />
                            <span>Alle auswählen</span>
                        </li>
                        <slot name="menu" />
                    </template>
                </ContextMenuButton>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ContextMenuButton } from '@/ui/components/common';

interface Props {
    count: number;
}

interface Emits {
    (e: 'selectAll'): void;
    (e: 'selectNone'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
<style>
.multi-select-bar {
    --anim-slide-diff-y: 2rem;
    animation: anim-slide-in 150ms ease;
}
</style>
