import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData.ts';
import { Routes } from '@/ui/views/Routes.ts';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.WRITE_QUALIFICATIONS, Permission.WRITE_POSITIONS],
};

const route: RouteRecordRaw = {
    path: '/basedata',
    name: Routes.Basedata,
    component: () => import('./BasedataAdminView.vue'),
    meta: routeMeta,
};

export default route;
