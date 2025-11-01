<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-flag"
        :label="$t('domain.user.nationality')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.nationality"> {{ nationalities.getName(props.modelValue.nationality) }} </span>
            <span v-else> {{ $t('generic.no-information') }} </span>
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
