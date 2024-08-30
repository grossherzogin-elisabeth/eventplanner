import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    title: 'app.page-title.404',
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/:catchAll(.*)',
    name: Routes.PageNotFound,
    component: () => import('./PageNotFoundView.vue'),
    meta: routeMeta,
};

export default route;
