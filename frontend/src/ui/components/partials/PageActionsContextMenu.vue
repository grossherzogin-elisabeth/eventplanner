<template>
    <div class="hidden lg:block" :class="$attrs.class" data-test-id="menu-trigger">
        <ContextMenuButton
            class="btn-secondary h-full"
            anchor-align-y="top"
            anchor-align-x="right"
            dropdown-position-x="left"
            dropdown-position-y="bottom"
        >
            <slot></slot>
        </ContextMenuButton>
    </div>
    <teleport to="#nav-right">
        <div class="flex h-full items-center lg:hidden" data-test-id="menu-trigger">
            <ContextMenuButton class="btn-icon">
                <template #default>
                    <slot name="default"></slot>
                </template>
            </ContextMenuButton>
        </div>
    </teleport>
</template>

<script lang="ts" setup>
import { ContextMenuButton } from '@/ui/components/common';
</script>
