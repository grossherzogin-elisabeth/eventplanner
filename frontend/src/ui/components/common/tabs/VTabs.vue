<template>
    <div :class="$attrs.class">
        <h2
            class="scrollbar-invisible flex space-x-8 overflow-x-auto border-b border-gray-300 px-8 text-base font-semibold md:px-16 xl:px-20"
        >
            <button
                v-for="tab in props.tabs"
                :key="tab"
                class="whitespace-nowrap border-b pb-2 hover:text-primary-600"
                :class="
                    tab === props.modelValue ? 'border-b border-primary-600 text-primary-600' : 'border-transparent'
                "
                @click="emit('update:modelValue', tab)"
            >
                <slot name="tab" :tab="tab">
                    {{ $t(tab) }}
                </slot>
            </button>
        </h2>
    </div>
    <div v-if="props.modelValue && $slots[props.modelValue]" class="flex-1 px-8 py-4 md:px-16 md:py-8 xl:px-20">
        <template v-for="tab in props.tabs" :key="tab">
            <div v-show="tab === props.modelValue">
                <slot :name="tab" :active="tab === props.modelValue" />
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
interface Props {
    // the active tab, bind with v-model
    modelValue?: string;
    // show errors, even if this field has not been focused jet, e.g. after pressing save
    tabs?: string[];
}

interface Emits {
    (e: 'update:modelValue', value: string): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
