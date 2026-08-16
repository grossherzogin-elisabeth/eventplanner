<template>
    <section class="relative mb-16 grid gap-4">
        <!-- TODO i18n -->
        <span id="other-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">Ernährung</span>
        <h2 class="text-secondary col-span-full font-bold">Ernährung</h2>
        <VInputSelect
            v-model="user.diet"
            data-test-id="diet"
            class="col-span-full"
            :label="$t('domain.user.diet')"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :options="[
                { value: 'omnivore', label: $t('generic.diet.omnivore') },
                { value: 'vegetarian', label: $t('generic.diet.vegetarian') },
                { value: 'vegan', label: $t('generic.diet.vegan') },
            ]"
        />
        <VInputTextArea
            v-model.trim="user.intolerances"
            data-test-id="intolerances"
            class="col-span-full"
            :label="$t('domain.user.intolerances')"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
        />
    </section>
    <section class="relative mb-16 grid gap-4">
        <span id="other-other" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">Sonstiges</span>
        <h2 class="text-secondary col-span-full font-bold">Sonstiges</h2>
        <VInputTextArea
            v-model.trim="user.comment"
            data-test-id="comment"
            class="col-span-full"
            label="Kommentar (nicht für den Nutzer einsehbar)"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
        />
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { VInputSelect, VInputTextArea } from '@/ui/components/common';
import { useSession } from '@/ui/composables/Session';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, string[]>;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { hasPermission } = useSession();

const user = ref<UserDetails>(props.modelValue);

watch(props.modelValue, () => (user.value = props.modelValue));
watch(() => user.value.comment, emitUpdate);
watch(() => user.value.diet, emitUpdate);
watch(() => user.value.intolerances, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
