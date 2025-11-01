<template>
    <div class="interactive-list">
        <slot></slot>
    </div>
</template>
<script setup lang="ts"></script>
<style>
.interactive-list {
    @apply space-y-0.5;
}

.interactive-list > * {
    @apply -mx-2 rounded-md bg-surface-container-low p-4 xs:-mx-4;
}
.interactive-list > *:first-child {
    @apply rounded-t-2xl;
}
.interactive-list > *:last-child {
    @apply rounded-b-2xl;
}
</style>
