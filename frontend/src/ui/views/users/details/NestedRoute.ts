import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import { Routes } from '@/ui/views/Routes.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_USER_DETAILS],
    backTo: Routes.UsersList,
};

const route: RouteRecordRaw = {
    path: 'edit/:key',
    name: Routes.UserDetails,
    component: () => import('./UserDetailsView.vue'),
    meta: routeMeta,
};

export default route;
