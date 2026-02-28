<template>
    <div v-if="props.errors && props.errors.length > 0 && props.showErrors" class="input-errors">
        <p v-for="err in props.errors" :key="err" class="input-error">
            {{ err }}
        </p>
    </div>
    <div v-else-if="props.hint" class="input-hint">
        {{ props.hint }}
    </div>
</template>
<script lang="ts" setup>
interface Props {
    hint?: string;
    showErrors?: boolean;
    errors?: string[];
}

const props = defineProps<Props>();
</script>
