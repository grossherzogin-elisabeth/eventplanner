<template>
    <section v-if="hasPermission(Permission.WRITE_USERS)" class="relative mb-16 grid gap-4">
        <span id="app-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">App</span>
        <h2 class="text-secondary col-span-full font-bold">App</h2>
        <VInputText
            v-model.trim="user.authKey"
            data-test-id="auth-key"
            class="col-span-full"
            label="OpenID Connect Subject ID"
            placeholder="Nicht verknüpft"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['authKey']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.nickName"
            data-test-id="nick-name"
            class="col-span-full"
            label="Anzeigename"
            :placeholder="user.firstName"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['nickName']"
            :errors-visible="true"
        />
        <VInputDate
            v-model.trim="user.createdAt"
            data-test-id="created-at"
            class="col-span-full"
            label="Erstellt am"
            disabled
            :errors="props.errors['createdAt']"
            :errors-visible="true"
        />
        <VInputDate
            v-model.trim="user.updatedAt"
            data-test-id="updated-at"
            class="col-span-full"
            label="Letzte Änderung am"
            disabled
            :errors="props.errors['updatedAt']"
            :errors-visible="true"
        />
        <VInputDate
            v-model.trim="user.lastLoginAt"
            data-test-id="last-login-at"
            class="col-span-full"
            label="Letzter Login am"
            disabled
            :errors="props.errors['lastLoginAt']"
            :errors-visible="true"
        />
        <VInputDate
            v-model.trim="user.verifiedAt"
            data-test-id="verified-at"
            class="col-span-full"
            label="Daten verifiziert am"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['verifiedAt']"
            :errors-visible="true"
        />
    </section>
    <section class="relative mb-16 grid gap-4 sm:grid-cols-6">
        <span id="personal-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">
            Persönliche Daten
        </span>
        <h2 class="text-secondary col-span-full font-bold">Persönliche Daten</h2>
        <VInputSelect
            v-model="user.gender"
            data-test-id="gender"
            class="col-span-full sm:col-span-3"
            label="Geschlecht"
            :options="genderOptions"
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['gender']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.title"
            data-test-id="title"
            class="col-span-full sm:col-span-3"
            label="Titel"
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['title']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.firstName"
            data-test-id="first-name"
            class="col-span-full"
            label="Vorname"
            required
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['firstName']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.secondName"
            data-test-id="second-name"
            class="col-span-full"
            label="Zweiter Vorname"
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['secondName']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.lastName"
            data-test-id="last-name"
            class="col-span-full"
            label="Nachname"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['lastName']"
            :errors-visible="true"
        />
        <VInputDate
            v-model="user.dateOfBirth"
            data-test-id="date-of-birth"
            class="col-span-full sm:col-span-2"
            label="Geboren am"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['dateOfBirth']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.placeOfBirth"
            data-test-id="place-of-birth"
            class="col-span-full sm:col-span-4"
            label="Geburtsort"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['placeOfBirth']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.passNr"
            data-test-id="pass-number"
            class="col-span-full sm:col-span-2"
            label="Pass Nummer"
            required
            placeholder="keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['passNr']"
            :errors-visible="true"
        />
        <VInputCombobox
            v-model="user.nationality"
            data-test-id="nationality"
            class="col-span-full sm:col-span-4"
            label="Nationalität"
            :options="nationalities.options"
            required
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['nationality']"
            :errors-visible="true"
        />
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { InputSelectOption, UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { VInputCombobox, VInputDate, VInputSelect, VInputText } from '@/ui/components/common';
import { useNationalities } from '@/ui/composables/Nationalities.ts';
import { useSession } from '@/ui/composables/Session.ts';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, string[]>;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const nationalities = useNationalities();
const { hasPermission } = useSession();

const user = ref<UserDetails>(props.modelValue);
const genderOptions: InputSelectOption[] = [
    { value: 'm', label: 'männlich' },
    { value: 'f', label: 'weiblich' },
    { value: 'd', label: 'divers' },
];

watch(props.modelValue, () => (user.value = props.modelValue));
watch(() => user.value.gender, emitUpdate);
watch(() => user.value.firstName, emitUpdate);
watch(() => user.value.secondName, emitUpdate);
watch(() => user.value.nickName, emitUpdate);
watch(() => user.value.lastName, emitUpdate);
watch(() => user.value.dateOfBirth, emitUpdate);
watch(() => user.value.placeOfBirth, emitUpdate);
watch(() => user.value.passNr, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
