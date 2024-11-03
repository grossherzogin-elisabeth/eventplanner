import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import Calendar from './calendar/NestedRoute';
import ConfirmParticipation from './confirm-participation/NestedRoute';
import Details from './details/NestedRoute';
import Edit from './edit/NestedRoute';
import ListAdmin from './list-admin/NestedRoute';
import List from './list/NestedRoute';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_EVENTS],
};

const route: RouteRecordRaw = {
    path: '/events',
    meta: routeMeta,
    name: 'app_event-parent',
    children: [List, Calendar, Details, ConfirmParticipation, ListAdmin, Edit],
};

export default route;
