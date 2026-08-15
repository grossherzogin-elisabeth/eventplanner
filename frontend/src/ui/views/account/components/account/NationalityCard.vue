<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-flag"
        :label="$t('domain.user.nationality')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.nationality" class="truncate">
                {{ nationalities.getName(props.modelValue.nationality) }}
            </p>
            <p v-else class="truncate italic">
                {{ $t('generic.no-information') }}
            </p>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.personal.nationality-description') }}
            </p>
            <VInfo class="mb-8">
                {{ $t('views.account.personal.passport-info') }}
            </VInfo>
            <VInputSelectionList v-model="value.nationality" :options="nationalities.options" />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInfo, VInputSelectionList, VInteractiveListItem } from '@/ui/components/common';
import { useNationalities } from '@/ui/composables/Nationalities';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const nationalities = useNationalities();
</script>
