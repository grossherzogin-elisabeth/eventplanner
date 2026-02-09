<template>
    <section>
        <h2 class="text-secondary mb-2 font-bold">
            {{ $t('components.event-registration-details-card.title') }}
        </h2>
        <VInteractiveList>
            <RegistrationPositionCard :model-value="props.registration" @update:model-value="saveRegistration($event)" />
            <RegistrationNoteCard :model-value="props.registration" @update:model-value="saveRegistration($event)" />
            <RegistrationOvernightStayCard
                v-if="props.event.days <= 1"
                :model-value="props.registration"
                @update:model-value="saveRegistration($event)"
            />
            <RegistrationArrivalDateCard :event="event" :model-value="props.registration" @update:model-value="saveRegistration($event)" />
        </VInteractiveList>
    </section>
</template>
<script setup lang="ts">
import { useEventUseCase } from '@/application';
import type { Event, Registration } from '@/domain';
import { VInteractiveList } from '@/ui/components/common';
import RegistrationArrivalDateCard from '@/ui/components/events/RegistrationArrivalDateCard.vue';
import RegistrationNoteCard from '@/ui/components/events/RegistrationNoteCard.vue';
import RegistrationOvernightStayCard from '@/ui/components/events/RegistrationOvernightStayCard.vue';
import RegistrationPositionCard from '@/ui/components/events/RegistrationPositionCard.vue';

interface Props {
    event: Event;
    registration: Registration;
}

type Emits = (e: 'update:event', event: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const eventUseCase = useEventUseCase();

async function saveRegistration(registration: Registration): Promise<void> {
    const updatedEvent = await eventUseCase.updateRegistration(props.event, registration);
    emit('update:event', updatedEvent);
}
</script>
