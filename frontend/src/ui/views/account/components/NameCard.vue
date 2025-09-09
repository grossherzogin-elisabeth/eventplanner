<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-user-tag"
        :label="$t('views.account.data.personal.name')"
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
        <template #edit="{ value }">
            <p class="mb-4 text-sm">
                {{ $t('views.account.data.personal.hint') }}
            </p>
            <p class="mb-4 text-sm">
                {{ $t('views.account.data.personal.display-name.hint') }}
            </p>
            <p class="mb-8 text-sm font-bold">{{ $t('views.account.data.personal.passport.hint') }}</p>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.data.personal.display-name.label') }}</VInputLabel>
                <VInputText v-model="value.nickName" :placeholder="value.firstName" />
            </div>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.data.personal.degree') }}</VInputLabel>
                <VInputText v-model="value.title" :placeholder="$t('generic.no-information')" disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.data.personal.first-name') }}</VInputLabel>
                <VInputText v-model="value.firstName" required disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.data.personal.middle-name') }}</VInputLabel>
                <VInputText v-model="value.secondName" :placeholder="$t('generic.no-information')" disabled />
            </div>
            <div class="mb-4">
                <VInputLabel>{{ $t('views.account.data.personal.last-name') }}</VInputLabel>
                <VInputText v-model="value.lastName" required :placeholder="$t('generic.no-information')" disabled />
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
