<template>
    <label ref="label" :for="id" class="input-label flex items-center space-x-2" :class="$attrs.class + (required ? ' required' : '')">
        <span>
            <slot>
                {{ props.modelValue }}
            </slot>
        </span>
    </label>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';

interface Props {
    // the label text
    modelValue?: string;
    // related input field
    for?: HTMLElement;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 *
 * Label that automatically finds the next input to set the `for` attribute. Both the input and the label must have the
 * same parent component.
 * --------------------------------------------------------------------------------------------------------
 */
const props = defineProps<Props>();

const label = ref<HTMLElement | null>(null);
const input = ref<HTMLInputElement | null>(null);
const id = computed<string | undefined>(() => (input.value ? input.value.id : undefined));
const required = computed<boolean>(() => (input.value ? input.value.required : false));

onMounted(() => {
    findAssociatedInput();
    watch(() => props.for, findAssociatedInput);
});

function findAssociatedInput(): void {
    const forElement: HTMLElement | null = props.for || null;
    const siblingElement: HTMLElement | null = (label.value?.nextElementSibling as HTMLElement | null) || null;
    const parentElement: HTMLElement | null = (label.value?.parentElement as HTMLElement | null) || null;
    const inputElement =
        findFirstInputElementOfParent(forElement) ||
        findFirstInputElementOfParent(siblingElement) ||
        findFirstInputElementOfParent(parentElement);
    if (inputElement) {
        input.value = inputElement;
    } else {
        console.warn('Could not find associated input element! Maybe you need to specify an explicit `for` element.');
        input.value = null;
    }
}

function findFirstInputElementOfParent(parent: HTMLElement | null): HTMLInputElement | null {
    if (parent instanceof HTMLElement) {
        const input = parent.querySelector('input, textarea') as HTMLInputElement | null;
        if (input) {
            return input;
        }
    }
    return null;
}
</script>
