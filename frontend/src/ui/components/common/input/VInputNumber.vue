<template>
    <div :class="$attrs.class" class="flex items-start">
        <label v-if="props.label" class="input-label">
            {{ props.label }}
        </label>
        <div class="w-1/2 flex-grow">
            <div ref="dropdownAnchor" class="input-field-wrapper">
                <slot name="before"></slot>
                <input
                    :id="id"
                    v-model="value"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    :class="{ invalid: showErrors && hasErrors }"
                    :disabled="props.disabled"
                    :placeholder="props.placeholder"
                    :required="props.required"
                    class="input-field"
                    type="number"
                    @blur="visited = true"
                    @input="onInput"
                    @keypress="onKeyPress"
                />
                <slot name="after"></slot>
            </div>
            <div v-if="showErrors && hasErrors" class="input-errors">
                <p v-for="err in errors" :key="err.key" class="input-error">
                    {{ $t(err.key, err.params) }}
                </p>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import type { ValidationHint } from '@/domain';
import { v4 as uuidv4 } from 'uuid';

interface Props {
    // an optional label to render before the input field
    label?: string | undefined;
    // the value we edit, bind with v-model
    modelValue?: number | string | undefined;
    // disables this input
    disabled?: boolean | undefined;
    // marks this input as required
    required?: boolean | undefined;
    // validation and/or service errors for this input
    errors?: ValidationHint[] | undefined;
    // show errors, even if this field has not been focused jet, e.g. after pressing save
    errorsVisible?: boolean | undefined;
    // placeholder to display if no value is entered
    placeholder?: string | undefined;
    // flag to disable decimal numbers
    decimal?: boolean | undefined;
}

interface Emits {
    (e: 'update:modelValue', value: number | string): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = withDefaults(defineProps<Props>(), {
    label: '',
    placeholder: '',
    modelValue: undefined,
    disabled: false,
    required: false,
    errors: () => [],
    errorsVisible: false,
    decimal: true,
});
const emit = defineEmits<Emits>();

const value = ref<number | string>('');

const id = uuidv4();
const visited = ref(false);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors.length > 0);

function onKeyPress(event: KeyboardEvent): void {
    if (!props.decimal && Number.isNaN(parseInt(event.key))) {
        event.preventDefault();
    }
}

function onInput(event: Event): void {
    visited.value = true;
    const element = event.target as HTMLInputElement;
    if (!Number.isNaN(element.valueAsNumber)) {
        if (!props.decimal) {
            element.value = `${Math.floor(element.valueAsNumber)}`;
        }
        emit('update:modelValue', element.valueAsNumber);
    } else {
        emit('update:modelValue', element.value);
    }
}

function onModelValueChange() {
    const modelValueString = `${props.modelValue}`;
    const valueString = `${value.value}`;

    if (valueString !== modelValueString + '.' && valueString !== modelValueString + ',') {
        value.value = props.modelValue || value.value;
    }
}

function init() {
    value.value = props.modelValue !== undefined ? props.modelValue : '';
    watch(
        () => props.modelValue,
        () => onModelValueChange()
    );
}

init();
</script>
