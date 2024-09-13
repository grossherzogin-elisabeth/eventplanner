<template>
    <div class="flex items-start" :class="$attrs.class">
        <label v-if="props.label" class="input-label">
            {{ props.label }}
        </label>
        <div class="relative h-full w-1/2 flex-grow">
            <div class="input-field-wrapper">
                <slot name="before"></slot>
                <input
                    :id="id"
                    :value="props.modelValue"
                    :placeholder="props.placeholder"
                    :disabled="props.disabled"
                    :required="props.required"
                    :readonly="props.readonly"
                    :type="props.type || 'text'"
                    :class="{
                        invalid: showErrors && hasErrors,
                        readonly: props.readonly,
                        disabled: props.disabled,
                    }"
                    class="input-field"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    @input="onInput"
                    @blur="visited = true"
                    @submit="emit('submit', String($event))"
                    @keydown.enter="emit('submit', String($event))"
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
import { computed, ref } from 'vue';
import type { ValidationHint } from '@/domain';
import { v4 as uuidv4 } from 'uuid';

interface Props {
    // an optional label to render before the input field
    label?: string;
    // the value we edit, bind with v-model
    modelValue?: string;
    // disables this input
    disabled?: boolean;
    // makes this input readonly
    readonly?: boolean;
    // marks this input as required
    required?: boolean;
    // validation and/or service errors for this input
    errors?: ValidationHint[];
    // show errors, even if this field has not been focused jet, e.g. after pressing save
    errorsVisible?: boolean;
    // placeholder to display if no value is entered
    placeholder?: string;
    // input type used, defaults to text
    type?: 'text' | 'password' | 'email' | 'time' | 'number';
}

interface Emits {
    (e: 'update:modelValue', value: string): void;

    (e: 'submit', value: string): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const id = uuidv4();
const visited = ref(false);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

function onInput(event: Event) {
    visited.value = true;
    const element = event.target as HTMLInputElement;
    emit('update:modelValue', element.value);
}
</script>
