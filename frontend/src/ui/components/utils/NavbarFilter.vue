<template>
    <div class="search xs:pr-6 md:pr-14" :class="showSearch ? 'open' : ''">
        <div class="wrapper flex grow items-stretch self-stretch rounded-lg">
            <button :class="showSearch ? 'w-11 px-4' : 'btn-icon'" @click="openSearch()">
                <i class="fa-solid fa-search"></i>
            </button>
            <input
                ref="input"
                name="search"
                :value="props.modelValue"
                :placeholder="placeholder || 'Einträge filtern'"
                :disabled="!showSearch"
                @input="onInput($event)"
            />
            <button class="px-4" @click="cancel()">
                <i class="fa-solid fa-xmark" />
            </button>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { wait } from '@/common';

interface Props {
    modelValue?: string;
    placeholder?: string;
}

type Emits = (e: 'update:modelValue', value: string) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const showSearch = ref<boolean>(false);
const input = ref<HTMLInputElement | null>(null);

async function openSearch(): Promise<void> {
    showSearch.value = true;
    await wait(200);
    input.value?.focus();
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onInput(event: any): void {
    emit('update:modelValue', event.target?.value);
}

function cancel(): void {
    if (props.modelValue) {
        emit('update:modelValue', '');
    } else {
        showSearch.value = false;
    }
}
</script>
<style scoped>
.search {
    --animation-duration: 100ms;
    position: absolute;
    top: 0;
    bottom: 0;
    left: calc(100vw - 3.25rem);
    width: 100vw;
    display: flex;
    align-items: center;
    transition-property: left, top;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: var(--animation-duration);
    background-color: var(--color-primary);
}

html.dark .search {
    background-color: var(--color-surface-container);
}

.search input {
    flex-grow: 1;
    opacity: 0;
    background-color: transparent;
    transition-property: opacity;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: var(--animation-duration);
}

.search input::placeholder {
    color: var(--color-onprimary);
    opacity: 0.3;
}

html.dark .search input::placeholder {
    color: var(--color-onsurface);
    opacity: 0.3;
}

.search .wrapper {
    transition-property: background-color;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: var(--animation-duration);
}

.search.open {
    left: 0;
}

.search.open input {
    opacity: 1;
}

@media (min-width: 400px) {
    .search {
        left: calc(100vw - 4.25rem);
    }
}

@media (min-width: 786px) {
    .search {
        left: calc(100vw - 6.25rem);
    }
}

@media (min-width: 40rem) {
    .search {
        width: 25rem;
    }

    .search.open {
        left: calc(100vw - 25rem);
    }

    .search.open .wrapper {
        background-color: --alpha(var(--color-onprimary) / 10%);
    }
}
</style>
