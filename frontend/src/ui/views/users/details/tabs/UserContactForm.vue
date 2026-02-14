<template>
    <section class="relative mb-16 grid gap-4">
        <span id="contact-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">Email & Telefon</span>
        <h2 class="text-secondary col-span-full font-bold">Email & Telefon</h2>
        <VInputText
            v-model.trim="user.email"
            class="col-span-full"
            label="Email"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['email']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.phone"
            class="col-span-full"
            label="Telefon"
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['phone']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.phoneWork"
            class="col-span-full"
            label="Telefon (dienstlich)"
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['phoneWork']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.mobile"
            class="col-span-full"
            label="Mobil"
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['mobile']"
            :errors-visible="true"
        />
    </section>
    <section class="relative mb-16 grid gap-4 sm:grid-cols-4">
        <span id="address-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">Addresse</span>
        <h2 class="text-secondary col-span-full font-bold">Addresse</h2>
        <VInputText
            v-model.trim="user.address.addressLine1"
            class="col-span-full"
            label="Straße, Hausnr"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['address.addressLine1']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.address.addressLine2"
            class="col-span-full"
            label="Adresszusatz"
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['address.addressLine2']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.address.zipcode"
            class="col-span-full sm:col-span-1"
            label="Postleitzahl"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['address.zipcode']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.address.town"
            class="col-span-full sm:col-span-3"
            label="Ort"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['address.town']"
            :errors-visible="true"
        />
        <VInputCombobox
            v-model="user.address.country"
            class="col-span-full"
            label="Land"
            :options="countries.options"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
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
import { useCountries } from '@/ui/composables/Countries.ts';
import { useSession } from '@/ui/composables/Session.ts';

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
