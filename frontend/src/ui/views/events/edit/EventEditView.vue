<template>
    <DetailsPage :back-to="{ name: Routes.EventsListAdmin }" :class="$attrs.class">
        <template #header>
            {{ event?.name || $t('generic.error') }}
        </template>
        <template #subheader>
            <section>
                <VInfo v-if="event?.state === EventState.Draft" class="mt-4" dismissable clamp>
                    {{ $t('views.events.edit.info-draft') }}
                </VInfo>
                <VWarning v-if="event?.state === EventState.Canceled" class="mt-4" dismissable>
                    {{ $t('views.events.edit.info-canceled') }}
                </VWarning>
                <VWarning v-else-if="event?.state === EventState.Planned && hasEmptyRequiredSlots" class="mt-4" dismissable>
                    {{ $t('views.events.edit.info-missing-crew') }}
                </VWarning>
            </section>
        </template>
        <template #content>
            <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-10 z-20 pt-4 xl:top-20">
                <template #[Tab.EVENT_DATA]>
                    <div class="max-w-2xl space-y-8 xl:space-y-16">
                        <section v-if="event">
                            <div class="mb-4">
                                <VInputSelect
                                    v-model="event.state"
                                    :label="$t('domain.event.status')"
                                    :options="eventStates.options.value"
                                    :errors="validation.errors.value['state']"
                                    :errors-visible="validation.showErrors.value"
                                    required
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4">
                                <VInputText
                                    v-model.trim="event.name"
                                    :label="$t('domain.event.name')"
                                    :errors="validation.errors.value['name']"
                                    :errors-visible="validation.showErrors.value"
                                    required
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4">
                                <VInputSelect
                                    v-model="event.type"
                                    :label="$t('domain.event.category')"
                                    :options="eventTypes.options.value"
                                    :errors="validation.errors.value['type']"
                                    :errors-visible="validation.showErrors.value"
                                    required
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4">
                                <VInputSelect
                                    v-model="event.signupType"
                                    :label="$t('domain.event.signup-type')"
                                    :options="eventSignupTypes.options.value"
                                    :errors="validation.errors.value['signupType']"
                                    :errors-visible="validation.showErrors.value"
                                    required
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4">
                                <VInputTextArea
                                    v-model.trim="event.description"
                                    :label="$t('domain.event.description')"
                                    hint="Markdown wird unterstützt"
                                    :errors="validation.errors.value['description']"
                                    :errors-visible="validation.showErrors.value"
                                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                />
                            </div>
                            <div class="mb-4 flex space-x-4">
                                <div class="w-3/5">
                                    <VInputDate
                                        :label="$t('domain.event.start-date')"
                                        :model-value="event.start"
                                        :highlight-from="event.start"
                                        :highlight-to="event.end"
                                        :errors="validation.errors.value['start']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="event.start = updateDate(event.start, $event)"
                                    />
                                </div>
                                <div class="w-2/5">
                                    <VInputTime
                                        :label="$t('domain.event.crew-on-board')"
                                        :model-value="event.start"
                                        :errors="validation.errors.value['start']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="event.start = updateTime(event.start, $event, 'minutes')"
                                    />
                                </div>
                            </div>

                            <div class="mb-4 flex space-x-4">
                                <div class="w-3/5">
                                    <VInputDate
                                        :label="$t('domain.event.end-date')"
                                        :model-value="event.end"
                                        :highlight-from="event.start"
                                        :highlight-to="event.end"
                                        :errors="validation.errors.value['end']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="event.end = updateDate(event.end, $event)"
                                    />
                                </div>
                                <div class="w-2/5">
                                    <VInputTime
                                        :label="$t('domain.event.crew-off-board')"
                                        :model-value="event.end"
                                        :errors="validation.errors.value['end']"
                                        :errors-visible="validation.showErrors.value"
                                        required
                                        :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                                        @update:model-value="event.end = updateTime(event.end, $event, 'minutes')"
                                    />
                                </div>
                            </div>
                        </section>
                    </div>
                </template>
                <template #[Tab.EVENT_TEAM]>
                    <div class="xl:max-w-5xl">
                        <CrewEditor v-if="event" v-model:event="event" />
                    </div>
                </template>
                <template #[Tab.EVENT_SLOTS]>
                    <div class="xl:max-w-5xl">
                        <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                            <SlotsTable v-if="event" :event="event" />
                        </div>
                    </div>
                </template>
                <template #[Tab.EVENT_LOCATIONS]>
                    <div class="xl:max-w-5xl">
                        <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                            <LocationsTable v-if="event" :event="event" />
                        </div>
                    </div>
                </template>
                <template #[Tab.EVENT_REGISTRATIONS]>
                    <div class="xl:max-w-5xl">
                        <CrewTab v-if="event" :event="event" />
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
                    <span>{{ $t('generic.save') }}</span>
                </template>
            </AsyncButton>
        </template>
        <template #secondary-buttons>
            <div class="hidden items-stretch space-x-2 lg:flex">
                <button v-if="tab === Tab.EVENT_TEAM" class="permission-write-registrations btn-secondary" @click="addRegistration()">
                    <i class="fa-solid fa-user-plus" />
                    <span>{{ $t('views.events.edit.actions.add-registration') }}</span>
                </button>
                <button v-else-if="tab === Tab.EVENT_SLOTS" class="permission-write-event-slots btn-secondary" @click="addSlot()">
                    <i class="fa-solid fa-list" />
                    <span>{{ $t('views.events.edit.actions.add-slot') }}</span>
                </button>
                <button v-else-if="tab === Tab.EVENT_LOCATIONS" class="permission-write-event-details btn-secondary" @click="addLocation()">
                    <i class="fa-solid fa-route" />
                    <span>{{ $t('views.events.edit.actions.add-location') }}</span>
                </button>
            </div>
        </template>
        <template #actions-menu>
            <li class="permission-write-registrations context-menu-item" @click="addRegistration()">
                <i class="fa-solid fa-user-plus" />
                <span>{{ $t('views.events.edit.actions.add-registration') }}</span>
            </li>
            <li class="permission-write-event-slots context-menu-item" @click="addSlot()">
                <i class="fa-solid fa-list" />
                <span>{{ $t('views.events.edit.actions.add-slot') }}</span>
            </li>
            <li class="permission-write-event-details context-menu-item" @click="addLocation()">
                <i class="fa-solid fa-route" />
                <span>{{ $t('views.events.edit.actions.add-location') }}</span>
            </li>
            <li class="permission-read-user-details context-menu-item" @click="contactTeam()">
                <i class="fa-solid fa-envelope" />
                <span>{{ $t('views.events.edit.actions.contact-crew') }}</span>
            </li>
            <template v-if="event">
                <li
                    v-for="template in exportTemplates"
                    :key="template"
                    class="permission-read-user-details context-menu-item"
                    @click="eventAdministrationUseCase.exportEvent(event, template)"
                >
                    <i class="fa-solid fa-file-excel" />
                    <span>{{ $t('views.events.admin-list.action.exportToTemplate', { template }) }}</span>
                </li>
            </template>
            <li
                v-if="event?.state === EventState.Draft"
                class="permission-write-event-details context-menu-item"
                @click="openEventForCrewSignup()"
            >
                <i class="fa-solid fa-lock-open" />
                <span>{{ $t('views.events.edit.open-signup') }}</span>
            </li>
            <li
                v-if="event?.state === EventState.OpenForSignup"
                class="permission-write-event-details context-menu-item"
                @click="publishPlannedCrew()"
            >
                <i class="fa-solid fa-earth-europe" />
                <span>{{ $t('views.events.edit.actions.publish-crew') }}</span>
            </li>
            <li class="permission-write-event-slots context-menu-item" @click="resetTeam()">
                <i class="fa-solid fa-rotate" />
                <span>{{ $t('views.events.edit.actions.reset-crew') }}</span>
            </li>
            <li class="permission-write-event-details context-menu-item text-error" @click="cancelEvent()">
                <i class="fa-solid fa-ban" />
                <span>{{ $t('views.events.edit.actions.cancel-event') }}</span>
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
import { useI18n } from 'vue-i18n';
import { deepCopy, diff, filterUndefined, updateDate, updateTime } from '@/common';
import type { Event, InputSelectOption, Location, Registration, Slot } from '@/domain';
import { EventState, Permission } from '@/domain';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import {
    AsyncButton,
    VConfirmationDialog,
    VInfo,
    VInputDate,
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
import { useEventSignupTypes } from '@/ui/composables/EventSignupTypes.ts';
import { useEventStates } from '@/ui/composables/EventStates.ts';
import { useEventTypes } from '@/ui/composables/EventTypes.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { Routes } from '@/ui/views/Routes.ts';
import CrewTab from '@/ui/views/events/edit/components/CrewTab.vue';
import VWarning from '../../../components/common/alerts/VWarning.vue';
import CrewEditor from './components/CrewEditor.vue';
import LocationEditDlg from './components/LocationEditDlg.vue';
import LocationsTable from './components/LocationsTable.vue';
import SlotEditDlg from './components/SlotEditDlg.vue';
import SlotsTable from './components/SlotsTable.vue';

enum Tab {
    EVENT_DATA = 'data',
    EVENT_TEAM = 'positions',
    EVENT_SLOTS = 'slots',
    EVENT_LOCATIONS = 'locations',
    EVENT_REGISTRATIONS = 'crew',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const router = useRouter();
const route = useRoute();
const eventStates = useEventStates();
const eventTypes = useEventTypes();
const eventSignupTypes = useEventSignupTypes();
const eventService = useEventService();
const eventUseCase = useEventUseCase();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const authUseCase = useAuthUseCase();
const usersUseCase = useUsersUseCase();
const usersAdminUseCase = useUserAdministrationUseCase();
const signedInUser = authUseCase.getSignedInUser();

const exportTemplates = ref<string[]>([]);
const eventOriginal = ref<Event | null>(null);
const event = ref<Event | null>(null);
const validation = useValidation(event, (evt) => (evt === null ? {} : eventService.validate(evt)));
const hasChanges = ref<boolean>(false);

const tab = ref<Tab>(Tab.EVENT_TEAM);
const tabs = computed<InputSelectOption<Tab>[]>(() => {
    const visibleTabs: Tab[] = [Tab.EVENT_DATA, Tab.EVENT_LOCATIONS];
    if (signedInUser.permissions.includes(Permission.WRITE_EVENT_SLOTS)) {
        visibleTabs.push(Tab.EVENT_TEAM);
        visibleTabs.push(Tab.EVENT_SLOTS);
        visibleTabs.push(Tab.EVENT_REGISTRATIONS);
    }
    return visibleTabs.map((it) => ({ value: it, label: t(`views.events.edit.tab.${it}`) }));
});

const createLocationDialog = ref<Dialog<void, Location | undefined> | null>(null);
const createSlotDialog = ref<Dialog<void, Slot | undefined> | null>(null);
const createRegistrationDialog = ref<Dialog<Event[], Registration | undefined> | null>(null);
const cancelEventDialog = ref<Dialog<Event, string | undefined> | null>(null);
const confirmDialog = ref<ConfirmationDialog | null>(null);

const hasEmptyRequiredSlots = computed<boolean>(() => {
    return event.value !== null && eventService.hasOpenRequiredSlots(event.value);
});

async function init(): Promise<void> {
    await fetchEvent();
    await fetchExportTemplates();
    preventPageUnloadOnUnsavedChanges();
}

async function fetchEvent(): Promise<void> {
    const year = parseInt(route.params.year as string, 10);
    const key = route.params.key as string;
    const event = await eventUseCase.getEventByKey(year, key, true);
    updateState(event);
    emit('update:tab-title', event.name);
}

async function fetchExportTemplates(): Promise<void> {
    exportTemplates.value = await eventAdministrationUseCase.getExportTemplates();
}

function preventPageUnloadOnUnsavedChanges(): void {
    watch(event, updateHasChanges, { deep: true });
    const removeNavigationGuard = router.beforeEach(async (to, from) => {
        if (to.name === from.name) {
            // we stay on the same page and only change
            return true;
        }
        if (hasChanges.value) {
            const continueNavigation = await confirmDialog.value?.open({
                title: t('views.events.edit.unsaved-changes.title'),
                message: t('views.events.edit.unsaved-changes.message'),
                cancel: t('generic.cancel'),
                submit: t('generic.discard-changes'),
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
    if (eventOriginal.value !== null && event.value !== null) {
        const changes = diff(eventOriginal.value, event.value);
        hasChanges.value = Object.keys(changes).length > 0;
    } else {
        hasChanges.value = true;
    }
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
            const updatedEvent = await eventAdministrationUseCase.cancelEvent(event.value, message);
            updateState(updatedEvent);
            await router.push({ name: Routes.EventsListAdmin });
        }
    }
}

async function addRegistration(): Promise<void> {
    if (event.value) {
        const result = await createRegistrationDialog.value?.open([event.value]);
        if (result) {
            event.value.registrations.push(result);
        }
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
        event.value = deepCopy(event.value); // to make sure the list is updated correctly
    }
}

async function contactTeam(): Promise<void> {
    if (event.value) {
        const userKeys = eventService
            .getAssignedRegistrations(event.value)
            .map((it) => it.userKey)
            .filter(filterUndefined);
        const users = await usersUseCase.getUsers(userKeys);
        await usersAdminUseCase.contactUsers(users);
    }
}

async function openEventForCrewSignup(): Promise<void> {
    if (event.value) {
        const updatedEvent = await eventAdministrationUseCase.updateEvent(event.value.key, {
            state: EventState.OpenForSignup,
        });
        updateState(updatedEvent);
    }
}

async function publishPlannedCrew(): Promise<void> {
    if (event.value) {
        const updatedEvent = await eventAdministrationUseCase.updateEvent(event.value.key, { state: EventState.Planned });
        updateState(updatedEvent);
    }
}

async function saveIfValid(): Promise<void> {
    if (event.value && validation.isValid.value) {
        const updatedEvent = await eventAdministrationUseCase.updateEvent(event.value.key, event.value);
        updateState(updatedEvent);
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

function updateState(value: Event): void {
    eventOriginal.value = value;
    event.value = deepCopy(value);
}

init();
</script>
