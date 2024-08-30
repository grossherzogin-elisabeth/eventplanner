import type { RouteLocationRaw, RouteRecordRaw, Router } from 'vue-router';

export class RouterUtils {
    public static registerRoutes(
        routes: RouteRecordRaw[],
        router: Router,
        overwrites?: Partial<RouteRecordRaw>[]
    ): void {
        for (let i = 0; i < routes.length; i++) {
            const route = routes[i];
            if (!route || !route.path || !route.name || (!route.component && !route.redirect)) {
                console.warn(`Skipping invalid route: ${JSON.stringify(route)}`);
                continue;
            }
            const routeOverride = (overwrites || []).find((r) => r.name === route.name);
            const mergedRoute = Object.assign(route, routeOverride);
            if (!router.getRoutes().find((r) => r.name === mergedRoute.name)) {
                router.addRoute(mergedRoute);
            } else {
                console.warn(`Route with name ${String(mergedRoute.name)} already exists. Skipping route.`);
            }
        }
    }

    public static storeQueryParameters(router: Router): void {
        const route = router.currentRoute.value;
        localStorage.setItem(`szhb-search.route.${route.name as string}.query`, JSON.stringify(route.query));
    }

    public static getStoredQueryParameters<T>(router: Router): Partial<T> {
        try {
            const route = router.currentRoute.value;
            const json = localStorage.getItem(`szhb-search.route.${route.name as string}.query`) || '{}';
            return JSON.parse(json);
        } catch (e) {
            return {};
        }
    }

    public static async back(router: Router, to: RouteLocationRaw): Promise<void> {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const _to = to as any;
        const route = router.getRoutes().find((it) => it.name === _to.name || it.path === _to || it.path === _to.path);
        const lastRoute = (router.options.history.state.back as string) || '';
        if (route && lastRoute.startsWith(route.path + '?')) {
            await router.push(lastRoute);
        } else {
            await router.push(to);
        }
    }

    public static async updateQuery(
        router: Router,
        prop: string,
        value: number | string | boolean | undefined
    ): Promise<void> {
        const route = router.currentRoute.value;
        if (route.query[prop] === value) {
            // nothing to do
            return;
        }
        if (value !== undefined) {
            // update the query param
            route.query[prop] = value.toString();
        } else {
            // remove the query param
            delete route.query[prop];
        }

        if (route.name) {
            await router.push({
                name: route.name || undefined,
                hash: route.hash,
                params: route.params,
                query: route.query,
                force: true,
            });
        } else {
            await router.push({
                hash: route.hash,
                path: route.path,
                query: route.query,
                force: true,
            });
        }
    }

    public static getQueryParameter(param: string): string | undefined {
        const url = window.location.href;
        // eslint-disable-next-line no-useless-escape
        const name = param.replace(/[\[\]]/g, '\\$&');
        const regex = new RegExp(`[?&]${name}(=([^&#]*)|&|#|$)`);
        const results = regex.exec(url);
        if (!results) {
            return undefined;
        }
        if (!results[2]) {
            return '';
        }
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
    }
}
