<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-passport"
        :label="$t('views.account.personal.passport.number.label')"
        :validate="UserService.validatePassNr"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.passNr">{{ props.modelValue.passNr }}</span>
            <span v-else> {{ $t('generic.no-information') }} </span>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.personal.passport.number.hint') }}
            </p>
            <p class="mb-8 text-sm font-bold">
                {{ $t('views.account.personal.passport.hint') }}
            </p>
            <p class="mb-8 text-sm italic">
                <a :href="$t('views.account.personal.passport.number.link')" target="_blank" class="link">
                    {{ $t('views.account.personal.passport.number.hint-link') }}
                    <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
                </a>
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.passNr"
                    :errors="errors['passNr']"
                    :errors-visible="true"
                    required
                    :label="$t('views.account.personal.passport.number.label')"
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { UserService } from '@/domain';
import { VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
