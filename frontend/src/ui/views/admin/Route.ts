import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import Events from './events/NestedRoute';
import Users from './users/NestedRoute';

const routeMeta: RouteMetaData = {
    title: 'Admin',
    authenticated: true,
    permissions: [Permission.WRITE_EVENTS],
};

const route: RouteRecordRaw = {
    path: '/admin',
    meta: routeMeta,
    name: 'app_admin-parent',
    children: [Events, Users],
};

export default route;
