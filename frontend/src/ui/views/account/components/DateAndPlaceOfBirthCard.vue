<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-birthday-cake"
        :label="$t('domain.user.date-and-place-of-birth')"
        :validate="UserService.validateDateAndPlaceOfBirth"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.dateOfBirth"> {{ $d(props.modelValue.dateOfBirth, DateTimeFormat.DD_MM_YYYY) }} </span>
            <span v-else> {{ $t('generic.no-information') }} </span>
            <span v-if="props.modelValue.placeOfBirth"> in {{ props.modelValue.placeOfBirth }} </span>
            <span v-else> in {{ $t('generic.no-information') }} </span>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.personal.date-and-place-of-birth-description') }}
            </p>
            <p class="mb-8 text-sm font-bold">{{ $t('views.account.personal.passport-info') }}</p>
            <div class="mb-4">
                <VInputDate
                    v-model="value.dateOfBirth"
                    :label="$t('domain.user.date-of-birth')"
                    :errors="errors['dateOfBirth']"
                    :errors-visible="true"
                    disabled
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.placeOfBirth"
                    :label="$t('domain.user.place-of-birth')"
                    :errors="errors['placeOfBirth']"
                    :errors-visible="true"
                    disabled
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { DateTimeFormat } from '@/common/date';
import type { UserDetails } from '@/domain';
import { UserService } from '@/domain';
import { VInputDate, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
