import type { RouteRecordRaw } from 'vue-router';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';

const routeMeta: RouteMetaData = {
    authenticated: false,
};

const route: RouteRecordRaw = {
    path: '/calculator',
    name: 'wind-calculator',
    component: () => import('./WindCalculatorView.vue'),
    meta: routeMeta,
};

export default route;
