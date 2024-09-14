<template>
    <div class="flex items-start" :class="$attrs.class">
        <label v-if="props.label" class="input-label">
            {{ props.label }}
        </label>
        <div class="relative w-1/2 flex-grow">
            <div class="input-field-wrapper cursor-pointer">
                <slot name="before"></slot>
                <input
                    :id="id"
                    :aria-disabled="props.disabled"
                    :aria-invalid="hasErrors"
                    :aria-required="props.required"
                    :class="{ invalid: showErrors && hasErrors }"
                    :disabled="props.disabled"
                    :placeholder="props.placeholder || $t('shared.please-select')"
                    :required="props.required"
                    :value="fileName"
                    aria-haspopup="true"
                    class="input-field w-full cursor-pointer overflow-ellipsis"
                    readonly
                />
                <div v-if="file" class="absolute bottom-0 right-0 top-0 z-20 flex w-12 items-center justify-center">
                    <button
                        class="h-10 w-10 rounded-full hover:bg-primary-300"
                        tabindex="-1"
                        @click.stop="clearSelection()"
                    >
                        <i class="fa-solid fa-file-circle-xmark text-primary-600" />
                    </button>
                </div>
                <div v-else>
                    <i class="fa-regular fa-file pr-4 text-primary-600" />
                </div>
                <input
                    class="absolute bottom-0 left-0 right-0 top-0 z-10 mr-12 cursor-pointer opacity-0"
                    :aria-disabled="props.disabled"
                    :disabled="props.disabled"
                    type="file"
                    @change="onInput($event as unknown as InputFileEvent)"
                />
                <slot name="after"></slot>
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
import type { Event, ValidationHint } from '@/domain';
import { v4 as uuidv4 } from 'uuid';

interface InputFileEvent extends Event {
    target: HTMLInputElement;
}

interface Props {
    // an optional label to render before the input field
    label?: string;
    // the value we edit, bind with v-model
    modelValue?: Blob;
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

interface Emits {
    (e: 'update:modelValue', value?: Blob): void;

    (e: 'submit', value?: Blob): void;
}

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

const fileName = ref<string>('');
const file = ref<Blob | null>(null);

function onInput(evt: InputFileEvent): void {
    const files = evt.target.files;
    if (!files || files.length > 1) {
        return;
    }
    file.value = files[0];
    const path = String(evt.target.value);
    fileName.value = path.substring(path.lastIndexOf('\\') + 1);
    emit('update:modelValue', file.value);
}

function clearSelection(): void {
    file.value = null;
    fileName.value = '';
    emit('update:modelValue', undefined);
}
</script>
