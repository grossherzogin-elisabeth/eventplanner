<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-pills"
        :label="$t('views.account.emergency.medication.label')"
        :validate="UserService.validateMedication"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.medication">{{ $t('views.account.emergency.hint-show') }}</span>
            <span v-else>{{ $t('generic.no-information') }}</span>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.emergency.medication.hint') }}
            </p>
            <p class="mb-8 text-sm font-bold">
                {{ $t('views.account.emergency.hint-privacy') }}
            </p>
            <div class="mb-4">
                <VInputTextArea
                    v-model="value.medication"
                    :errors="errors['medication']"
                    :errors-visible="true"
                    :label="$t('views.account.emergency.medication.label')"
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { UserService } from '@/domain';
import { VInputTextArea, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
