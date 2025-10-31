<template>
    <div class="v-input-textarea" :class="$attrs.class">
        <div class="input-field-wrapper" @click="focus()">
            <label :for="id">{{ props.label }}</label>
            <textarea
                :id="id"
                ref="inputField"
                :value="props.modelValue"
                :placeholder="props.placeholder"
                :disabled="props.disabled"
                :readonly="props.readonly"
                :required="props.required"
                rows="5"
                :maxlength="props.maxLength"
                :class="{
                    invalid: showErrors && hasErrors,
                    readonly: props.readonly,
                    disabled: props.disabled,
                }"
                class="input-field h-full"
                aria-multiline="true"
                @input="onInput"
                @blur="visited = true"
            />
            <span v-if="maxLength && props.modelValue" class="input-character-counter">
                {{ props.modelValue.length }}/{{ maxLength }}
            </span>
        </div>
        <div v-if="showErrors && hasErrors" class="input-errors">
            <p v-for="err in errors" :key="err.key" class="input-error">
                {{ $t(err.key, err.params) }}
            </p>
        </div>
        <div v-else-if="props.hint" class="input-hint">
            {{ props.hint }}
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { ValidationHint } from '@/domain';
import { v4 as uuidv4 } from 'uuid';

interface Props {
    label: string;
    hint?: string;
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
    type?: 'text' | 'passwort' | 'email' | 'time' | 'number';
    // max number of characters a user can enter
    maxLength?: number;
}

type Emits = (e: 'update:modelValue', value: string) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const id = uuidv4();
const visited = ref(false);
const inputField = ref<HTMLTextAreaElement | undefined>(undefined);
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
