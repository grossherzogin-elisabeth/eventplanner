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
                    {{ props.label }}
                </span>
            </slot>
        </label>
        <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" />
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { v4 as uuid4 } from 'uuid';
import VInputHint from './VInputHint.vue';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: boolean;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
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
