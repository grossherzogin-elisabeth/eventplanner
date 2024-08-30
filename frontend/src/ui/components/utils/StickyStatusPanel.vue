<template>
    <div
        class="fixed left-4 right-4 top-16 overflow-hidden rounded-2xl bg-white bg-opacity-75 shadow backdrop-blur md:static"
    >
        <div class="bg-opacity-10" :class="props.color ? props.color : 'bg-blue-500 text-blue-500'">
            <div class="flex items-center space-x-4 px-4 py-4 lg:px-8">
                <slot> Lorem Ipsum dolor sit amet. </slot>
            </div>
        </div>
    </div>
    <div class="h-12 md:hidden"></div>
</template>

<script lang="ts" setup>
interface Props {
    color?: string;
}

const props = defineProps<Props>();
</script>
