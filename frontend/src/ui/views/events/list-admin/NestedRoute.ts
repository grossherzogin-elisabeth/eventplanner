import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import { Routes } from '@/ui/views/Routes.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.WRITE_EVENTS],
};

const route: RouteRecordRaw = {
    path: 'admin',
    name: Routes.EventsListAdmin,
    component: () => import('./EventsAdminListView.vue'),
    meta: routeMeta,
};

export default route;
