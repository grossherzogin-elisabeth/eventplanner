import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    title: 'app.page-title.onboarding',
    authenticated: true,
};

const route: RouteRecordRaw = {
    path: '/onboarding',
    name: Routes.Onboarding,
    component: () => import('./OnboardingView.vue'),
    meta: routeMeta,
};

export default route;
