import type { RouteRecordRaw, Router } from 'vue-router';
import { beforeEach, describe, expect, it } from 'vitest';
import { HttpResponse, http } from 'msw';
import { useAuthService, useAuthUseCase } from '@/application';
import { Permission } from '@/domain';
import { restoreScrollPosition, setupRouter } from '@/ui/plugins/router';
import { Routes } from '@/ui/views/Routes';
import { mockSignedInUser, server } from '~/mocks';

const mockedOpenRoute: RouteRecordRaw = {
    path: '/mock/open',
    name: 'mock-open',
    component: () => import('./MockView.vue'),
    meta: {
        authenticated: false,
    },
};

const mockedAuthenticatedRoute: RouteRecordRaw = {
    path: '/mock/authenticated',
    name: 'mock-authenticated',
    component: () => import('./MockView.vue'),
    meta: {
        authenticated: true,
        permissions: [Permission.READ_EVENTS],
    },
};

describe('router', () => {
    const authService = useAuthService();
    const authUseCase = useAuthUseCase();
    let router: Router;

    beforeEach(async () => {
        router = setupRouter(authUseCase);
        router.addRoute(mockedOpenRoute);
        router.addRoute(mockedAuthenticatedRoute);

        // Navigation to a real route within this test counts towards the coverage of this route, which would be a false
        // coverage, as we don't test any views here. To prevent this false coverage, we replace the components to load
        // with our mock view.
        for (const route of router.getRoutes()) {
            if (route.name && route.components?.default) {
                router.removeRoute(route.name);
                router.addRoute({ ...route, components: { default: () => import('./MockView.vue') } });
            }
        }

        localStorage.clear();
        sessionStorage.clear();

        document.body.innerHTML = `<div id="router-view"></div>`;
    });

    it('should initialize with all routes ', () => {
        const routes = router.getRoutes().map((it) => it.name);
        for (const route of Object.values(Routes)) {
            expect(routes).toContain(route);
        }
    });

    it('should save scroll position of previous route', async () => {
        await router.push({ name: mockedOpenRoute.name }); // the route which's scroll position we want to save
        await router.push('/');
        expect(sessionStorage.getItem(mockedOpenRoute.path)).toEqual('{"scrollY":0,"scrollX":0}');
    });

    it('should restore scroll position', async () => {
        await router.push({ name: mockedOpenRoute.name }); // the route we want to navigate back to
        await router.push('/'); // intermediate route
        // fake scroll position after mocking the history
        sessionStorage.setItem(mockedOpenRoute.path, '{"scrollY":200,"scrollX":100}');
        // scroll position is only restored when a browser back navigation is detected
        window.history.back();

        restoreScrollPosition(mockedOpenRoute.path);
        const routerView = document.getElementById('router-view');
        expect(routerView?.scrollTop).toBe(200);
        expect(routerView?.scrollLeft).toBe(100);
    });

    describe('anonymous users', () => {
        beforeEach(() => {
            authService.setSignedInUser(undefined);
            server.use(http.get('/api/v1/account', () => HttpResponse.text('', { status: 401 })));
        });

        it('should allow access to open route ', async () => {
            await router.push({ name: mockedOpenRoute.name });
            expect(router.currentRoute.value.name).toEqual(mockedOpenRoute.name);
        });

        it('should block access to authenticated route ', async () => {
            await router.push({ name: mockedAuthenticatedRoute.name });
            expect(router.currentRoute.value.name).toEqual(Routes.Login);
            expect(localStorage.getItem('login-redirect')).toEqual(mockedAuthenticatedRoute.path);
        });
    });

    describe('unauthorized users', () => {
        beforeEach(() => {
            authService.setSignedInUser(mockSignedInUser({ permissions: [] }));
        });

        it('should block access to route with insufficient permission ', async () => {
            await router.push({ name: mockedAuthenticatedRoute.name });
            expect(router.currentRoute.value.name).toEqual(Routes.Home);
        });
    });

    describe('authorized users', () => {
        beforeEach(() => {
            authService.setSignedInUser(mockSignedInUser({ permissions: [Permission.READ_EVENTS] }));
        });

        it('should allow access to authenticated page ', async () => {
            await router.push({ name: mockedAuthenticatedRoute.name });
            expect(router.currentRoute.value.name).toEqual(mockedAuthenticatedRoute.name);
        });

        it('should redirect to pre login page ', async () => {
            localStorage.setItem('login-redirect', mockedAuthenticatedRoute.path);
            await router.push('/');
            expect(router.currentRoute.value.name).toEqual(mockedAuthenticatedRoute.name);
        });
    });

    describe('offline access', () => {
        beforeEach(() => {
            localStorage.setItem('user', JSON.stringify(mockSignedInUser({ permissions: [Permission.READ_EVENTS] })));
            server.use(http.get('/api/v1/account', () => HttpResponse.error()));
        });

        it('should allow access to authenticated page ', async () => {
            await router.push({ name: mockedAuthenticatedRoute.name });
            expect(router.currentRoute.value.name).toEqual(mockedAuthenticatedRoute.name);
        });
    });
});
