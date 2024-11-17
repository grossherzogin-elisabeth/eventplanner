<template>
    <VDialog ref="dlg">
        <template #title>
            <h1>Nutzer hinzufügen</h1>
        </template>
        <template #default>
            <div class="px-8 pt-4 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputLabel>Vorname</VInputLabel>
                        <VInputText
                            v-model="user.firstName"
                            :errors="validation.errors.value['firstName']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Vorname laut Auweisdokument"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Nachname</VInputLabel>
                        <VInputText
                            v-model="user.lastName"
                            :errors="validation.errors.value['lastName']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Nachname laut Auweisdokument"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputLabel>Email</VInputLabel>
                        <VInputText
                            v-model="user.email"
                            :errors="validation.errors.value['email']"
                            :errors-visible="validation.showErrors.value"
                            placeholder="Kontakt Email Adresse"
                        />
                    </div>
                    <VInfo>
                        Du kannst im Anschluss weitere Daten eingeben und alle Eingaben noch nachträglich bearbeiten.
                    </VInfo>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" @click="cancel">
                <span>Abbrechen</span>
            </button>
            <div class="w-auto">
                <AsyncButton :action="submit" :disabled="validation.disableSubmit.value">
                    <template #label> Speichern </template>
                </AsyncButton>
            </div>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import type { User } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { VInfo } from '@/ui/components/common';
import { VInputText } from '@/ui/components/common';
import { AsyncButton, VDialog, VInputLabel } from '@/ui/components/common';
import { useUserAdministrationUseCase } from '@/ui/composables/Application.ts';
import { useValidation } from '@/ui/composables/Validation.ts';
import { Routes } from '@/ui/views/Routes.ts';

const router = useRouter();
const userAdministrationUseCase = useUserAdministrationUseCase();

const dlg = ref<Dialog<void, User | undefined> | null>(null);
const user = ref<User>({
    firstName: '',
    lastName: '',
    key: '',
    email: '',
    positionKeys: [],
});

const validation = useValidation(user, userAdministrationUseCase.validate);

async function open(): Promise<User | undefined> {
    user.value = {
        firstName: '',
        lastName: '',
        key: '',
        email: '',
        positionKeys: [],
    };
    return await dlg.value?.open().catch(() => undefined);
}

async function submit(): Promise<void> {
    if (validation.isValid.value) {
        const usr = await userAdministrationUseCase.createUser(user.value);
        dlg.value?.submit(user.value);
        await router.push({ name: Routes.UserDetails, params: { key: usr.key } });
    } else {
        validation.showErrors.value = true;
        throw validation.errors;
    }
}

function cancel(): void {
    dlg.value?.submit(undefined);
}

defineExpose<Dialog<void, User | undefined>>({
    open: () => open(),
    close: () => dlg.value?.reject(),
    submit: (user: User | undefined) => dlg.value?.submit(user),
    reject: () => dlg.value?.reject(),
});
</script>
