<template>
    <div class="mt-8">
        <h2 class="card-headline">Diese Seite ist nur für angemeldete Benutzer sichtbar</h2>
    </div>
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { useAuthUseCase } from '@/ui/composables/Application';
import { Routes } from '@/ui/views/Routes';

const authUseCase = useAuthUseCase();
const router = useRouter();

async function init(): Promise<void> {
    await authUseCase.onLogin();
    await router.push({ name: Routes.Events, params: { year: new Date().getFullYear() } });
}

init();
</script>
