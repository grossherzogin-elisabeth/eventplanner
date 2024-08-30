<template>
    <button
        :disabled="props.disabled || loading || success === true"
        class="flex-grow whitespace-nowrap"
        :class="$attrs.class || 'btn-primary'"
        @click="onClick()"
    >
        <VLoadingSpinner v-if="loading" class="-mx-1 block w-6 text-current" />
        <span v-else-if="success === true" class="block w-4">
            <i class="fa-solid fa-check"></i>
        </span>
        <span v-else-if="success === false" class="block w-4">
            <i class="fa-solid fa-warning"></i>
        </span>
        <span v-else class="block w-4">
            <slot name="icon"></slot>
        </span>
        <span>
            <slot :failed="success === false" :loading="loading" :success="success === true" name="label" />
        </span>
    </button>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { Timer } from '@/common';
import { VLoadingSpinner } from '@/ui/components/common';

interface Props {
    disabled?: boolean;
    action?: () => Promise<unknown>;
}

const props = defineProps<Props>();

const loading = ref<boolean>(false);
const success = ref<boolean | null>(null);

async function onClick(): Promise<void> {
    if (props.action) {
        loading.value = true;
        try {
            await Timer.wait(500);
            await props.action();
            success.value = true;
        } catch (e) {
            success.value = false;
            setTimeout(() => (success.value = null), 2000);
        } finally {
            loading.value = false;
        }
    }
}
</script>
