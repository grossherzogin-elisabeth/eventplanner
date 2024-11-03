import type {
    RouteLocationNamedRaw,
    RouteLocationNormalized,
    RouteLocationPathRaw,
    RouteLocationRaw,
    RouteRecordName,
} from 'vue-router';

const stack: (RouteLocationNamedRaw | RouteLocationPathRaw)[] = [];

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useRouterStack() {
    function push(route: RouteLocationNormalized): void {
        if (stack.length > 0) {
            const lastRoute = stack[stack.length - 1];
            if ((lastRoute as RouteLocationNamedRaw).name === route.name) {
                stack.pop();
            }
        }
        if (route.name) {
            stack.push({
                name: route.name,
                query: route.query,
                hash: route.hash,
                params: route.params,
            });
        } else if (route.path) {
            stack.push({
                path: route.path,
                query: route.query,
                hash: route.hash,
            });
        }
    }

    function getLastOfKind(filterRouteNames: RouteRecordName[]): RouteLocationRaw | undefined {
        for (let i = stack.length - 1; i >= 0; i--) {
            const it = stack[i];
            const name = (it as RouteLocationNamedRaw).name;
            if (name && filterRouteNames.includes(name)) {
                return it;
            }
        }
        return undefined;
    }

    function getLastOther(): RouteLocationRaw | undefined {
        return stack[stack.length - 2];
    }

    return { push, getLastOfKind, getLastOther };
}
