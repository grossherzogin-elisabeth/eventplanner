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
                    maxlength="7"
                    @blur="onBlur()"
                    @input="onInput($event)"
                    @focus="inputValue = displayValue"
                    @keydown.up.prevent="onArrowUp()"
                    @keydown.down.prevent="onArrowDown()"
                />
            </div>
            <div v-if="showErrors && hasErrors" class="input-errors">
                <p v-for="err in errors" :key="err.key" class="input-error">
                    {{ $t(err.key, err.params) }}
                </p>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import { DateUtils } from '@/common/date/DateUtils';
import type { ValidationHint } from '@/domain';
import { v4 as uuidv4 } from 'uuid';

interface Props {
    // an optional label to render before the input field
    label?: string;
    // the value we edit, bind with v-model
    modelValue?: Date;
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
    // input type used, defaults to text
    type?: 'text' | 'passwort' | 'email' | 'time' | 'number';
}

interface Emits {
    (e: 'update:modelValue', value: Date): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const i18n = useI18n();

const id = uuidv4();
const visited = ref<boolean>(false);
const inputValue = ref<string | null>(null);

const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

const displayValue = computed<string>(() => {
    if (inputValue.value !== null) {
        return inputValue.value;
    } else if (props.modelValue instanceof Date) {
        return i18n.d(props.modelValue, DateTimeFormat.hh_mm).replace(':', ' : ');
    }
    return '00 : 00';
});

function onBlur() {
    visited.value = true;
    inputValue.value = null;
}

function onInput(inputEvent: Event) {
    visited.value = true;
    const oldValue = inputValue.value || '';
    inputValue.value = (inputEvent.target as HTMLInputElement).value.replace(/[^0-9 :]/g, '');
    if (inputValue.value.length === 2 && !inputValue.value.includes(':') && oldValue.length < inputValue.value.length) {
        inputValue.value = inputValue.value + ' : ';
    }
    const [hourRaw, minuteRaw] = inputValue.value.split(':').map((it) => it.trim());
    try {
        const d = new Date(props.modelValue?.getTime() || new Date().getTime());
        const hh = parseInt(hourRaw, 10);
        const mm = parseInt(minuteRaw, 10);
        if (hh >= 0 && hh < 24) {
            d.setHours(hh);
        }
        if (mm >= 0 && mm < 60) {
            d.setMinutes(mm);
        }
        d.setSeconds(0);
        d.setMilliseconds(0);
        emit('update:modelValue', d);
    } catch (e) {
        console.warn(e);
    }
}

function onArrowUp(): void {
    let date = props.modelValue || new Date();
    date = DateUtils.add(date, { hours: 1 });
    inputValue.value = i18n.d(date, DateTimeFormat.hh_mm).replace(':', ' : ');
    emit('update:modelValue', date);
}

function onArrowDown(): void {
    let date = props.modelValue || new Date();
    date = DateUtils.subtract(date, { hours: 1 });
    inputValue.value = i18n.d(date, DateTimeFormat.hh_mm).replace(':', ' : ');
    emit('update:modelValue', date);
}
</script>
