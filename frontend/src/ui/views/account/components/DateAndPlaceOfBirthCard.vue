<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-birthday-cake"
        :label="$t('views.account.data.personal.birth.label')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.dateOfBirth"> {{ $d(props.modelValue.dateOfBirth, DateTimeFormat.DD_MM_YYYY) }} </span>
            <span v-else> {{ $t('generic.no-information') }} </span>
            <span v-if="props.modelValue.placeOfBirth"> in {{ props.modelValue.placeOfBirth }} </span>
            <span v-else> in {{ $t('generic.no-information') }} </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.data.personal.hint') }}
            </p>
            <p class="mb-8 text-sm font-bold">{{ $t('views.account.data.personal.passport.hint')}}</p>
            <div class="mb-4">
                <VInputLabel>{{$t('views.account.data.personal.birth.day')}}</VInputLabel>
                <VInputDate v-model="value.dateOfBirth" disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>{{$t('views.account.data.personal.birth.place-of-birth')}}</VInputLabel>
                <VInputText v-model="value.placeOfBirth" disabled />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import { DateTimeFormat } from '@/common/date';
import type { UserDetails } from '@/domain';
import { VInputDate, VInputLabel, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
