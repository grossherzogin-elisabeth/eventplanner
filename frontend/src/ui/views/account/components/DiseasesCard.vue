<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-file-medical-alt"
        :label="$t('views.account.emergency.diseases.label')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.diseases"> {{ $t('views.account.emergency.hint-show') }} </span>
            <span v-else>{{ $t('generic.no-information') }}</span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.emergency.diseases.hint') }}
            </p>
            <p class="mb-8 text-sm font-bold">
                {{ $t('views.account.emergency.hint-privacy') }}
            </p>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.emergency.diseases.label') }}</VInputLabel>
                <VInputTextArea v-model="value.diseases" />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputTextArea, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
