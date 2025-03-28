import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import { Routes } from '@/ui/views/Routes.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_EVENTS],
    backTo: Routes.EventsCalendar,
};

const route: RouteRecordRaw = {
    path: ':year/details/:key',
    name: Routes.EventDetails,
    component: () => import('./EventDetailsView.vue'),
    meta: routeMeta,
};

export default route;
