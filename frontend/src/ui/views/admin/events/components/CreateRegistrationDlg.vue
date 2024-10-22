<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Anmeldung hinzufügen</h1>
        </template>
        <template #default>
            <div class="p-8 lg:px-16">
                <p class="mb-8 max-w-lg">
                    Du kannst eine Anmeldung für ein Stammcrew Mitglied oder Gastcrew anlegen. Die Position kannst du
                    nachträglich noch auf der Warteliste ändern.
                </p>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Nutzer</VInputLabel>
                    <VInputCombobox v-model="registration.userKey" :options="userOptions" />
                </div>
                <div v-if="!registration.userKey" class="-mx-4 mb-4">
                    <VInputLabel>Name</VInputLabel>
                    <VInputText v-model="registration.name" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Position</VInputLabel>
                    <VInputCombobox v-model="registration.positionKey" :options="positionOptions" />
                </div>
                <div class="-mx-4 mb-4">
                    <VInputLabel>Notiz</VInputLabel>
                    <VInputTextArea v-model="registration.note" />
                </div>
                <template v-if="selectedUser !== undefined">
                    <VWarning
                        v-if="
                            registration.positionKey && !selectedUser?.positionKeys.includes(registration.positionKey)
                        "
                        class="-mx-4 my-4"
                    >
                        {{ selectedUser?.nickName || selectedUser?.firstName }} hat keine Qualifikation für die Position
                        <i>{{ selectedPosition?.name }}</i>
                    </VWarning>
                    <VWarning v-if="expiredQualifications.length > 0" class="-mx-4 my-4">
                        Die folgenden Qualificationen sind abgelaufen oder laufen vor Ende der Reise ab:
                        <ul class="ml-4 mt-2 list-disc">
                            <li v-for="quali in expiredQualifications" :key="quali">{{ quali }}</li>
                        </ul>
                    </VWarning>
                </template>
                <VWarning v-else-if="registration.name" class="-mx-4 my-4">
                    {{ registration.name }} ist Gastcrew. Die Gültigkeit der Qualifikationen kann daher nicht
                    automatisiert geprüft werden.
                </VWarning>
            </div>
        </template>
        <template #buttons="{ reject, submit }">
            <button class="btn-secondary" @click="reject">
                <span>Abbrechen</span>
            </button>
            <button class="btn-primary" :disabled="!isValid" @click="submit">
                <span>Anmeldung hinzufügen</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { ArrayUtils } from '@/common';
import type { Event, Qualification, QualificationKey, Registration, User } from '@/domain';
import { type InputSelectOption, type Position, type PositionKey } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VWarning } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application';
import { useUserService } from '@/ui/composables/Domain';

const usersUseCase = useUsersUseCase();
const usersService = useUserService();

const dlg = ref<Dialog<Event, Event> | null>(null);
const event = ref<Event | null>(null);
const users = ref<User[]>([]);
const positions = ref<Map<PositionKey, Position>>(new Map<PositionKey, Position>());
const qualifications = ref<Map<QualificationKey, Qualification>>(new Map<QualificationKey, Qualification>());
const registration = ref<Registration>({
    key: '',
    positionKey: '',
    userKey: undefined,
    name: undefined,
});
const hiddenUsers = ref<string[]>([]);

const userOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = users.value
        .filter((it) => !hiddenUsers.value.includes(it.key))
        .map((it) => ({
            label: `${it.firstName} ${it.lastName}`,
            value: it.key,
        }));
    options.unshift({ label: 'Gastcrew', value: undefined });
    return options;
});

const selectedUser = computed<User | undefined>(() => {
    if (!registration.value?.userKey) {
        return undefined;
    }
    return users.value.find((u) => u.key === registration.value?.userKey);
});

const positionOptions = computed<InputSelectOption<string | undefined>[]>(() => {
    const options: InputSelectOption<string | undefined>[] = [...positions.value.values()]
        .sort((a, b) => b.prio - a.prio)
        .map((it) => ({
            label: it.name,
            value: it.key,
        }));
    return options;
});

const selectedPosition = computed<Position | undefined>(() => {
    return positions.value.get(registration.value?.positionKey);
});

const expiredQualifications = computed<string[]>(() => {
    if (!selectedUser.value) {
        return [];
    }
    return usersService
        .getExpiredQualifications(selectedUser.value, event.value?.end)
        .map((key) => qualifications.value.get(key))
        .filter(ArrayUtils.filterUndefined)
        .map((qualification) => qualification.name);
});

const isValid = computed<boolean>(() => {
    return (
        (registration.value.name !== undefined || registration.value.userKey !== undefined) &&
        registration.value.positionKey !== undefined
    );
});

async function init(): Promise<void> {
    await fetchPositions();
    await fetchQualifications();
    await fetchUsers();
}

async function fetchUsers(): Promise<void> {
    users.value = await usersUseCase.getUsers();
}

async function fetchPositions(): Promise<void> {
    positions.value = await usersUseCase.resolvePositionNames();
}

async function fetchQualifications(): Promise<void> {
    qualifications.value = await usersUseCase.resolveQualifications();
}

async function open(evt: Event): Promise<Event> {
    event.value = evt;
    registration.value = {
        key: '',
        positionKey: '',
        userKey: undefined,
        name: undefined,
        note: '',
    };
    hiddenUsers.value = evt.registrations.map((it) => it.userKey).filter(ArrayUtils.filterUndefined);

    // wait until user submits
    await dlg.value?.open();

    if (registration.value.userKey) {
        registration.value.name = undefined;
    }
    evt.registrations.push(registration.value);
    return evt;
}

defineExpose<Dialog<Event, Event>>({
    open: (evt: Event) => open(evt),
    close: () => dlg.value?.reject(),
    submit: (result: Event) => dlg.value?.submit(result),
    reject: (reason?: void) => dlg.value?.reject(reason),
});

init();
</script>
