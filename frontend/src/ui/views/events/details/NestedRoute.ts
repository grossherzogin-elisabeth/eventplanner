import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_EVENTS],
    backTo: Routes.Events,
};

const route: RouteRecordRaw = {
    path: 'details/:key',
    name: Routes.EventDetails,
    component: () => import('./EventDetailsView.vue'),
    meta: routeMeta,
};

export default route;
