<template>
    <div class="mt-8">
        <h2 class="card-headline">{{ $t('views.unauthorized.message') }}</h2>
    </div>
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { useAuthUseCase } from '@/application';
import { Routes } from '@/ui/views/Routes';
import { useI18n } from 'vue-i18n';

type RouteEmits = (e: 'update:tab-title', value: string) => void;

const emit = defineEmits<RouteEmits>();

const { t } = useI18n();
const authUseCase = useAuthUseCase();
const router = useRouter();

async function init(): Promise<void> {
    emit('update:tab-title', t('views.unauthorized.title'));
    await authUseCase.onLogin();
    await router.push({ name: Routes.EventsCalendar, params: { year: new Date().getFullYear() } });
}

init();
</script>
