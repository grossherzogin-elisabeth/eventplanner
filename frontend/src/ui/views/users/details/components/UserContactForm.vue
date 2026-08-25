<template>
    <section class="relative mb-16 grid gap-4">
        <span id="contact-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">
            {{ $t('domain.user.email-and-phone') }}
        </span>
        <h2 class="text-secondary col-span-full font-bold">{{ $t('domain.user.email-and-phone') }}</h2>
        <VInputText
            v-model.trim="user.email"
            data-test-id="email"
            class="col-span-full"
            :label="$t('domain.user.email')"
            required
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['email']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.phone"
            data-test-id="phone"
            class="col-span-full"
            :label="$t('domain.user.phone')"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['phone']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.phoneWork"
            data-test-id="phone-work"
            class="col-span-full"
            :label="$t('domain.user.phone-work')"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['phoneWork']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.mobile"
            data-test-id="mobile"
            class="col-span-full"
            :label="$t('domain.user.mobile')"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['mobile']"
            :errors-visible="true"
        />
    </section>
    <section class="relative mb-16 grid gap-4 sm:grid-cols-4">
        <span id="address-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">
            {{ $t('domain.user.address') }}
        </span>
        <h2 class="text-secondary col-span-full font-bold">{{ $t('domain.user.address') }}</h2>
        <VInputText
            v-model.trim="user.address.addressLine1"
            data-test-id="address-line-1"
            class="col-span-full"
            :label="$t('domain.address.address-line-1')"
            required
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['address.addressLine1']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.address.addressLine2"
            data-test-id="address-line-2"
            class="col-span-full"
            :label="$t('domain.address.address-line-2')"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['address.addressLine2']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.address.zipcode"
            data-test-id="address-zipcode"
            class="col-span-full sm:col-span-1"
            :label="$t('domain.address.zipcode')"
            required
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['address.zipcode']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.address.town"
            data-test-id="address-town"
            class="col-span-full sm:col-span-3"
            :label="$t('domain.address.town')"
            required
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['address.town']"
            :errors-visible="true"
        />
        <VInputCombobox
            v-model="user.address.country"
            data-test-id="address-country"
            class="col-span-full"
            :label="$t('domain.address.country')"
            :options="countries.options"
            required
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :errors="props.errors['address.country']"
            :errors-visible="true"
        />
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { VInputCombobox, VInputText } from '@/ui/components/common';
import { useCountries } from '@/ui/composables/Countries';
import { useSession } from '@/ui/composables/Session';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, string[]>;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { hasPermission } = useSession();
const countries = useCountries();
const user = ref<UserDetails>(props.modelValue);

watch(props.modelValue, () => (user.value = props.modelValue));
watch(() => user.value.address, emitUpdate, { deep: true });
watch(() => user.value.email, emitUpdate);
watch(() => user.value.phone, emitUpdate);
watch(() => user.value.mobile, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
