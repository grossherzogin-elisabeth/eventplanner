import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import { Routes } from '@/ui/views/Routes.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_USERS],
};

const route: RouteRecordRaw = {
    path: '',
    name: Routes.UsersList,
    component: () => import('./UsersListView.vue'),
    meta: routeMeta,
};

export default route;
