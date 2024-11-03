import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import Details from './details/NestedRoute.ts';
import List from './list/NestedRoute.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_USER_DETAILS],
};

const route: RouteRecordRaw = {
    path: '/users',
    meta: routeMeta,
    name: 'app_admin-users-parent',
    children: [List, Details],
};

export default route;
