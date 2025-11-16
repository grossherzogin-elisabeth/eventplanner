<template>
    <div class="mt-8">
        <h2 class="card-headline">Diese Seite ist nur für angemeldete Benutzer sichtbar</h2>
    </div>
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { useAuthUseCase } from '@/application';
import { Routes } from '@/ui/views/Routes';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const authUseCase = useAuthUseCase();
const router = useRouter();

async function init(): Promise<void> {
    emit('update:tab-title', 'Fehlende Berechtigung');
    await authUseCase.onLogin();
    await router.push({ name: Routes.EventsCalendar, params: { year: new Date().getFullYear() } });
}

init();
</script>
