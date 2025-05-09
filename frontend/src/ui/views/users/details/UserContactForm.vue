<template>
    <section v-if="user">
        <h2 class="mb-4 font-bold text-secondary">Email & Telefon</h2>
        <div class="mb-4">
            <VInputLabel>Email</VInputLabel>
            <VInputText
                v-model.trim="user.email"
                required
                placeholder="keine Angabe"
                :errors="props.errors['email']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Telefon</VInputLabel>
            <VInputText v-model.trim="user.phone" placeholder="keine Angabe" :errors="props.errors['phone']" :errors-visible="true" />
        </div>
        <div class="mb-4">
            <VInputLabel>Telefon (dienstlich)</VInputLabel>
            <VInputText
                v-model.trim="user.phoneWork"
                placeholder="keine Angabe"
                :errors="props.errors['phoneWork']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Mobil</VInputLabel>
            <VInputText v-model.trim="user.mobile" placeholder="keine Angabe" :errors="props.errors['mobile']" :errors-visible="true" />
        </div>

        <h2 class="mb-4 mt-8 font-bold text-secondary">Addresse</h2>
        <div class="mb-4">
            <VInputLabel>Straße, Hausnr</VInputLabel>
            <VInputText
                v-model.trim="user.address.addressLine1"
                required
                placeholder="keine Angabe"
                :errors="props.errors['address.addressLine1']"
                :errors-visible="true"
            />
        </div>
        <div class="mb-4">
            <VInputLabel>Adresszusatz</VInputLabel>
            <VInputText
                v-model.trim="user.address.addressLine2"
                placeholder="keine Angabe"
                :errors="props.errors['address.addressLine2']"
                :errors-visible="true"
            />
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-36">
                <VInputLabel>PLZ</VInputLabel>
                <VInputText
                    v-model.trim="user.address.zipcode"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['address.zipcode']"
                    :errors-visible="true"
                />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputLabel>Ort</VInputLabel>
                <VInputText
                    v-model.trim="user.address.town"
                    required
                    placeholder="keine Angabe"
                    :errors="props.errors['address.town']"
                    :errors-visible="true"
                />
            </div>
        </div>
        <div class="mb-4">
            <VInputLabel>Land</VInputLabel>
            <VInputCombobox
                v-model="user.address.country"
                :options="countries.options"
                required
                placeholder="keine Angabe"
                :errors="props.errors['address.country']"
                :errors-visible="true"
            />
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails, ValidationHint } from '@/domain';
import { VInputCombobox, VInputLabel, VInputText } from '@/ui/components/common';
import { useCountries } from '@/ui/composables/Countries.ts';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, ValidationHint[]>;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

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
