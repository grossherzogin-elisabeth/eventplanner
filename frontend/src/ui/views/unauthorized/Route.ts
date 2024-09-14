import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/unauthorized',
    name: Routes.Unauthorized,
    component: () => import('./UnauthorizedView.vue'),
    meta: routeMeta,
};

export default route;
