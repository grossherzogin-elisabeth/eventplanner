<template>
    <div>
        <ul class="">
            <li
                v-for="option in props.options"
                :key="String(option.value)"
                class="-mx-3 flex cursor-pointer items-center gap-2 py-1"
                @click="emit('update:modelValue', option.value)"
            >
                <button v-if="props.modelValue === option.value" class="btn-icon">
                    <i class="fa-regular fa-circle text-primary text-xl" />
                    <i class="fa-solid fa-circle text-primary absolute text-xs" />
                </button>
                <button v-else class="btn-icon">
                    <i class="fa-regular fa-circle text-onsurface-variant text-xl" />
                </button>
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
