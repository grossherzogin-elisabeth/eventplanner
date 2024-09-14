import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/login',
    name: Routes.Login,
    component: () => import('./LoginView.vue'),
    meta: routeMeta,
};

export default route;
