<template>
    <div :class="$attrs.class" data-test-id="tabbar">
        <div class="scrollbar-invisible border-outline-variant/50 border-b">
            <MainContent>
                <div class="full-width-scrollable">
                    <div class="scrollbar-invisible flex gap-x-4 text-base font-semibold">
                        <slot name="before" />
                        <div class="flex gap-x-8">
                            <h2
                                v-for="tab in localizedTabs"
                                :key="tab.value"
                                class="tab"
                                :class="{ active: tab.value === props.modelValue && !showSearch }"
                            >
                                <button class="btn-tab" :data-test-id="`tab-${tab.value}`" @click="emit('update:modelValue', tab.value)">
                                    <slot name="tab" :tab="tab.value">
                                        {{ tab.label }}
                                    </slot>
                                </button>
                            </h2>
                        </div>
                        <slot name="after" />
                        <div class="grow"></div>
                        <slot name="end" />
                    </div>
                </div>
            </MainContent>
        </div>
    </div>
    <!-- tab pane -->
    <MainContent v-if="props.modelValue && $slots[props.modelValue]" class="flex-1 pt-4">
        <template v-for="tab in localizedTabs" :key="tab.value">
            <div v-show="tab.value === props.modelValue" class="h-full">
                <slot :name="tab.value" :active="tab.value === props.modelValue" />
            </div>
        </template>
    </MainContent>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { InputSelectOption } from '@/domain';
import { useQuery } from '@/ui/composables/QueryState';
import MainContent from '@/ui/components/partials/MainContent.vue';

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
@reference "tailwindcss";

.tab {
    white-space: nowrap;
    border-bottom-width: 1px;
    border-color: transparent;
    color: var(--color-onsurface-variant);
    @apply pb-1;
}

.tab button {
    background-color: transparent;
    font-size: var(--text-base);
    font-weight: var(--font-weight-bold);
    @apply -mx-4;
    border-radius: var(--radius-lg);
    @apply px-4;
    @apply py-2;
}

@media not all and (hover: none) {
    .tab button:hover {
        background-color: --alpha(var(--color-primary) / 10%);
        color: var(--color-primary);
    }
}

.tab.active {
    border-color: var(--color-primary);
    color: var(--color-primary);
}
</style>
