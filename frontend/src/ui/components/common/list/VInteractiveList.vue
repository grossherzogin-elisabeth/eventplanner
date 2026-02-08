<template>
    <div class="interactive-list xs:-mx-4 -mx-2">
        <slot></slot>
    </div>
</template>
<script setup lang="ts"></script>
<style>
@reference "tailwindcss";

.interactive-list {
    @apply space-y-0.5;
}

.interactive-list > * {
    background-color: --alpha(var(--color-surface-container) / 50%);
    border-radius: var(--radius-md);
    @apply p-4;
}
.interactive-list > *:first-child {
    border-top-left-radius: var(--radius-2xl);
    border-top-right-radius: var(--radius-2xl);
}
.interactive-list > *:last-child {
    border-bottom-left-radius: var(--radius-2xl);
    border-bottom-right-radius: var(--radius-2xl);
}
</style>
