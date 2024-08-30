import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    title: 'Reise bearbeiten',
    authenticated: true,
    permissions: [Permission.WRITE_EVENTS],
    backTo: Routes.EventsAdmin,
};

const route: RouteRecordRaw = {
    path: ':year/edit/:key',
    name: Routes.EventEdit,
    component: () => import('./EventEditView.vue'),
    meta: routeMeta,
};

export default route;
