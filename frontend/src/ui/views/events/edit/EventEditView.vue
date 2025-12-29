<template>
    <DetailsPage :back-to="{ name: Routes.EventsListAdmin }" :class="$attrs.class">
        <template #header>
            {{ event?.name || $t('generic.loading') }}
        </template>
        <template #subheader> </template>
        <template #content>
            <div class="xs:px-8 flex flex-col px-4 md:px-16 xl:px-20">
                <section class="xl:max-w-5xl">
                    <VInfo v-if="event?.state === EventState.Draft" class="mt-4" dismissable clamp data-test-id="info-draft-state">
                        {{ $t('views.events.edit.info-draft') }}
                    </VInfo>
                    <VWarning v-else-if="event?.state === EventState.Canceled" class="mt-4" dismissable data-test-id="info-canceled-state">
                        {{ $t('views.events.edit.info-canceled') }}
                    </VWarning>
                    <VInfo
                        v-else-if="event?.signupType === EventSignupType.Assignment && event?.state === EventState.OpenForSignup"
                        class="mt-4"
                        data-test-id="info-crew-signup-state"
                        dismissable
                        clamp
                    >
                        {{ $t('views.events.edit.info-signup') }}
                    </VInfo>
                    <VWarning
                        v-else-if="event?.state === EventState.Planned && hasEmptyRequiredSlots"
                        class="mt-4"
                        dismissable
                        data-test-id="info-missing-crew"
                    >
                        {{ $t('views.events.edit.info-missing-crew') }}
                    </VWarning>
                </section>
            </div>
            <VTabs v-model="tab" :tabs="tabs" class="bg-surface sticky top-12 z-20 pt-4 lg:top-14 xl:top-20">
                <template #[Tab.EVENT_DATA]>
                    <div class="max-w-2xl space-y-8 xl:space-y-16">
                        <TabEventDetailsForm v-if="event" v-model:event="event" />
                    </div>
                </template>
                <template #[Tab.EVENT_CREW_EDITOR]>
                    <div class="xl:max-w-5xl">
                        <TabCrewEditor v-if="event" v-model:event="event" :crew="crew" :waitinglist="waitinglist" />
                    </div>
                </template>
                <template #[Tab.EVENT_SLOTS]>
                    <div class="xl:max-w-5xl">
                        <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                            <TabSlots v-if="event" v-model:event="event" :crew="crew" :registrations="registrations" />
                        </div>
                    </div>
                </template>
                <template #[Tab.EVENT_LOCATIONS]>
                    <div class="xl:max-w-5xl">
                        <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
                            <TabLocations v-if="event" v-model:event="event" />
                        </div>
                    </div>
                </template>
                <template #[Tab.EVENT_REGISTRATIONS]>
                    <div class="xl:max-w-5xl">
                        <TabRegistrations v-if="event" v-model:event="event" :crew="crew" :waitinglist="waitinglist" />
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
                <button
                    v-if="tab === Tab.EVENT_CREW_EDITOR"
                    class="permission-write-registrations btn-secondary"
                    @click="addRegistration()"
                >
                    <i class="fa-solid fa-user-plus" />
                    <span>{{ $t('views.events.edit.actions.add-registration') }}</span>
                </button>
                <button
                    v-else-if="tab === Tab.EVENT_REGISTRATIONS"
                    class="permission-write-registrations btn-secondary"
                    @click="addRegistration()"
                >
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
            <li class="permission-write-registrations context-menu-item" data-test-id="action-add-registration" @click="addRegistration()">
                <i class="fa-solid fa-user-plus" />
                <span>{{ $t('views.events.edit.actions.add-registration') }}</span>
            </li>
            <li
                v-if="event?.signupType === EventSignupType.Assignment"
                class="permission-write-event-slots context-menu-item"
                data-test-id="action-add-slot"
                @click="addSlot()"
            >
                <i class="fa-solid fa-list" />
                <span>{{ $t('views.events.edit.actions.add-slot') }}</span>
            </li>
            <li class="permission-write-event-details context-menu-item" data-test-id="action-add-location" @click="addLocation()">
                <i class="fa-solid fa-route" />
                <span>{{ $t('views.events.edit.actions.add-location') }}</span>
            </li>
            <li class="permission-read-user-details context-menu-item" data-test-id="action-contact-crew" @click="contactCrew()">
                <i class="fa-solid fa-envelope" />
                <span>{{ $t('views.events.edit.actions.contact-crew') }}</span>
            </li>
            <template v-if="event">
                <li
                    v-for="template in exportTemplates"
                    :key="template"
                    class="permission-read-user-details context-menu-item"
                    data-test-id="action-export"
                    @click="eventAdministrationUseCase.exportEvent(event, template)"
                >
                    <i class="fa-solid fa-file-excel" />
                    <span>{{ $t('views.events.admin-list.action.exportToTemplate', { template }) }}</span>
                </li>
            </template>
            <template v-if="event?.signupType === EventSignupType.Assignment">
                <li
                    v-if="event?.state === EventState.Draft"
                    class="permission-write-event-details context-menu-item"
                    data-test-id="action-open-for-crew-signup"
                    @click="openEventForCrewSignup()"
                >
                    <i class="fa-solid fa-lock-open" />
                    <span>{{ $t('views.events.edit.open-signup') }}</span>
                </li>
                <li
                    v-if="event?.state === EventState.OpenForSignup"
                    class="permission-write-event-details context-menu-item"
                    data-test-id="action-publish-crew-planning"
                    @click="publishPlannedCrew()"
                >
                    <i class="fa-solid fa-earth-europe" />
                    <span>{{ $t('views.events.edit.actions.publish-crew') }}</span>
                </li>
                <li
                    class="permission-write-event-slots context-menu-item"
                    data-test-id="action-reset-crew-planning"
                    @click="resetCrewPlanning()"
                >
                    <i class="fa-solid fa-rotate" />
                    <span>{{ $t('views.events.edit.actions.reset-crew') }}</span>
                </li>
            </template>
            <li class="permission-write-event-details context-menu-item text-error" data-test-id="action-cancel" @click="cancelEvent()">
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
import {
    useAuthUseCase,
    useEventAdministrationUseCase,
    useEventUseCase,
    useUserAdministrationUseCase,
    useUsersUseCase,
} from '@/application';
import { deepCopy, diff, filterUndefined } from '@/common';
import type { Event, InputSelectOption, Location, Registration, ResolvedRegistrationSlot, Slot } from '@/domain';
import { EventSignupType, EventState, Permission } from '@/domain';
import { useEventService } from '@/domain/services.ts';
import type { ConfirmationDialog, Dialog } from '@/ui/components/common';
import { AsyncButton, VConfirmationDialog, VInfo, VTabs } from '@/ui/components/common';
import CreateRegistrationDlg from '@/ui/components/events/CreateRegistrationDlg.vue';
import EventCancelDlg from '@/ui/components/events/EventCancelDlg.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import { useValidation } from '@/ui/composables/Validation.ts';
import { Routes } from '@/ui/views/Routes.ts';
import TabEventDetailsForm from '@/ui/views/events/edit/TabEventDetailsForm.vue';
import TabRegistrations from '@/ui/views/events/edit/TabRegistrations.vue';
import VWarning from '../../../components/common/alerts/VWarning.vue';
import TabCrewEditor from './TabCrewEditor.vue';
import TabLocations from './TabLocations.vue';
import TabSlots from './TabSlots.vue';
import LocationEditDlg from './components/LocationEditDlg.vue';
import SlotEditDlg from './components/SlotEditDlg.vue';

enum Tab {
    EVENT_DATA = 'data',
    EVENT_CREW_EDITOR = 'crew',
    EVENT_SLOTS = 'slots',
    EVENT_LOCATIONS = 'locations',
    EVENT_REGISTRATIONS = 'registrations',
}

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const router = useRouter();
const route = useRoute();
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
const waitinglist = ref<ResolvedRegistrationSlot[]>([]);
const crew = ref<ResolvedRegistrationSlot[]>([]);
const validation = useValidation(event, (evt) => (evt === null ? {} : eventService.validate(evt)));
const hasChanges = ref<boolean>(false);

const registrations = computed<ResolvedRegistrationSlot[]>(() => crew.value.concat(waitinglist.value));
const tabs = computed<InputSelectOption<Tab>[]>(() => {
    const visibleTabs: Tab[] = [Tab.EVENT_DATA, Tab.EVENT_LOCATIONS];
    if (signedInUser.permissions.includes(Permission.WRITE_EVENT_SLOTS)) {
        visibleTabs.push(Tab.EVENT_REGISTRATIONS);
        if (event.value?.signupType === EventSignupType.Assignment) {
            visibleTabs.push(Tab.EVENT_SLOTS, Tab.EVENT_CREW_EDITOR);
        }
    }
    return visibleTabs.map((it) => ({ value: it, label: t(`views.events.edit.tab.${it}`) }));
});
const tab = ref<Tab>(tabs.value[0].value);

const createLocationDialog = ref<Dialog<void, Location | undefined> | null>(null);
const createSlotDialog = ref<Dialog<void, Slot | undefined> | null>(null);
const createRegistrationDialog = ref<Dialog<Event[], Registration | undefined> | null>(null);
const cancelEventDialog = ref<Dialog<Event, string | undefined> | null>(null);
const confirmDialog = ref<ConfirmationDialog | null>(null);

const hasEmptyRequiredSlots = computed<boolean>(() => {
    return event.value !== null && eventService.hasOpenRequiredSlots(event.value);
});

async function init(): Promise<void> {
    watch(
        () => event.value,
        () => fetchCrew(),
        { deep: true }
    );
    await Promise.all([fetchEvent(), fetchExportTemplates()]);
    preventPageUnloadOnUnsavedChanges();
}

async function fetchEvent(): Promise<void> {
    const year = Number.parseInt(route.params.year as string, 10);
    const key = route.params.key as string;
    const event = await eventUseCase.getEventByKey(year, key, true);
    updateState(event);
    emit('update:tab-title', event.name);
}

async function fetchCrew(): Promise<void> {
    console.log('⚙️ Resolving crew and waiting list');
    if (!event.value) {
        crew.value = [];
        waitinglist.value = [];
        return;
    }
    const all = await eventUseCase.resolveRegistrations(event.value);
    if (event.value?.signupType === EventSignupType.Open) {
        crew.value = all;
        waitinglist.value = [];
    } else {
        crew.value = eventAdministrationUseCase.filterForCrew(all);
        waitinglist.value = eventAdministrationUseCase.filterForWaitingList(all);
    }
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

function resetCrewPlanning(): void {
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

async function contactCrew(): Promise<void> {
    if (event.value) {
        let userKeys = event.value.registrations.map((it) => it.userKey).filter(filterUndefined);
        if (event.value.signupType === EventSignupType.Open) {
            userKeys = eventService
                .getAssignedRegistrations(event.value)
                .map((it) => it.userKey)
                .filter(filterUndefined);
        }
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
