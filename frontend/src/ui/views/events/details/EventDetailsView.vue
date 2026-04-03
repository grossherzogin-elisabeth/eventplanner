<template>
    <DetailsPage :back-to="{ name: Routes.EventsCalendar }" :class="$attrs.class" :loading="!event">
        <template #header>
            {{ event?.name }}
        </template>
        <template #content>
            <div v-if="event" class="xs:px-8 px-4 pt-6 pb-8 md:px-16 xl:px-20">
                <div class="flex flex-col gap-x-20 gap-y-8 md:flex-row-reverse md:gap-y-4 md:pr-4 xl:max-w-5xl">
                    <div class="flex w-full flex-col gap-8 md:w-96">
                        <EventStateBanner :event="event" sticky />
                        <EventRegistrationDetailsCard
                            v-if="event.signedInUserRegistration"
                            v-model:event="event"
                            :registration="event.signedInUserRegistration"
                            @edit="editUserRegistration()"
                        />
                        <EventDetailsCard :event="event" />
                        <EventLocationsCard :event="event" />
                    </div>
                    <div class="w-full md:w-1/2 md:grow">
                        <EventParticipantsCard :event="event" />
                    </div>
                </div>
            </div>
        </template>
        <template v-if="event && hasPermission(Permission.WRITE_OWN_REGISTRATIONS)" #primary-button>
            <AsyncButton
                v-if="event.isSignedInUserAssigned"
                class="btn-danger"
                :disabled="!event.canSignedInUserLeave"
                :action="() => leaveEvent()"
            >
                <template #icon>
                    <i class="fa-solid fa-cancel" />
                </template>
                <template #label>
                    {{ $t('views.event-details.leave-crew') }}
                </template>
            </AsyncButton>
            <AsyncButton
                v-else-if="event.signedInUserRegistration"
                class="btn-danger"
                :disabled="!event.canSignedInUserLeave"
                :action="() => leaveEvent()"
            >
                <template #icon>
                    <i class="fa-solid fa-user-minus" />
                </template>
                <template #label>
                    {{ $t('views.event-details.leave-waitinglist') }}
                </template>
            </AsyncButton>
            <button v-else class="btn-primary max-w-80" :disabled="!event.canSignedInUserJoin" @click="joinEvent()">
                <i class="fa-solid fa-user-plus" />
                <span class="truncate text-left"> {{ $t('views.event-details.sign-up') }} </span>
            </button>
        </template>
        <template v-if="event" #secondary-buttons>
            <RouterLink v-if="hasPermission(Permission.WRITE_EVENTS)" :to="{ name: Routes.EventEdit }" class="btn-secondary">
                <i class="fa-solid fa-drafting-compass" />
                <span>{{ $t('views.event-details.edit-event') }}</span>
            </RouterLink>
            <button v-else class="btn-secondary" @click="eventUseCase.downloadCalendarEntry(event)">
                <i class="fa-solid fa-calendar-alt" />
                <span>{{ $t('views.event-details.save-calendar') }}</span>
            </button>
        </template>
        <template v-if="event" #actions-menu>
            <li class="context-menu-item" data-test-id="action-create-calendar-entry" @click="eventUseCase.downloadCalendarEntry(event)">
                <i class="fa-solid fa-calendar-alt" />
                <span>{{ $t('views.event-details.create-calendar-entry') }}</span>
            </li>
            <template v-if="event.signedInUserRegistration">
                <li
                    data-test-id="action-edit-registration"
                    class="context-menu-item"
                    :class="{ disabled: !event.canSignedInUserUpdateRegistration }"
                    @click="editUserRegistration()"
                >
                    <i class="fa-solid fa-edit" />
                    <span>{{ $t('views.event-details.edit-registration') }}</span>
                </li>
                <li
                    class="context-menu-item"
                    :class="{ disabled: !event.canSignedInUserUpdateRegistration }"
                    @click="editUserRegistration()"
                >
                    <i class="fa-solid fa-note-sticky" />
                    <span>{{ $t('views.event-details.add-note') }}</span>
                </li>
            </template>
            <li class="permission-write-events" data-test-id="action-edit-event">
                <RouterLink :to="{ name: Routes.EventEdit }" class="context-menu-item">
                    <i class="fa-solid fa-drafting-compass" />
                    <span>{{ $t('views.event-details.edit-event') }}</span>
                </RouterLink>
            </li>
        </template>
    </DetailsPage>
    <VConfirmationDialog ref="confirmationDialog" />
    <RegistrationDetailsSheet ref="registrationSheet" />
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useEventUseCase } from '@/application';
import type { Event, Registration } from '@/domain';
import { Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { AsyncButton, VConfirmationDialog } from '@/ui/components/common';
import EventDetailsCard from '@/ui/components/events/EventDetailsCard.vue';
import EventLocationsCard from '@/ui/components/events/EventLocationsCard.vue';
import EventParticipantsCard from '@/ui/components/events/EventParticipantsCard.vue';
import EventRegistrationDetailsCard from '@/ui/components/events/EventRegistrationDetailsCard.vue';
import EventStateBanner from '@/ui/components/events/EventStateBanner.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import RegistrationDetailsSheet from '@/ui/components/sheets/RegistrationDetailsSheet.vue';
import { useSession } from '@/ui/composables/Session.ts';
import { Routes } from '@/ui/views/Routes.ts';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const eventUseCase = useEventUseCase();
const { hasPermission } = useSession();

const event = ref<Event | null>(null);

const registrationSheet = ref<Dialog<
    {
        registration?: Registration;
        event: Event;
    },
    Registration | undefined
> | null>(null);
const confirmationDialog = ref<ConfirmationDialog | null>(null);

function init(): void {
    fetchEvent();
    watch(event, onEventChanged);
}

async function fetchEvent(): Promise<void> {
    try {
        const key = route.params.key as string;
        const year = Number.parseInt(route.params.year as string, 10) || new Date().getFullYear();
        event.value = await eventUseCase.getEventByKey(year, key);
    } catch (e) {
        console.error(e);
        await router.push({ name: Routes.EventsCalendar });
    }
}

async function onEventChanged(): Promise<void> {
    emit('update:tab-title', event.value?.name || '');
    if (!event.value) {
        return;
    }
}

async function joinEvent(): Promise<void> {
    if (!event.value) {
        return;
    }
    const registration = await registrationSheet.value?.open({
        event: event.value,
        registration: undefined,
    });
    if (event.value && registration) {
        event.value = await eventUseCase.joinEvent(event.value, registration);
    }
}

async function leaveEvent(): Promise<void> {
    if (event.value) {
        if (event.value.signedInUserAssignedSlot) {
            const confirmed = await confirmationDialog.value?.open({
                title: t('views.event-details.leave-crew-dialog.title'),
                message: t('views.event-details.leave-crew-dialog.message', { event: event.value.name }),
                submit: t('views.event-details.leave-crew-dialog.submit'),
                danger: true,
            });
            if (!confirmed) {
                return;
            }
        }
        event.value = await eventUseCase.leaveEvent(event.value);
    }
}

async function editUserRegistration(): Promise<void> {
    if (event.value && event.value.signedInUserRegistration) {
        const updatedRegistration = await registrationSheet.value?.open({
            event: event.value,
            registration: event.value.signedInUserRegistration,
        });
        if (updatedRegistration) {
            await eventUseCase.updateRegistration(event.value, updatedRegistration);
            await fetchEvent();
        }
    }
}

init();
</script>
