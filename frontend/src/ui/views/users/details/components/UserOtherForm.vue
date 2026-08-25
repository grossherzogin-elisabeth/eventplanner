<template>
    <section class="relative mb-16 grid gap-4">
        <span id="other-data" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">
            {{ $t('components.user-other-form.diet') }}
        </span>
        <h2 class="text-secondary col-span-full font-bold">{{ $t('components.user-other-form.diet') }}</h2>
        <VInputSelect
            v-model="user.diet"
            data-test-id="diet"
            class="col-span-full"
            :label="$t('domain.user.diet')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
            :options="diet.options"
        />
        <VInputTextArea
            v-model.trim="user.intolerances"
            data-test-id="intolerances"
            class="col-span-full"
            :label="$t('domain.user.intolerances')"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
        />
    </section>
    <section class="relative mb-16 grid gap-4">
        <span id="other-other" class="site-link pointer-events-none absolute -top-48 -z-10 col-span-full opacity-0">
            {{ $t('components.user-other-form.other') }}
        </span>
        <h2 class="text-secondary col-span-full font-bold">{{ $t('components.user-other-form.other') }}</h2>
        <VInputTextArea
            v-model.trim="user.comment"
            data-test-id="comment"
            class="col-span-full"
            :label="$t('components.user-other-form.comment')"
            :placeholder="$t('generic.no-information')"
            :disabled="!hasPermission(Permission.UPDATE_USERS)"
        />
    </section>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue';
import type { UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { VInputSelect, VInputTextArea } from '@/ui/components/common';
import { useSession } from '@/ui/composables/Session';
import { useDiet } from '@/ui/composables/Diet.ts';

interface Props {
    modelValue: UserDetails;
    errors: Record<string, string[]>;
}

type Emits = (e: 'update:modelValue', user: UserDetails) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const diet = useDiet();
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
