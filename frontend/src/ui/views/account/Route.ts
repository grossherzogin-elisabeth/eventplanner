import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    title: 'app.page-title.account',
    authenticated: true,
};

const route: RouteRecordRaw = {
    path: '/account',
    name: Routes.Account,
    component: () => import('./AccountView.vue'),
    meta: routeMeta,
};

export default route;
