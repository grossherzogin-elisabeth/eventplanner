<template>
    <DetailsPage :back-to="{ name: Routes.EventsAdmin }">
        <template #header>
            <div v-if="event">
                <h1 class="mb-1 truncate">{{ event.name }}</h1>
                <p class="mb-4 text-sm">
                    {{ $d(event.start, DateTimeFormat.DDD_DD_MM) }}
                    -
                    {{ $d(event.end, DateTimeFormat.DDD_DD_MM) }}
                </p>
                <div class="-mx-4 flex flex-wrap items-start font-semibold">
                    <div class="w-full">
                        <VWarning v-if="hasEmptyRequiredSlots" class="mr-2 mt-2">
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
                            <!--<div class="mb-2">-->
                            <!--    <VInputLabel>Typ</VInputLabel>-->
                            <!--    <VInputSelect v-model="event.type" :options="eventTypeOptions" required />-->
                            <!--</div>-->
                            <div class="mb-2">
                                <VInputLabel>Name</VInputLabel>
                                <VInputText v-model="event.name" required />
                            </div>
                            <div class="mb-2">
                                <VInputLabel>Typ</VInputLabel>
                                <VInputSelect
                                    v-model="event.type"
                                    :options="[
                                        { value: EventType.WorkEvent, label: 'Arbeitsdienst' },
                                        { value: EventType.VOYAGE, label: 'Mehrtagesreise' },
                                    ]"
                                    required
                                />
                            </div>
                            <div class="mb-2">
                                <VInputLabel>Beschreibung</VInputLabel>
                                <VInputTextArea v-model="event.description" />
                            </div>
                            <div class="mb-2 flex space-x-4">
                                <div class="flex-grow">
                                    <VInputLabel>Startdatum</VInputLabel>
                                    <VInputDate v-model="event.start" required />
                                </div>
                                <div class="flex-grow">
                                    <VInputLabel>Crew an Board</VInputLabel>
                                    <VInputText model-value="16:00" required />
                                </div>
                            </div>

                            <div class="mb-2 flex space-x-4">
                                <div class="flex-grow">
                                    <VInputLabel>Enddatum</VInputLabel>
                                    <VInputDate v-model="event.end" required />
                                </div>
                                <div class="flex-grow">
                                    <VInputLabel>Crew von Board</VInputLabel>
                                    <VInputText model-value="16:00" required />
                                </div>
                            </div>

                            <div class="flex-grow">
                                <VInputLabel>Seemeilen</VInputLabel>
                                <VInputText model-value="300" required />
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
                                            class="position mb-1 mr-1"
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
            <AsyncButton :action="save">
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
                <AsyncButton
                    v-if="signedInUser.permissions.includes(Permission.WRITE_EVENTS)"
                    :action="save"
                    class="btn-secondary"
                >
                    <template #icon>
                        <i class="fa-solid fa-paper-plane" />
                    </template>
                    <template #label>
                        <span>Anmeldungen freischalten</span>
                    </template>
                </AsyncButton>
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
                <i class="fa-solid fa-envelope" />
                <span>Crew kontaktieren</span>
            </li>
            <li class="context-menu-item" @click="contactWaitingList()">
                <i class="fa-solid fa-envelope" />
                <span>Warteliste kontaktieren</span>
            </li>
            <li
                v-if="signedInUser.permissions.includes(Permission.EVENT_TEAM_WRITE)"
                class="context-menu-item"
                @click="resetTeam()"
            >
                <i class="fa-solid fa-rotate" />
                <span>Crew zurücksetzen</span>
            </li>
        </template>
    </DetailsPage>
    <CreateRegistrationDlg ref="createRegistrationDialog" />
    <SlotCreateDlg ref="createSlotDialog" />
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ArrayUtils } from '@/common';
import { DateTimeFormat } from '@/common/date';
import { Event, Position, PositionKey, Slot, SlotCriticality, SlotKey } from '@/domain';
import { EventType, Permission } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import {
    AsyncButton,
    VInputDate,
    VInputLabel,
    VInputSelect,
    VInputText,
    VInputTextArea,
    VTable,
    VTabs,
} from '@/ui/components/common';
import VWarning from '@/ui/components/common/alerts/VWarning.vue';
import CreateRegistrationDlg from '@/ui/components/events/CreateRegistrationDlg.vue';
import DetailsPage from '@/ui/components/partials/DetailsPage.vue';
import {
    useAuthUseCase,
    useErrorHandling,
    useEventAdministrationUseCase,
    useEventUseCase,
    useUsersUseCase,
} from '@/ui/composables/Application';
import { useEventService } from '@/ui/composables/Domain';
import { Routes } from '@/ui/views/Routes';
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

const route = useRoute();
const eventService = useEventService();
const eventUseCase = useEventUseCase();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const usersUseCase = useUsersUseCase();
const authUseCase = useAuthUseCase();
const errorHandlingUseCase = useErrorHandling();
const signedInUser = authUseCase.getSignedInUser();

const tabs = [Tab.EVENT_POSITIONS, Tab.EVENT_DATA, Tab.EVENT_SLOTS];
const tab = ref<Tab>(Tab.EVENT_POSITIONS);
const event = ref<Event | null>(null);
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());

const createSlotDialog = ref<Dialog<Event> | null>(null);
const editSlotDialog = ref<Dialog<Slot> | null>(null);
const createRegistrationDialog = ref<Dialog<Event> | null>(null);

const slots = computed<SlotTableItem[]>(() => {
    if (!event.value) {
        return [];
    }
    return event.value.slots.map((slot) => ({
        key: slot.key,
        name: slot.positionName,
        required: slot.criticality >= 1,
        criticality: slot.criticality,
        position: positions.value.get(slot.positionKeys[0])!,
        alternativePositions: slot.positionKeys.map((it) => positions.value.get(it)).filter(ArrayUtils.filterUndefined),
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
}

function resetTeam(): void {
    if (event.value) {
        event.value.registrations.forEach((it) => (it.slotKey = undefined));
        event.value.assignedUserCount = 0;
    }
}

async function addRegistration(): Promise<void> {
    if (createRegistrationDialog.value && event.value) {
        await createRegistrationDialog.value.open(event.value).catch();
    }
}

async function addSlot(): Promise<void> {
    if (createSlotDialog.value && event.value) {
        await createSlotDialog.value.open(event.value).catch();
    }
}

async function contactTeam(): Promise<void> {
    if (event.value) {
        await eventAdministrationUseCase
            .contactTeam(event.value)
            .catch((e) => errorHandlingUseCase.handleUnexpectedError(e));
    }
}

async function contactWaitingList(): Promise<void> {
    if (event.value) {
        await eventAdministrationUseCase
            .contactWaitingList(event.value)
            .catch((e) => errorHandlingUseCase.handleUnexpectedError(e));
    }
}

async function editSlot(slotkey: SlotKey): Promise<void> {
    let slot = event.value?.slots.find((it) => it.key === slotkey);
    try {
        slot = await editSlotDialog.value?.open(slot);
        if (event.value && slot) {
            event.value = eventService.updateSlot(event.value, slot);
        }
    } catch (e) {
        // ignore, dialog has been cancelled
    }
}

async function save(): Promise<void> {
    if (event.value) {
        try {
            await eventAdministrationUseCase.updateEvent(event.value.key, event.value);
        } catch (e) {
            errorHandlingUseCase.handleError({
                title: 'Speichern fehlgeschlagen',
                message: `Deine Änderungen konnten nicht gespeichert werden. Bitte versuche es erneut. Sollte der Fehler
                    wiederholt auftreten, melde ihn gerne.`,
                error: e,
                retry: () => save(),
            });
            throw e;
        }
    }
}

init();
</script>
