<template>
    <VDialog ref="dlg">
        <template #title>Anmeldung hinzufügen</template>
        <template #default>
            <div v-if="registration" class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <p class="mb-8 max-w-lg">
                        {{ registration.user.firstName }} wird zur Warteliste der ausgewählten Veranstaltung hinzugefügt. Wenn
                        {{ registration.user.firstName }} auch direkt zur Crew hinzugefügt werden soll, musst du die Veranstaltung noch
                        manuell bearbeiten.
                    </p>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="registration.eventKey"
                            label="Veranstaltung"
                            :options="eventOptions"
                            :errors="validation.errors.value['eventKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="registration.positionKey"
                            label="Position"
                            :options="positions.options.value"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model="registration.note"
                            label="Notiz"
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
            <AsyncButton class="btn-ghost" name="save" :action="submit" :disabled="validation.disableSubmit.value">
                <template #label>Speichern</template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useEventAdministrationUseCase, useEventUseCase } from '@/application';
import { DateTimeFormat } from '@/common/date';
import { Validator, notEmpty } from '@/common/validation';
import type { Event, EventKey, InputSelectOption, PositionKey, User } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputTextArea } from '@/ui/components/common';
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
    return Validator.validate('positionKey', value?.positionKey, notEmpty()).validate('eventKey', value?.eventKey, notEmpty()).getErrors();
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
        positionKey: user.positionKeys?.[0] ?? '',
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
