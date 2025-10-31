<template>
    <div>
        <ul class="border-t border-onsurface-variant/20">
            <li
                v-for="option in props.options"
                :key="String(option.value)"
                class="flex cursor-pointer items-center space-x-4 border-b border-onsurface-variant/20 py-4 hover:bg-surface-container-low"
                @click="emit('update:modelValue', option.value)"
            >
                <div v-if="props.modelValue === option.value">
                    <i class="fa-solid fa-check-circle w-5 text-xl text-primary sm:text-2xl"></i>
                </div>
                <div v-else>
                    <i class="fa-solid fa-circle w-5 text-xl text-surface-container-high sm:text-2xl"></i>
                </div>
                <span>{{ option.label }}</span>
            </li>
        </ul>
        <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" />
    </div>
</template>
<script generic="T extends any = string" lang="ts" setup>
import type { ComputedRef, Ref } from 'vue';
import { computed, ref } from 'vue';
import type { InputSelectOption } from '@/domain';
import VInputHint from './VInputHint.vue';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: T;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
    placeholder?: string;
    loading?: boolean;
    options: InputSelectOption<T>[];
}

type Emits = (e: 'update:modelValue', value: T) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

// `as props` is a workaround
// generic="T" in combination with optional props causes some compiler errors
// this issue might be related: https://github.com/vuejs/core/issues/8969
const props = defineProps<Props>() as Props;
const emit = defineEmits<Emits>();

const visited: Ref<boolean> = ref(false);
const showErrors: ComputedRef<boolean> = computed(() => visited.value || props.errorsVisible === true);
</script>
