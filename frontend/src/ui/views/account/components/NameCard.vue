<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-user-tag"
        :label="$t('views.account.personal.name')"
        :validate="UserService.validateName"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p v-if="props.modelValue.nickName">
                {{ props.modelValue.title }}
                {{ props.modelValue.nickName }}
                {{ props.modelValue.lastName }}
            </p>
            <p v-else>
                {{ props.modelValue.title }}
                {{ props.modelValue.firstName }}
                {{ props.modelValue.secondName }}
                {{ props.modelValue.lastName }}
            </p>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.personal.hint') }}
            </p>
            <p class="mb-4 text-sm">
                {{ $t('views.account.personal.display-name.hint') }}
            </p>
            <p class="mb-8 text-sm font-bold">{{ $t('views.account.personal.passport.hint') }}</p>
            <div class="mb-4">
                <VInputText
                    v-model="value.nickName"
                    :label="$t('views.account.personal.display-name.label')"
                    :errors="errors['nickName']"
                    :errors-visible="true"
                    :placeholder="value.firstName"
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.title"
                    :label="$t('views.account.personal.degree')"
                    :placeholder="$t('generic.no-information')"
                    :errors="errors['title']"
                    :errors-visible="true"
                    disabled
                />
            </div>
            <div class="mb-4">
                <VInputText v-model="value.firstName" :label="$t('views.account.personal.first-name')" required disabled />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.secondName"
                    :label="$t('views.account.personal.middle-name')"
                    :placeholder="$t('generic.no-information')"
                    :errors="errors['secondName']"
                    :errors-visible="true"
                    disabled
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.lastName"
                    :label="$t('views.account.personal.last-name')"
                    required
                    :placeholder="$t('generic.no-information')"
                    :errors="errors['lastName']"
                    :errors-visible="true"
                    disabled
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
