import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: false,
    permissions: [],
};

const route: RouteRecordRaw = {
    path: ':year/details/:eventKey/registrations/:registrationKey/confirm',
    alias: ':year/details/:eventKey/registrations/:registrationKey/deny',
    name: Routes.EventConfirmParticipation,
    component: () => import('./ConfirmParticipationView.vue'),
    meta: routeMeta,
};

export default route;
