import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';

const routeMeta: RouteMetaData = {
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/test',
    name: 'test',
    component: () => import('./TestView.vue'),
    meta: routeMeta,
};

export default route;
