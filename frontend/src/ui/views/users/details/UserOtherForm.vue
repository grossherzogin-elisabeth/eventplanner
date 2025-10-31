<template>
    <section v-if="user">
        <h2 class="mb-4 font-bold text-secondary">Ernährung</h2>
        <div class="mb-4">
            <VInputSelect
                v-model="user.diet"
                label="Ernährungsweise"
                :options="[
                    { value: 'omnivore', label: 'Fleisch' },
                    { value: 'vegetarian', label: 'Vegetarisch' },
                    { value: 'vegan', label: 'Vegan' },
                ]"
            />
        </div>
        <div class="mb-4">
            <VInputTextArea v-model.trim="user.intolerances" label="Unverträglichkeiten" placeholder="Keine Angabe" />
        </div>
        <h2 class="mb-4 mt-8 font-bold text-secondary">Sonstiges</h2>
        <div class="mb-4">
            <VInputTextArea v-model.trim="user.comment" label="Kommentar (nicht für den Nutzer einsehbar)" placeholder="Keine Angabe" />
        </div>
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails } from '@/domain';
import { VInputSelect, VInputTextArea } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, string[]>;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const user = ref<UserDetails>(props.modelValue);

watch(props.modelValue, () => (user.value = props.modelValue));
watch(() => user.value.comment, emitUpdate);
watch(() => user.value.diet, emitUpdate);
watch(() => user.value.intolerances, emitUpdate);

function emitUpdate(): void {
    emit('update:modelValue', user.value);
}
</script>
