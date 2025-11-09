<template>
    <div :class="$attrs.class" class="v-input-number">
        <div class="input-field-wrapper" @click="focus()">
            <slot name="before"></slot>
            <label :for="id">{{ props.label }}</label>
            <input
                :id="id"
                ref="inputField"
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
        <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" />
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { v4 as uuidv4 } from 'uuid';
import VInputHint from './VInputHint.vue';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: number | string;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
    placeholder?: string;
    decimal?: boolean;
}

type Emits = (e: 'update:modelValue', value: number | string) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = withDefaults(defineProps<Props>(), {
    label: '',
    hint: undefined,
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
const inputField = ref<HTMLElement | undefined>(undefined);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors.length > 0);

function focus(): void {
    inputField.value?.focus();
}

function onKeyPress(event: KeyboardEvent): void {
    if (!props.decimal && Number.isNaN(Number.parseInt(event.key, 10))) {
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

function onModelValueChange(): void {
    const modelValueString = `${props.modelValue}`;
    const valueString = `${value.value}`;

    if (valueString !== modelValueString + '.' && valueString !== modelValueString + ',') {
        value.value = props.modelValue || value.value;
    }
}

function init(): void {
    value.value = props.modelValue !== undefined ? props.modelValue : '';
    watch(
        () => props.modelValue,
        () => onModelValueChange()
    );
}

init();
</script>
