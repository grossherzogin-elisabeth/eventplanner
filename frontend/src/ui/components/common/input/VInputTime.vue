<template>
    <div class="v-input-time" v-bind="$attrs">
        <div class="input-field-wrapper" @click="focus()">
            <slot name="before" />
            <label :for="id">{{ props.label }}</label>
            <input
                :id="id"
                ref="inputField"
                :aria-disabled="props.disabled"
                :aria-invalid="hasErrors"
                :aria-required="props.required"
                :class="{ invalid: showErrors && hasErrors }"
                :disabled="props.disabled"
                :placeholder="$t('generic.please-select')"
                :required="props.required"
                :value="displayValue"
                aria-haspopup="true"
                class="input-field w-full pr-10 text-ellipsis"
                maxlength="7"
                @blur="onBlur()"
                @input="onInput($event)"
                @focus="inputValue = displayValue"
                @keydown.up.prevent="onArrowUp()"
                @keydown.down.prevent="onArrowDown()"
            />
            <slot name="after" />
        </div>
        <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" />
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import { addToDate, subtractFromDate } from '@/common/date/DateUtils';
import { v4 as uuidv4 } from 'uuid';
import VInputHint from './VInputHint.vue';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: Date;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
    placeholder?: string;
}

type Emits = (e: 'update:modelValue', value: Date) => void;

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
const inputField = ref<HTMLInputElement | undefined>(undefined);

const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

const displayValue = computed<string>(() => {
    if (inputValue.value !== null) {
        return inputValue.value;
    } else if (props.modelValue instanceof Date) {
        return i18n.d(props.modelValue, DateTimeFormat.hh_mm).replace(':', ' : ');
    }
    return '-- : --';
});

function focus(): void {
    inputField.value?.focus();
}

function onBlur(): void {
    visited.value = true;
    inputValue.value = null;
}

function onInput(inputEvent: Event): void {
    visited.value = true;
    const oldValue = inputValue.value || '';
    inputValue.value = (inputEvent.target as HTMLInputElement).value.replace(/[^0-9 :]/g, '');
    if (inputValue.value.length === 2 && !inputValue.value.includes(':') && oldValue.length < inputValue.value.length) {
        inputValue.value = inputValue.value + ' : ';
    }
    const [hourRaw, minuteRaw] = inputValue.value.split(':').map((it) => it.trim());
    try {
        const d = new Date(props.modelValue?.getTime() || new Date().getTime());
        const hh = Number.parseInt(hourRaw, 10);
        const mm = Number.parseInt(minuteRaw, 10);
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
    date = addToDate(date, { hours: 1 });
    inputValue.value = i18n.d(date, DateTimeFormat.hh_mm).replace(':', ' : ');
    emit('update:modelValue', date);
}

function onArrowDown(): void {
    let date = props.modelValue || new Date();
    date = subtractFromDate(date, { hours: 1 });
    inputValue.value = i18n.d(date, DateTimeFormat.hh_mm).replace(':', ' : ');
    emit('update:modelValue', date);
}
</script>
