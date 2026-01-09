<template>
    <ContextMenuButton anchor-align-x="left" dropdown-position-x="right" class="btn-toggle" :class="{ active: modelValue }">
        <template #icon>
            <span v-if="!modelValue">{{ placeholder }}</span>
            <span v-else class="block max-w-64 truncate">{{ resolve(modelValue) }}</span>
            <button
                v-if="modelValue"
                class="text-xs opacity-75 transition-colors duration-1000 hover:opacity-100"
                @click.stop="modelValue = undefined"
            >
                <i class="fa-solid fa-close" />
            </button>
            <span v-else class="text-xs">
                <i class="fa-solid fa-chevron-down" />
            </span>
        </template>
        <template #default>
            <ul>
                <li v-if="!modelValue" class="context-menu-item">
                    <i class="fa-solid fa-check"></i>
                    <span>{{ placeholder }}</span>
                </li>
                <li v-else class="context-menu-item" @click="modelValue = undefined">
                    <i class="w-4"></i>
                    <span>{{ placeholder }}</span>
                </li>
                <template v-for="option in props.options" :key="String(option)">
                    <li v-if="modelValue === option.value" class="context-menu-item" @click="modelValue = undefined">
                        <i class="fa-solid fa-check w-4"></i>
                        <span class="w-0 grow truncate">{{ option.label }}</span>
                    </li>
                    <li v-else class="context-menu-item" @click="modelValue = option.value">
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

const modelValue = defineModel<T | undefined>();

function resolve(value: T): string | undefined {
    return props.options.find((it) => it.value === value)?.label;
}
</script>
