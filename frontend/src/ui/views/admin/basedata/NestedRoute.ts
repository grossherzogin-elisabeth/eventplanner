import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.WRITE_QUALIFICATIONS],
};

const route: RouteRecordRaw = {
    path: 'qualifications',
    name: Routes.Basedata,
    component: () => import('./BasedataAdminView.vue'),
    meta: routeMeta,
};

export default route;
