<template>
    <div :class="$attrs.class">
        <h2
            class="scrollbar-invisible flex gap-x-4 overflow-x-auto border-b border-outline-variant px-8 text-base font-semibold md:px-16 xl:px-20"
        >
            <slot name="before" />
            <div class="flex gap-x-8">
                <div v-for="tab in props.tabs" :key="tab" class="tab" :class="{ active: tab === props.modelValue && !showSearch }">
                    <button class="btn-tab" @click="emit('update:modelValue', tab)">
                        <slot name="tab" :tab="tab">
                            {{ $t(tab) }}
                        </slot>
                    </button>
                </div>
            </div>
            <slot name="after" />
            <span class="flex-grow"></span>
            <slot name="end" />
        </h2>
    </div>
    <!-- tab pane -->
    <div v-if="props.modelValue && $slots[props.modelValue]" class="flex-1 px-8 py-4 md:px-16 md:py-8 xl:px-20">
        <template v-for="tab in props.tabs" :key="tab">
            <div v-show="tab === props.modelValue" class="h-full">
                <slot :name="tab" :active="tab === props.modelValue" />
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useQueryStateSync } from '@/ui/composables/QueryState';

interface Props {
    // the active tab, bind with v-model
    modelValue?: string;
    // show errors, even if this field has not been focused jet, e.g. after pressing save
    tabs?: string[];
}

type Emits = (e: 'update:modelValue', value: string) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const showSearch = ref<boolean>(false);

useQueryStateSync<string>(
    'tab',
    () => props.modelValue || '',
    (v) => emit('update:modelValue', v)
);
</script>
<style>
.tab {
    @apply whitespace-nowrap border-b border-transparent pb-1;
}
.tab button {
    @apply text-base font-bold;
    @apply -mx-4 rounded-lg px-4 py-2;
    @apply bg-transparent;
}
.tab button:hover {
    @apply bg-secondary-container text-onsecondary-container;
}
.active.tab {
    @apply border-b border-primary text-primary;
}
</style>
