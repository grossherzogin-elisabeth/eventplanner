import { nextTick, ref } from 'vue';
import type { RouteLocation, RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { vi } from 'vitest';

let mockedRouter: Partial<Router> = {};

export const mockedCurrentRoute = ref<RouteLocationNormalizedLoadedGeneric>({
    name: 'test-route',
    path: '/test',
    hash: '',
    params: {},
    query: {},
    fullPath: '/test',
    matched: [],
    meta: {},
    redirectedFrom: undefined,
});

export const mockedReplaceFunc = vi.fn(async (to) => changeRoute(to));

export const mockedPushFunc = vi.fn(async (to) => changeRoute(to));

async function changeRoute(to: RouteLocation): Promise<void> {
    const path = to.path || mockedCurrentRoute.value.path;
    const query = to.query || {};
    mockedCurrentRoute.value = {
        ...mockedCurrentRoute.value,
        query: to.query || {},
        path: path,
        name: to.name || mockedCurrentRoute.value.name,
        hash: to.hash || mockedCurrentRoute.value.hash,
        params: to.params || mockedCurrentRoute.value.params,
        fullPath: `${path}?${new URLSearchParams(query as Record<string, string>).toString()}`,
    };
    await nextTick();
}

export function mockRouter(overwrite?: Partial<Router>): Router {
    mockedRouter = {
        push: mockedPushFunc,
        replace: mockedReplaceFunc,
        currentRoute: mockedCurrentRoute,
        isReady(): Promise<void> {
            return Promise.resolve();
        },
        install(): void {
            // do nothing
        },
        ...overwrite,
        beforeEach(): () => void {
            return () => {
                // do nothing
            };
        },
    };
    return mockedRouter as Router;
}
