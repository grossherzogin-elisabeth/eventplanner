<template>
    <div class="multi-select-bar sticky bottom-0 z-20">
        <div class="border-outline-variant bg-surface border-t px-2 md:px-12 xl:rounded-bl-3xl xl:pr-20 xl:pl-16">
            <div class="flex h-18 items-stretch gap-2 py-4 whitespace-nowrap">
                <button class="btn-ghost" @click="emit('selectNone')">
                    <i class="fa-solid fa-xmark" />
                </button>
                <span class="self-center font-bold">{{ props.count }} ausgewählt</span>
                <div class="grow"></div>
                <slot name="action" />
                <ContextMenuButton v-if="$slots.menu" class="btn-ghost">
                    <template #default>
                        <ul>
                            <li class="context-menu-item" @click="emit('selectAll')">
                                <i class="fa-solid fa-list-check" />
                                <span>Alle auswählen</span>
                            </li>
                        </ul>
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
