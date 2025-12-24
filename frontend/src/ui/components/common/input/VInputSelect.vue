<template>
    <div :class="$attrs.class" class="v-input-select">
        <div ref="dropdownAnchor" class="input-field-wrapper cursor-pointer" @click="showDropdown()">
            <slot name="before" />
            <label :for="id">{{ props.label }}</label>
            <input
                :id="id"
                :aria-disabled="props.disabled"
                :aria-invalid="hasErrors"
                :aria-required="props.required"
                :class="{ invalid: showErrors && hasErrors }"
                :disabled="props.disabled"
                :placeholder="props.placeholder || $t('generic.please-select')"
                :required="props.required"
                :value="displayValue"
                aria-haspopup="true"
                readonly
                @blur="onBlur"
                @focus="onFocus()"
                @keydown.down.stop.prevent="focusNextOption()"
                @keydown.up.stop.prevent="focusPrevOption()"
                @keydown.enter="selectFocusedOption"
                @keydown.esc="hideDropdown(true)"
            />
            <VLoadingSpinner v-if="loading"></VLoadingSpinner>
            <button
                v-else
                :class="focusOptionIndex === null ? 'rotate-0' : 'rotate-180'"
                class="transition-transform"
                tabindex="-1"
                @click="showDropdown()"
            >
                <i class="fa-solid fa-chevron-down"></i>
            </button>
        </div>
        <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" />
    </div>
    <VDropdownWrapper
        v-if="focusOptionIndex !== null"
        :anchor="dropdownAnchor"
        anchor-align-y="bottom"
        class="input-dropdown"
        max-height="300px"
        min-width="50px"
        :prefer-anchor-width="true"
        @close="hideDropdown(true)"
    >
        <ul v-if="visibleOptions.length === 0" ref="list">
            <li v-if="loading" class="italic opacity-50">{{ $t('generic.loading') }}</li>
            <li v-else class="italic opacity-50">{{ $t('generic.no-entries') }}</li>
        </ul>
        <ul v-else ref="list">
            <li
                v-for="(option, i) in visibleOptions"
                :key="String(option.value)"
                :class="{ 'input-dropdown-option-focus': i === focusOptionIndex }"
                class="input-dropdown-option"
                @click.stop="selectOption(option as InputSelectOption<T>)"
                @keydown.enter="selectOption(option as InputSelectOption<T>)"
            >
                <slot :item="option" name="item">
                    {{ option.label || props.placeholder }}
                </slot>
            </li>
        </ul>
    </VDropdownWrapper>
</template>

<script generic="T = string" lang="ts" setup>
import type { ComputedRef, Ref } from 'vue';
import { computed, nextTick, ref } from 'vue';
import type { InputSelectOption } from '@/domain';
import { v4 as uuidv4 } from 'uuid';
import VDropdownWrapper from '../dropdown/VDropdownWrapper.vue';
import VLoadingSpinner from '../loading/VLoadingSpinner.vue';
import VInputHint from './VInputHint.vue';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: T;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
    placeholder?: string;
    loading?: boolean;
    options: InputSelectOption<T>[];
}

type Emits = (e: 'update:modelValue', value: T) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

// `as props` is a workaround
// generic="T" in combination with optional props causes some compiler errors
// this issue might be related: https://github.com/vuejs/core/issues/8969
const props = defineProps<Props>() as Props;
const emit = defineEmits<Emits>();

const id = uuidv4();
const visited: Ref<boolean> = ref(false);
const showErrors: ComputedRef<boolean> = computed(() => visited.value || props.errorsVisible === true);
const hasErrors: ComputedRef<boolean> = computed(() => props.errors !== undefined && props.errors.length > 0);

const list: Ref<HTMLUListElement | null> = ref(null);
const dropdownAnchor: Ref<HTMLInputElement | null> = ref(null);
const focusOptionIndex: Ref<number | null> = ref(null);
const selectedOptionIndex: ComputedRef<number> = computed(() => props.options.findIndex((opt) => opt.value === props.modelValue));
const displayValue: ComputedRef<string> = computed(() => props.options.find((it) => it.value === props.modelValue)?.label || '');
const visibleOptions: ComputedRef<InputSelectOption<T>[]> = computed(() =>
    props.options.filter((it) => !it.hidden || it.value === props.modelValue)
);

function onFocus(): void {
    // clear the text selection on this input on focus
    // default behavior is to select all text when navigating with tab
    if (window.getSelection) {
        const selection = window.getSelection();
        if (selection && selection.empty) {
            // Chrome
            selection.empty();
        } else if (selection && selection.removeAllRanges) {
            // Firefox
            selection.removeAllRanges();
        }
    }
}

function showDropdown(): void {
    focusOptionIndex.value = selectedOptionIndex.value;
    scrollFocussedOptionIntoView();
}

function onBlur(evt: FocusEvent): void {
    // when clicking on a dropdown option, the blur event will be triggered without a new input element as target
    // don`t close the dropdown in that case to prevent the click pointing to nowhere
    if (evt.relatedTarget) {
        hideDropdown();
    }
}

function hideDropdown(focusInput = false): void {
    setTimeout(() => {
        focusOptionIndex.value = null;
        visited.value = true;
        if (dropdownAnchor.value && focusInput) {
            dropdownAnchor.value.focus();
        }
    }, 100);
}

function selectOption(option: InputSelectOption<T>): void {
    visited.value = true;
    emit('update:modelValue', option.value);
    hideDropdown(true);
}

function focusNextOption(): void {
    if (focusOptionIndex.value === null) {
        showDropdown();
    } else if (focusOptionIndex.value === props.options.length - 1) {
        focusOptionIndex.value = 0;
        scrollFocussedOptionIntoView();
    } else {
        focusOptionIndex.value += 1;
        scrollFocussedOptionIntoView();
    }
}

function focusPrevOption(): void {
    if (!props.options || props.options.length === 0) {
        hideDropdown(true);
    }
    if (focusOptionIndex.value === null) {
        focusOptionIndex.value = 0;
    } else if (focusOptionIndex.value === 0) {
        focusOptionIndex.value = props.options.length - 1;
        scrollFocussedOptionIntoView();
    } else {
        focusOptionIndex.value -= 1;
        scrollFocussedOptionIntoView();
    }
}

function selectFocusedOption(e: KeyboardEvent): void {
    if (focusOptionIndex.value !== null) {
        selectOption(props.options[focusOptionIndex.value]);
        hideDropdown(true);
        e.preventDefault();
    }
}

function scrollFocussedOptionIntoView(): void {
    nextTick(() => {
        if (list.value) {
            const activeEntry = list.value.querySelector('.input-dropdown-option-focus');
            if (activeEntry) {
                activeEntry.scrollIntoView();
            }
        }
    });
}
</script>
