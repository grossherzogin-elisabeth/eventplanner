import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    title: 'app.page-title.imprint',
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/impressum',
    name: Routes.Imprint,
    component: () => import('./ImprintView.vue'),
    meta: routeMeta,
};

export default route;
