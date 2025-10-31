<template>
    <div class="v-input-file" :class="$attrs.class">
        <div class="input-field-wrapper">
            <slot name="before"></slot>
            <input
                :id="id"
                :aria-disabled="props.disabled"
                :aria-invalid="hasErrors"
                :aria-required="props.required"
                :class="{ invalid: showErrors && hasErrors }"
                :disabled="props.disabled"
                :placeholder="props.placeholder || $t('generic.please-select')"
                :required="props.required"
                :value="fileName"
                aria-haspopup="true"
                class="input-field w-full cursor-pointer overflow-ellipsis"
                readonly
            />
            <button v-if="file" class="h-10 w-10 rounded-full hover:bg-primary-container" tabindex="-1" @click.stop="clearSelection()">
                <i class="fa-solid fa-file-circle-xmark text-onprimary-container" />
            </button>
            <span v-else>
                <i class="fa-regular fa-file pr-4 text-primary" />
            </span>
            <input
                class="absolute bottom-0 left-0 right-0 top-0 z-10 mr-12 cursor-pointer opacity-0"
                :aria-disabled="props.disabled"
                :disabled="props.disabled"
                type="file"
                @change="onInput($event as unknown as InputFileEvent)"
            />
            <slot name="after"></slot>
        </div>
        <VInputHint :hint="props.hint" :errors="props.errors" :show-errors="showErrors" />
    </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import type { Event } from '@/domain';
import VInputHint from '@/ui/components/common/input/VInputHint.vue';
import { v4 as uuidv4 } from 'uuid';

interface InputFileEvent extends Event {
    target: HTMLInputElement;
}

interface Props {
    label?: string;
    hint?: string;
    modelValue?: Blob;
    disabled?: boolean;
    required?: boolean;
    errors?: string[];
    errorsVisible?: boolean;
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
