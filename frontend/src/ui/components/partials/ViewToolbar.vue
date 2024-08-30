<template>
    <div class="hidden items-center space-x-4 pb-8 pl-20 pr-14 pt-8 xl:flex">
        <div class="flex h-8 items-center">
            <slot name="breadcrumps" />
        </div>
        <div class="flex flex-grow items-center justify-center space-x-4">
            <slot name="center" />
        </div>
        <slot name="right" />
    </div>
</template>

<script lang="ts" setup></script>
