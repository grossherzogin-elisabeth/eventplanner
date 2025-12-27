<template>
    <div class="v-input-text" v-bind="$attrs">
        <div class="input-field-wrapper" @click="focus()">
            <slot name="before" />
            <label :for="id">{{ props.label }}</label>
            <input
                :id="id"
                ref="inputField"
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
        <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" />
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { v4 as uuidv4 } from 'uuid';
import VInputHint from './VInputHint.vue';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: string;
    disabled?: boolean;
    readonly?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
    placeholder?: string;
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
const visited = ref<boolean>(false);
const inputField = ref<HTMLInputElement | undefined>(undefined);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

function focus(): void {
    inputField.value?.focus();
}

function onInput(event: Event): void {
    visited.value = true;
    const element = event.target as HTMLInputElement;
    emit('update:modelValue', element.value);
}
</script>
