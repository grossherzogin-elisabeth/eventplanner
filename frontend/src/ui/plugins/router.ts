import type { RouteRecordRaw, Router } from 'vue-router';
import { createRouter, createWebHistory } from 'vue-router';
import type { AuthUseCase } from '@/application';
import type { Permission } from '@/domain';
import type { RouteMetaData } from '@/ui/model/RouteMetaData';

export function setupRouter(authUseCase: AuthUseCase): Router {
    const routes = getRoutes();
    const router = createRouter({
        history: createWebHistory(import.meta.env.BASE_URL),
        routes,
        scrollBehavior: (to, from, savedPosition) => {
            if (to.hash) {
                return {
                    el: to.hash,
                    behavior: 'smooth',
                };
            }
            if (savedPosition) {
                return savedPosition;
            }
            return { top: 0 };
        },
    });

    /**
     * Add an authentication guard to the router. This guard requires the auth service of app context and therefore
     * must be initialized here instead of within the router plugin
     */
    router.beforeResolve(async (to, from, next) => {
        const meta = to.meta as RouteMetaData | undefined;
        const user = authUseCase.getSignedInUser();
        // authentication guard
        if (meta?.authenticated && user === undefined) {
            await authUseCase.onLogin();
            console.warn(`🛤️ Login required for route '${to.fullPath}'!`);
            // next({ path: '/unauthorized' });
        }
        // permission guard
        if (meta?.permissions && meta.permissions.find((it) => !user?.permissions.includes(it as Permission))) {
            console.warn(`🛤️ Missing permission for route '${to.fullPath}'!`);
            next({ path: '/' });
            return;
        }
        console.log(`🛤️ Entering route '${to.fullPath}'`);
        next();
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
