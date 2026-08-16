<template>
    <VDialog ref="dlg">
        <template #title>{{ $t('domain.user.actions.create') }}</template>
        <template #default>
            <div class="px-4 pt-4 sm:px-8 lg:px-10">
                <section>
                    <div class="mb-4">
                        <VInputText
                            v-model="user.firstName"
                            :label="$t('domain.user.first-name')"
                            :errors="validation.errors.value['firstName']"
                            :errors-visible="validation.showErrors.value"
                            :hint="$t('domain.user.official-name-hint')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="user.lastName"
                            :label="$t('domain.user.last-name')"
                            :errors="validation.errors.value['lastName']"
                            :errors-visible="validation.showErrors.value"
                            :hint="$t('domain.user.official-name-hint')"
                        />
                    </div>
                    <div class="mb-4">
                        <VInputText
                            v-model="user.email"
                            :label="$t('domain.user.email')"
                            :errors="validation.errors.value['email']"
                            :errors-visible="validation.showErrors.value"
                        />
                    </div>
                    <VInfo> {{ $t('components.create-user-dialog.note-edits') }} </VInfo>
                </section>
            </div>
        </template>
        <template #buttons>
            <button class="btn-ghost" type="button" @click="cancel">
                <span>{{ $t('generic.cancel') }}</span>
            </button>
            <AsyncButton class="btn-ghost" name="save" :action="submit" :disabled="validation.disableSubmit.value">
                <template #label> {{ $t('generic.save') }} </template>
            </AsyncButton>
        </template>
    </VDialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserAdministrationUseCase } from '@/application';
import type { User } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import { AsyncButton, VDialog, VInfo, VInputText } from '@/ui/components/common';
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

const validation = useValidation(user, userAdministrationUseCase.validateForCreate);

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
