<template>
    <VSheet ref="sheet" min-height="20rem" :show-back-button="view !== View.OVERVIEW" @back="view = View.OVERVIEW">
        <template #title>
            <h1 v-if="view === View.POSITION">Position auswählen</h1>
            <h1 v-else-if="view === View.NOTE">Notiz fürs Büro</h1>
            <h1 v-else-if="view === View.OVERNIGHT">Übernachtung und Anreise</h1>
            <h1 v-else-if="registration.key">Anmeldung bearbeiten</h1>
            <h1 v-else>Anmeldung erstellen</h1>
        </template>
        <template #content>
            <div class="px-8 sm:w-[30rem] lg:px-10">
                <RegistrationForm v-if="registration" v-model:registration="registration" v-model:view="view" :events="events" />
            </div>
        </template>
        <template #bottom>
            <div v-if="view === View.OVERVIEW" class="lg:px-10-lg flex justify-end gap-2 px-8 py-4">
                <button class="btn-ghost" name="save" @click="cancel()">
                    <span>Abbrechen</span>
                </button>
                <button class="btn-primary" @click="submit()">
                    <span v-if="registration.key">Speichern</span>
                    <span v-else>Anmelden</span>
                </button>
            </div>
        </template>
    </VSheet>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { deepCopy } from '@/common';
import type { Event, Registration, UserDetails } from '@/domain';
import type { Sheet } from '@/ui/components/common';
import { VSheet } from '@/ui/components/common';
import RegistrationForm, { View } from '@/ui/components/events/RegistrationForm.vue';
import { useUsersUseCase } from '@/ui/composables/Application.ts';
import { v4 as uuid } from 'uuid';

const usersUseCase = useUsersUseCase();

const view = ref<View>(View.OVERVIEW);
const sheet = ref<Sheet<{ registration: Registration; event: Event | Event[] }, Registration | undefined> | null>(null);
const signedInUserDetails = ref<UserDetails | null>(null);
const registration = ref<Registration>({ key: uuid(), positionKey: '' });
const events = ref<Event[]>([]);

async function init(): Promise<void> {
    await fetchSignedInUserDetails();
}

async function fetchSignedInUserDetails(): Promise<void> {
    signedInUserDetails.value = await usersUseCase.getUserDetailsForSignedInUser();
}

async function open(value: { registration: Registration; event: Event | Event[] }): Promise<Registration | undefined> {
    registration.value = deepCopy(value.registration);
    events.value = deepCopy(Array.isArray(value.event) ? value.event : [value.event]);
    view.value = View.OVERVIEW;

    // wait until user submits
    const result = await sheet.value?.open().catch(() => undefined);

    if (!result) {
        return undefined;
    }
    if (!registration.value.userKey) {
        registration.value.name = result.name;
    }
    registration.value.positionKey = result.positionKey;
    registration.value.note = result.note;
    registration.value.overnightStay = result.overnightStay;
    registration.value.arrival = result.arrival;
    return registration.value;
}

function submit(): void {
    sheet.value?.submit(registration.value);
}

function cancel(): void {
    sheet.value?.submit(undefined);
}

defineExpose<Sheet<{ registration: Registration; event: Event | Event[] }, Registration | undefined>>({
    open: (params: { registration: Registration; event: Event | Event[] }) => open(params),
    close: () => sheet.value?.reject(),
    submit: (result: Registration) => sheet.value?.submit(result),
    reject: () => sheet.value?.reject(),
});

init();
</script>
