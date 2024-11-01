<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung für {{ registration?.user.firstName }} hinzufügen</h1>
        </template>
        <template #default>
            <div v-if="registration" class="p-8 lg:px-16">
                <p class="mb-8 max-w-lg">
                    {{ registration.user.firstName }} wird zur Warteliste der ausgewählten Reise hinzugefügt. Wenn
                    {{ registration.user.firstName }} auch direkt zur Crew hinzugefügt werden soll, musst du die Reise
                    noch manuell bearbeiten.
                </p>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Reise</VInputLabel>
                    <VInputCombobox v-model="registration.eventKey" :options="eventOptions" />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Position</VInputLabel>
                    <VInputCombobox v-model="registration.positionKey" :options="positions.options.value" />
                </div>
                <div class="-mx-4 mb-2">
                    <VInputLabel>Notiz</VInputLabel>
                    <VInputTextArea v-model="registration.note" />
                </div>
            </div>
        </template>
        <template #buttons>
            <button class="btn-secondary" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>Anmeldung hinzufügen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import type { Event, EventKey, InputSelectOption, PositionKey, User, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputTextArea } from '@/ui/components/common';
import { useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useValidation } from '@/ui/composables/Validation.ts';

interface UserRegistration {
    user: User;
    eventKey?: EventKey;
    positionKey: PositionKey;
    note: string;
}

const eventsUseCase = useEventUseCase();
const eventAdministrationUseCase = useEventAdministrationUseCase();
const i18n = useI18n();
const positions = usePositions();

const dlg = ref<Dialog<User, UserRegistration | undefined> | null>(null);
const events = ref<Event[]>([]);
const hiddenEvents = ref<string[]>([]);

const registration = ref<UserRegistration | undefined>(undefined);

const validation = useValidation(registration, (value) => {
    // TODO extract to service
    const errors: Record<string, ValidationHint[]> = {};
    if (!value) {
        return errors;
    }
    if (!value.positionKey) {
        errors.positionKey = errors.positionKey || [];
        errors.positionKey.push({ key: 'Bitte wähle eine Position', params: {} });
    }
    if (!value.eventKey) {
        errors.eventKey = errors.eventKey || [];
        errors.eventKey.push({ key: 'Bitte wähle eine Reise', params: {} });
    }
    return errors;
});

const eventOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = events.value
        .filter((it) => !hiddenEvents.value.includes(it.key))
        .map((it) => ({
            label: `${i18n.d(it.start, DateTimeFormat.DD_MM_YYYY)} - ${it.name}`,
            value: it.key,
        }));
    return options;
});

async function init(): Promise<void> {
    await fetchEvents();
}

async function fetchEvents(): Promise<void> {
    events.value = await eventsUseCase.getFutureEvents();
}

async function open(user: User): Promise<void> {
    validation.reset();
    registration.value = {
        user: user,
        positionKey: user.positionKeys[0],
        eventKey: undefined,
        note: '',
    };

    const eventsByUser = await eventsUseCase.getFutureEventsByUser(user.key);
    hiddenEvents.value = eventsByUser.map((it) => it.key);

    const result = await dlg.value?.open().catch(() => undefined);
    if (result && result.eventKey) {
        await eventAdministrationUseCase.addRegistration(result.eventKey, {
            key: '',
            userKey: result.user.key,
            positionKey: result.positionKey,
            note: result.note,
        });
    }
}

function submit() {
    if (validation.isValid.value) {
        dlg.value?.submit(registration.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<User, void>>({
    open: (user: User) => open(user),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(),
    reject: () => dlg.value?.reject(),
});

init();
</script>
