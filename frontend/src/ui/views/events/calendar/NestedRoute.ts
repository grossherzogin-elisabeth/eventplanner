import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_EVENTS],
    hasTransparentHeader: true,
};

const route: RouteRecordRaw = {
    path: '',
    name: Routes.Events,
    component: () => import('./EventCalendarView.vue'),
    meta: routeMeta,
};

export default route;
