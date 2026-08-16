<template>
    <div
        v-if="props.event.assignedUserCount === 0 && waitingList.length === 0"
        class="bg-surface-container/50 -mx-4 rounded-2xl px-4 md:-mx-4 md:-mt-4 md:bg-transparent"
        data-test-id="registrations-placeholder"
    >
        <div class="flex items-center py-4">
            <div class="mr-4">
                <h3 class="mb-4 text-base">
                    <i class="fa-solid fa-trophy opacity-75"></i>
                    <span v-if="signedInUser?.gender === 'm'" class="ml-4">
                        {{ $t('components.event-participants-card.placeholder-title-m') }}
                    </span>
                    <span v-else-if="signedInUser?.gender === 'f'" class="ml-4">
                        {{ $t('components.event-participants-card.placeholder-title-f') }}
                    </span>
                    <span v-else class="ml-4">
                        {{ $t('components.event-participants-card.placeholder-title') }}
                    </span>
                </h3>
                <p class="text-sm">
                    {{ $t('components.event-participants-card.placeholder') }}
                    <template v-if="event.signupType === EventSignupType.Assignment">
                        {{ $t('components.event-participants-card.placeholder-waiting-list-hint') }}
                    </template>
                </p>
            </div>
        </div>
    </div>
    <div v-else-if="hasHiddenCrewAssignments">
        <section>
            <h2 class="text-secondary mb-2 flex space-x-4 font-bold md:mb-6 md:ml-0">
                {{ $t('domain.event.registrations') }} ({{ waitingList.length }})
            </h2>
            <div class="bg-surface-container/50 -mx-4 rounded-2xl p-4 md:mx-0 md:rounded-none md:bg-transparent md:p-0">
                <ul class="space-y-2" data-test-id="registration-list">
                    <RegistrationRow v-for="it in waitingList" :key="it.slot?.key || ''" :registration="it" />
                </ul>
            </div>
        </section>
    </div>
    <div v-else>
        <section>
            <h2 class="text-secondary mb-2 flex space-x-4 font-bold md:mb-6 md:ml-0">{{ $t('domain.event.crew') }} ({{ crew.length }})</h2>
            <div class="bg-surface-container/50 -mx-4 rounded-2xl p-4 md:mx-0 md:rounded-none md:bg-transparent md:p-0">
                <ul class="space-y-2" data-test-id="crew-list">
                    <RegistrationRow v-for="it in crew" :key="it.slot?.key || ''" :registration="it" />
                </ul>
            </div>
        </section>
        <section>
            <h2 class="text-secondary mt-8 mb-2 flex space-x-4 font-bold md:mb-6 md:ml-0">
                {{ $t('domain.event.waiting-list') }} ({{ waitingList.length }})
            </h2>
            <div class="bg-surface-container/50 -mx-4 rounded-2xl p-4 md:mx-0 md:rounded-none md:bg-transparent md:p-0">
                <ul class="space-y-2" data-test-id="waiting-list">
                    <RegistrationRow v-for="(it, index) in waitingList" :key="index" :registration="it" />
                </ul>
                <div v-if="waitingList.length === 0" class="text-sm">
                    <p v-if="hasHiddenCrewAssignments">
                        {{ $t('components.event-participants-card.no-registrations') }}
                    </p>
                    <p v-else>
                        {{ $t('components.event-participants-card.no-waitinglist') }}
                    </p>
                </div>
            </div>
        </section>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useEventUseCase } from '@/application';
import type { Event, ResolvedRegistrationSlot } from '@/domain';
import { EventSignupType, EventState } from '@/domain';
import RegistrationRow from '@/ui/components/events/RegistrationRow.vue';
import { useSession } from '@/ui/composables/Session.ts';

interface Props {
    event: Event;
}

const props = defineProps<Props>();

const eventUseCase = useEventUseCase();
const { signedInUser } = useSession();

const waitingList = ref<ResolvedRegistrationSlot[]>([]);
const crew = ref<ResolvedRegistrationSlot[]>([]);

const hasHiddenCrewAssignments = computed<boolean>(() => {
    return (
        props.event.signupType !== EventSignupType.Assignment || [EventState.OpenForSignup, EventState.Draft].includes(props.event.state)
    );
});

function init(): void {
    fetchTeam(props.event);
    watch(() => props.event, fetchTeam, { deep: true });
}

async function fetchTeam(event: Event): Promise<void> {
    const registrations = await eventUseCase.resolveRegistrations(event);
    crew.value = eventUseCase.filterForCrew(event, registrations);
    waitingList.value = eventUseCase.filterForWaitingList(event, registrations);
}

init();
</script>
