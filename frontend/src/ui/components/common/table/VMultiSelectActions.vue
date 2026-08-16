<template>
    <div class="multi-select-bar sticky bottom-0 z-20" data-test-id="multi-select-actions">
        <MainContent class="border-outline-variant bg-surface border-t xl:rounded-bl-3xl">
            <div class="-ml-3 flex h-18 items-stretch gap-2 py-4 whitespace-nowrap">
                <button class="btn-ghost" type="button" @click="emit('selectNone')">
                    <i class="fa-solid fa-xmark" />
                </button>
                <span class="self-center font-bold">
                    {{ $t('generic.selected-count', { count: props.count }) }}
                </span>
                <div class="grow"></div>
                <slot name="action" />
                <ContextMenuButton v-if="$slots.menu" class="btn-ghost">
                    <template #default>
                        <li class="context-menu-item" @click="emit('selectAll')">
                            <i class="fa-solid fa-list-check" />
                            <span>{{ $t('generic.select-all') }}</span>
                        </li>
                        <slot name="menu" />
                    </template>
                </ContextMenuButton>
            </div>
        </MainContent>
    </div>
</template>
<script setup lang="ts">
import { ContextMenuButton } from '@/ui/components/common';
import MainContent from '@/ui/components/partials/MainContent.vue';

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
