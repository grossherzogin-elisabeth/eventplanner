<template>
    <div class="grid gap-x-32 gap-y-8 md:grid-cols-1">
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">{{ $t('views.account.app-settings.title') }}</h2>
            <VInteractiveList>
                <ThemeCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <ThemeColorCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <ThemeContrastCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PreferredPositionCard
                    :model-value="props.modelValue"
                    :available-positions="user.positionKeys"
                    @update:model-value="emit('update:modelValue', $event)"
                />
            </VInteractiveList>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { UserDetails, UserSettings } from '@/domain';
import PreferredPositionCard from '@/ui/views/account/components/PreferredPositionCard.vue';
import ThemeCard from '@/ui/views/account/components/ThemeCard.vue';
import ThemeColorCard from '@/ui/views/account/components/ThemeColorCard.vue';
import ThemeContrastCard from '@/ui/views/account/components/ThemeContrastCard.vue';
import VInteractiveList from '@/ui/components/common/list/VInteractiveList.vue';

interface Props {
    modelValue: UserSettings;
    user: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
