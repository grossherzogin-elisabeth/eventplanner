<template>
    <h1>Hello Test</h1>
    <p data-test-id="route-name">{{ route.name }}</p>
</template>
<script lang="ts" setup>
import { useRoute } from 'vue-router';

const route = useRoute();
</script>
