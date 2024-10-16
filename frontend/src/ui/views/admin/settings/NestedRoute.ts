import type { RouteRecordRaw } from 'vue-router';
import { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';
import { Routes } from '@/ui/views/Routes';

const routeMeta: RouteMetaData = {
    authenticated: true,
    permissions: [Permission.WRITE_SETTINGS],
};

const route: RouteRecordRaw = {
    path: 'settings',
    name: Routes.AppSettings,
    component: () => import('./AppSettingsView.vue'),
    meta: routeMeta,
};

export default route;