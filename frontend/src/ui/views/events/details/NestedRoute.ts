import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    title: () => 'Reisedetails Test',
    authenticated: true,
    permissions: [Permission.READ_EVENTS],
    backTo: Routes.Events,
    breadcrumps: (route) => ['Reisen', String(route.params.year), 'Reisedetails'],
};

const route: RouteRecordRaw = {
    path: 'details/:key',
    name: Routes.EventDetails,
    components: {
        default: () => import('./EventDetailsView.vue'),
        // default: () => getActiveDefaultRouteOrElse(import('../list/ListEventsView.vue')),
        // dialog: () => import('./EventDetailsView.vue'),
    },
    meta: routeMeta,
};

export default route;
