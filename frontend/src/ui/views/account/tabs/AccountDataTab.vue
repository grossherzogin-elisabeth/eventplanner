<template>
    <div class="grid gap-x-32 gap-y-8 md:grid-cols-1">
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">{{ $t('views.account.personal.title') }}</h2>
            <VInteractiveList>
                <NameCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <GenderCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <DateAndPlaceOfBirthCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <NationalityCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PassNumberCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
            </VInteractiveList>
        </div>
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">{{ $t('views.account.contact.title') }}</h2>
            <VInteractiveList>
                <EmailCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PhoneCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PhoneMobileCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <PhoneWorkCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <AddressCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
            </VInteractiveList>
        </div>
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">
                {{ $t('views.account.diet.kitchen-info') }}
            </h2>
            <VInteractiveList>
                <DietCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
                <IntolerancesCard :model-value="props.modelValue" @update:model-value="emit('update:modelValue', $event)" />
            </VInteractiveList>
        </div>
        <div class="col-start-1">
            <h2 class="text-secondary mb-2 flex items-center justify-between font-bold">{{ $t('views.account.emergency.title') }}</h2>
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
import AddressCard from '../components/account/AddressCard.vue';
import DateAndPlaceOfBirthCard from '../components/account/DateAndPlaceOfBirthCard.vue';
import DietCard from '../components/account/DietCard.vue';
import DiseasesCard from '../components/account/DiseasesCard.vue';
import EmailCard from '../components/account/EmailCard.vue';
import EmergencyContactCard from '../components/account/EmergencyContactCard.vue';
import GenderCard from '../components/account/GenderCard.vue';
import IntolerancesCard from '../components/account/IntolerancesCard.vue';
import MedicationCard from '../components/account/MedicationCard.vue';
import NameCard from '../components/account/NameCard.vue';
import NationalityCard from '../components/account/NationalityCard.vue';
import PassNumberCard from '../components/account/PassNumberCard.vue';
import PhoneCard from '../components/account/PhoneCard.vue';
import PhoneMobileCard from '../components/account/PhoneMobileCard.vue';
import PhoneWorkCard from '../components/account/PhoneWorkCard.vue';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
