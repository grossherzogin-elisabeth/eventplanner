<template>
    <div class="-mx-2 flex items-stretch sm:space-x-4">
        <div class="flex w-1/2 flex-col px-2">
            <p class="mb-2" @click="focusList('available')">Verfügbare</p>
            <div class="v-multi-selection-list flex flex-1 flex-col overflow-hidden border shadow-inner">
                <ul
                    ref="availableOptionsList"
                    aria-multiselectable="true"
                    class="flex-1 overflow-y-auto outline-none"
                    tabindex="0"
                    @blur="clearHighlight()"
                    @focus="highlightFirst('available')"
                    @keydown.up.prevent="highlightPrevious('available')"
                    @keydown.down.prevent="highlightNext('available')"
                    @keydown.enter="addHighlighted()"
                    @keydown.space.prevent="addHighlighted()"
                >
                    <li
                        v-for="(item, index) in availableOptions"
                        :key="`${index}-${item.label}`"
                        :class="{
                            active: highlightList === 'available' && highlightIndex === index,
                        }"
                        class="v-multi-selection-entry"
                        @click="selectOption(item.value)"
                    >
                        <span>{{ item.label }}</span>
                        <IconArrowDoubleRight class="v-multi-selection-icon-add" />
                    </li>
                </ul>
            </div>
        </div>

        <div class="flex w-1/2 flex-col px-2">
            <p class="mb-2" @click="focusList('selected')">Ausgewählte</p>
            <div class="v-multi-selection-list flex flex-1 flex-col overflow-hidden border shadow-inner">
                <ul
                    ref="selectedOptionsList"
                    class="flex-1 overflow-y-auto outline-none"
                    tabindex="0"
                    @blur="clearHighlight()"
                    @focus="highlightFirst('selected')"
                    @keydown.up.prevent="highlightPrevious('selected')"
                    @keydown.down.prevent="highlightNext('selected')"
                    @keydown.enter="removeHighlighted()"
                    @keydown.space.prevent="removeHighlighted()"
                >
                    <li
                        v-for="(item, index) in selectedOptions"
                        :key="`${index}-${item.label}`"
                        :class="{
                            active: highlightList === 'selected' && highlightIndex === index,
                        }"
                        class="v-multi-selection-entry"
                        @click="deselectOption(item.value)"
                    >
                        {{ item.label }}
                        <IconRemoveCircle class="v-multi-selection-icon-remove" />
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { InputSelectOption } from '@/domain';
import { IconArrowDoubleRight, IconRemoveCircle } from '@/ui/icons/bold';

interface Props<T> {
    options?: InputSelectOption<T>[];
    modelValue?: T[];
}

interface Emits {
    (e: 'update:modelValue', value: string[]): void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type T = any;

const props = defineProps<Props<T>>();
const emit = defineEmits<Emits>();
const highlightIndex = ref<number>(-1);
const highlightList = ref<'available' | 'selected' | undefined>(undefined);
const availableOptionsList = ref<HTMLUListElement | null>(null);
const selectedOptionsList = ref<HTMLUListElement | null>(null);

const selectedOptions = computed<InputSelectOption<T>[]>(() => {
    if (!props.modelValue || !props.options || props.modelValue.length === 0) {
        return [];
    }
    return props.options.filter((it) => props.modelValue?.includes(it.value));
});

const availableOptions = computed<InputSelectOption<T>[]>(() => {
    if (!props.options) {
        return [];
    }
    return props.options.filter((it) => !it.hidden).filter((it) => !props.modelValue?.includes(it.value));
});

function focusList(list: 'available' | 'selected'): void {
    if (list === 'available') {
        availableOptionsList.value?.focus();
    } else {
        selectedOptionsList.value?.focus();
    }
}

function clearHighlight(): void {
    highlightList.value = undefined;
    highlightIndex.value = -1;
}

function highlightFirst(list: 'available' | 'selected'): void {
    highlightList.value = list;
    highlightIndex.value = 0;
}

function highlightPrevious(list: 'available' | 'selected'): void {
    highlightList.value = list;
    const listMaxIndex = list === 'available' ? availableOptions.value.length - 1 : selectedOptions.value.length - 1;
    if (highlightIndex.value === 0) {
        highlightIndex.value = listMaxIndex;
    } else {
        highlightIndex.value = highlightIndex.value - 1;
    }
}

function highlightNext(list: 'available' | 'selected'): void {
    highlightList.value = list;
    const listMaxIndex = list === 'available' ? availableOptions.value.length - 1 : selectedOptions.value.length - 1;
    if (highlightIndex.value === listMaxIndex) {
        highlightIndex.value = 0;
    } else {
        highlightIndex.value = highlightIndex.value + 1;
    }
}

function addHighlighted(): void {
    if (availableOptions.value[highlightIndex.value]) {
        selectOption(availableOptions.value[highlightIndex.value].value);
    }
}

function removeHighlighted(): void {
    if (selectedOptions.value[highlightIndex.value]) {
        deselectOption(selectedOptions.value[highlightIndex.value].value);
    }
}

function selectOption(value: T): void {
    const selected = props.modelValue || [];
    selected.push(value);
    emit('update:modelValue', selected);
}

function deselectOption(value: T): void {
    let selected = props.modelValue || [];
    selected = selected.filter((it) => it !== value);
    emit('update:modelValue', selected);
}
</script>
