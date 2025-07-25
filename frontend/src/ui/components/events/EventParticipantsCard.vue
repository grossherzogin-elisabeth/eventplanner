<template>
    <section>
        <h2 v-if="hasHiddenCrewAssignments" class="mb-2 flex space-x-4 font-bold text-secondary md:mb-6 md:ml-0">
            <span>{{ $t('components.event-participants-card.registrations') }}</span>
        </h2>
        <h2 v-else class="mb-2 flex space-x-4 font-bold text-secondary md:mb-6 md:ml-0">
            <button class="hover:text-primary" :class="{ 'text-primary underline': tab === Tab.Team }" @click="tab = Tab.Team">
                {{ $t('components.event-participants-card.assigned', { count: props.event.assignedUserCount }) }}
            </button>
            <button
                class="hover:text-primary"
                :class="{ 'text-primary underline': tab === Tab.WaitingList }"
                @click="tab = Tab.WaitingList"
            >
                {{ $t('components.event-participants-card.waitinglist', { count: waitingList.length }) }}
            </button>
        </h2>
        <div
            v-if="props.event.assignedUserCount === 0 && waitingList.length === 0"
            class="rounded-2xl bg-surface-container bg-opacity-50 px-4 shadow xs:-mx-4 md:-mx-4 md:-mt-4 md:bg-transparent md:shadow-none"
        >
            <div class="flex items-center py-4">
                <div class="mr-4">
                    <h3 class="mb-4 text-base">
                        <i class="fa-solid fa-trophy opacity-75"></i>
                        <span v-if="['m', 'f'].includes(signedInUser.gender ?? '')" class="ml-4">
                            {{ $t(`components.event-participants-card.placeholder-title-${signedInUser.gender}`) }}
                        </span>
                        <span v-else class="ml-4">
                            {{ $t('components.event-participants-card.placeholder-title') }}
                        </span>
                    </h3>
                    <p class="text-sm">
                        {{ $t('components.event-participants-card.placeholder') }}
                    </p>
                </div>
            </div>
        </div>
        <div
            v-else
            class="rounded-2xl bg-surface-container bg-opacity-50 p-4 shadow xs:-mx-4 md:mx-0 md:rounded-none md:bg-transparent md:p-0 md:shadow-none"
        >
            <template v-if="tab === Tab.Team">
                <ul class="space-y-2">
                    <template v-for="it in team" :key="it.slot?.key || ''">
                        <li class="flex items-center space-x-4">
                            <i v-if="it.name" class="fa-solid fa-user-circle text-secondary" />
                            <i v-else class="fa-solid fa-user-circle text-error" />
                            <RouterLink
                                v-if="it.user && signedInUser.permissions.includes(Permission.READ_USER_DETAILS)"
                                :to="{ name: Routes.UserDetails, params: { key: it.user.key } }"
                                class="truncate hover:text-primary hover:underline"
                            >
                                {{ it.name }}
                            </RouterLink>
                            <span v-else-if="it.name" class="truncate">{{ it.name }}</span>
                            <span v-else-if="it.user?.key" class="italic text-error">
                                {{ $t('components.event-participants-card.unknown') }}
                            </span>
                            <span v-else class="truncate italic text-error text-opacity-60">
                                {{ $t('components.event-participants-card.empty') }}
                            </span>
                            <span class="flex-grow"></span>
                            <span
                                :style="{ background: it.position.color }"
                                class="position ml-auto text-xs"
                                :class="{ 'opacity-50': !it.registration }"
                            >
                                {{ it.position.name }}
                            </span>
                        </li>
                    </template>
                </ul>
            </template>
            <template v-else-if="tab === Tab.WaitingList">
                <ul class="space-y-2">
                    <li v-for="(it, index) in waitingList" :key="index" class="flex items-center justify-between space-x-4">
                        <i class="fa-solid fa-user-circle text-secondary" />
                        <RouterLink
                            v-if="it.user && signedInUser.permissions.includes(Permission.READ_USER_DETAILS)"
                            :to="{ name: Routes.UserDetails, params: { key: it.user.key } }"
                            class="flex-grow truncate hover:text-primary hover:underline"
                        >
                            {{ it.name }}
                        </RouterLink>
                        <span v-else-if="it.name" class="flex-grow truncate">{{ it.name }}</span>
                        <span :style="{ background: it.position.color }" class="position text-xs">
                            {{ it.position.name }}
                        </span>
                    </li>
                </ul>
                <div v-if="waitingList.length === 0" class="-mx-4 -mt-4 rounded-xl bg-surface-container bg-opacity-50 p-4 text-sm shadow">
                    <p v-if="hasHiddenCrewAssignments">
                        {{ $t('components.event-participants-card.no-registrations') }}
                    </p>
                    <p v-else>
                        {{ $t('components.event-participants-card.no-waitinglist') }}
                    </p>
                </div>
            </template>
        </div>
    </section>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import type { Event, ResolvedRegistrationSlot, SignedInUser } from '@/domain';
import { EventSignupType, EventState, Permission } from '@/domain';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { Routes } from '@/ui/views/Routes.ts';

enum Tab {
    Team = 'team',
    WaitingList = 'waitinglist',
}

interface Props {
    event: Event;
}

const props = defineProps<Props>();

const authUseCase = useAuthUseCase();
const eventUseCase = useEventUseCase();

const signedInUser = ref<SignedInUser>(authUseCase.getSignedInUser());
const tab = ref<Tab>(Tab.Team);

const waitingList = ref<ResolvedRegistrationSlot[]>([]);
const team = ref<ResolvedRegistrationSlot[]>([]);

const hasHiddenCrewAssignments = computed<boolean>(() => {
    return (
        props.event.signupType !== EventSignupType.Assignment || [EventState.OpenForSignup, EventState.Draft].includes(props.event.state)
    );
});

function init(): void {
    fetchTeam(props.event).then(() => {
        tab.value = team.value.length > 0 ? Tab.Team : Tab.WaitingList;
    });
    watch(() => props.event, fetchTeam, { deep: true });
}

async function fetchTeam(event: Event): Promise<void> {
    const registrations = await eventUseCase.resolveRegistrations(event);
    team.value = eventUseCase.filterForCrew(event, registrations);
    waitingList.value = eventUseCase.filterForWaitingList(event, registrations);
}

init();
</script>
