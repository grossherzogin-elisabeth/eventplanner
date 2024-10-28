import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/info',
    name: Routes.SystemInfo,
    component: () => import('./SystemInfoView.vue'),
    meta: routeMeta,
};

export default route;
