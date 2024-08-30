import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    title: 'Wiki',
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/wiki',
    name: Routes.Wiki,
    component: () => import('./WikiView.vue'),
    meta: routeMeta,
};

export default route;
