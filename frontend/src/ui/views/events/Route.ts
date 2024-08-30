import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import List from '@/ui/views/events/calendar/NestedRoute';
import Details from './details/NestedRoute';

const routeMeta: RouteMetaData = {
    title: (route) => 'Reisen ' + route.params.year,
    authenticated: true,
    permissions: [Permission.READ_EVENTS],
};

const route: RouteRecordRaw = {
    path: '/events/:year',
    meta: routeMeta,
    name: 'app_event-parent',
    children: [List, Details],
};

export default route;
