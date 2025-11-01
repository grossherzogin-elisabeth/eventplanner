<template>
    <div v-if="items.length > 0 && props.showErrors" class="input-errors">
        <p v-for="err in items" :key="err.key" class="input-error">
            {{ $t(err.key, err.params) }}
        </p>
    </div>
    <div v-else-if="props.hint" class="input-hint">
        {{ props.hint }}
    </div>
</template>
<script lang="ts" setup>
import { computed } from 'vue';

interface Props {
    hint?: string;
    showErrors?: boolean;
    errors?: string[];
}

const props = defineProps<Props>();

const items = computed<{ key: string; params: string[] }[]>(() => {
    if (!props.errors) {
        return [];
    }
    return props.errors.map((it) => {
        const parts = it.split(':');
        return { key: parts[0], params: parts[1]?.split(',') ?? [] };
    });
});
</script>
