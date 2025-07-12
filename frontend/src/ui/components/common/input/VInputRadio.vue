<template>
    <div :class="$attrs.class" class="">
        <label v-if="props.label" class="input-label">
            {{ props.label }}
        </label>
        <div class="flex flex-wrap items-start gap-4" :class="props.orientation === 'vertical' ? 'flex-col' : 'flex-row'">
            <label v-for="(option, index) in props.options" :key="index" :for="`${id}-${index}`" class="flex items-center">
                <input
                    :id="`${id}-${index}`"
                    :aria-checked="props.modelValue === option.value"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    :checked="props.modelValue === option.value"
                    :disabled="props.disabled"
                    :required="props.required"
                    class="hidden"
                    type="radio"
                    :name="id"
                    @input="onInput(option.value)"
                />
                <span v-if="props.modelValue === option.value">
                    <i class="fa-solid fa-check-circle w-5 text-xl text-primary sm:text-2xl"></i>
                </span>
                <span v-else>
                    <i class="fa-solid fa-circle w-5 text-xl text-surface-container-high sm:text-2xl"></i>
                </span>
                <span :class="{ invalid: showErrors && hasErrors }" class="ml-4 cursor-pointer">
                    {{ $t(option.label) }}
                </span>
            </label>
            <div v-if="showErrors && hasErrors" class="input-errors">
                <p v-for="err in errors" :key="err.key" class="input-error">
                    {{ $t(err.key, err.params) }}
                </p>
            </div>
        </div>
    </div>
</template>

<script generic="T extends any = string" lang="ts" setup>
import { computed, ref } from 'vue';
import type { ValidationHint } from '@/domain';
import { v4 as uuid4 } from 'uuid';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type T = any; // Value type

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
    // the options to display
    options: InputRadioOption<T>[];
    // should the radio items be oriented in a vertical or horizontal way
    orientation?: 'vertical' | 'horizontal';
}

type Emits = (e: 'update:modelValue', value: T) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const id = uuid4();
const visited = ref(false);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

function onInput(option: T): void {
    visited.value = true;
    if (!props.disabled) {
        emit('update:modelValue', option);
    }
}
</script>

<script lang="ts">
// fake default export for typescript
export default {};

export interface InputRadioOption<T> {
    label: string;
    value: T;
}
</script>
