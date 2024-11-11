<template>
    <div :class="$attrs.class" class="input-datepicker flex items-start">
        <label v-if="props.label" class="input-label">
            {{ props.label }}
        </label>
        <div class="relative w-1/2 flex-grow">
            <div ref="dropdownAnchor" class="input-field-wrapper">
                <input
                    :id="id"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    :class="{ invalid: showErrors && hasErrors }"
                    :disabled="props.disabled"
                    :placeholder="$t('shared.please-select')"
                    :required="props.required"
                    :value="displayValue"
                    aria-haspopup="true"
                    class="input-field w-full overflow-ellipsis pr-10"
                    readonly
                    @blur="visited = true"
                    @click="openDropdown()"
                    @keydown.enter.prevent="openDropdown()"
                    @keydown.space.prevent="openDropdown()"
                    @keydown.left="onArrowLeft()"
                    @keydown.right="onArrowRight()"
                    @keydown.up.prevent="onArrowUp()"
                    @keydown.down.prevent="onArrowDown()"
                    @keydown.esc="showDropdown = false"
                />

                <div class="input-icon-right">
                    <i class="fa-solid fa-calendar-day text-sm text-primary"></i>
                </div>
            </div>
            <div v-if="showErrors && hasErrors" class="input-errors">
                <p v-for="err in errors" :key="err.key" class="input-error">
                    {{ $t(err.key, err.params) }}
                </p>
            </div>
        </div>

        <VDropdownWrapper
            v-if="showDropdown"
            :anchor="dropdownAnchor"
            anchor-align-y="top"
            class="input-datepicker-dropdown"
            max-height="100vh"
            max-width="100vw"
            min-width="300px"
            @close="showDropdown = false"
        >
            <Datepicker
                ref="datepicker"
                :inline="true"
                :model-value="props.modelValue"
                language="de"
                monday-first
                :open-date="props.modelValue"
                :highlighted="{
                    from: props.highlightFrom || props.modelValue,
                    to: props.highlightTo || props.modelValue,
                }"
                @input="onInput($event)"
                @mouseup.stop=""
                @click.stop=""
            />
        </VDropdownWrapper>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import { addToDate, subtractFromDate } from '@/common/date/DateUtils';
import type { ValidationHint } from '@/domain';
import { v4 as uuidv4 } from 'uuid';
import Datepicker from 'vuejs3-datepicker';
import VDropdownWrapper from '../dropdown/VDropdownWrapper.vue';

interface Props {
    // an optional label to render before the input field
    label?: string;
    // the value we edit, bind with v-model
    modelValue?: Date;
    highlightFrom?: Date;
    highlightTo?: Date;
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
}

type Emits = (e: 'update:modelValue', value: Date) => void;

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

const dropdownAnchor = ref<HTMLElement | null>(null);
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const datepicker = ref<any | null>(null);
const showDropdown = ref<boolean>(false);
const displayValue = computed<string>(() => {
    if (props.modelValue instanceof Date) {
        return i18n.d(props.modelValue, DateTimeFormat.DDD_DD_MM_YYYY);
    }
    return '';
});

function onInput(date: Date): void {
    visited.value = true;
    showDropdown.value = false;
    emit('update:modelValue', date);
}

function openDropdown(): void {
    showDropdown.value = true;
}

function onArrowLeft(): void {
    let date = props.modelValue || new Date();
    date = subtractFromDate(date, { days: 1 });
    emit('update:modelValue', date);
}

function onArrowRight(): void {
    let date = props.modelValue || new Date();
    date = addToDate(date, { days: 1 });
    emit('update:modelValue', date);
}

function onArrowUp(): void {
    let date = props.modelValue || new Date();
    if (showDropdown.value) {
        // if the datepicker is open, select the previous row (= week)
        date = subtractFromDate(date, { days: 7 });
    } else {
        // if the datepicker is not currently open, selecting the previous day feels more natural
        date = subtractFromDate(date, { days: 1 });
    }
    emit('update:modelValue', date);
}

function onArrowDown(): void {
    let date = props.modelValue || new Date();
    if (showDropdown.value) {
        // if the datepicker is open, select the next row (= week)
        date = addToDate(date, { days: 7 });
    } else {
        // if the datepicker is not currently open, selecting the next day feels more natural
        date = addToDate(date, { days: 1 });
    }
    emit('update:modelValue', date);
}
</script>
