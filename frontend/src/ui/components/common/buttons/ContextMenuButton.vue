<template>
    <button ref="button" :class="$attrs.class" class="cursor-pointer" @click.stop="open = true">
        <slot name="icon">
            <i class="fa-solid fa-ellipsis-vertical mx-1" />
        </slot>
    </button>
    <VDropdownWrapper
        v-if="open"
        :anchor="$refs.button as HTMLElement"
        anchor-align-x="right"
        anchor-align-y="top"
        dropdown-position-x="left"
        max-width="18rem"
        min-width="18rem"
        @close="open = false"
    >
        <div class="mt-2 rounded-xl bg-primary-100 p-4 shadow-xl" @click="open = false">
            <slot />
        </div>
    </VDropdownWrapper>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import VDropdownWrapper from '@/ui/components/common/dropdown/VDropdownWrapper.vue';

const open = ref<boolean>(false);
</script>

<style>
.context-menu-item {
    @apply -mx-4 flex cursor-pointer items-center px-4 py-3 hover:bg-primary-200 md:py-2;
}

.context-menu-item > i,
.context-menu-item > svg {
    @apply mr-4 block w-6;
}
</style>
