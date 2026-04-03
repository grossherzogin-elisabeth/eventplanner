<template>
    <div ref="datepicker" class="v-datepicker max-w-full" @click.stop="" @mouseup.stop="">
        <div v-if="!props.readonly" class="header text-onsurface-variant px-4 pt-4 pb-3">
            <div class="flex items-stretch justify-between">
                <button data-test-id="datepicker-previous-month" class="btn-header" :disabled="view !== 'day'" @click="previousMonth()">
                    <i class="fa-solid fa-chevron-left text-xs"></i>
                </button>
                <button data-test-id="datepicker-month" :disabled="view === 'year'" class="btn-header" @click="toggleMonthSelection()">
                    <span class="">{{ $t(`generic.month.${month}`) }}</span>
                </button>
                <button data-test-id="datepicker-next-month" class="btn-header" :disabled="view !== 'day'" @click="nextMonth()">
                    <i class="fa-solid fa-chevron-right text-xs"></i>
                </button>
            </div>
            <div class="flex items-stretch justify-between">
                <button data-test-id="datepicker-previous-year" class="btn-header" :disabled="view !== 'day'" @click="year--">
                    <i class="fa-solid fa-chevron-left text-xs"></i>
                </button>
                <button data-test-id="datepicker-year" :disabled="view === 'month'" class="btn-header" @click="toggleYearSelection()">
                    <span class="">{{ year }}</span>
                </button>
                <button data-test-id="datepicker-next-year" class="btn-header" :disabled="view !== 'day'" @click="year++">
                    <i class="fa-solid fa-chevron-right text-xs"></i>
                </button>
            </div>
        </div>
        <div v-else class="header text-onsurface-variant px-4 pt-4 pb-3">{{ $t(`generic.month.${month}`) }} {{ year }}</div>
        <div class="content pt-3 pb-4">
            <div class="day-selection grid grid-cols-7 gap-y-2 px-4">
                <span class="label">{{ $t('generic.weekday-short.0') }}</span>
                <span class="label">{{ $t('generic.weekday-short.1') }}</span>
                <span class="label">{{ $t('generic.weekday-short.2') }}</span>
                <span class="label">{{ $t('generic.weekday-short.3') }}</span>
                <span class="label">{{ $t('generic.weekday-short.4') }}</span>
                <span class="label">{{ $t('generic.weekday-short.5') }}</span>
                <span class="label">{{ $t('generic.weekday-short.6') }}</span>
                <div
                    v-for="day in days"
                    :key="day.date.getTime()"
                    class="day"
                    :class="{
                        previous: day.previousMonth,
                        next: day.nextMonth,
                        selected: isSameDate(day.date, props.modelValue),
                        current: isSameDate(day.date, new Date()),
                        start: isSameDate(day.date, min(props.highlightFrom, props.highlightTo)),
                        highlight: isHighlighted(day.date),
                        end: isSameDate(day.date, max(props.highlightFrom, props.highlightTo)),
                    }"
                >
                    <button class="btn-icon" @click="selectDate(day)">
                        {{ day.date.getDate() }}
                    </button>
                </div>
            </div>
            <!-- month selection -->
            <div v-if="view === 'month'" class="month-selection border-onsurface-variant/40 border-t">
                <ul>
                    <li
                        v-for="i in 12"
                        :key="i"
                        class="input-dropdown-option month"
                        :class="{ 'input-dropdown-option-focus': i - 1 === month }"
                        @click="selectMonth(i - 1)"
                    >
                        <span v-if="i - 1 === month" class="inline-block w-8">
                            <i class="fa-solid fa-check" />
                        </span>
                        <span v-else class="inline-block w-8"></span>
                        <span>{{ $t(`generic.month.${i - 1}`) }}</span>
                    </li>
                </ul>
            </div>
            <!-- year selection -->
            <div v-if="view === 'year'" class="year-selection border-onsurface-variant/40 flex flex-col items-center border-t px-4 py-4">
                <button class="btn-header" @click="showYearSelectionFrom -= 12">
                    <span class="px-4">...</span>
                </button>
                <div class="grid grid-cols-3 gap-2 self-stretch">
                    <button
                        v-for="y in years"
                        :key="y"
                        class="year"
                        :class="{
                            selected: y === year,
                            current: y === new Date().getFullYear(),
                        }"
                        @click="selectYear(y)"
                    >
                        {{ y }}
                    </button>
                </div>
                <button class="btn-header" @click="showYearSelectionTo += 12">
                    <span class="px-4">...</span>
                </button>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import { Month, cropToPrecision, isSameDate } from '@/common/date';

interface DateItem {
    date: Date;
    disabled: boolean;
    previousMonth: boolean;
    nextMonth: boolean;
}

interface Props {
    readonly?: boolean;
    modelValue?: Date;
    highlightFrom?: Date;
    highlightTo?: Date;
}
type Emits = (e: 'update:modelValue', value: Date) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const datepicker = ref<HTMLDivElement | null>(null);

const view = ref<'day' | 'month' | 'year'>('day');
const year = ref<number>(new Date().getFullYear());
const month = ref<number>(new Date().getMonth());
const showYearSelectionFrom = ref<number>(new Date().getFullYear() - 90);
const showYearSelectionTo = ref<number>(new Date().getFullYear() + 30);

const days = computed<DateItem[]>(() => {
    return getItemsForDate(year.value, month.value);
});

const years = computed<number[]>(() => {
    const result: number[] = [];
    for (let i = showYearSelectionFrom.value; i < showYearSelectionTo.value; i++) {
        result.push(i);
    }
    return result;
});

function init(): void {
    onModelValueChanged(props.modelValue);
    watch(
        () => props.modelValue,
        (date) => onModelValueChanged(date)
    );
}

function onModelValueChanged(date: Date = cropToPrecision(new Date(), 'days')): void {
    year.value = date.getFullYear();
    month.value = date.getMonth();
}

function selectDate(item: DateItem): void {
    emit('update:modelValue', item.date);
}

async function toggleMonthSelection(): Promise<void> {
    view.value = view.value === 'month' ? 'day' : 'month';
    await nextTick();
    if (view.value === 'month' && datepicker.value) {
        const activeElement = datepicker.value.querySelector('.input-dropdown-option-focus');
        if (activeElement) {
            activeElement.scrollIntoView({ block: 'center' });
        }
    }
}

async function toggleYearSelection(): Promise<void> {
    view.value = view.value === 'year' ? 'day' : 'year';
    await nextTick();
    if (view.value === 'year' && datepicker.value) {
        const activeElement = datepicker.value.querySelector('.year.selected');
        if (activeElement) {
            activeElement.scrollIntoView({ block: 'center' });
        }
    }
}

function selectMonth(selectedMonth: number): void {
    month.value = selectedMonth;
    view.value = 'day';
}

function selectYear(selectedYear: number): void {
    year.value = selectedYear;
    view.value = 'day';
}

function previousMonth(): void {
    if (month.value === Month.JANUARY) {
        month.value = Month.DECEMBER;
        year.value--;
    } else {
        month.value--;
    }
}

function nextMonth(): void {
    if (month.value === Month.DECEMBER) {
        month.value = Month.JANUARY;
        year.value++;
    } else {
        month.value++;
    }
}

function min(a?: Date, b?: Date): Date | undefined {
    if (a === undefined || b === undefined) return a || b;
    if (a.getTime() < b.getTime()) return a;
    return b;
}

function max(a?: Date, b?: Date): Date | undefined {
    if (a === undefined || b === undefined) return a || b;
    if (a.getTime() > b.getTime()) return a;
    return b;
}

function isHighlighted(date: Date): boolean {
    const from = min(props.highlightFrom, props.highlightTo);
    const to = max(props.highlightFrom, props.highlightTo);
    if (!from || !to) {
        return false;
    }
    return isSameDate(date, from) || isSameDate(date, to) || (date.getTime() > from.getTime() && date.getTime() < to.getTime());
}

function getItemsForDate(year: number, month: number): DateItem[] {
    const days: DateItem[] = [];
    let day = new Date(year, month, 1);
    // go back to last monday
    day = new Date(day.getFullYear(), day.getMonth(), -day.getDay() + 2);
    const previousMonth = day.getMonth();
    while ([previousMonth, month].includes(day.getMonth())) {
        days.push({
            date: day,
            disabled: false,
            previousMonth: day.getMonth() !== month,
            nextMonth: false,
        });
        day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1);
    }
    // add more days to fill the week up
    while (day.getDay() !== 1) {
        days.push({
            date: day,
            disabled: false,
            previousMonth: false,
            nextMonth: true,
        });
        day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1);
    }
    return days;
}

init();
</script>
<style scoped>
@reference "tailwindcss"

.test {
    /* this class will be lost because of @reference "tailwindcss" */
}

.v-datepicker {
    background-color: inherit;
}
.label {
    text-align: center;
}

.day {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
}

.day button {
    border-radius: calc(infinity * 1px);
    border-width: 1px;
    border-color: transparent;
}

.day.previous button,
.day.next button {
    color: --alpha(var(--color-onsurface-variant) / 40%);
}

.day.start button,
.day.end button {
    background-color: var(--color-tertiary);
    color: var(--color-ontertiary);
}

.day.selected button {
    background-color: var(--color-primary);
    color: var(--color-onprimary);
}

.day.highlight::before {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: --alpha(var(--color-tertiary) / 10%);
}

.day.highlight.start::before {
    left: 50%;
}

.day.highlight.end::before {
    right: 50%;
}

.day.current button {
    border-color: var(--color-primary);
}

.header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.content {
    position: relative;
    background-color: inherit;
}

.month-selection,
.year-selection {
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    right: 0;
    background-color: inherit;
    overflow-y: auto;
}

.year {
    padding: 0.25rem;
    cursor: pointer;
    border-radius: calc(infinity * 1px);
    border-width: 1px;
    border-color: transparent;
    display: flex;
    align-items: center;
    justify-content: center;
}

.year:hover {
    background-color: --alpha(var(--color-onsurface-variant) / 10%);
}

.year.selected {
    background-color: var(--color-primary);
    color: var(--color-onprimary);
}

.year.current {
    border-color: var(--color-primary);
}

.btn-header {
    color: var(--color-onsurface-variant);
    border-radius: var(--radius-xl);
    @apply px-2 py-2;
    @apply font-bold;
    @apply gap-2;
}

.btn-header:not(:disabled):hover {
    background-color: --alpha(var(--color-onsurface-variant) / 10%);
}

.btn-header:disabled {
    opacity: 0.5;
    cursor: default;
}

.btn-header:disabled > i,
.btn-header:disabled > svg {
    opacity: 0;
}
</style>
