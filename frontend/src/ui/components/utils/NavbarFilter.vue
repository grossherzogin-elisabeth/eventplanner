<template>
    <div class="h-full w-full lg:hidden">
        <div class="search" :class="showSearch ? 'open' : ''">
            <div class="wrapper flex grow items-stretch self-stretch rounded-lg">
                <button
                    class="mr-1 flex h-10 w-10 items-center justify-center"
                    :class="showSearch ? 'xs:ml-4' : 'btn-icon'"
                    @click="openSearch()"
                >
                    <i class="fa-solid fa-search"></i>
                </button>
                <input
                    ref="input"
                    name="search"
                    :value="props.modelValue"
                    :placeholder="placeholder || 'Einträge filtern'"
                    :disabled="!showSearch"
                    @input="onInput($event)"
                    @keydown.esc="input?.blur()"
                />
                <button class="btn-icon xs:mr-4" @click="cancel()">
                    <i class="fa-solid fa-xmark" />
                </button>
            </div>
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
@import 'tailwindcss';

.search {
    --animation-duration: 100ms;
    position: absolute;
    top: 0;
    bottom: 0;
    /* left side - btn-icon width (10) */
    left: calc(100vw - var(--spacing) * 10);
    width: 100vw;
    padding-block: calc(var(--spacing) * 1);
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
    display: flex;
    align-items: center;
}

.search.open {
    left: 0;
}

.search.open input {
    opacity: 1;
}

@media (min-width: 30rem) {
    .search {
        /* btn-icon width (10) + padding to side (4) */
        left: calc(100vw - (var(--spacing) * 14));
    }
}

@media (min-width: 48rem) {
    .search {
        /* btn-icon width (10) + padding to side (12) */
        left: calc(100vw - (var(--spacing) * 22));
    }
}

@media (min-width: 40rem) {
    .search {
        width: 25rem;
    }

    .search.open {
        left: calc(100vw - 25rem - var(--spacing) * 12);
    }

    .search.open .wrapper {
        background-color: --alpha(var(--color-onprimary) / 10%);
    }
}
</style>
