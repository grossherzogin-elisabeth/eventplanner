<template>
    <VDialog ref="dlg">
        <template #title>
            <h1 v-if="selectedUser">
                {{
                    $t('views.events.edit.registration-edit-dlg.title', {
                        name: selectedUser.nickName || selectedUser.firstName,
                        lastName: selectedUser.lastName,
                    })
                }}
            </h1>
            <h1 v-else>{{ $t('views.events.edit.registration-edit-dlg.guest-title') }}</h1>
        </template>
        <template #default>
            <div class="px-4 pt-4 xs:px-8 lg:px-10">
                <section>
                    <div v-if="!registration.userKey" class="mb-4">
                        <VInputLabel>{{ $t('views.events.edit.registration-edit-dlg.name') }}</VInputLabel>
                        <VInputText
                            v-model.trim="registration.name"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>{{ $t('views.events.edit.registration-edit-dlg.position') }}</VInputLabel>
                        <VInputCombobox
                            v-model="registration.positionKey"
                            :options="positions.options.value"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                        >
                            <template #item="{ item }">
                                <span class="flex-grow">{{ item.label }}</span>
                                <i
                                    v-if="selectedUser && item.value && !selectedUser.positionKeys?.includes(item.value)"
                                    class="fa-solid fa-warning mr-4 text-yellow"
                                />
                            </template>
                        </VInputCombobox>
                    </div>
                    <div class="mb-4">
                        <VInputLabel>{{ $t('views.events.edit.registration-edit-dlg.overnightStay') }}</VInputLabel>
                        <VInputSelect
                            v-model="registration.overnightStay"
                            :options="[
                                { value: undefined, label: $t('generic.no-information') },
                                { value: true, label: $t('generic.yes') },
                                { value: false, label: $t('generic.no') },
                            ]"
                        />
                    </div>
                    <div v-if="registration.overnightStay" class="mb-4">
                        <VInputLabel>{{ $t('views.events.edit.registration-edit-dlg.arrival') }}</VInputLabel>
                        <VInputDate v-model="registration.arrival" />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>{{ $t('views.events.edit.registration-edit-dlg.note') }}</VInputLabel>
                        <VInputTextArea
                            v-model.trim="registration.note"
                            :errors="validation.errors.value['note']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <template v-if="selectedUser !== undefined">
                        <VWarning v-if="!selectedUser?.positionKeys?.includes(registration.positionKey)" class="my-4">
                            {{
                                $t('views.events.edit.registration-edit-dlg.no-qualification', {
                                    name: selectedUser?.nickName || selectedUser?.firstName,
                                    position: selectedPosition?.name,
                                })
                            }}
                        </VWarning>
                    </template>
                    <VWarning v-else-if="registration.name" class="my-4">
                        {{ $t('views.events.edit.registration-edit-dlg.guest-warning', { name: registration.name }) }}
                    </VWarning>
                </section>
                <template v-if="selectedUser">
                    <p class="mb-2">
                        {{
                            $t('views.events.edit.registration-edit-dlg.qualifications', {
                                name: selectedUser.nickName || selectedUser.firstName,
                            })
                        }}
                    </p>
                    <div class="flex flex-wrap items-center gap-1 text-sm">
                        <span
                            v-for="q in selectedUser.qualifications"
                            :key="q.qualificationKey"
                            class="truncate whitespace-nowrap rounded-lg px-2 py-1 font-bold"
                            :class="
                                expiredQualifications.includes(q.qualificationKey)
                                    ? 'bg-error-container text-onerror-container'
                                    : 'bg-secondary-container text-onsecondary-container'
                            "
                        >
                            <i v-if="expiredQualifications.includes(q.qualificationKey)" class="fa-solid fa-warning"></i>
                            <i v-else class="fa-solid fa-check"></i>
                            {{ qualifications.get(q.qualificationKey).name }}
                        </span>
                    </div>
                </template>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" name="save" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <button class="btn-primary" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ $t('generic.apply') }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { deepCopy, filterUndefined } from '@/common';
import type { Event, Position, QualificationKey, Registration, User, ValidationHint } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInputSelect } from '@/ui/components/common';
import { VInputDate } from '@/ui/components/common';
import { VWarning } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputLabel, VInputText, VInputTextArea } from '@/ui/components/common';
import { useUsersUseCase } from '@/ui/composables/Application.ts';
import { useUserService } from '@/ui/composables/Domain.ts';
import { usePositions } from '@/ui/composables/Positions.ts';
import { useQualifications } from '@/ui/composables/Qualifications.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { v4 as uuid } from 'uuid';

interface Props {
    event: Event;
}

const props = defineProps<Props>();

const usersUseCase = useUsersUseCase();
const usersService = useUserService();
const positions = usePositions();
const qualifications = useQualifications();

const dlg = ref<Dialog<Registration, Registration | undefined> | null>(null);
const users = ref<User[]>([]);
const registration = ref<Registration>({
    key: uuid(),
    positionKey: '',
});
const validation = useValidation(registration, (value) => {
    // TODO extract to service
    const errors: Record<string, ValidationHint[]> = {};
    if (!value.positionKey) {
        errors.positionKey = errors.positionKey || [];
        errors.positionKey.push({ key: 'views.events.edit.registration-edit-dlg.select-position', params: {} });
    }
    if (!value.name && !value.userKey) {
        errors.userKey = errors.userKey || [];
        errors.userKey.push({ key: 'views.events.edit.registration-edit-dlg.select-user', params: {} });
    }
    return errors;
});

const selectedUser = computed<User | undefined>(() => {
    if (!registration.value?.userKey) {
        return undefined;
    }
    return users.value.find((u) => u.key === registration.value?.userKey);
});

const selectedPosition = computed<Position | undefined>(() => {
    if (!registration.value?.positionKey) {
        return undefined;
    }
    return positions.get(registration.value.positionKey);
});

const expiredQualifications = computed<QualificationKey[]>(() => {
    if (!selectedUser.value) {
        return [];
    }
    return usersService
        .getExpiredQualifications(selectedUser.value, props.event.end)
        .map((key) => qualifications.get(key))
        .filter(filterUndefined)
        .map((qualification) => qualification.key);
});

async function init(): Promise<void> {
    await fetchUsers();
}

async function fetchUsers(): Promise<void> {
    users.value = await usersUseCase.getUsers();
}

async function open(value: Registration): Promise<Registration | undefined> {
    validation.reset();
    registration.value = deepCopy(value);
    // wait until user submits
    const result = await dlg.value?.open().catch(() => undefined);
    if (!result) {
        return undefined;
    }
    if (!registration.value.userKey) {
        value.name = result.name;
    }
    value.positionKey = result.positionKey;
    value.note = result.note;
    value.overnightStay = result.overnightStay;
    if (result.overnightStay) {
        value.arrival = result.arrival;
    } else {
        value.arrival = undefined;
    }
    return value;
}

function submit(): void {
    if (validation.isValid.value) {
        dlg.value?.submit(registration.value);
    } else {
        validation.showErrors.value = true;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<Registration, Registration | undefined>>({
    open: (eventRegistration: Registration) => open(eventRegistration),
    close: () => dlg.value?.reject(),
    submit: (result: Registration) => dlg.value?.submit(result),
    reject: () => dlg.value?.reject(),
});

init();
</script>
