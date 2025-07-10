<template>
    <!-- overview -->
    <div v-if="view === View.OVERVIEW">
        <p class="mb-8 text-sm">
            Bitte überprüfe die Angaben zu deiner Anmeldung zu
            <i>{{ props.event.name }}.</i>
        </p>
        <ul class="border-t border-onsurface-variant border-opacity-20 md:mx-0">
            <li
                v-if="availablePositionsForSignedInUser.length > 1"
                class="flex cursor-pointer items-center space-x-4 border-b border-onsurface-variant border-opacity-20 py-3 sm:px-0"
                @click="emit('update:view', View.POSITION)"
            >
                <div class="w-0 flex-grow">
                    <p class="mb-1 text-sm font-bold">Deine Position</p>
                    <p>{{ positions.get(props.registration.positionKey).name }}</p>
                </div>
                <i class="fa-solid fa-chevron-right" />
            </li>
            <li v-else class="flex items-center space-x-4 border-b border-onsurface-variant border-opacity-20 py-3 sm:px-0">
                <div class="w-0 flex-grow">
                    <p class="mb-1 text-sm font-bold">Deine Position</p>
                    <p class="">{{ positions.get(props.registration.positionKey).name }}</p>
                </div>
            </li>
            <li
                v-if="isSingleDayEvent"
                class="flex cursor-pointer items-center space-x-4 border-b border-onsurface-variant border-opacity-20 py-3 sm:px-0"
                @click="emit('update:view', View.OVERNIGHT)"
            >
                <div class="w-0 flex-grow">
                    <p class="mb-1 text-sm font-bold">Übernachtung an Bord</p>
                    <p v-if="!props.registration.overnightStay">Nein</p>
                    <p v-else-if="props.registration.arrival">Ja, Anreise am Vortag</p>
                    <p v-else>Ja</p>
                </div>
                <i class="fa-solid fa-chevron-right" />
            </li>
            <li
                class="flex cursor-pointer items-center space-x-4 border-b border-onsurface-variant border-opacity-20 py-3 sm:px-0"
                @click="emit('update:view', View.NOTE)"
            >
                <div class="w-0 flex-grow">
                    <p class="mb-1 text-sm font-bold">Notiz fürs Büro</p>
                    <p v-if="props.registration.note" class="truncate italic">{{ props.registration.note }}</p>
                    <p v-else>-</p>
                </div>
                <i class="fa-solid fa-chevron-right" />
            </li>
        </ul>
    </div>
    <!-- position -->
    <div v-else-if="view === View.POSITION">
        <p class="mb-8 text-sm">
            Du hast die Qualifikation für mehrere Positionen. Mit welcher Position möchtest du dich für diese Reise anmelden?
        </p>
        <VInputSelectionList
            :model-value="props.registration.positionKey"
            class="mb-8"
            :options="availablePositionsForSignedInUser"
            orientation="vertical"
            @update:model-value="updateRegistration({ positionKey: $event })"
        />
        <p class="mb-4 text-sm">
            Du kannst die ausgewählte Position als Standard festlegen, um dich für weitere Reisen noch einfacher anmelden zu können.
        </p>
        <div class="mb-4">
            <VInputCheckBox v-model="saveAsDefaultPosition" label="Als Standardposition festlegen" />
        </div>
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
    <!-- overnight -->
    <div v-else-if="view === View.OVERNIGHT">
        <p class="mb-4 text-sm">
            Bitte teile uns mit, ob du an Bord übernachten möchtest. Falls ja, gib bitte außerdem an, ob du bereits am Vortag anreist.
        </p>
        <div class="mb-4">
            <VInputCheckBox
                :model-value="props.registration.overnightStay"
                label="Koje benötigt"
                @update:model-value="updateRegistration({ overnightStay: $event })"
            />
        </div>
        <div class="mb-4">
            <VInputCheckBox
                :model-value="props.registration.arrival !== undefined"
                label="Anreise am Vortag"
                @update:model-value="
                    (value) =>
                        updateRegistration({
                            arrival: value ? cropToPrecision(subtractFromDate(props.event.start, { days: 1 }), 'days') : undefined,
                        })
                "
            />
        </div>
        <VInfo class="-mx-4"> Bitte beachte, das eine Übernachtung an Bord vor Reisebeginn nicht garantiert werden kann. </VInfo>
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
    event: Event;
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
const saveAsDefaultPosition = ref<boolean>(false);
const isSingleDayEvent = computed<boolean>(() => isSameDate(props.event.start, props.event.end));
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
    watch(saveAsDefaultPosition, () => savePreferredPosition());
    watch(
        () => props.registration.positionKey,
        () => savePreferredPosition()
    );
}

async function fetchSignedInUserDetails(): Promise<void> {
    signedInUserDetails.value = await usersUseCase.getUserDetailsForSignedInUser();
}

async function savePreferredPosition(): Promise<void> {
    if (saveAsDefaultPosition.value) {
        await usersUseCase.saveUserSettings({ preferredPosition: props.registration.positionKey });
    }
}

init();
</script>
<script lang="ts">
export enum View {
    OVERVIEW = 'overview',
    POSITION = 'position',
    ARRIVAL = 'arrival',
    OVERNIGHT = 'overnight',
    NOTE = 'note',
}
</script>
