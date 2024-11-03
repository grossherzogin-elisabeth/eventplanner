import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import { Routes } from '@/ui/views/Routes.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_EVENTS],
};

const route: RouteRecordRaw = {
    path: 'list',
    name: Routes.EventsList,
    component: () => import('./EventsListView.vue'),
    meta: routeMeta,
};

export default route;
