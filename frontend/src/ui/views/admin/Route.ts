import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import Basedata from './basedata/NestedRoute';
import Events from './events/NestedRoute';
import Settings from './settings/NestedRoute';
import Users from './users/NestedRoute';

const routeMeta: RouteMetaData = {
    authenticated: true,
};

const route: RouteRecordRaw = {
    path: '/admin',
    meta: routeMeta,
    name: 'app_admin-parent',
    children: [Events, Users, Settings, Basedata],
};

export default route;
