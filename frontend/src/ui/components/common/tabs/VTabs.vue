<template>
    <div :class="$attrs.class">
        <h2
            class="scrollbar-invisible flex gap-x-4 overflow-x-auto border-b border-outline-variant px-4 text-base font-semibold xs:px-8 md:px-16 xl:px-20"
        >
            <slot name="before" />
            <div class="flex gap-x-8">
                <div
                    v-for="tab in localizedTabs"
                    :key="tab.value"
                    class="tab"
                    :class="{ active: tab.value === props.modelValue && !showSearch }"
                >
                    <button class="btn-tab" @click="emit('update:modelValue', tab.value)">
                        <slot name="tab" :tab="tab.value">
                            {{ tab.label }}
                        </slot>
                    </button>
                </div>
            </div>
            <slot name="after" />
            <span class="flex-grow"></span>
            <slot name="end" />
        </h2>
    </div>
    <!-- tab pane -->
    <div v-if="props.modelValue && $slots[props.modelValue]" class="flex-1 px-4 py-4 xs:px-8 md:px-16 md:py-8 xl:px-20">
        <template v-for="tab in localizedTabs" :key="tab.value">
            <div v-show="tab.value === props.modelValue" class="h-full">
                <slot :name="tab.value" :active="tab.value === props.modelValue" />
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { InputSelectOption } from '@/domain';
import { useQuery } from '@/ui/composables/QueryState';

interface Props {
    modelValue?: string;
    tabs?: string[] | InputSelectOption[];
}

type Emits = (e: 'update:modelValue', value: string) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { t } = useI18n();

const query = useQuery<string>('tab', props.modelValue || '');

const showSearch = ref<boolean>(false);
const localizedTabs = computed<InputSelectOption[]>(
    () => props.tabs?.map((it) => (typeof it === 'string' ? { value: it, label: t(it) } : it)) ?? []
);

function init(): void {
    watch(query.parameter, (v) => emit('update:modelValue', v));
    watch(
        () => props.modelValue,
        () => (query.parameter.value = props.modelValue || '')
    );
    emit('update:modelValue', query.parameter.value);
}

init();
</script>
<style>
.tab {
    @apply whitespace-nowrap border-b border-transparent pb-1;
}
.tab button {
    @apply text-base font-bold;
    @apply -mx-4 rounded-lg px-4 py-2;
    @apply bg-transparent;
}
.tab button:hover {
    @apply bg-secondary-container text-onsecondary-container;
}
.active.tab {
    @apply border-b border-primary text-primary;
}
</style>
