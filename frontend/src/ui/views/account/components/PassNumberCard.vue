<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-passport"
        :label="$t('views.account.data.personal.passport.number.label')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.passNr">{{ props.modelValue.passNr }}</span>
            <span v-else> {{ $t('generic.no-information') }} </span>
        </template>
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.data.personal.passport.number.hint') }}
            </p>
            <p class="mb-8 text-sm font-bold">
                {{ $t('views.account.data.personal.passport.hint') }}
            </p>
            <p class="mb-8 text-sm italic">
                <a :href="$t('views.account.data.personal.passport.number.link')" target="_blank" class="link">
                    {{ $t('views.account.data.personal.passport.number.hint-link') }}
                    <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
                </a>
            </p>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.data.personal.passport.number.label') }}</VInputLabel>
                <VInputText v-model="value.passNr" />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
