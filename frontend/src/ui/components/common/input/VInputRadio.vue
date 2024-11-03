<template>
    <div :class="$attrs.class" class="flex items-start">
        <label v-if="props.label" class="input-label">
            {{ props.label }}
        </label>
        <div class="input-radio">
            <label
                v-for="(option, index) in props.options"
                :key="index"
                :for="`${id}-${index}`"
                class="my-4 flex items-center"
            >
                <input
                    :id="`${id}-${index}`"
                    :aria-checked="props.modelValue === option.value"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    :checked="props.modelValue === option.value"
                    :disabled="props.disabled"
                    :required="props.required"
                    class="check-box-input hidden"
                    type="radio"
                    @input="onInput(option.value)"
                />
                <span
                    :class="{ invalid: showErrors && hasErrors }"
                    class="check-box-container select-box-container"
                    tabindex="0"
                >
                    <icon-check class="check-box-icon" />
                </span>
                <span :class="{ invalid: showErrors && hasErrors }" class="check-box-label">
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

<script lang="ts" setup>
import { v4 as uuid4 } from 'uuid';
import { IconCheck } from '@/ui/icons/bold';
import { computed, ref } from 'vue';
import type { ValidationHint } from '@/domain';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type T = any; // Value type

interface Props {
    // an optional label to render before the input field
    label?: string;
    // the value we edit, bind with v-model
    modelValue?: string;
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

function onInput(option: T) {
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
