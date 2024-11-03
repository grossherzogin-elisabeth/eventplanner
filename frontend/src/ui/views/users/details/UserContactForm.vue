<template>
    <section v-if="user" class="-mx-4">
        <div class="mb-4">
            <VInputLabel>Email</VInputLabel>
            <VInputText v-model="user.email" required />
        </div>
        <div class="mb-4">
            <VInputLabel>Telefon</VInputLabel>
            <VInputText v-model="user.phone" />
        </div>
        <div class="mb-4">
            <VInputLabel>Mobil</VInputLabel>
            <VInputText v-model="user.mobile" />
        </div>
        <div class="mb-4 mt-16">
            <VInputLabel>Straße, Hausnr</VInputLabel>
            <VInputText v-model="user.address.addressLine1" required />
        </div>
        <div class="mb-4">
            <VInputLabel>Adresszusatz</VInputLabel>
            <VInputText v-model="user.address.addressLine2" />
        </div>
        <div class="flex space-x-4">
            <div class="mb-4 w-32">
                <VInputLabel>PLZ</VInputLabel>
                <VInputText v-model="user.address.zipcode" required />
            </div>
            <div class="mb-4 flex-grow">
                <VInputLabel>Ort</VInputLabel>
                <VInputText v-model="user.address.town" required />
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
