<template>
    <div :class="$attrs.class" class="v-input-select flex items-start">
        <label v-if="props.label" class="input-label">
            {{ props.label }}
        </label>
        <div class="relative h-full w-1/2 flex-grow">
            <div ref="dropdownAnchor" class="input-field-wrapper cursor-pointer" @click="showDropdown()">
                <slot name="before" />
                <input
                    :id="id"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    :class="{ invalid: showErrors && hasErrors }"
                    :disabled="props.disabled"
                    :placeholder="props.placeholder || $t('shared.please-select')"
                    :required="props.required"
                    :value="displayValue"
                    aria-haspopup="true"
                    class="input-field w-full cursor-pointer overflow-ellipsis"
                    readonly
                    @blur="onBlur"
                    @focus="onFocus()"
                    @keydown.down.prevent="focusNextOption()"
                    @keydown.up.prevent="focusPrevOption()"
                    @keydown.enter="selectFocusedOption"
                    @keydown.esc="hideDropdown(true)"
                />
                <div class="input-icon-right">
                    <VLoadingSpinner v-if="loading"></VLoadingSpinner>
                    <button
                        v-else
                        :class="focusOptionIndex === null ? 'rotate-0' : 'rotate-180'"
                        class="transition-transform"
                        tabindex="-1"
                        @click="showDropdown()"
                    >
                        <i class="fa-solid fa-chevron-down text-sm text-primary-600"></i>
                    </button>
                </div>
                <slot name="after" />
            </div>
            <div v-if="showErrors && hasErrors" class="input-errors">
                <p v-for="err in errors" :key="err.key" class="input-error">
                    {{ $t(err.key, err.params) }}
                </p>
            </div>
        </div>
    </div>
    <VDropdownWrapper
        v-if="focusOptionIndex !== null"
        :anchor="dropdownAnchor"
        anchor-align-y="top"
        class="input-dropdown"
        max-height="300px"
        min-width="50px"
        @close="hideDropdown(true)"
    >
        <ul v-if="visibleOptions.length === 0" ref="list">
            <li v-if="loading" class="italic opacity-50">{{ $t('shared.loading') }}</li>
            <li v-else class="italic opacity-50">{{ $t('shared.no-entries') }}</li>
        </ul>
        <ul v-else ref="list">
            <li
                v-for="(option, i) in visibleOptions"
                :key="String(option.value)"
                :class="{ 'input-dropdown-option-focus': i === focusOptionIndex }"
                class="input-dropdown-option"
                @click.stop="selectOption(option)"
                @keydown.enter="selectOption(option)"
            >
                <slot :item="option" name="item">
                    {{ option.label }}
                </slot>
            </li>
        </ul>
    </VDropdownWrapper>
</template>

<script generic="T extends any = any" lang="ts" setup>
import type { Ref } from 'vue';
import { computed, nextTick, ref } from 'vue';
import type { InputSelectOption, ValidationHint } from '@/domain';
import { VLoadingSpinner } from '@/ui/components/common';
import VDropdownWrapper from '@/ui/components/common/dropdown/VDropdownWrapper.vue';
import { v4 as uuidv4 } from 'uuid';

interface Props {
    // an optional label to render before the input field
    label?: string;
    // the value we edit, bind with v-model
    modelValue?: T;
    // disables this input
    disabled?: boolean;
    // marks this input as required
    required?: boolean;
    // validation and/or service errors for this input
    errors?: ValidationHint[];
    // show errors, even if this field has not been focused jet, e.g. after pressing save
    errorsVisible?: boolean;
    // placeholder to display if no value is entered
    placeholder?: string;
    // Show a spinning icon instead of the dropdown arrow
    loading?: boolean;
    // the options to choose from in this select
    options: InputSelectOption<T>[];
}

interface Emits {
    (e: 'update:modelValue', value: T): void;
}

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
const visited = ref(false);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

const list = ref<HTMLUListElement | null>(null);
const dropdownAnchor = ref<HTMLInputElement | null>(null);
const focusOptionIndex: Ref<number | null> = ref(null);
const selectedOptionIndex = computed<number>(() => props.options.findIndex((opt) => opt.value === props.modelValue));
const displayValue = computed<string>(() => props.options.find((it) => it.value === props.modelValue)?.label || '');
const visibleOptions = computed<InputSelectOption<T>[]>(() =>
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

function hideDropdown(focusInput: boolean = false): void {
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

function focusNextOption() {
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

function focusPrevOption() {
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

function selectFocusedOption(e: KeyboardEvent) {
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
