<template>
    <div v-if="props.errors && props.errors.length > 0 && props.showErrors" class="input-errors">
        <p v-for="err in props.errors" :key="err" class="input-error">
            {{ err }}
        </p>
    </div>
    <p v-else-if="props.hint" class="input-hint">
        {{ props.hint }}
    </p>
</template>
<script lang="ts" setup>
interface Props {
    hint?: string;
    showErrors?: boolean;
    errors?: string[];
}

const props = defineProps<Props>();
</script>
