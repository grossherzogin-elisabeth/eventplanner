import type { RouteLocationNormalizedLoaded, RouteMeta } from 'vue-router';

export interface RouteMetaData extends RouteMeta {
    /**
     * Tab title
     */
    title?: string | ((route: RouteLocationNormalizedLoaded) => string);
    /**
     * Is this route authenticated? If true the router will automatically trigger a login before entering the route.
     */
    authenticated?: boolean;

    backTo?: string;

    permissions?: string[];

    breadcrumps?: (route: RouteLocationNormalizedLoaded) => string[];

    hasTransparentHeader?: boolean;
}
