<template>
    <VInteractiveListItem
        :model-value="props.modelValue"
        icon="fa-envelope"
        :label="$t('views.settings.notifications.email')"
        @update:model-value="emit('update:modelValue', $event)"
    >
        <template #default>
            <p class="truncate">
                {{ props.modelValue.email.from }}
            </p>
        </template>
        <template #edit="{ value, errors }">
            <p class="mb-8 text-sm">
                {{ $t('views.settings.notifications.email-description') }}
            </p>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.email.from"
                    data-test-id="input-from"
                    label="From"
                    :errors="errors['from']"
                    :errors-visible="true"
                    required
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.email.fromDisplayName"
                    data-test-id="input-from-display-name"
                    label="From Anzeigename"
                    :errors="errors['fromDisplayName']"
                    :errors-visible="true"
                    required
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.email.replyTo"
                    data-test-id="input-reply-to"
                    label="Reply to"
                    :errors="errors['replyTo']"
                    :errors-visible="true"
                    required
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.email.replyToDisplayName"
                    data-test-id="input-reply-to-display-name"
                    label="Reply to Anzeigename"
                    :errors="errors['replyToDisplayName']"
                    :errors-visible="true"
                    required
                />
            </div>
            <div class="mt-12 mb-4">
                <VInputText
                    v-model.trim="value.email.host"
                    data-test-id="input-host"
                    label="Hostname"
                    :errors="errors['host']"
                    :errors-visible="true"
                    required
                />
            </div>
            <div class="mb-4">
                <VInputNumber
                    v-model="value.email.port"
                    data-test-id="input-port"
                    label="Port"
                    :errors="errors['port']"
                    :errors-visible="true"
                    required
                />
            </div>
            <div class="mb-4">
                <VInputCheckBox
                    v-model="value.email.enableStartTls"
                    data-test-id="input-enable-start-tls"
                    :errors="errors['enableStartTls']"
                    :errors-visible="true"
                    label="Start TLS"
                />
            </div>
            <div class="mb-4">
                <VInputCheckBox
                    v-model="value.email.enableSSL"
                    data-test-id="input-enable-ssl"
                    :errors="errors['enableSSL']"
                    :errors-visible="true"
                    label="SSL"
                />
            </div>
            <div class="mt-12 mb-4">
                <VInputText label="Authentifizierungsmethode" model-value="Benutzername / Passwort" required disabled />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.email.username"
                    data-test-id="input-username"
                    label="Benutzername"
                    :errors="errors['username']"
                    :errors-visible="true"
                    required
                />
            </div>
            <div class="mb-4">
                <VInputText
                    v-model.trim="value.email.password"
                    data-test-id="input-password"
                    label="Password"
                    type="password"
                    placeholder="****************"
                    :errors="errors['password']"
                    :errors-visible="true"
                    required
                />
            </div>
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { AppSettings } from '@/domain';
import { VInputCheckBox, VInputNumber, VInputText, VInteractiveListItem } from '@/ui/components/common';

interface Props {
    modelValue: AppSettings;
}

type Emits = (e: 'update:modelValue', user: AppSettings) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
