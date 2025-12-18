<template>
    <div :class="$attrs.class" class="v-input-date">
        <div ref="dropdownAnchor" class="input-field-wrapper" @click="focus()">
            <label :for="id">{{ props.label }}</label>
            <input
                :id="id"
                ref="inputField"
                v-model="displayValue"
                :aria-disabled="props.disabled"
                :aria-invalid="hasErrors"
                :aria-required="props.required"
                :class="{ invalid: showErrors && hasErrors }"
                :disabled="props.disabled"
                :placeholder="props.placeholder ?? 'dd.mm.yyyy'"
                :required="props.required"
                aria-haspopup="true"
                @blur="updatedSelectedDate(displayValue)"
                @keydown.up.prevent="subtractDays(showDropdown ? 7 : 1)"
                @keydown.down.prevent="addDays(showDropdown ? 7 : 1)"
                @keydown.esc="showDropdown = false"
            />
            <button
                v-if="!disabled"
                class="btn-icon -mr-3"
                @click.stop="showDatepicker()"
                @keydown.esc="showDropdown = false"
                @keydown.exact.left="subtractDays(1)"
                @keydown.shift.left="subtractDays(getDaysOfMonth(props.modelValue ?? new Date()))"
                @keydown.exact.right="addDays(1)"
                @keydown.shift.right="addDays(getDaysOfMonth(props.modelValue ?? new Date()))"
                @keydown.up.prevent="subtractDays(7)"
                @keydown.down.prevent="addDays(7)"
                @keydown.enter.stop.prevent="showDropdown ? confirmDate(props.modelValue ?? new Date()) : showDatepicker()"
            >
                <i class="fa-solid fa-calendar-day"></i>
            </button>
        </div>
        <VInputHint :hint="props.hint" :errors="allErrors" :show-errors="showErrors" />

        <VDropdownWrapper
            v-if="showDropdown"
            :anchor="dropdownAnchor"
            anchor-align-y="bottom"
            class="input-dropdown"
            min-width="10rem"
            @close="showDropdown = false"
        >
            <VDatepicker
                :model-value="props.modelValue ?? new Date()"
                :highlight-from="props.highlightFrom"
                :highlight-to="props.highlightTo"
                @update:model-value="confirmDate($event)"
            />
        </VDropdownWrapper>
        <VDialog ref="dialog" type="modal" width="auto">
            <template #title>{{ props.label }}</template>
            <template #default>
                <VDatepicker v-model="dialogValue" :highlight-from="props.highlightFrom" :highlight-to="props.highlightTo" />
            </template>
            <template #buttons="{ close, submit }">
                <button class="btn-ghost" @click="close()">
                    <span>{{ $t('generic.cancel') }}</span>
                </button>
                <button class="btn-ghost" @click="submit(dialogValue)">
                    <span>{{ $t('generic.ok') }}</span>
                </button>
            </template>
        </VDialog>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
    DATE_DD_MM_YYYY_REGEX,
    DATE_DD_MM_YY_REGEX,
    DATE_MM_DD_YYYY_REGEX,
    DATE_YYYY_MM_DD_REGEX,
    NUMBER_REGEX,
} from '@/application/utils/RegExpresions';
import { DateTimeFormat, getDaysOfMonth } from '@/common/date';
import { addToDate, subtractFromDate } from '@/common/date/DateUtils';
import type { Dialog } from '@/ui/components/common';
import { VDialog } from '@/ui/components/common';
import VDatepicker from '@/ui/components/common/datepicker/VDatepicker.vue';
import { v4 as uuidv4 } from 'uuid';
import VDropdownWrapper from '../dropdown/VDropdownWrapper.vue';
import VInputHint from './VInputHint.vue';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: Date;
    highlightFrom?: Date;
    highlightTo?: Date;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
    placeholder?: string;
}

type Emits = (e: 'update:modelValue', value: Date | undefined) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 * Requires https://www.npmjs.com/package/vuejs3-datepicker
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const i18n = useI18n();

const id = uuidv4();
const visited = ref(false);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

const inputField = ref<HTMLInputElement | null>(null);
const dropdownAnchor = ref<HTMLElement | null>(null);
const dialog = ref<Dialog | null>(null);
const showDropdown = ref<boolean>(false);
const dialogValue = ref<Date>(new Date());
const displayValue = ref<string>('');
const hasInvalidInput = ref<boolean>(false);

const allErrors = computed<string[] | undefined>(() => {
    if (hasInvalidInput.value) {
        return (props.errors ?? []).concat(['generic.validation.invalid-date']);
    }
    return props.errors;
});

function init(): void {
    updateDisplayValue(props.modelValue);
    watch(
        () => props.modelValue,
        () => updateDisplayValue(props.modelValue)
    );
}

function updateDisplayValue(date?: Date): void {
    if (date) {
        displayValue.value = i18n.d(date, DateTimeFormat.DD_MM_YYYY);
    }
}

function focus(): void {
    inputField.value?.focus();
}

async function showDatepicker(): Promise<void> {
    if (props.disabled) {
        return;
    }
    if (window.innerWidth >= 640) {
        showDropdown.value = true;
    } else {
        dialogValue.value = props.modelValue ?? new Date();
        const selected = await dialog.value?.open().catch(() => {
            // ignore
        });
        if (selected) {
            confirmDate(selected);
        }
    }
}

function confirmDate(date: Date): void {
    updatedSelectedDate(date);
    showDropdown.value = false;
}

function subtractDays(count: number): void {
    let date = props.modelValue || new Date();
    date = subtractFromDate(date, { days: count });
    updatedSelectedDate(date);
}

function addDays(count: number): void {
    let date = props.modelValue || new Date();
    date = addToDate(date, { days: count });
    updatedSelectedDate(date);
}

function updatedSelectedDate(input: Date | string): void {
    let date: Date | undefined = undefined;
    if (typeof input === 'string') {
        try {
            date = parseDate(input.trim());
            hasInvalidInput.value = false;
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (e) {
            hasInvalidInput.value = true;
        }
    } else {
        date = input ?? undefined;
        hasInvalidInput.value = false;
    }
    visited.value = true;
    updateDisplayValue(date);
    emit('update:modelValue', date);
}

function parseDate(value: string): Date | undefined {
    if (value.length === 0) {
        return undefined;
    }

    let year: number | undefined = undefined;
    let month: number | undefined = undefined;
    let day: number | undefined = undefined;
    if (value.match(DATE_YYYY_MM_DD_REGEX)) {
        const parts = value.split(new RegExp('[- /.]'));
        day = Number.parseInt(parts[2], 10);
        month = Number.parseInt(parts[1], 10);
        year = Number.parseInt(parts[0], 10);
    } else if (value.match(DATE_DD_MM_YYYY_REGEX) || value.match(DATE_DD_MM_YY_REGEX)) {
        const parts = value.split(new RegExp('[- /.]'));
        day = Number.parseInt(parts[0], 10);
        month = Number.parseInt(parts[1], 10);
        year = Number.parseInt(parts[2], 10);
    } else if (value.match(DATE_MM_DD_YYYY_REGEX)) {
        const parts = value.split(new RegExp('[- /.]'));
        day = Number.parseInt(parts[1], 10);
        month = Number.parseInt(parts[0], 10);
        year = Number.parseInt(parts[2], 10);
    } else if (value.match(NUMBER_REGEX) && value.length >= 6) {
        day = Number.parseInt(value.substring(0, 2), 10);
        month = Number.parseInt(value.substring(2, 4), 10);
        year = Number.parseInt(value.substring(4), 10);
    } else {
        throw new Error('Invalid date input');
    }
    if (year !== undefined && year < new Date().getFullYear() - 2000 + 5) {
        year += 2000;
    } else if (year !== undefined && year < 100) {
        year += 1900;
    }
    if (year !== undefined && month !== undefined && day !== undefined) {
        const parsedDate = new Date(year, month - 1, day);
        if (Number.isNaN(parsedDate.getTime())) {
            throw new Error('Date input was parsed to invalid date');
        }
        return parsedDate;
    }
    return undefined;
}

init();
</script>
