<template>
    <div class="grid gap-x-32 gap-y-8 md:grid-cols-1">
        <div class="col-start-1">
            <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">{{ $t('views.account.personal.title') }}</h2>
            <VInteractiveList>
                <NameCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <GenderCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <DateAndPlaceOfBirthCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <NationalityCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PassNumberCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
            </VInteractiveList>
        </div>
        <div class="col-start-1">
            <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">{{ $t('views.account.contact.title') }}</h2>
            <VInteractiveList>
                <EmailCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PhoneCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PhoneMobileCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PhoneWorkCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <AddressCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
            </VInteractiveList>
        </div>
        <div class="col-start-1">
            <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">
                {{ $t('views.account.diet.kitchen-info') }}
            </h2>
            <VInteractiveList>
                <DietCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <IntolerancesCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
            </VInteractiveList>
        </div>
        <div class="col-start-1">
            <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">{{ $t('views.account.emergency.title') }}</h2>
            <VInteractiveList>
                <EmergencyContactCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <MedicationCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <DiseasesCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
            </VInteractiveList>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { UserDetails } from '@/domain';
import VInteractiveList from '@/ui/components/common/list/VInteractiveList.vue';
import AddressCard from '@/ui/views/account/components/AddressCard.vue';
import DateAndPlaceOfBirthCard from '@/ui/views/account/components/DateAndPlaceOfBirthCard.vue';
import DietCard from '@/ui/views/account/components/DietCard.vue';
import DiseasesCard from '@/ui/views/account/components/DiseasesCard.vue';
import EmailCard from '@/ui/views/account/components/EmailCard.vue';
import EmergencyContactCard from '@/ui/views/account/components/EmergencyContactCard.vue';
import GenderCard from '@/ui/views/account/components/GenderCard.vue';
import IntolerancesCard from '@/ui/views/account/components/IntolerancesCard.vue';
import MedicationCard from '@/ui/views/account/components/MedicationCard.vue';
import NameCard from '@/ui/views/account/components/NameCard.vue';
import NationalityCard from '@/ui/views/account/components/NationalityCard.vue';
import PassNumberCard from '@/ui/views/account/components/PassNumberCard.vue';
import PhoneCard from '@/ui/views/account/components/PhoneCard.vue';
import PhoneMobileCard from '@/ui/views/account/components/PhoneMobileCard.vue';
import PhoneWorkCard from '@/ui/views/account/components/PhoneWorkCard.vue';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
