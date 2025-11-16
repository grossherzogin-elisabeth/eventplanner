<template>
    <div class="fixed h-screen w-screen animate-pulse"></div>
</template>

<script lang="ts" setup>
import { useAuthUseCase } from '@/application';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const authUseCase = useAuthUseCase();

async function init(): Promise<void> {
    emit('update:tab-title', 'Login');
    await authUseCase.login();
}

init();
</script>
