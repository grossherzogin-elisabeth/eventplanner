import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import Edit from './edit/NestedRoute';
import List from './list/NestedRoute';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.WRITE_EVENTS],
};

const route: RouteRecordRaw = {
    path: 'events',
    meta: routeMeta,
    name: 'app_admin-events-parent',
    children: [List, Edit],
};

export default route;
