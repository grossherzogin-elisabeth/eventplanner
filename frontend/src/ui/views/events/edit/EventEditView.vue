<template>
    <DetailsPage :back-to="{ name: Routes.EventsListAdmin }" :class="$attrs.class">
        <template #header>
            <div v-if="eventDetails" class="">
                <h1 class="mb-1 mt-8 hidden truncate xl:block">
                    {{ eventDetails.name || 'Err' }}
                </h1>
                <section class="">
                    <VInfo v-if="eventDetails.state === EventState.Draft" class="mt-4" dismissable clamp>
                        Diese Reise befindet sich noch im Entwurfsstadium und ist noch nicht für Anmeldungen freigegeben. Du kannst als
                        Admin allerdings bereits Anmeldungen eintragen.
                    </VInfo>
                    <VWarning v-if="eventDetails.state === EventState.Canceled" class="mt-4" dismissable>
                        Diese Reise wurde abgesagt. Du kannst sie trotzdem weiter bearbeiten und auch die Absage im Tab Reisedaten
                        zurücknehmen.
                    </VWarning>
                    <VWarning v-else-if="eventDetails.state === EventState.Planned && hasEmptyRequiredSlots" class="mt-4" dismissable>
                        Die Vorraussetzungen für eine sichere Mindesbesatzung für diese Reise sind noch nicht erfüllt!
                    </VWarning>
                </section>
            </div>
        </template>
        <template #content>
            <VTabs v-model="tab" :tabs="tabs" class="sticky top-10 z-20 bg-surface pt-4 xl:top-0 xl:pt-8">
                <template #[Tab.EVENT_DATA]>
                    <div class="max-w-2xl space-y-8 xl:space-y-16">
                        <section v-if="eventDetails" class="">
                            <div class="mb-4">
                                <VInputLabel>Status</VInputLabel>
                                <VInputSelect
                                    v-model="eventDetails.state"
                                    :options="[
                                        { value: EventState.Draft, label: 'Entwurf' },
                                        { value: EventState.OpenForSignup, label: 'Crew Anmeldung' },
                                        { value: EventState.Planned, label: 'Crew veröffentlicht' },
                                        { value: EventState.Canceled, label: 'Reise ist abgesagt', hidden: true },
                                    ]"
                                    :errors="validation.errors.value['state']"
                                    :errors-visible="validation.showErrors.value"
                                    required
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4">
                                <VInputLabel>Name</VInputLabel>
                                <VInputText
                                    v-model.trim="eventDetails.name"
                                    :errors="validation.errors.value['name']"
                                    :errors-visible="validation.showErrors.value"
                                    required
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4">
                                <VInputLabel>Typ</VInputLabel>
                                <VInputSelect
                                    v-model="eventDetails.type"
                                    :options="[
                                        { value: EventType.WorkEvent, label: 'Arbeitsdienst' },
                                        { value: EventType.SingleDayEvent, label: 'Tagesfahrt' },
                                        { value: EventType.WeekendEvent, label: 'Wochenendreise' },
                                        { value: EventType.MultiDayEvent, label: 'Mehrtagesfahrt' },
                                    ]"
                                    :errors="validation.errors.value['type']"
                                    :errors-visible="validation.showErrors.value"
                                    required
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4">
                                <VInputLabel>Beschreibung</VInputLabel>
                                <VInputTextArea
                                    v-model.trim="eventDetails.description"
                                    :errors="validation.errors.value['description']"
                                    :errors-visible="validation.showErrors.value"
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4 flex space-x-4">
                                <div class="w-3/5">
                                    <VInputLabel>Startdatum</VInputLabel>
                                    <VInputDate
                                        :model-value="eventDetails.start"
                                        :highlight-from="eventDetails.start"
                                        :highlight-to="eventDetails.end"
                                        :errors="validation.errors.value['start']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="eventDetails.start = updateDate(eventDetails.start, $eventDetails)"
                                    />
                                </div>
                                <div class="w-2/5">
                                    <VInputLabel>Crew an Bord</VInputLabel>
                                    <VInputTime
                                        :model-value="eventDetails.start"
                                        :errors="validation.errors.value['start']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="eventDetails.start = updateTime(eventDetails.start, $eventDetails, 'minutes')"
                                    />
                                </div>
                            </div>

                            <div class="mb-4 flex space-x-4">
                                <div class="w-3/5">
                                    <VInputLabel>Enddatum</VInputLabel>
                                    <VInputDate
                                        :model-value="eventDetails.end"
                                        :highlight-from="eventDetails.start"
                                        :highlight-to="eventDetails.end"
                                        :errors="validation.errors.value['end']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="eventDetails.end = updateDate(eventDetails.end, $eventDetails)"
                                    />
                                </div>
                                <div class="w-2/5">
                                    <VInputLabel>Crew von Bord</VInputLabel>
                                    <VInputTime
                                        :model-value="eventDetails.end"
                                        :errors="validation.errors.value['end']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="eventDetails.end = updateTime(eventDetails.end, $eventDetails, 'minutes')"
                                    />
                                </div>
                            </div>
                        </section>
                    </div>
                </template>
                <template #[Tab.EVENT_TEAM]>
                    <CrewEditor v-if="eventDetails" v-model:eventDetails="eventDetails" />
                </template>
                <template #[Tab.EVENT_SLOTS]>
                    <div class="xl:max-w-5xl">
                        <div class="-mx-8 md:-mx-16 xl:-mx-20">
                            <SlotsTable v-if="eventDetails" :eventDetails="eventDetails" />
                        </div>
                    </div>
                </template>
                <template #[Tab.EVENT_LOCATIONS]>
                    <div class="xl:max-w-5xl">
                        <div class="-mx-8 md:-mx-16 xl:-mx-20">
                            <LocationsTable v-if="eventDetails" :eventDetails="eventDetails" />
                        </div>
                    </div>
                </template>
            </VTabs>
        </template>
        <template v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)" #primary-button>
            <AsyncButton :action="saveIfValid" :disabled="!validation.isValid" name="save">
                <template #icon>
                    <i class="fa-solid fa-save" />
                </template>
                <template #label>
                    <span> Speichern </span>
                </template>
            </AsyncButton>
        </template>
        <template #secondary-buttons>
            <div class="hidden items-stretch space-x-2 lg:flex">
                <button v-if="tab === Tab.EVENT_TEAM" class="permission-write-registrations btn-secondary" @click="addRegistration()">
                    <i class="fa-solid fa-user-plus" />
                    <span>Anmeldung hinzufügen</span>
                </button>
                <button v-else-if="tab === Tab.EVENT_SLOTS" class="permission-write-eventDetails-slots btn-secondary" @click="addSlot()">
                    <i class="fa-solid fa-list" />
                    <span>Crewslot hinzufügen</span>
                </button>
                <button
                    v-else-if="tab === Tab.EVENT_LOCATIONS"
                    class="permission-write-eventDetails-details btn-secondary"
                    @click="addLocation()"
                >
                    <i class="fa-solid fa-route" />
                    <span>Reiseabschnitt hinzufügen</span>
                </button>
            </div>
        </template>
        <template #actions-menu>
            <li class="permission-write-registrations context-menu-item" @click="addRegistration()">
                <i class="fa-solid fa-user-plus" />
                <span>Anmeldung hinzufügen</span>
            </li>
            <li class="permission-write-eventDetails-slots context-menu-item" @click="addSlot()">
                <i class="fa-solid fa-list" />
                <span>Crewslot hinzufügen</span>
            </li>
            <li class="permission-write-eventDetails-details context-menu-item" @click="addLocation()">
                <i class="fa-solid fa-route" />
                <span>Reiseabschnitt hinzufügen</span>
            </li>
            <li class="permission-read-user-details context-menu-item" @click="contactTeam()">
                <i class="fa-solid fa-envelope" />
                <span>Crew kontaktieren</span>
            </li>
            <li
                v-if="eventDetails"
                class="permission-read-user-details context-menu-item"
                @click="eventUseCase.downloadImoList(eventDetails)"
            >
                <i class="fa-solid fa-clipboard-user" />
                <span>IMO Liste generieren</span>
            </li>
            <li
                v-if="eventDetails"
                class="permission-read-users context-menu-item"
                @click="eventUseCase.downloadConsumptionList(eventDetails)"
            >
                <i class="fa-solid fa-beer-mug-empty" />
                <span>Verzehrliste generieren</span>
            </li>
            <li
                v-if="eventDetails"
                class="permission-read-user-details context-menu-item"
                @click="eventUseCase.downloadCaptainList(eventDetails)"
            >
                <i class="fa-solid fa-file-medical" />
                <span>Kapitänsliste generieren</span>
            </li>
            <li
                v-if="eventDetails?.state === EventState.Draft"
                class="permission-write-eventDetails-details context-menu-item"
                @click="openEventForCrewSignup()"
            >
                <i class="fa-solid fa-lock-open" />
                <span>Anmeldungen freischalten</span>
            </li>
            <li
                v-if="eventDetails?.state === EventState.OpenForSignup"
                class="permission-write-eventDetails-details context-menu-item"
                @click="publishPlannedCrew()"
            >
                <i class="fa-solid fa-earth-europe" />
                <span>Crew veröffentlichen</span>
            </li>
            <li class="permission-write-eventDetails-slots context-menu-item" @click="resetTeam()">
                <i class="fa-solid fa-rotate" />
                <span>Crew zurücksetzen</span>
            </li>
            <li class="permission-write-eventDetails-details context-menu-item text-error" @click="cancelEvent()">
                <i class="fa-solid fa-ban" />
                <span>Reise absagen</span>
            </li>
        </template>
    </DetailsPage>
    <CreateRegistrationDlg ref="createRegistrationDialog" />
    <SlotEditDlg ref="createSlotDialog" />
    <EventCancelDlg ref="cancelEventDialog" />
    <LocationEditDlg ref="createLocationDialog" />
    <VConfirmationDialog ref="confirmDialog" />
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { deepCopy, diff, filterUndefined, updateDate, updateTime } from '@/common';
import type { Event, Location, Registration, Slot } from '@/domain';
import { EventState, EventType, Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';
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
import {
    useAuthUseCase,
    useEventAdministrationUseCase,
    useEventUseCase,
    useUserAdministrationUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application.ts';
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
    EVENT_DATA = 'app.edit-eventDetails.tab.data',
    EVENT_TEAM = 'Crew verwalten',
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
const usersUseCase = useUsersUseCase();
const usersAdminUseCase = useUserAdministrationUseCase();
const signedInUser = authUseCase.getSignedInUser();

const eventOriginal = ref<Event | null>(null);
const eventDetails = ref<Event | null>(null);
const validation = useValidation(eventDetails, (evt) => (evt === null ? {} : eventService.validate(evt)));
const hasChanges = ref<boolean>(false);

const tab = ref<Tab>(Tab.EVENT_TEAM);
const tabs = computed<Tab[]>(() => {
    const visibleTabs: Tab[] = [Tab.EVENT_DATA, Tab.EVENT_LOCATIONS];
    if (signedInUser.permissions.includes(Permission.WRITE_EVENT_SLOTS)) {
        visibleTabs.push(Tab.EVENT_TEAM);
        visibleTabs.push(Tab.EVENT_SLOTS);
    }
    return visibleTabs;
});

const createLocationDialog = ref<Dialog<void, Location | undefined> | null>(null);
const createSlotDialog = ref<Dialog<void, Slot | undefined> | null>(null);
const createRegistrationDialog = ref<Dialog<Event[], Registration | undefined> | null>(null);
const cancelEventDialog = ref<Dialog<Event, string | undefined> | null>(null);
const confirmDialog = ref<ConfirmationDialog | null>(null);

const hasEmptyRequiredSlots = computed<boolean>(() => {
    return eventDetails.value !== null && eventService.hasOpenRequiredSlots(eventDetails.value);
});

async function init(): Promise<void> {
    await fetchEvent();
    preventPageUnloadOnUnsavedChanges();
}

async function fetchEvent(): Promise<void> {
    const year = parseInt(route.params.year as string, 10);
    const key = route.params.key as string;
    const eventDetails = await eventUseCase.getEventByKey(year, key, true);
    updateState(eventDetails);
    emit('update:title', eventDetails.name);
}

function preventPageUnloadOnUnsavedChanges(): void {
    watch(eventDetails, updateHasChanges, { deep: true });
    const removeNavigationGuard = router.beforeEach(async (to, from) => {
        if (to.name === from.name) {
            // we stay on the same page and only change
            return true;
        }
        if (hasChanges.value) {
            const continueNavigation = await confirmDialog.value?.open({
                title: 'Änderungen verwerfen?',
                message: `Du hast ungespeicherte Änderungen. Wenn du die Seite verlässt oder neu lädst, werden
                    diese Änderungen verworfen. Möchtest du forfahren?`,
                cancel: 'Abbrechen',
                submit: 'Änderungen verwerfen',
            });
            if (!continueNavigation) {
                return false;
            }
        }
        removeNavigationGuard();
        return true;
    });
}

function updateHasChanges(): void {
    if (eventOriginal.value !== null && eventDetails.value !== null) {
        const changes = diff(eventOriginal.value, eventDetails.value);
        hasChanges.value = Object.keys(changes).length > 0;
    } else {
        hasChanges.value = true;
    }
}

function resetTeam(): void {
    if (eventDetails.value) {
        eventDetails.value.slots.forEach((it) => (it.assignedRegistrationKey = undefined));
        eventDetails.value.assignedUserCount = 0;
    }
}

async function cancelEvent(): Promise<void> {
    if (eventDetails.value) {
        const message = await cancelEventDialog.value?.open(eventDetails.value);
        if (message !== undefined) {
            const updatedEvent = await eventAdministrationUseCase.cancelEvent(eventDetails.value, message);
            updateState(updatedEvent);
            await router.push({ name: Routes.EventsListAdmin });
        }
    }
}

async function addRegistration(): Promise<void> {
    if (eventDetails.value) {
        const result = await createRegistrationDialog.value?.open([eventDetails.value]);
        if (result) {
            eventDetails.value.registrations.push(result);
        }
    }
}

async function addLocation(): Promise<void> {
    const newLocation = await createLocationDialog.value?.open();
    if (newLocation && eventDetails.value) {
        newLocation.order = eventDetails.value.locations.length + 1;
        eventDetails.value.locations.push(newLocation);
    }
}

async function addSlot(): Promise<void> {
    const newSlot = await createSlotDialog.value?.open();
    if (eventDetails.value && newSlot) {
        eventDetails.value.slots.push(newSlot);
        eventDetails.value = deepCopy(eventDetails.value); // to make sure the list is updated correctly
    }
}

async function contactTeam(): Promise<void> {
    if (eventDetails.value) {
        const userKeys = eventService
            .getAssignedRegistrations(eventDetails.value)
            .map((it) => it.userKey)
            .filter(filterUndefined);
        const users = await usersUseCase.getUsers(userKeys);
        await usersAdminUseCase.contactUsers(users);
    }
}

async function openEventForCrewSignup(): Promise<void> {
    if (eventDetails.value) {
        const updatedEvent = await eventAdministrationUseCase.updateEvent(eventDetails.value.key, {
            state: EventState.OpenForSignup,
        });
        updateState(updatedEvent);
    }
}

async function publishPlannedCrew(): Promise<void> {
    if (eventDetails.value) {
        const updatedEvent = await eventAdministrationUseCase.updateEvent(eventDetails.value.key, { state: EventState.Planned });
        updateState(updatedEvent);
    }
}

async function saveIfValid(): Promise<void> {
    if (eventDetails.value && validation.isValid.value) {
        const updatedEvent = await eventAdministrationUseCase.updateEvent(eventDetails.value.key, eventDetails.value);
        updateState(updatedEvent);
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

function updateState(value: Event): void {
    eventOriginal.value = value;
    eventDetails.value = deepCopy(value);
}

init();
</script>
