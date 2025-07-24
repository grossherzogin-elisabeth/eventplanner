<template>
    <div class="grid gap-x-32 gap-y-8 md:grid-cols-2">
        <UserDetailsCard :model-value="props.modelValue" class="" @update:model-value="emit('update:modelValue', $event)" />
        <UserContactCard :model-value="props.modelValue" class="" @update:model-value="emit('update:modelValue', $event)" />
        <UserDietCard :model-value="props.modelValue" class="" @update:model-value="emit('update:modelValue', $event)" />
        <UserEmergencyInformationCard :model-value="props.modelValue" class="" @update:model-value="emit('update:modelValue', $event)" />
    </div>
</template>
<script lang="ts" setup>
import type { UserDetails } from '@/domain';
import UserContactCard from '@/ui/components/user/UserContactCard.vue';
import UserDetailsCard from '@/ui/components/user/UserDetailsCard.vue';
import UserDietCard from '@/ui/components/user/UserDietCard.vue';
import UserEmergencyInformationCard from '@/ui/components/user/UserEmergencyInformationCard.vue';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
