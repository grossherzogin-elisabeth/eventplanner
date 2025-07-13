<template>
    <!-- overview -->
    <div v-if="view === View.OVERVIEW">
        <p v-if="events.length === 1" class="mb-8 text-sm">
            Bitte überprüfe die Angaben zu deiner Anmeldung für
            <i>{{ events[0].name }}.</i>
        </p>
        <div v-else class="mb-8 text-sm">
            <p class="mb-2">Bitte prüfe deine Angaben. Du meldest dich hiermit für die folgenden Veranstaltungen an:</p>
            <ul class="list-inside list-disc pl-2">
                <li v-for="evt in events" :key="evt.key" class="truncate">
                    {{ evt.name }}
                </li>
            </ul>
        </div>
        <ul class="border-t border-onsurface-variant border-opacity-20 md:mx-0">
            <li
                class="flex items-center border-b border-onsurface-variant border-opacity-20 py-3 sm:px-0"
                :class="availablePositionsForSignedInUser.length > 1 ? 'cursor-pointer' : 'pointer-events-none'"
                @click="emit('update:view', View.POSITION)"
            >
                <div class="mr-4 w-0 flex-grow">
                    <p class="mb-1 text-sm font-bold">Deine Position</p>
                    <p>{{ positions.get(props.registration.positionKey).name }}</p>
                </div>
                <button v-if="availablePositionsForSignedInUser.length > 1" class="icon-button -mr-4">
                    <i class="fa-solid fa-chevron-right" />
                </button>
            </li>
            <li
                v-if="hasSingleDayEvent"
                class="flex cursor-pointer items-center border-b border-onsurface-variant border-opacity-20 py-3 sm:px-0"
                @click="emit('update:view', View.OVERNIGHT)"
            >
                <div class="mr-4 w-0 flex-grow">
                    <p class="mb-1 text-sm font-bold">Übernachtung an Bord</p>
                    <p v-if="!props.registration.overnightStay">Nein</p>
                    <p v-else-if="props.registration.arrival">Ja, Anreise am Vortag</p>
                    <p v-else>Ja</p>
                </div>
                <button class="icon-button -mr-4">
                    <i class="fa-solid fa-chevron-right" />
                </button>
            </li>
            <li
                class="flex cursor-pointer items-center border-b border-onsurface-variant border-opacity-20 py-3 sm:px-0"
                @click="emit('update:view', View.NOTE)"
            >
                <div class="mr-4 w-0 flex-grow">
                    <p class="mb-1 text-sm font-bold">Notiz fürs Büro</p>
                    <p v-if="props.registration.note" class="truncate italic">{{ props.registration.note }}</p>
                    <p v-else>-</p>
                </div>
                <button class="icon-button -mr-4">
                    <i class="fa-solid fa-chevron-right" />
                </button>
            </li>
        </ul>

        <VInfo v-if="events.length > 1" class="-mx-4 mt-8 text-sm">
            Wenn du dich bei mehreren Veranstaltungen anmeldest, werden diese Angaben für alle Anmeldungen übernommen. Du kannst jede
            Anmeldung im Nachgang einzeln bearbeiten, um z.B. individuelle Notizen fürs Büro zu hinterlegen.
        </VInfo>
    </div>
    <!-- position -->
    <div v-else-if="view === View.POSITION">
        <p class="mb-8 text-sm">Du hast die Qualifikation für mehrere Positionen. Mit welcher Position möchtest du dich anmelden?</p>
        <VInputSelectionList
            :model-value="props.registration.positionKey"
            class="mb-8"
            :options="availablePositionsForSignedInUser"
            orientation="vertical"
            @update:model-value="updateRegistration({ positionKey: $event })"
        />
    </div>
    <!-- note -->
    <div v-else-if="view === View.NOTE">
        <p class="mb-8 text-sm">
            Hier kannst du zusätzliche Informationen für deine Anmeldung hinterlegen, die für das Büro bei der Planung relevant sein
            könnten.
        </p>
        <div class="-mx-4 mb-4">
            <VInputLabel>Notiz fürs Büro</VInputLabel>
            <VInputTextArea :model-value="props.registration.note" @update:model-value="updateRegistration({ note: $event })" />
        </div>
    </div>
    <!-- overnight stay -->
    <div v-else-if="view === View.OVERNIGHT">
        <p class="mb-4 text-sm">
            Bitte teile uns mit, ob du an Bord übernachten möchtest, und falls ja, ob du bereits am Vortag anreist. Bei mehrtägigen
            Veranstaltungen wird immer von einer Übernachtung an Bord ausgegangen.
        </p>
        <div class="mb-4">
            <VInputCheckBox
                :model-value="props.registration.overnightStay"
                label="Ich möchte an Bord übernachten"
                @update:model-value="updateRegistration({ overnightStay: $event })"
            />
        </div>
        <div class="mb-4">
            <VInputCheckBox
                :model-value="props.registration.arrival !== undefined"
                label="Ich möchte am Vortag anreisen"
                @update:model-value="
                    (value) =>
                        updateRegistration({
                            arrival: value
                                ? cropToPrecision(subtractFromDate(events[0]?.start ?? new Date(), { days: 1 }), 'days')
                                : undefined,
                        })
                "
            />
        </div>
        <VInfo class="-mx-4"> Bitte beachte, das eine Übernachtung an Bord vor Reisebeginn nicht immer garantiert werden kann. </VInfo>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { cropToPrecision, isSameDate, subtractFromDate } from '@/common';
import type { Event, InputSelectOption, PositionKey, Registration, UserDetails } from '@/domain';
import { VInfo } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VInputLabel, VInputSelectionList, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { usePositions } from '@/ui/composables/Positions';

interface Props {
    event: Event | Event[];
    registration: Registration;
    view: View;
}
interface RouteEmits {
    (e: 'update:registration', value: Registration): void;
    (e: 'update:view', value: string): void;
}

const props = defineProps<Props>();
const emit = defineEmits<RouteEmits>();

const usersUseCase = useUsersUseCase();
const positions = usePositions();

const signedInUserDetails = ref<UserDetails | null>(null);
const events = computed<Event[]>(() => (Array.isArray(props.event) ? props.event : [props.event]));
const hasSingleDayEvent = computed<boolean>(() => events.value.find((it) => isSameDate(it.start, it.end)) !== undefined);
const availablePositionsForSignedInUser = computed<InputSelectOption<string | undefined>[]>(() => {
    const validPositionKeys: (PositionKey | undefined)[] = signedInUserDetails.value?.positionKeys || [];
    if (!validPositionKeys.includes(props.registration.positionKey)) {
        validPositionKeys.push(props.registration.positionKey);
    }
    return positions.options.value.filter((it) => validPositionKeys.includes(it.value));
});

function updateRegistration(update: Partial<Registration>): void {
    const updated = Object.assign(props.registration, update);
    emit('update:registration', updated);
}

async function init(): Promise<void> {
    await fetchSignedInUserDetails();
    watch(
        () => props.registration.positionKey,
        () => savePreferredPosition()
    );
}

async function fetchSignedInUserDetails(): Promise<void> {
    signedInUserDetails.value = await usersUseCase.getUserDetailsForSignedInUser();
}

async function savePreferredPosition(): Promise<void> {
    await usersUseCase.saveUserSettings({ preferredPosition: props.registration.positionKey });
}

init();
</script>
<script lang="ts">
export enum View {
    OVERVIEW = 'overview',
    POSITION = 'position',
    OVERNIGHT = 'overnight',
    NOTE = 'note',
}
</script>
