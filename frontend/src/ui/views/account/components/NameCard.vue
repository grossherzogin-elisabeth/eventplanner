<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-user-tag"
        :label="$t('domain.user.name')"
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
                {{ $t('views.account.personal.name-description') }}
            </p>
            <VInfo class="mb-8">
                {{ $t('views.account.personal.passport-info') }}
            </VInfo>
            <div class="mb-4">
                <VInputText
                    v-model="value.nickName"
                    :label="$t('domain.user.nick-name')"
                    :placeholder="value.firstName"
                    :hint="$t('views.account.personal.nick-name-hint')"
                    :errors="errors['nickName']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.title"
                    :label="$t('domain.user.title')"
                    :placeholder="$t('generic.no-information')"
                    disabled
                    :errors="errors['title']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.firstName"
                    :label="$t('domain.user.first-name')"
                    :hint="$t('views.account.personal.official-name-hint')"
                    required
                    disabled
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.secondName"
                    :label="$t('domain.user.middle-name')"
                    :placeholder="$t('generic.no-information')"
                    :hint="$t('views.account.personal.official-name-hint')"
                    disabled
                    :errors="errors['secondName']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model="value.lastName"
                    :label="$t('domain.user.last-name')"
                    :placeholder="$t('generic.no-information')"
                    :hint="$t('views.account.personal.official-name-hint')"
                    required
                    disabled
                    :errors="errors['lastName']"
                    :errors-visible="true"
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { UserDetails } from '@/domain';
import { UserService } from '@/domain';
import { VInfo, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
