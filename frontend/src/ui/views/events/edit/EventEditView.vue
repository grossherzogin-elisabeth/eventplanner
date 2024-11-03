<template>
    <DetailsPage :back-to="{ name: Routes.EventsListAdmin }" :class="$attrs.class">
        <template #header>
            <div v-if="event" class="">
                <h1 class="mb-1 mt-8 hidden truncate xl:block">{{ event.name || 'Err' }}</h1>
                <!--                <h1 class="mb-1 mt-4 hidden truncate xl:block">{{ event.name || 'Err' }}</h1>-->
                <!--                <p class="mb-4 text-sm">-->
                <!--                    {{ $d(event.start, DateTimeFormat.DDD_DD_MM) }}-->
                <!--                    - -->
                <!--                    {{ $d(event.end, DateTimeFormat.DDD_DD_MM) }}-->
                <!--                </p>-->
                <div class="-mx-4 flex flex-wrap items-start font-semibold">
                    <div class="w-full">
                        <VInfo v-if="event.state === EventState.Draft" class="mt-4" dismissable>
                            Diese Reise befindet sich noch im Entwurfsstadium und ist noch nicht für Anmeldungen
                            freigegeben. Du kannst als Admin allerdings bereits Anmeldungen eintragen.
                        </VInfo>
                        <VWarning v-if="event.state === EventState.Canceled" class="mt-4" dismissable>
                            Diese Reise wurde abgesagt. Du kannst sie trotzdem weiter bearbeiten und auch die Absage im
                            Tab Reisedaten zurücknehmen.
                        </VWarning>
                        <VWarning
                            v-else-if="event.state === EventState.Planned && hasEmptyRequiredSlots"
                            class="mt-4"
                            dismissable
                        >
                            Die Vorraussetzungen für eine sichere Mindesbesatzung für diese Reise sind noch nicht
                            erfüllt!
                        </VWarning>
                    </div>
                </div>
            </div>
        </template>
        <template #content>
            <VTabs v-model="tab" :tabs="tabs" class="sticky top-10 z-20 bg-primary-50 pt-4 xl:top-0 xl:pt-8">
                <template #[Tab.EVENT_DATA]>
                    <div class="max-w-2xl space-y-8 xl:space-y-16">
                        <section v-if="event" class="-mx-4">
                            <div class="mb-4">
                                <VInputLabel>Status</VInputLabel>
                                <VInputSelect
                                    v-model="event.state"
                                    :options="[
                                        { value: EventState.Draft, label: 'Entwurf' },
                                        { value: EventState.OpenForSignup, label: 'Crew Anmeldung' },
                                        { value: EventState.Planned, label: 'Crewplanung verlöffentlicht' },
                                        { value: EventState.Canceled, label: 'Reise ist abgesagt', hidden: true },
                                    ]"
                                    :errors="validation.errors.value['state']"
                                    :errors-visible="true"
                                    required
                                />
                            </div>
                            <div class="mb-4">
                                <VInputLabel>Name</VInputLabel>
                                <VInputText
                                    v-model="event.name"
                                    :errors="validation.errors.value['name']"
                                    :errors-visible="true"
                                    required
                                />
                            </div>
                            <div class="mb-4">
                                <VInputLabel>Typ</VInputLabel>
                                <VInputSelect
                                    v-model="event.type"
                                    :options="[
                                        { value: EventType.WorkEvent, label: 'Arbeitsdienst' },
                                        { value: EventType.SingleDayEvent, label: 'Tagesfahrt' },
                                        { value: EventType.WeekendEvent, label: 'Wochenendreise' },
                                        { value: EventType.MultiDayEvent, label: 'Mehrtagesfahrt' },
                                    ]"
                                    :errors="validation.errors.value['type']"
                                    :errors-visible="true"
                                    required
                                />
                            </div>
                            <div class="mb-4">
                                <VInputLabel>Beschreibung</VInputLabel>
                                <VInputTextArea
                                    v-model="event.description"
                                    :errors="validation.errors.value['description']"
                                    :errors-visible="true"
                                />
                            </div>
                            <div class="mb-4 flex space-x-4">
                                <div class="w-3/5">
                                    <VInputLabel>Startdatum</VInputLabel>
                                    <VInputDate
                                        v-model="event.start"
                                        :errors="validation.errors.value['start']"
                                        :errors-visible="true"
                                        required
                                    />
                                </div>
                                <div class="w-2/5">
                                    <VInputLabel>Crew an Board</VInputLabel>
                                    <VInputTime
                                        v-model="event.start"
                                        required
                                        :errors="validation.errors.value['start']"
                                        :errors-visible="true"
                                    />
                                </div>
                            </div>

                            <div class="mb-4 flex space-x-4">
                                <div class="w-3/5">
                                    <VInputLabel>Enddatum</VInputLabel>
                                    <VInputDate
                                        v-model="event.end"
                                        :errors="validation.errors.value['end']"
                                        :errors-visible="true"
                                        required
                                    />
                                </div>
                                <div class="w-2/5">
                                    <VInputLabel>Crew von Board</VInputLabel>
                                    <VInputTime
                                        v-model="event.end"
                                        :errors="validation.errors.value['end']"
                                        :errors-visible="true"
                                        required
                                    />
                                </div>
                            </div>
                        </section>
                    </div>
                </template>
                <template #[Tab.EVENT_POSITIONS]>
                    <CrewEditor v-if="event" v-model:event="event" />
                </template>
                <template #[Tab.EVENT_SLOTS]>
                    <div class="xl:max-w-5xl">
                        <div class="-mx-8 md:-mx-16 xl:-mx-20">
                            <SlotsTable v-if="event" :event="event" />
                        </div>
                    </div>
                </template>
                <template #[Tab.EVENT_LOCATIONS]>
                    <div class="xl:max-w-5xl">
                        <div class="-mx-8 md:-mx-16 xl:-mx-20">
                            <LocationsTable v-if="event" :event="event" />
                        </div>
                    </div>
                </template>
            </VTabs>
        </template>
        <template v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)" #primary-button>
            <AsyncButton :action="saveIfValid" :disabled="!validation.isValid">
                <template #icon>
                    <i class="fa-solid fa-save" />
                </template>
                <template #label>
                    <span>Speichern</span>
                </template>
            </AsyncButton>
        </template>
        <template #secondary-buttons>
            <div class="hidden items-stretch space-x-2 lg:flex">
                <button v-if="tab === Tab.EVENT_POSITIONS" class="btn-secondary" @click="addRegistration()">
                    <i class="fa-solid fa-user-plus" />
                    <span>Anmeldung hinzufügen</span>
                </button>
                <button v-else-if="tab === Tab.EVENT_SLOTS" class="btn-secondary" @click="addSlot()">
                    <i class="fa-solid fa-plus" />
                    <span>Slot hinzufügen</span>
                </button>
                <button v-else-if="tab === Tab.EVENT_LOCATIONS" class="btn-secondary" @click="addLocation()">
                    <i class="fa-solid fa-plus" />
                    <span>Reiseabschnitt hinzufügen</span>
                </button>
            </div>
        </template>
        <template #actions-menu>
            <li v-if="tab === Tab.EVENT_POSITIONS" class="lg:hidden" @click="addRegistration()">
                <div class="context-menu-item">
                    <i class="fa-solid fa-user-plus" />
                    <span>Anmeldung hinzufügen</span>
                </div>
            </li>
            <li v-else-if="tab === Tab.EVENT_SLOTS" class="lg:hidden" @click="addSlot()">
                <div class="context-menu-item">
                    <i class="fa-solid fa-plus" />
                    <span>Slot hinzufügen</span>
                </div>
            </li>
            <li v-else-if="tab === Tab.EVENT_LOCATIONS" class="lg:hidden" @click="addLocation()">
                <div class="context-menu-item">
                    <i class="fa-solid fa-plus" />
                    <span>Reiseabschnitt hinzufügen</span>
                </div>
            </li>
            <li class="context-menu-item disabled" @click="contactTeam()">
                <i class="fa-solid fa-envelope" />
                <span>Crew kontaktieren</span>
            </li>
            <li v-if="event?.state === EventState.Draft" class="context-menu-item" @click="openEventForCrewSignup()">
                <i class="fa-solid fa-lock-open" />
                <span>Anmeldungen freischalten</span>
            </li>
            <li
                v-if="event?.state === EventState.OpenForSignup"
                class="context-menu-item"
                @click="publishPlannedCrew()"
            >
                <i class="fa-solid fa-earth-europe" />
                <span>Crewplanung veröffentlichen</span>
            </li>
            <li
                v-if="signedInUser.permissions.includes(Permission.EVENT_TEAM_WRITE)"
                class="context-menu-item"
                @click="resetTeam()"
            >
                <i class="fa-solid fa-rotate" />
                <span>Crew zurücksetzen</span>
            </li>
            <li
                v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)"
                class="context-menu-item text-red-700"
                @click="cancelEvent()"
            >
                <i class="fa-solid fa-ban" />
                <span>Reise absagen</span>
            </li>
        </template>
    </DetailsPage>
    <CreateRegistrationDlg ref="createRegistrationDialog" />
    <SlotEditDlg ref="createSlotDialog" />
    <EventCancelDlg ref="cancelEventDialog" />
    <LocationEditDlg ref="createLocationDialog" />
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { Event, Location, Slot } from '@/domain';
import { EventState, EventType, Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import {
    AsyncButton,
    VInfo,
    VInputDate,
    VInputLabel,
    VInputSelect,
    VInputText,
    VInputTextArea,
    VInputTime,
    VTabs,
} from '@/ui/components/common';
import CreateRegistrationDlg from '@/ui/components/events/CreateRegistrationDlg.vue';
import EventCancelDlg from '@/ui/components/events/EventCancelDlg.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useAuthUseCase, useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
import { useEventService } from '@/ui/composables/Domain.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { Routes } from '@/ui/views/Routes.ts';
import VWarning from '../../../components/common/alerts/VWarning.vue';
import CrewEditor from './CrewEditor.vue';
import LocationEditDlg from './LocationEditDlg.vue';
import LocationsTable from './LocationsTable.vue';
import SlotEditDlg from './SlotEditDlg.vue';
import SlotsTable from './SlotsTable.vue';

enum Tab {
    EVENT_DATA = 'app.edit-event.tab.data',
    EVENT_POSITIONS = 'Crew verwalten',
    EVENT_SLOTS = 'Crew Slots',
    EVENT_LOCATIONS = 'Reiseroute',
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const router = useRouter();
const route = useRoute();
const eventService = useEventService();
const eventUseCase = useEventUseCase();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const authUseCase = useAuthUseCase();
const signedInUser = authUseCase.getSignedInUser();

const event = ref<Event | null>(null);
const validation = useValidation(event, (evt) => (evt === null ? {} : eventService.validate(evt)));

const tabs = [Tab.EVENT_POSITIONS, Tab.EVENT_DATA, Tab.EVENT_LOCATIONS, Tab.EVENT_SLOTS];
const tab = ref<Tab>(Tab.EVENT_POSITIONS);

const createLocationDialog = ref<Dialog<void, Location | undefined> | null>(null);
const createSlotDialog = ref<Dialog<void, Slot | undefined> | null>(null);
const createRegistrationDialog = ref<Dialog<Event, Event> | null>(null);
const cancelEventDialog = ref<Dialog<Event, string | undefined> | null>(null);

const hasEmptyRequiredSlots = computed<boolean>(() => {
    return event.value !== null && eventService.hasOpenRequiredSlots(event.value);
});

async function init(): Promise<void> {
    await fetchEvent();
}

async function fetchEvent(): Promise<void> {
    const year = parseInt(route.params.year as string, 10);
    const key = route.params.key as string;
    event.value = await eventUseCase.getEventByKey(year, key);
    emit('update:title', event.value.name);
}

function resetTeam(): void {
    if (event.value) {
        event.value.slots.forEach((it) => (it.assignedRegistrationKey = undefined));
        event.value.assignedUserCount = 0;
    }
}

async function cancelEvent(): Promise<void> {
    if (event.value) {
        const message = await cancelEventDialog.value?.open(event.value);
        if (message !== undefined) {
            event.value = await eventAdministrationUseCase.cancelEvent(event.value, message);
            await router.push({ name: Routes.EventsListAdmin });
        }
    }
}

async function addRegistration(): Promise<void> {
    if (createRegistrationDialog.value && event.value) {
        await createRegistrationDialog.value.open(event.value).catch(() => console.debug('dialog was canceled'));
    }
}

async function addLocation(): Promise<void> {
    const newLocation = await createLocationDialog.value?.open();
    if (newLocation && event.value) {
        newLocation.order = event.value.locations.length + 1;
        event.value.locations.push(newLocation);
    }
}

async function addSlot(): Promise<void> {
    const newSlot = await createSlotDialog.value?.open();
    if (event.value && newSlot) {
        event.value.slots.push(newSlot);
    }
}

async function contactTeam(): Promise<void> {
    if (event.value) {
        await eventAdministrationUseCase.contactTeam(event.value);
    }
}

async function openEventForCrewSignup(): Promise<void> {
    if (event.value) {
        event.value = await eventAdministrationUseCase.updateEvent(event.value.key, {
            state: EventState.OpenForSignup,
        });
    }
}

async function publishPlannedCrew(): Promise<void> {
    if (event.value) {
        event.value = await eventAdministrationUseCase.updateEvent(event.value.key, { state: EventState.Planned });
    }
}

async function saveIfValid(): Promise<void> {
    if (event.value && validation.isValid.value) {
        await eventAdministrationUseCase.updateEvent(event.value.key, event.value);
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

init();
</script>
