import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import Details from './details/NestedRoute';
import List from './list/NestedRoute';

const routeMeta: RouteMetaData = {
    title: 'Crewverwaltung',
    authenticated: true,
    permissions: [Permission.READ_USER_DETAILS],
};

const route: RouteRecordRaw = {
    path: 'users',
    meta: routeMeta,
    name: 'app_admin-users-parent',
    children: [List, Details],
};

export default route;
