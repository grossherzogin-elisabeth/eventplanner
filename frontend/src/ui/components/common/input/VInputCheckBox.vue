<template>
    <div class="flex flex-col justify-center">
        <label :for="id" class="inline-flex items-center">
            <input
                :id="id"
                :aria-checked="props.modelValue"
                :aria-disabled="props.disabled"
                :aria-invalid="hasErrors"
                :checked="props.modelValue"
                :disabled="props.disabled"
                :required="props.required"
                class="check-box-input hidden"
                type="checkbox"
                @input="onInput"
            />
            <span :class="{ invalid: showErrors && hasErrors }" class="check-box-container" tabindex="0">
                <span class="check-box-icon">
                    <i class="fa-solid fa-check"></i>
                </span>
            </span>
            <slot :invalid="showErrors && hasErrors" name="label">
                <span v-if="props.label" :class="{ invalid: showErrors && hasErrors }" class="check-box-label">
                    {{ $t(props.label) }}
                </span>
                <button
                    v-if="hint"
                    :title="hint"
                    class="ml-2 rounded bg-gray-700 px-2 text-sm font-semibold hover:bg-gray-600 hover:text-primary-500"
                >
                    <span>?</span>
                </button>
            </slot>
        </label>
        <div v-if="showErrors && hasErrors" class="input-errors">
            <p v-for="err in errors" :key="err.key" class="input-error">
                {{ $t(err.key, err.params) }}
            </p>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { ValidationHint } from '@/domain';
import { v4 as uuid4 } from 'uuid';

interface Props {
    // an optional label to render before the input field
    label?: string;
    // an optional hint to display next to the label
    hint?: string;
    // the value we edit, bind with v-model
    modelValue?: boolean;
    // disables this input
    disabled?: boolean;
    // marks this input as required
    required?: boolean;
    // validation and/or service errors for this input
    errors?: ValidationHint[];
    // show errors, even if this field has not been focused jet, e.g. after pressing save
    errorsVisible?: boolean;
}

type Emits = (e: 'update:modelValue', value: boolean) => void;

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

function onInput(event: Event): void {
    visited.value = true;
    if (!props.disabled) {
        const element = event.target as HTMLInputElement;
        emit('update:modelValue', element.checked);
    }
}
</script>
