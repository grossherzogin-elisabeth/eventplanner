<template>
    <section>
        <h2 class="mb-2 flex items-center justify-between font-bold text-secondary">Informationen für die Kombüse</h2>
        <div class="grid gap-3 rounded-2xl bg-surface-container bg-opacity-50 p-4 shadow xs:-mx-4 md:bg-transparent md:shadow-none">
            <!-- diet -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-utensils"
                label="Ernährungsweise"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.diet">
                        {{ diet.getName(props.modelValue.diet) }}
                    </span>
                    <span v-else>Ernährungsweise</span>
                </template>
                <template #edit="{ value }">
                    <VInputSelectionList v-model="value.diet" :options="diet.options" />
                </template>
            </CardFact>
            <!-- intolerances -->
            <CardFact
                :model-value="props.modelValue"
                icon="fa-wheat-awn-circle-exclamation"
                label="Unverträglichkeiten"
                @update:model-value="emit('update:modelValue', $event)"
            >
                <template #default>
                    <span v-if="props.modelValue.intolerances" class="line-clamp-3">
                        {{ props.modelValue.intolerances }}
                    </span>
                    <span v-else>keine Angabe</span>
                </template>
                <template #edit="{ value }">
                    <div class="mb-4">
                        <VInputLabel>Unverträglichkeiten</VInputLabel>
                        <VInputTextArea v-model="value.intolerances" />
                    </div>
                </template>
            </CardFact>
        </div>
    </section>
</template>

<script lang="ts" setup>
import type { UserDetails } from '@/domain';
import { VInputLabel, VInputSelectionList, VInputTextArea } from '@/ui/components/common';
import CardFact from '@/ui/components/user/CardFact.vue';
import { useDiet } from '@/ui/composables/Diet.ts';

interface Props {
    modelValue: UserDetails;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const diet = useDiet();
</script>
