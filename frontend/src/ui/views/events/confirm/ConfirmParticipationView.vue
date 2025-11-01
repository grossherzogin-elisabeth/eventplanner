<template>
    <div class="h-full overflow-y-auto px-4 pb-8 pt-8 xs:px-8 md:px-16 xl:px-20">
        <div class="w-full max-w-2xl">
            <div v-if="registrationState === State.REGISTRATION_UNCONFIRMED">
                <div class="mb-8 rounded-2xl">
                    <h1 class="mb-4 text-lg">{{ $t('views.events.confirm-participation.title') }}</h1>
                    <p class="mb-4">
                        <i18n-t keypath="views.events.confirm-participation.info.message">
                            <template #days>{{ daysUntilStart }}</template>
                            <template #event>{{ event?.name }}</template>
                            <template #deadline>
                                <b>{{ $t('views.events.confirm-participation.info.deadline') }}</b>
                            </template>
                        </i18n-t>
                    </p>
                </div>
                <EventDetails v-if="event" :event="event" class="mb-8" />

                <div class="hidden items-center gap-4 sm:flex">
                    <button class="btn-primary" @click="confirm()">
                        <i class="fa-solid fa-check"></i>
                        <span class="py-2 sm:py-0">{{ $t('views.events.confirm-participation.confirm') }}</span>
                    </button>
                    <button class="btn-danger" @click="decline()">
                        <i class="fa-solid fa-xmark"></i>
                        <span class="py-2 sm:py-0">{{ $t('views.events.confirm-participation.decline') }}</span>
                    </button>
                </div>
                <div class="h-20 sm:hidden"></div>
                <div class="fixed bottom-0 left-0 right-0 flex flex-col items-stretch gap-4 bg-surface px-4 py-2 sm:hidden">
                    <button class="btn-primary" @click="confirm()">
                        <i class="fa-solid fa-check"></i>
                        <span class="whitespace-normal py-2 text-sm">{{ $t('views.events.confirm-participation.confirm') }}</span>
                    </button>
                    <button class="btn-danger" @click="decline()">
                        <i class="fa-solid fa-xmark"></i>
                        <span class="whitespace-normal py-2 text-sm">{{ $t('views.events.confirm-participation.decline') }}</span>
                    </button>
                </div>
            </div>
            <div v-else-if="registrationState === State.REGISTRATION_WAS_CANCELED && !signedInUser.key">
                <div class="mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container xs:-mx-4">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">{{ $t('views.events.confirm-participation.not-found.title') }}</span>
                    </p>
                    <p class="mb-4">
                        {{ $t('views.events.confirm-participation.not-found.info') }}
                    </p>
                </div>
            </div>
            <div v-else-if="registrationState === State.REGISTRATION_WAS_CANCELED">
                <div class="mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container xs:-mx-4 md:p-8">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">{{ $t('views.events.confirm-participation.canceled.title') }}</span>
                    </p>
                    <p class="mb-4">
                        {{ $t('views.events.confirm-participation.canceled.info') }}
                    </p>
                </div>
            </div>
            <div v-else-if="registrationState === State.SIGNED_IN_USER_HAS_NO_REGISTRATION">
                <div class="mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container xs:-mx-4 md:p-8">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">{{ $t('views.events.confirm-participation.no-registration.title') }}</span>
                    </p>
                    <p class="mb-4">
                        {{ $t('views.events.confirm-participation.no-registration.info') }}
                    </p>
                </div>
            </div>
            <div v-else-if="registrationState === State.REGISTRATION_BELONGS_TO_OTHER_USER">
                <div class="mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container xs:-mx-4 md:p-8">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-warning"></i>
                        <span class="ml-4">{{ $t('views.events.confirm-participation.invalid-link.title') }}</span>
                    </p>
                    <p class="mb-4">
                        {{ $t('views.events.confirm-participation.invalid-link.info') }}
                    </p>
                </div>
            </div>
            <template v-else-if="registrationState === State.REGISTRATION_WAS_CONFIRMED">
                <div class="mb-8 rounded-2xl bg-success-container/50 p-4 font-bold text-onsuccess-container xs:-mx-4 md:p-8">
                    <p class="mb-4 text-lg">
                        <i class="fa-solid fa-check"></i>
                        <span class="ml-4">{{ $t('views.events.confirm-participation.confirmed.title') }}</span>
                    </p>
                    <p class="mb-8">
                        {{ $t('views.events.confirm-participation.confirmed.info') }}
                    </p>
                    <h2 class="mb-4 text-base">{{ $t('views.events.confirm-participation.details.title') }}</h2>
                    <EventDetails v-if="event" :event="event" />
                </div>
            </template>
            <div
                v-else-if="registrationState === State.REGISTRATION_WAS_JUST_CANCELED"
                class="mb-8 rounded-2xl bg-error-container p-4 font-bold text-onerror-container xs:-mx-4 md:p-8"
            >
                <p class="mb-4 text-lg">
                    <i class="fa-solid fa-xmark"></i>
                    <span class="ml-4">{{ $t('views.events.confirm-participation.canceled-now.title') }}</span>
                </p>
                <p class="mb-4">
                    {{ $t('views.events.confirm-participation.canceled-now.info') }}
                </p>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import type { Event, EventKey, RegistrationKey, SignedInUser } from '@/domain';
import { useAuthUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import EventDetails from '@/ui/views/events/confirm/EventDetails.vue';

enum State {
    REGISTRATION_UNCONFIRMED = 'registration_unconfirmed',
    REGISTRATION_WAS_CANCELED = 'registration_canceled',
    REGISTRATION_WAS_JUST_CANCELED = 'registration_just_canceled',
    REGISTRATION_WAS_CONFIRMED = 'registration_confirmed',
    SIGNED_IN_USER_HAS_NO_REGISTRATION = 'user_has_no_registration',
    REGISTRATION_BELONGS_TO_OTHER_USER = 'registration_belongs_to_other_user',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const route = useRoute();
const eventUseCase = useEventUseCase();
const auth = useAuthUseCase();
const signedInUser = ref<SignedInUser>(auth.getSignedInUser());

const event = ref<Event | null>(null);
const registrationState = ref<State | null>(null);

const daysUntilStart = computed<number>(() => {
    if (!event.value) {
        return 0;
    }
    const nowSec = new Date().getTime() / 1000;
    const startSec = event.value.start.getTime() / 1000;
    const diffSec = startSec - nowSec;
    return Math.floor(diffSec / 60 / 60 / 24);
});

function init(): void {
    auth.onLogin().then((user) => (signedInUser.value = user));
    fetchEvent();
}

async function fetchEvent(): Promise<void> {
    try {
        const eventKey = route.params.eventKey as EventKey;
        const accessKey = route.query.accessKey as string;
        event.value = await eventUseCase.getEventByAccessKey(eventKey, accessKey);
        emit('update:tab-title', event.value.name);
        registrationState.value = State.REGISTRATION_UNCONFIRMED;
        const registration = event.value?.registrations.find((it) => it.key === route.params.registrationKey);
        if (!registration) {
            registrationState.value = State.REGISTRATION_WAS_CANCELED;
        } else if (signedInUser.value.key && registration.userKey !== signedInUser.value.key) {
            registrationState.value = State.REGISTRATION_BELONGS_TO_OTHER_USER;
        } else if (registration.confirmed) {
            registrationState.value = State.REGISTRATION_WAS_CONFIRMED;
        }
    } catch (e: unknown) {
        if ((e as Response).status === 404) {
            registrationState.value = State.REGISTRATION_WAS_CANCELED;
        }
    }
}

async function confirm(): Promise<void> {
    const eventKey = route.params.eventKey as EventKey;
    const registrationKey = route.params.registrationKey as RegistrationKey;
    const accessKey = route.query.accessKey as string;
    await eventUseCase.confirmParticipation(eventKey, registrationKey, accessKey);
    registrationState.value = State.REGISTRATION_WAS_CONFIRMED;
}

async function decline(): Promise<void> {
    const eventKey = route.params.eventKey as EventKey;
    const registrationKey = route.params.registrationKey as RegistrationKey;
    const accessKey = route.query.accessKey as string;
    await eventUseCase.declineParticipation(eventKey, registrationKey, accessKey);
    registrationState.value = State.REGISTRATION_WAS_JUST_CANCELED;
}

init();
</script>
