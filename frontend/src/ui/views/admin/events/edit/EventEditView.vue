<template>
    <DetailsPage :back-to="{ name: Routes.EventsAdmin }">
        <template #header>
            <div v-if="event" class="">
                <h1 class="mb-1 mt-4 hidden truncate xl:block">{{ event.name || 'Err' }}</h1>
                <p class="mb-4 hidden text-sm">
                    {{ $d(event.start, DateTimeFormat.DDD_DD_MM) }}
                    -
                    {{ $d(event.end, DateTimeFormat.DDD_DD_MM) }}
                </p>
                <div class="-mx-4 flex flex-wrap items-start font-semibold">
                    <div class="w-full">
                        <VWarning v-if="hasEmptyRequiredSlots" class="mr-2 mt-4">
                            Die Vorraussetzungen für eine sichere Mindesbesatzung für diese Reise sind noch nicht
                            erfüllt!
                        </VWarning>
                    </div>
                </div>
            </div>
        </template>
        <template #content>
            <VTabs v-model="tab" :tabs="tabs" class="sticky top-10 z-20 bg-primary-50 pt-8 xl:top-0">
                <template #[Tab.EVENT_DATA]>
                    <div class="max-w-2xl space-y-8 xl:space-y-16">
                        <section v-if="event" class="-mx-4">
                            <div class="mb-4">
                                <VInputLabel>Status</VInputLabel>
                                <VInputSelect
                                    v-model="event.state"
                                    :options="[
                                        { value: EventState.Draft, label: 'Entwurf' },
                                        { value: EventState.OpenForSignup, label: 'Anmeldungen freigegeben' },
                                        { value: EventState.Planned, label: 'Crew geplant' },
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
                    <div class="-mx-8 overflow-y-auto px-8">
                        <VTable :items="slots" :page-size="-1" class="interactive-table" @click="editSlot($event.key)">
                            <template #head>
                                <th class="hidden w-0 md:table-cell"></th>
                                <th class="w-0"></th>
                                <th class="w-1/3">Name</th>
                                <th class="w-2/3">Mögliche Positionen</th>
                                <th class="w-64">Status</th>
                            </template>
                            <template #row="{ item, index }">
                                <td class="hidden md:table-cell">
                                    <button class="cursor-move">
                                        <i class="fa-solid fa-grip-vertical text-sm opacity-25"></i>
                                    </button>
                                </td>
                                <td>
                                    <span
                                        class="inline-block rounded-full bg-gray-200 px-2 py-1 text-sm font-semibold text-gray-700"
                                    >
                                        #{{ index + 1 }}
                                    </span>
                                </td>
                                <td>
                                    <div class="whitespace-nowrap font-semibold">
                                        {{ item.name || item.position.name }}
                                    </div>
                                </td>
                                <td class="">
                                    <div class="flex items-center">
                                        <div
                                            v-for="position in item.alternativePositions"
                                            :key="position.key"
                                            :style="{ background: position.color }"
                                            class="position mb-1 mr-1 text-xs"
                                        >
                                            <span>{{ position.name }}</span>
                                        </div>
                                    </div>
                                </td>
                                <td class="">
                                    <span
                                        v-if="item.filled"
                                        class="inline-flex w-auto items-center rounded-full bg-green-100 py-1 pl-3 pr-4 text-green-700"
                                    >
                                        <i class="fa-solid fa-circle-check"></i>
                                        <span class="ml-2 whitespace-nowrap font-semibold">Besetzt</span>
                                    </span>
                                    <span
                                        v-else-if="item.required"
                                        class="inline-flex w-auto items-center rounded-full bg-yellow-100 py-1 pl-3 pr-4 text-yellow-700"
                                    >
                                        <i class="fa-solid fa-warning"></i>
                                        <span class="ml-2 whitespace-nowrap font-semibold">Nicht besetzt</span>
                                    </span>
                                    <span
                                        v-else
                                        class="inline-flex w-auto items-center rounded-full bg-blue-100 py-1 pl-3 pr-4 text-blue-700"
                                    >
                                        <i class="fa-solid fa-circle-info"></i>
                                        <span class="ml-2 whitespace-nowrap font-semibold">Optional</span>
                                    </span>
                                </td>
                            </template>
                        </VTable>
                    </div>
                    <SlotEditDlg ref="editSlotDialog" />
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
                <button v-if="tab === Tab.EVENT_SLOTS" class="btn-secondary" @click="addSlot()">
                    <i class="fa-regular fa-square-plus" />
                    <span>Slot hinzufügen</span>
                </button>
            </div>
        </template>
        <template #actions-menu>
            <li class="context-menu-item" @click="addRegistration()">
                <i class="fa-solid fa-user-plus" />
                <span>Anmeldung hinzufügen</span>
            </li>
            <li class="context-menu-item" @click="contactTeam()">
                <i class="fa-solid fa-paper-plane" />
                <span>Crew kontaktieren</span>
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
                @click="deleteEvent()"
            >
                <i class="fa-solid fa-trash" />
                <span>Reise absagen</span>
            </li>
        </template>
    </DetailsPage>
    <CreateRegistrationDlg ref="createRegistrationDialog" />
    <SlotCreateDlg ref="createSlotDialog" />
    <EventCancelDlg ref="deleteEventDialog" />
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { filterUndefined } from '@/common';
import { DateTimeFormat } from '@/common/date';
import type { Event, Position, PositionKey, Slot, SlotCriticality, SlotKey } from '@/domain';
import { EventState } from '@/domain';
import { EventType, Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import {
    AsyncButton,
    VInputDate,
    VInputLabel,
    VInputSelect,
    VInputText,
    VInputTextArea,
    VInputTime,
    VTable,
    VTabs,
} from '@/ui/components/common';
import VWarning from '@/ui/components/common/alerts/VWarning.vue';
import EventCancelDlg from '@/ui/components/events/EventCancelDlg.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import {
    useAuthUseCase,
    useEventAdministrationUseCase,
    useEventUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { useValidation } from '@/ui/composables/Validation';
import { Routes } from '@/ui/views/Routes';
import CreateRegistrationDlg from '@/ui/views/admin/events/components/CreateRegistrationDlg.vue';
import CrewEditor from './CrewEditor.vue';
import SlotCreateDlg from './SlotCreateDlg.vue';
import SlotEditDlg from './SlotEditDlg.vue';

enum Tab {
    EVENT_DATA = 'app.edit-event.tab.data',
    EVENT_POSITIONS = 'Crew verwalten',
    EVENT_SLOTS = 'Slots',
}

interface SlotTableItem {
    key: SlotKey;
    name?: string;
    required: boolean;
    criticality: SlotCriticality;
    position: Position;
    alternativePositions: Position[];
    filled: boolean;
}

type RouteEmits = (e: 'update:title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const router = useRouter();
const route = useRoute();
const eventService = useEventService();
const eventUseCase = useEventUseCase();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const usersUseCase = useUsersUseCase();
const authUseCase = useAuthUseCase();
const signedInUser = authUseCase.getSignedInUser();

const event = ref<Event | null>(null);
const validation = useValidation(event, (evt) => (evt === null ? {} : eventService.validate(evt)));

const tabs = [Tab.EVENT_POSITIONS, Tab.EVENT_DATA, Tab.EVENT_SLOTS];
const tab = ref<Tab>(Tab.EVENT_POSITIONS);
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());

const createSlotDialog = ref<Dialog<Event, Event> | null>(null);
const editSlotDialog = ref<Dialog<Slot, Slot> | null>(null);
const createRegistrationDialog = ref<Dialog<Event, Event> | null>(null);
const deleteEventDialog = ref<Dialog<Event, string> | null>(null);

const slots = computed<SlotTableItem[]>(() => {
    if (!event.value) {
        return [];
    }
    return event.value.slots.map((slot) => ({
        key: slot.key,
        name: slot.positionName,
        required: slot.criticality >= 1,
        criticality: slot.criticality,
        position: positions.value.get(slot.positionKeys[0]),
        alternativePositions: slot.positionKeys.map((it) => positions.value.get(it)).filter(filterUndefined),
        filled: eventService.isSlotFilled(event.value, slot.key),
    }));
});

const hasEmptyRequiredSlots = computed<boolean>(() => {
    return event.value !== null && eventService.hasOpenRequiredSlots(event.value);
});

async function init(): Promise<void> {
    await fetchPositions();
    await fetchEvent();
}

async function fetchPositions(): Promise<void> {
    positions.value = await usersUseCase.resolvePositionNames();
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

async function deleteEvent(): Promise<void> {
    if (event.value && deleteEventDialog.value) {
        const evt = event.value;
        await deleteEventDialog.value
            .open(evt)
            .then((message) => eventAdministrationUseCase.cancelEvent(evt, message))
            .then(() => router.push({ name: Routes.EventsAdmin }))
            .catch(() => console.debug('dialog was canceled'));
    }
}

async function addRegistration(): Promise<void> {
    if (createRegistrationDialog.value && event.value) {
        await createRegistrationDialog.value.open(event.value).catch(() => console.debug('dialog was canceled'));
    }
}

async function addSlot(): Promise<void> {
    if (createSlotDialog.value && event.value) {
        await createSlotDialog.value.open(event.value).catch(() => console.debug('dialog was canceled'));
    }
}

async function contactTeam(): Promise<void> {
    if (event.value) {
        await eventAdministrationUseCase.contactTeam(event.value);
    }
}

async function editSlot(slotkey: SlotKey): Promise<void> {
    const slot = event.value?.slots.find((it) => it.key === slotkey);
    const evt = event.value;
    if (slot && evt) {
        await editSlotDialog.value
            ?.open(slot)
            .then((s) => (event.value = eventService.updateSlot(evt, s)))
            .catch(() => console.debug('dialog was canceled'));
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
