import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.WRITE_EVENTS],
};

const route: RouteRecordRaw = {
    path: '',
    name: Routes.EventsAdmin,
    component: () => import('./EventsAdminView.vue'),
    meta: routeMeta,
};

export default route;
