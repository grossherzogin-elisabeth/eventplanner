import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: true,
};

const route: RouteRecordRaw = {
    path: '/',
    name: Routes.Home,
    component: () => import('./HomeView.vue'),
    meta: routeMeta,
};

export default route;
