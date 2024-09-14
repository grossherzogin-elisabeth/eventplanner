import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.READ_USER_DETAILS],
    breadcrumps: () => ['Admin', 'Nutzerverwaltung', 'Nutzer bearbeiten'],
    backTo: Routes.UsersList,
};

const route: RouteRecordRaw = {
    path: 'edit/:key',
    name: Routes.UserDetails,
    component: () => import('./UserDetailsView.vue'),
    meta: routeMeta,
};

export default route;
