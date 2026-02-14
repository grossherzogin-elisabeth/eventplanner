<template>
    <section v-if="user">
        <h2 class="text-secondary mb-4 font-bold">Email & Telefon</h2>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.email"
                label="Email"
                required
                placeholder="keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :errors="props.errors['email']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.phone"
                label="Telefon"
                placeholder="keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :errors="props.errors['phone']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.phoneWork"
                label="Telefon (dienstlich)"
                placeholder="keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :errors="props.errors['phoneWork']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.mobile"
                label="Mobil"
                placeholder="keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :errors="props.errors['mobile']"
                :errors-visible="true"
            />
        </div>

        <h2 class="text-secondary mt-8 mb-4 font-bold">Addresse</h2>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.address.addressLine1"
                label="Straße, Hausnr"
                required
                placeholder="keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :errors="props.errors['address.addressLine1']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputText
                v-model.trim="user.address.addressLine2"
                label="Adresszusatz"
                placeholder="keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :errors="props.errors['address.addressLine2']"
                :errors-visible="true"
            />
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-36">
                <VInputText
                    v-model.trim="user.address.zipcode"
                    label="Postleitzahl"
                    required
                    placeholder="keine Angabe"
                    :disabled="!hasPermission(Permission.WRITE_USERS)"
                    :errors="props.errors['address.zipcode']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4 sm:grow">
                <VInputText
                    v-model.trim="user.address.town"
                    label="Ort"
                    required
                    placeholder="keine Angabe"
                    :disabled="!hasPermission(Permission.WRITE_USERS)"
                    :errors="props.errors['address.town']"
                    :errors-visible="true"
                />
            </div>
        </div>
        <div class="mb-4">
            <VInputCombobox
                v-model="user.address.country"
                label="Land"
                :options="countries.options"
                required
                placeholder="keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :errors="props.errors['address.country']"
                :errors-visible="true"
            />
        </div>
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
