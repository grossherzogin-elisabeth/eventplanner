<template>
    <div>
        <ul class="border-t border-onsurface-variant border-opacity-20">
            <li
                v-for="option in props.options"
                :key="String(option.value)"
                class="flex cursor-pointer items-center space-x-4 border-b border-onsurface-variant border-opacity-20 py-4 hover:bg-surface-container-low"
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
        <div v-if="showErrors && hasErrors" class="input-errors">
            <p v-for="err in errors" :key="err.key" class="input-error">
                {{ $t(err.key, err.params) }}
            </p>
        </div>
    </div>
</template>
<script generic="T extends any = string" lang="ts" setup>
import type { ComputedRef, Ref } from 'vue';
import { computed, ref } from 'vue';
import type { InputSelectOption, ValidationHint } from '@/domain';

interface Props {
    // an optional label to render before the input field
    label?: string;
    // the value we edit, bind with v-model
    modelValue?: T;
    // disables this input
    disabled?: boolean;
    // marks this input as required
    required?: boolean;
    // validation and/or service errors for this input
    errors?: ValidationHint[];
    // show errors, even if this field has not been focused jet, e.g. after pressing save
    errorsVisible?: boolean;
    // placeholder to display if no value is entered
    placeholder?: string;
    // Show a spinning icon instead of the dropdown arrow
    loading?: boolean;
    // the options to choose from in this select
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
const hasErrors: ComputedRef<boolean> = computed(() => props.errors !== undefined && props.errors.length > 0);
</script>
