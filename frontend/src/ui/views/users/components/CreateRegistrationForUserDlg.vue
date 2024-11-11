<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 class="truncate">Anmeldung hinzufügen</h1>
        </template>
        <template #default>
            <div v-if="registration" class="px-8 pt-4 lg:px-10">
                <section>
                    <p class="mb-8 max-w-lg">
                        {{ registration.user.firstName }} wird zur Warteliste der ausgewählten Reise hinzugefügt. Wenn
                        {{ registration.user.firstName }} auch direkt zur Crew hinzugefügt werden soll, musst du die
                        Reise noch manuell bearbeiten.
                    </p>
                    <div class="mb-4">
                        <VInputLabel>Reise</VInputLabel>
                        <VInputCombobox
                            v-model="registration.eventKey"
                            :options="eventOptions"
                            :errors="validation.errors.value['eventKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Position</VInputLabel>
                        <VInputCombobox
                            v-model="registration.positionKey"
                            :options="positions.options.value"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Notiz</VInputLabel>
                        <VInputTextArea
                            v-model="registration.note"
                            :errors="validation.errors.value['note']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <AsyncButton class="btn-primary" :action="submit" :disabled="validation.disableSubmit.value">
                <template #label>Speichern</template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import type { Event, EventKey, InputSelectOption, PositionKey, User, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputTextArea } from '@/ui/components/common';
import { useEventAdministrationUseCase, useEventUseCase } from '@/ui/composables/Application.ts';
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

const dlg = ref<Dialog<User, boolean> | null>(null);
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

async function open(user: User): Promise<boolean> {
    validation.reset();
    registration.value = {
        user: user,
        positionKey: user.positionKeys[0],
        eventKey: undefined,
        note: '',
    };
    const eventsByUser = await eventsUseCase.getFutureEventsByUser(user.key);
    hiddenEvents.value = eventsByUser.map((it) => it.key);
    return (await dlg.value?.open().catch(() => false)) || false;
}

async function submit(): Promise<void> {
    if (validation.isValid.value && registration.value?.eventKey) {
        await eventAdministrationUseCase.addRegistration(registration.value.eventKey, {
            key: '',
            userKey: registration.value.user.key,
            positionKey: registration.value.positionKey,
            note: registration.value.note,
        });
        dlg.value?.submit(true);
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<User, boolean>>({
    open: (user: User) => open(user),
    close: () => dlg.value?.reject(),
    submit: () => dlg.value?.submit(false),
    reject: () => dlg.value?.reject(),
});

init();
</script>
