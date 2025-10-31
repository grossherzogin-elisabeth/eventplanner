<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-mobile"
        :label="$t('views.account.contact.mobile')"
        :validate="UserService.validateMobile"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.mobile">{{ props.modelValue.mobile }}</span>
            <span v-else class="italic">{{ $t('generic.no-information') }}</span>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.account.contact.hint-phone') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model="value.mobile"
                    :errors="errors['mobile']"
                    :errors-visible="true"
                    :label="$t('views.account.contact.mobile')"
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
