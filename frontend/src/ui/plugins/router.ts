import type { RouteRecordRaw, Router } from 'vue-router';
import { createRouter, createWebHistory } from 'vue-router';
import type { AuthUseCase } from '@/application';
import type { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';

interface SavedRouteState {
    query: unknown;
    scrollY: number;
    scrollX: number;
}

let isBackNavigation = false;
window.addEventListener('popstate', () => (isBackNavigation = true));
window.history.pushState = new Proxy(window.history.pushState, {
    apply: (target, thisArg, argArray: never): unknown => {
        isBackNavigation = false;
        return target.apply(thisArg, argArray);
    },
});

export function saveScrollPosition(path: string = window.location.pathname): void {
    try {
        const state: SavedRouteState = JSON.parse(localStorage.getItem(path) || '{}');
        const routerView = document.getElementById('router-view');
        state.scrollY = routerView?.scrollTop || window.scrollY;
        state.scrollX = routerView?.scrollLeft || window.scrollX;
        localStorage.setItem(path, JSON.stringify(state));
    } catch (e) {
        console.error(e);
    }
}

export function restoreScrollPosition(path: string = window.location.pathname): void {
    if (isBackNavigation) {
        try {
            const state = JSON.parse(localStorage.getItem(path) || '{}');
            const routerView = document.getElementById('router-view');
            routerView?.scrollTo({
                top: state.scrollY,
                left: state.scrollX,
                behavior: 'auto',
            });
        } catch (e) {
            console.error(e);
        }
    }
}

export function setupRouter(authUseCase: AuthUseCase): Router {
    const routes = getRoutes();
    const router = createRouter({
        history: createWebHistory(import.meta.env.BASE_URL),
        routes,
    });

    router.beforeEach((to, from, next) => {
        if (to.name !== from.name) {
            saveScrollPosition(from.path);
        }
        next();
    });

    /**
     * Add an authentication guard to the router. This guard requires the auth service of app context and therefore
     * must be initialized here instead of within the router plugin
     */
    router.beforeResolve(async (to, _, next) => {
        const meta = to.meta as RouteMetaData | undefined;
        // authentication guard
        const redirect = await authUseCase.firstAuthentication(to.fullPath);
        if (to.fullPath === '/' && redirect) {
            next({ path: redirect });
            return;
        }
        if (meta?.authenticated && !authUseCase.isLoggedIn()) {
            console.warn(`🛤️ Login required for route '${String(to.name)}'!`);
            next({ path: '/login' });
            return;
        }
        const user = authUseCase.getSignedInUser();
        // permission guard
        if (meta?.permissions && meta.permissions.find((it) => !user.permissions.includes(it as Permission))) {
            console.warn(`🛤️ Missing permission for route '${String(to.name)}'!`);
            next({ path: '/' });
            return;
        }
        console.log(`🛤️ Entering route '${String(to.name)}'`);
        next();
    });

    router.onError((error, to) => {
        if (
            error.message.includes('Failed to fetch dynamically imported module') ||
            error.message.includes('Importing a module script failed')
        ) {
            // load the target page with a page reload
            // this error occurs when we redeploy and a page hash changes while a user is on the page
            window.location.href = to.fullPath;
        }
    });

    return router;
}

/**
 * Dynamically load all routes of this context. Routes must be defined in a `Route.ts` file containing a single
 * route and exported as default.
 */
function getRoutes(): RouteRecordRaw[] {
    return (
        Object.values(
            import.meta.glob<{
                default?: RouteRecordRaw;
            }>('@/ui/views/**/Route.ts', { eager: true })
        )
            .filter((module) => module.default !== undefined)
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            .map((module) => module.default!)
    );
}
