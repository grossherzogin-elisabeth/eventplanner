<template>
    <div class="group">
        <div class="relative flex items-start space-x-6">
            <i class="fa-solid text-onsurface-variant mt-2 w-4 sm:w-8 sm:text-xl" :class="props.icon" />
            <div class="w-0 grow">
                <h4 class="text-onsurface/50 mb-1 text-xs font-bold">{{ props.label }}</h4>
                <slot name="default">
                    <p class="line-clamp-3">{{ props.content }}</p>
                </slot>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup generic="T">
interface Props {
    icon: string;
    label: string;
    content?: string;
}

const props = defineProps<Props>();
</script>
