<template>
    <div class="v-input-textarea" v-bind="$attrs">
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
