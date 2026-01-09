<template>
    <!-- overview -->
    <div v-if="view === View.OVERVIEW">
        <p v-if="filteredEvents.length === 1" class="mb-8 text-sm">
            Bitte überprüfe deine Anmeldung für
            <i>{{ filteredEvents[0].name }}.</i> Du kannst alle Angaben später noch ändern.
        </p>
        <div v-else class="mb-8 text-sm">
            <p class="mb-2">Bitte überprüfe deine Anmeldung für die folgenden Veranstaltungen:</p>
            <ul class="mb-2 list-inside list-disc pl-2">
                <li v-for="evt in filteredEvents" :key="evt.key" class="truncate">
                    {{ evt.name }}
                </li>
            </ul>
            <p>Du kannst alle Angaben später noch ändern.</p>
        </div>
        <ul class="border-onsurface-variant/20 border-t md:mx-0">
            <li
                class="border-onsurface-variant/20 flex items-center border-b py-3 sm:px-0"
                :class="availablePositionsForSignedInUser.length > 1 ? 'cursor-pointer' : 'pointer-events-none'"
                @click="emit('update:view', View.POSITION)"
            >
                <div class="mr-4 w-0 grow">
                    <p class="mb-1 text-sm font-bold">Deine Position</p>
                    <p>{{ positions.get(props.registration.positionKey).name }}</p>
                </div>
                <button v-if="availablePositionsForSignedInUser.length > 1" class="btn-icon -mr-4">
                    <i class="fa-solid fa-chevron-right" />
                </button>
            </li>
            <li
                v-if="hasSingleDayEvent"
                class="border-onsurface-variant/20 flex cursor-pointer items-center border-b py-3 sm:px-0"
                @click="emit('update:view', View.OVERNIGHT)"
            >
                <div class="mr-4 w-0 grow">
                    <p class="mb-1 text-sm font-bold">Übernachtung an Bord</p>
                    <p v-if="!props.registration.overnightStay">Nein</p>
                    <p v-else>Ja</p>
                </div>
                <button class="btn-icon -mr-4">
                    <i class="fa-solid fa-chevron-right" />
                </button>
            </li>
            <li
                class="border-onsurface-variant/20 flex cursor-pointer items-center border-b py-3 sm:px-0"
                @click="emit('update:view', View.ARRIVAL)"
            >
                <div class="mr-4 w-0 grow">
                    <p class="mb-1 text-sm font-bold">Anreise am Vortag</p>
                    <p v-if="props.registration.arrival">Ja</p>
                    <p v-else>Nein</p>
                </div>
                <button class="btn-icon -mr-4">
                    <i class="fa-solid fa-chevron-right" />
                </button>
            </li>
            <li
                class="border-onsurface-variant/20 flex cursor-pointer items-center border-b py-3 sm:px-0"
                @click="emit('update:view', View.NOTE)"
            >
                <div class="mr-4 w-0 grow">
                    <p class="mb-1 text-sm font-bold">Notiz fürs Büro</p>
                    <p v-if="props.registration.note" class="truncate italic">{{ props.registration.note }}</p>
                    <p v-else>-</p>
                </div>
                <button class="btn-icon -mr-4">
                    <i class="fa-solid fa-chevron-right" />
                </button>
            </li>
        </ul>

        <VInfo v-if="filteredEvents.length > 1" class="xs:-mx-4 mt-8 text-sm">
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
        <div class="mb-4">
            <VInputTextArea
                label="Notiz fürs Büro"
                :model-value="props.registration.note"
                @update:model-value="updateRegistration({ note: $event })"
            />
        </div>
    </div>
    <!-- overnight stay -->
    <div v-else-if="view === View.OVERNIGHT">
        <p class="mb-8 text-sm">
            Möchtest du an Bord übernachten? Bei mehrtägigen Veranstaltungen wird immer von einer Übernachtung an Bord ausgegangen.
        </p>
        <div class="mb-4">
            <VInputCheckBox
                :model-value="props.registration.overnightStay"
                label="Ich möchte an Bord übernachten"
                @update:model-value="updateRegistration({ overnightStay: $event })"
            />
        </div>
        <VInfo class="xs:-mx-4 mt-8">
            Bitte beachte, das eine Übernachtung an Bord vor Veranstaltungsbeginn nicht garantiert werden kann.
        </VInfo>
    </div>
    <!-- overnight stay -->
    <div v-else-if="view === View.ARRIVAL">
        <p class="mb-8 text-sm">
            Bei manchen Veranstaltungen ist es möglich bereits am Vortag anzureisen. Bitte gib hier an, falls du bereits vorher anreisen
            möchtest, damit das Büro entsprechend planen kann.
        </p>
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
        <VInfo class="xs:-mx-4 mt-8">
            Bitte beachte, das eine Übernachtung an Bord vor Veranstaltungsbeginn nicht garantiert werden kann.
        </VInfo>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useUsersUseCase } from '@/application';
import { cropToPrecision, deepCopy, isSameDate, subtractFromDate } from '@/common';
import type { Event, InputSelectOption, PositionKey, Registration, UserDetails } from '@/domain';
import { VInfo } from '@/ui/components/common';
import { VInputCheckBox } from '@/ui/components/common';
import { VInputSelectionList, VInputTextArea } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions';

interface Props {
    events: Event[];
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
const filteredEvents = computed<Event[]>(() => {
    if (!props.registration.key) {
        return props.events.filter((e) => e.canSignedInUserJoin);
    } else {
        return props.events.filter((e) => e.canSignedInUserUpdateRegistration);
    }
});
const hasSingleDayEvent = computed<boolean>(() => filteredEvents.value.find((it) => isSameDate(it.start, it.end)) !== undefined);
const availablePositionsForSignedInUser = computed<InputSelectOption<string | undefined>[]>(() => {
    const validPositionKeys: (PositionKey | undefined)[] = signedInUserDetails.value?.positionKeys || [];
    if (!validPositionKeys.includes(props.registration.positionKey)) {
        validPositionKeys.push(props.registration.positionKey);
    }
    return positions.options.value.filter((it) => validPositionKeys.includes(it.value));
});

function updateRegistration(update: Partial<Registration>): void {
    const updated = Object.assign(deepCopy(props.registration), update);
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
    ARRIVAL = 'arrival',
    NOTE = 'note',
}
</script>
