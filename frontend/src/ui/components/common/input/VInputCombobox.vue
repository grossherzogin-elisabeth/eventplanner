<template>
    <div class="v-input-combobox" v-bind="$attrs">
        <div ref="dropdownAnchor" class="input-field-wrapper" @click="showDropdown()">
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
                class="input-field w-full pr-10 text-ellipsis"
                readonly
                @keydown.down.prevent="focusNextOption()"
                @keydown.up.prevent="focusPrevOption()"
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
        class="input-dropdown combobox"
        max-height="400px"
        :prefer-anchor-width="true"
        @close="hideDropdown(true)"
    >
        <div class="flex h-full flex-col overflow-hidden">
            <div class="border-outline-variant flex items-stretch border-b">
                <slot name="before-filter">
                    <i class="fa-solid fa-search mx-4 self-center text-sm opacity-50" />
                </slot>
                <input
                    :id="id"
                    ref="input"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    :class="{ invalid: showErrors && hasErrors }"
                    :disabled="props.disabled"
                    :placeholder="$t('generic.filter-entries')"
                    :required="props.required"
                    :value="focusOptionIndex === null ? displayValue : filter"
                    aria-haspopup="true"
                    class="placeholder:text-onsurface-variant/50 h-14 w-full bg-transparent py-3 text-ellipsis"
                    @blur="onBlur"
                    @click="showDropdown()"
                    @input="filterValues($event)"
                    @keydown.down.stop.prevent="focusNextOption()"
                    @keydown.up.stop.prevent="focusPrevOption()"
                    @keydown.enter="selectFocusedOption"
                    @keydown.esc="hideDropdown(true)"
                />
                <slot name="after-filter">
                    <button v-if="filter" class="flex items-center self-stretch pr-4" @click.stop="filter = ''">
                        <i class="fa-solid fa-xmark-circle opacity-75 hover:opacity-100"></i>
                    </button>
                </slot>
            </div>
            <slot name="first-option" />
            <div class="flex-1 overflow-y-auto">
                <ul v-if="filteredOptions.length === 0" ref="list">
                    <li v-if="loading" class="input-dropdown-hint">{{ $t('generic.loading') }}</li>
                    <li v-else-if="props.options.length === 0" class="input-dropdown-hint">
                        {{ $t('generic.no-entries') }}
                    </li>
                    <li v-else class="input-dropdown-hint">{{ $t('generic.no-matches') }}</li>
                </ul>
                <template v-else>
                    <ul ref="list">
                        <li v-if="lastUsedOptions.length > 0" class="input-dropdown-heading">Zuletzt verwendet</li>
                        <li
                            v-for="(option, i) in lastUsedOptions"
                            :key="String(option.value)"
                            :class="{ 'input-dropdown-option-focus': i === focusOptionIndex }"
                            class="input-dropdown-option"
                            @click.stop="selectOption(option as InputSelectOption<T>)"
                            @keydown.enter="selectOption(option as InputSelectOption<T>)"
                        >
                            <slot :active="option.value === props.modelValue" :item="option" name="item">
                                {{ option.label }}
                            </slot>
                        </li>
                        <li
                            v-for="(option, i) in filteredOptions"
                            :key="String(option.value)"
                            :class="{ 'input-dropdown-option-focus': i === focusOptionIndex }"
                            class="input-dropdown-option"
                            @click.stop="selectOption(option as InputSelectOption<T>)"
                            @keydown.enter="selectOption(option as InputSelectOption<T>)"
                        >
                            <slot :active="option.value === props.modelValue" :item="option" name="item">
                                {{ option.label }}
                            </slot>
                        </li>
                    </ul>
                </template>
            </div>
            <slot name="last-option" />
        </div>
    </VDropdownWrapper>
</template>

<script generic="T extends any = string" lang="ts" setup>
import type { Ref } from 'vue';
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
    lastUsed?: T[];
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
const visited = ref(false);

const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

const list = ref<HTMLUListElement | null>(null);
const dropdownAnchor = ref<HTMLInputElement | null>(null);
const focusOptionIndex: Ref<number | null> = ref(null);
const input = ref<HTMLInputElement | null>(null);
const filter = ref('');

const selectedOptionIndex = computed<number>(() => filteredOptions.value.findIndex((opt) => opt.value === props.modelValue));

const visibleOptions = computed<InputSelectOption<T>[]>(() => {
    return props.options.filter((it) => !it.hidden || it.value === props.modelValue);
});

const lastUsedOptions = computed<InputSelectOption<T>[]>(() => {
    return visibleOptions.value.filter((it) => props.lastUsed?.includes(it.value));
});

const filteredOptions = computed<InputSelectOption<T>[]>(() => {
    let filtered = visibleOptions.value;
    const f = filter.value.trim().toLowerCase();
    if (f !== '') {
        filtered = filtered.filter((opt) => opt.label.toLowerCase().includes(f));
    }
    return filtered;
});
const displayValue = computed<string>(() => props.options.find((it) => it.value === props.modelValue)?.label || '');

function showDropdown(): void {
    focusOptionIndex.value = selectedOptionIndex.value;
    scrollFocussedOptionIntoView();
    nextTick(() => input.value?.focus());
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
    } else if (focusOptionIndex.value === filteredOptions.value.length - 1) {
        focusOptionIndex.value = 0;
        scrollFocussedOptionIntoView();
    } else {
        focusOptionIndex.value += 1;
        scrollFocussedOptionIntoView();
    }
}

function focusPrevOption(): void {
    if (focusOptionIndex.value === null) {
        showDropdown();
    } else if (focusOptionIndex.value === 0) {
        focusOptionIndex.value = filteredOptions.value.length - 1;
        scrollFocussedOptionIntoView();
    } else {
        focusOptionIndex.value -= 1;
        scrollFocussedOptionIntoView();
    }
}

function selectFocusedOption(e: KeyboardEvent): void {
    if (focusOptionIndex.value !== null) {
        selectOption(filteredOptions.value[focusOptionIndex.value]);
        hideDropdown(true);
        e.preventDefault();
        nextTick(() => (filter.value = ''));
    }
}

function scrollFocussedOptionIntoView(): void {
    nextTick(() => {
        if (list.value) {
            const activeEntry = list.value.querySelector('.input-dropdown-option-focus');
            if (activeEntry) {
                activeEntry.scrollIntoView({ block: 'center' });
            }
        }
    });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function filterValues(event: any): void {
    filter.value = event.target.value;
    focusOptionIndex.value = 0;
}
</script>
