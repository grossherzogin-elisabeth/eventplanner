<template>
    <ContextMenuButton
        v-bind="$attrs"
        anchor-align-x="left"
        dropdown-position-x="right"
        class="btn-toggle flex items-center"
        :class="{ active: modelValue.length > 0 }"
    >
        <template #icon>
            <span v-if="modelValue.length === 0">{{ placeholder }}</span>
            <span v-else-if="modelValue.length > 4">{{ modelValue.length }} {{ placeholder }}</span>
            <span v-else class="block max-w-64 truncate">
                {{ modelValue.map((it) => resolve(it)).join(', ') }}
            </span>
            <button
                v-if="modelValue.length > 0"
                class="text-xs opacity-75 transition-colors duration-1000 hover:opacity-100"
                @click.stop="clear()"
            >
                <i class="fa-solid fa-close" />
            </button>
            <span v-else class="text-xs">
                <i class="fa-solid fa-chevron-down" />
            </span>
        </template>
        <template #default>
            <ul>
                <li v-if="modelValue.length === 0" class="context-menu-item">
                    <i class="fa-solid fa-check"></i>
                    <span>{{ placeholder }}</span>
                </li>
                <li v-else class="context-menu-item" @click="clear()">
                    <i class="w-4"></i>
                    <span>{{ placeholder }}</span>
                </li>
                <template v-for="option in props.options" :key="String(option.value)">
                    <li v-if="modelValue.includes(option.value)" class="context-menu-item" @click="remove(option.value)">
                        <i class="fa-solid fa-check w-4"></i>
                        <span class="w-0 grow truncate">{{ option.label }}</span>
                    </li>
                    <li v-else class="context-menu-item" @click="add(option.value)">
                        <i class="w-4"></i>
                        <span class="w-0 grow truncate">{{ option.label }}</span>
                    </li>
                </template>
            </ul>
        </template>
    </ContextMenuButton>
</template>
<script lang="ts" setup generic="T">
import type { InputSelectOption } from '@/domain';
import { ContextMenuButton } from '@/ui/components/common';

interface Props {
    placeholder: string;
    options: InputSelectOption<T>[];
}

const props = defineProps<Props>();

const modelValue = defineModel<T[]>({ required: true });

function add(value: T): void {
    modelValue.value.push(value);
}

function remove(value: T): void {
    modelValue.value = modelValue.value.filter((it) => it !== value);
}

function clear(): void {
    modelValue.value = [];
}

function resolve(value: T): string | undefined {
    return props.options.find((it) => it.value === value)?.label;
}
</script>
