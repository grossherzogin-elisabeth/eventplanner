<template>
    <section v-if="user" class="">
        <h2 class="mb-4 font-bold text-secondary">Email & Telefon</h2>
        <div class="mb-4">
            <VInputLabel>Email</VInputLabel>
            <VInputText v-model="user.email" required placeholder="keine Angabe" />
        </div>
        <div class="mb-4">
            <VInputLabel>Telefon</VInputLabel>
            <VInputText v-model="user.phone" placeholder="keine Angabe" />
        </div>
        <div class="mb-4">
            <VInputLabel>Telefon (dienstlich)</VInputLabel>
            <VInputText v-model="user.phoneWork" placeholder="keine Angabe" />
        </div>
        <div class="mb-4">
            <VInputLabel>Mobil</VInputLabel>
            <VInputText v-model="user.mobile" placeholder="keine Angabe" />
        </div>

        <h2 class="mb-4 mt-8 font-bold text-secondary">Addresse</h2>
        <div class="mb-4">
            <VInputLabel>Straße, Hausnr</VInputLabel>
            <VInputText v-model="user.address.addressLine1" required placeholder="keine Angabe" />
        </div>
        <div class="mb-4">
            <VInputLabel>Adresszusatz</VInputLabel>
            <VInputText v-model="user.address.addressLine2" placeholder="keine Angabe" />
        </div>
        <div class="flex flex-col sm:flex-row sm:space-x-4">
            <div class="mb-4 sm:w-36">
                <VInputLabel>PLZ</VInputLabel>
                <VInputText v-model="user.address.zipcode" required placeholder="keine Angabe" />
            </div>
            <div class="mb-4 sm:flex-grow">
                <VInputLabel>Ort</VInputLabel>
                <VInputText v-model="user.address.town" required placeholder="keine Angabe" />
            </div>
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputText } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

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
