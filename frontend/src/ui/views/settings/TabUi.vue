<template>
    <div class="grid gap-x-32 gap-y-8 md:grid-cols-1">
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">{{ $t('views.settings.ui.title') }}</h2>
            <VInteractiveList>
                <TabTitleCard
                    :model-value="props.modelValue"
                    data-test-id="tab-title"
                    @update:model-value="emit('update:modelValue', $event)"
                />
                <MenuTitleCard
                    :model-value="props.modelValue"
                    data-test-id="menu-title"
                    @update:model-value="emit('update:modelValue', $event)"
                />
                <ThemeColorCard
                    :model-value="props.modelValue"
                    data-test-id="theme-color"
                    @update:model-value="emit('update:modelValue', $event)"
                />
                <SupportEmailCard
                    :model-value="props.modelValue"
                    data-test-id="support-email"
                    @update:model-value="emit('update:modelValue', $event)"
                />
                <TechSupportEmailCard
                    :model-value="props.modelValue"
                    data-test-id="tech-support-email"
                    @update:model-value="emit('update:modelValue', $event)"
                />
            </VInteractiveList>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { AppSettings } from '@/domain';
import { VInteractiveList } from '@/ui/components/common';
import MenuTitleCard from './components/MenuTitleCard.vue';
import SupportEmailCard from './components/SupportEmailCard.vue';
import TabTitleCard from './components/TabTitleCard.vue';
import TechSupportEmailCard from './components/TechSupportEmailCard.vue';
import ThemeColorCard from './components/ThemeColorCard.vue';

interface Props {
    modelValue: AppSettings;
}

type Emits = (e: 'update:modelValue', user: AppSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
