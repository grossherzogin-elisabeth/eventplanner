<template>
    <div class="v-input-switch flex items-center gap-8" :class="{ active: props.modelValue }">
        <div class="w-0 grow">
            <label :for="id" :class="{ invalid: showErrors && hasErrors }" class="block">
                {{ props.label }}
            </label>
            <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" class="-mx-4" />
        </div>
        <button
            :id="id"
            class="track"
            role="switch"
            :disabled="props.disabled"
            :aria-checked="props.modelValue"
            @click="toggle()"
            @keydown.space.prevent="toggle()"
        >
            <span class="handle skeleton:hidden"></span>
        </button>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import VInputHint from './VInputHint.vue';
import { v4 as uuidv4 } from 'uuid';

interface Props {
    label?: string;
    hint?: string;
    modelValue?: boolean;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
}

type Emits = (e: 'update:modelValue', value: boolean) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const id = uuidv4();
const visited = ref(false);
const showErrors = computed<boolean>(() => visited.value || props.errorsVisible === true);
const hasErrors = computed<boolean>(() => props.errors !== undefined && props.errors.length > 0);

function toggle(): void {
    visited.value = true;
    if (!props.disabled) {
        emit('update:modelValue', !props.modelValue);
    }
}
</script>
<style>
.v-input-switch .track {
    position: relative;
    display: flex;
    align-items: center;
    border: 1px solid var(--color-outline);
    background: var(--color-surface-container-highest);
    width: 2.5em;
    height: 1.5em;
    border-radius: 1em;
}

.v-input-switch .handle {
    display: inline-block;
    position: absolute;
    transition-property: left, width, height, background;
    transition-duration: 0.3s;
    transition-timing-function: ease;
    left: 0.25em;
    width: 0.75em;
    height: 0.75em;
    border-radius: 1em;
    background: var(--color-outline);
}

.v-input-switch.active .track {
    background: var(--color-primary);
}

.v-input-switch.active .handle {
    left: calc(100% - 1.125em);
    background: var(--color-onprimary);
    width: 1em;
    height: 1em;
}

.v-input-switch:focus-within .track {
    outline: 2px var(--color-primary) solid;
    outline-offset: 2px;
}

.v-input-switch.active:focus-within .track {
    border: 1px var(--color-primary) solid;
}

.v-input-switch .track:disabled {
    opacity: 0.12;
    cursor: default;
}

.v-input-switch .track:hover .handle {
    background: var(--color-onsurface-variant);
}

.v-input-switch.active .track:hover .handle {
    background: var(--color-primary-container);
}
</style>
