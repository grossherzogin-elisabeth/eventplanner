import type { RouteRecordRaw, Router } from 'vue-router';
import { createRouter, createWebHistory } from 'vue-router';
import type { AuthUseCase } from '@/application';
import { hasAnyOverlap } from '@/common';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';

interface SavedRouteState {
    query: unknown;
    scrollY: number;
    scrollX: number;
}

let isBackNavigation = false;
globalThis.addEventListener('popstate', () => (isBackNavigation = true));
globalThis.history.pushState = new Proxy(globalThis.history.pushState, {
    apply: (target, thisArg, argArray: never): unknown => {
        isBackNavigation = false;
        return target.apply(thisArg, argArray);
    },
});

export function saveScrollPosition(path: string = globalThis.location.pathname): void {
    try {
        const state: SavedRouteState = JSON.parse(sessionStorage.getItem(path) || '{}');
        const routerView = document.getElementById('router-view');
        state.scrollY = routerView?.scrollTop || window.scrollY;
        state.scrollX = routerView?.scrollLeft || window.scrollX;
        sessionStorage.setItem(path, JSON.stringify(state));
    } catch (e) {
        console.error(e);
    }
}

export function restoreScrollPosition(path: string = globalThis.location.pathname): void {
    if (isBackNavigation) {
        try {
            const state = JSON.parse(sessionStorage.getItem(path) || '{}');
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
     * Add an authentication guard to the router
     */
    router.beforeResolve(async (to, _, next) => {
        const meta = to.meta as RouteMetaData | undefined;
        // if there is a pending redirect from pre login, restore the wanted page first
        const redirect = localStorage.getItem('login-redirect');
        if (redirect && to.fullPath === '/') {
            localStorage.removeItem('login-redirect');
            next({ path: redirect });
            return;
        }

        // authentication and authorization guard
        if (meta?.authenticated) {
            try {
                const user = await authUseCase.authenticate();
                if (meta?.permissions && !hasAnyOverlap(meta.permissions, user.permissions)) {
                    console.warn(`🛤️ Missing permission for route '${String(to.name)}'!`);
                    next({ path: '/' });
                    return;
                }
                // eslint-disable-next-line @typescript-eslint/no-unused-vars
            } catch (e) {
                console.warn(`🛤️ Login required for route '${String(to.name)}'!`);
                localStorage.setItem('login-redirect', to.fullPath);
                next({ path: '/login' });
                return;
            }
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
            globalThis.location.href = to.fullPath;
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
