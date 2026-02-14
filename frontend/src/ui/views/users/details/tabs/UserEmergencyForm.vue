<template>
    <section class="gab-4 relative mb-16 grid gap-4">
        <span id="emergency-contact" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">
            Notfallkontakt
        </span>
        <h2 class="text-secondary col-span-full font-bold">Notfallkontakt</h2>
        <VInputText
            v-model.trim="user.emergencyContact.name"
            data-test-id="emergency-contact-name"
            class="col-span-full"
            label="Name des Notfallkontakts"
            placeholder="Keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['emergencyContact.name']"
            :errors-visible="true"
        />
        <VInputText
            v-model.trim="user.emergencyContact.phone"
            data-test-id="emergency-contact-phone"
            class="col-span-full"
            label="Telefonnummer des Notfallkontakts"
            placeholder="Keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['emergencyContact.phone']"
            :errors-visible="true"
        />
    </section>
    <section class="relative mb-16 grid gap-4">
        <span id="emergency-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">Gesundheit</span>
        <h2 class="text-secondary col-span-full font-bold">Wichtige gesundheitliche Informationen</h2>
        <VInfo class="col-span-full">
            Diese Daten sind vertraulich zu behandlen und nur für den Kapitän im Rahmen der Reiseunterlagen bestimmt! Alle Angaben werden
            von der Stammcrew freiwillig gemacht.
        </VInfo>
        <VInputTextArea
            v-model.trim="user.diseases"
            data-test-id="diseases"
            class="col-span-full"
            label="Krankheiten"
            placeholder="Keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['diseases']"
            :errors-visible="true"
        />
        <VInputTextArea
            v-model.trim="user.medication"
            data-test-id="medication"
            class="col-span-full"
            label="Medikamente"
            placeholder="Keine Angabe"
            :disabled="!hasPermission(Permission.WRITE_USERS)"
            :errors="props.errors['medication']"
            :errors-visible="true"
        />
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { VInfo, VInputText, VInputTextArea } from '@/ui/components/common';
import { useSession } from '@/ui/composables/Session.ts';

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
watch(() => user.value.emergencyContact.name, emitUpdate);
watch(() => user.value.emergencyContact.phone, emitUpdate);
watch(() => user.value.diseases, emitUpdate);
watch(() => user.value.medication, emitUpdate);
watch(() => user.value.intolerances, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
