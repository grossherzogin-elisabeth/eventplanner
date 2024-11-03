import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import { Routes } from '@/ui/views/Routes.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.WRITE_EVENTS],
    backTo: Routes.EventsListAdmin,
};

const route: RouteRecordRaw = {
    path: ':year/edit/:key',
    name: Routes.EventEdit,
    component: () => import('./EventEditView.vue'),
    meta: routeMeta,
};

export default route;
