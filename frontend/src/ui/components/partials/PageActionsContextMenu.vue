<template>
    <div class="hidden lg:block" :class="$attrs.class">
        <ContextMenuButton
            class="btn-secondary h-full"
            anchor-align-y="bottom"
            anchor-align-x="left"
            dropdown-position-x="right"
            dropdown-position-y="top"
        >
            <slot></slot>
        </ContextMenuButton>
    </div>
    <teleport to="#nav-right">
        <div class="flex items-center py-2 lg:hidden">
            <ContextMenuButton class="-mr-6 -mt-2 pr-6 pt-3">
                <slot></slot>
            </ContextMenuButton>
        </div>
    </teleport>
</template>

<script lang="ts" setup>
import { ContextMenuButton } from '@/ui/components/common';
</script>
