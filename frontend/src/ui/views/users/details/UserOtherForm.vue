<template>
    <section v-if="user">
        <h2 class="text-secondary mb-4 font-bold">Ernährung</h2>
        <div class="mb-4">
            <VInputSelect
                v-model="user.diet"
                label="Ernährungsweise"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
                :options="[
                    { value: 'omnivore', label: 'Fleisch' },
                    { value: 'vegetarian', label: 'Vegetarisch' },
                    { value: 'vegan', label: 'Vegan' },
                ]"
            />
        </div>
        <div class="mb-4">
            <VInputTextArea
                v-model.trim="user.intolerances"
                label="Unverträglichkeiten"
                placeholder="Keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
            />
        </div>
        <h2 class="text-secondary mt-8 mb-4 font-bold">Sonstiges</h2>
        <div class="mb-4">
            <VInputTextArea
                v-model.trim="user.comment"
                label="Kommentar (nicht für den Nutzer einsehbar)"
                placeholder="Keine Angabe"
                :disabled="!hasPermission(Permission.WRITE_USERS)"
            />
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { VInputSelect, VInputTextArea } from '@/ui/components/common';
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
watch(() => user.value.comment, emitUpdate);
watch(() => user.value.diet, emitUpdate);
watch(() => user.value.intolerances, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
