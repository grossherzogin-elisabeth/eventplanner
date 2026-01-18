<template>
    <VDialog ref="dlg">
        <template #title>
            <template v-if="selectedUser">
                {{
                    $t('views.events.edit.edit-registration.title', {
                        name: selectedUser.nickName || selectedUser.firstName,
                        lastName: selectedUser.lastName,
                    })
                }}
            </template>
            <template v-else>{{ $t('views.events.edit.edit-registration.guest-title') }}</template>
        </template>
        <template #default>
            <div class="xs:px-8 px-4 pt-4 lg:px-10">
                <section>
                    <div v-if="!registration.userKey" class="mb-4">
                        <VInputText
                            v-model.trim="registration.name"
                            :label="$t('domain.registration.name')"
                            :errors="validation.errors.value['name']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputCombobox
                            v-model="registration.positionKey"
                            required
                            :label="$t('domain.registration.position')"
                            :options="positions.options.value"
                            :errors="validation.errors.value['positionKey']"
                            :errors-visible="validation.showErrors.value"
                        >
                            <template #item="{ item }">
                                <span class="grow">{{ item.label }}</span>
                                <i
                                    v-if="selectedUser && item.value && !selectedUser.positionKeys?.includes(item.value)"
                                    class="fa-solid fa-warning text-warning mr-4"
                                />
                            </template>
                        </VInputCombobox>
                    </div>
                    <div class="mb-4">
                        <VInputSelect
                            v-model="registration.overnightStay"
                            required
                            :label="$t('domain.registration.overnight-stay')"
                            :options="[
                                { value: undefined, label: $t('generic.no-information') },
                                { value: true, label: $t('generic.yes') },
                                { value: false, label: $t('generic.no') },
                            ]"
                        />
                    </div>
                    <div v-if="registration.overnightStay" class="mb-4">
                        <VInputDate v-model="registration.arrival" :label="$t('domain.registration.arrival')" />
                    </div>
                    <div class="mb-4">
                        <VInputTextArea
                            v-model.trim="registration.note"
                            :label="$t('domain.registration.note')"
                            :errors="validation.errors.value['note']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <template v-if="selectedUser !== undefined">
                        <VWarning v-if="!selectedUser?.positionKeys?.includes(registration.positionKey)" class="my-4">
                            {{
                                $t('views.events.edit.edit-registration.no-qualification', {
                                    name: selectedUser?.nickName || selectedUser?.firstName,
                                    position: selectedPosition?.name,
                                })
                            }}
                        </VWarning>
                    </template>
                    <VWarning v-else-if="registration.name" class="my-4">
                        {{ $t('views.events.edit.edit-registration.guest-warning', { name: registration.name }) }}
                    </VWarning>
                </section>
                <template v-if="selectedUser">
                    <p class="mb-2">
                        {{
                            $t('views.events.edit.edit-registration.qualifications', {
                                name: selectedUser.nickName || selectedUser.firstName,
                            })
                        }}
                    </p>
                    <div class="flex flex-wrap items-center gap-1 text-sm">
                        <span
                            v-for="q in selectedUser.qualifications"
                            :key="q.qualificationKey"
                            class="truncate rounded-lg px-2 py-1 font-bold whitespace-nowrap"
                            :class="
                                expiredQualifications.includes(q.qualificationKey)
                                    ? 'bg-error-container/50 text-onerror-container'
                                    : 'bg-secondary-container text-onsecondary-container'
                            "
                        >
                            <span v-if="expiredQualifications.includes(q.qualificationKey)">
                                <i class="fa-solid fa-warning" />
                            </span>
                            <span v-else>
                                <i class="fa-solid fa-check" />
                            </span>
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
            <button class="btn-ghost" :disabled="validation.disableSubmit.value" @click="submit">
                <span>{{ $t('generic.apply') }}</span>
            </button>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useUsersUseCase } from '@/application';
import { deepCopy, filterUndefined } from '@/common';
import { Validator, notEmpty } from '@/common/validation';
import type { Event, Position, QualificationKey, Registration, User } from '@/domain';
import { useUserService } from '@/domain/services';
import type { Dialog } from '@/ui/components/common';
import { VInputSelect } from '@/ui/components/common';
import { VInputDate } from '@/ui/components/common';
import { VWarning } from '@/ui/components/common';
import { VDialog, VInputCombobox, VInputText, VInputTextArea } from '@/ui/components/common';
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
    const errors = Validator.validate('positionKey', value.positionKey, notEmpty()).getErrors();
    if (!value.name && !value.userKey) {
        errors.userKey = errors.userKey || [];
        errors.userKey.push('views.events.edit.edit-registration.select-user');
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
