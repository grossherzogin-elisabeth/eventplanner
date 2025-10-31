<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-wheat-awn-circle-exclamation"
        :label="$t('domain.user.intolerances')"
        :validate="UserService.validateIntolerances"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <span v-if="props.modelValue.intolerances" class="line-clamp-3">
                {{ props.modelValue.intolerances }}
            </span>
            <span v-else>{{ $t('generic.no-information') }}</span>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">{{ $t('views.account.diet.intolerances-description') }}</p>
            <div class="mb-4">
                <VInputTextArea
                    v-model="value.intolerances"
                    :errors="errors['intolerances']"
                    :errors-visible="true"
                    :max-length="1000"
                    :label="$t('domain.user.intolerances')"
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
