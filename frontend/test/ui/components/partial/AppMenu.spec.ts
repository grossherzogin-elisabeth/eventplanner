import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { AuthService } from '@/application';
import { useAuthService } from '@/application';
import { Permission, Role } from '@/domain';
import AppMenu from '@/ui/components/partials/AppMenu.vue';
import { mockRouter, mockSignedInUser, server } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): Partial<RouteLocationNormalizedLoadedGeneric> => router.currentRoute.value,
}));

describe('AppMenu.vue', () => {
    let authService: AuthService;
    let testee: VueWrapper;

    beforeEach(() => {
        authService = useAuthService();
    });

    describe('anonymous users', () => {
        beforeEach(() => {
            server.use(http.get('/api/v1/account', () => HttpResponse.text('', { status: 401 })));
            authService.setSignedInUser(undefined);
            testee = mount(AppMenu, {});
        });

        it('should render login hint', async () => {
            await expect.poll(() => testee.find('[data-test-id="menu-loading"]').exists()).toBe(false);
            expect(testee.find('[data-test-id="create-account-hint"]').exists()).toBe(true);
        });

        it('should show correct menu items', async () => {
            const expected = new Map<string, boolean>();
            expected.set('[data-test-id="menu-item-home"]', false);
            expected.set('[data-test-id="menu-item-onboarding"]', false);
            expected.set('[data-test-id="menu-item-calendar"]', false);
            expected.set('[data-test-id="menu-item-event-list"]', false);
            expected.set('[data-test-id="menu-item-event-admin"]', false);
            expected.set('[data-test-id="menu-item-user-list"]', false);
            expected.set('[data-test-id="menu-item-admin-settings"]', false);
            expected.set('[data-test-id="menu-item-account"]', false);
            expected.set('[data-test-id="menu-item-logout"]', false);
            expected.set('[data-test-id="menu-item-login"]', true);
            expected.set('[data-test-id="menu-item-register"]', true);

            await expect.poll(() => testee.find('[data-test-id="menu-loading"]').exists()).toBe(false);
            expected.forEach((expected, selector) => {
                console.log(`Testing ${selector}`);
                expect(testee.find(selector).exists()).toBe(expected);
            });
        });
    });

    describe('users with role TEAM_MEMBER', () => {
        const signedInUser = mockSignedInUser({
            permissions: [
                Permission.READ_EVENTS,
                Permission.READ_USERS,
                Permission.READ_QUALIFICATIONS,
                Permission.READ_POSITIONS,
                Permission.READ_OWN_USER,
                Permission.WRITE_OWN_USER,
                Permission.WRITE_OWN_REGISTRATIONS,
            ],
            roles: [Role.TEAM_MEMBER],
        });

        beforeEach(() => {
            server.use(http.get('/api/v1/account', () => HttpResponse.json(signedInUser, { status: 200 })));
            authService.setSignedInUser(undefined);
            testee = mount(AppMenu, {});
        });

        it('should show correct menu items', async () => {
            const expected = new Map<string, boolean>();
            expected.set('[data-test-id="menu-item-home"]', true);
            expected.set('[data-test-id="menu-item-onboarding"]', false);
            expected.set('[data-test-id="menu-item-calendar"]', true);
            expected.set('[data-test-id="menu-item-event-list"]', true);
            expected.set('[data-test-id="menu-item-event-admin"]', false);
            expected.set('[data-test-id="menu-item-user-list"]', false);
            expected.set('[data-test-id="menu-item-admin-settings"]', false);
            expected.set('[data-test-id="menu-item-account"]', true);
            expected.set('[data-test-id="menu-item-logout"]', true);
            expected.set('[data-test-id="menu-item-login"]', false);
            expected.set('[data-test-id="menu-item-register"]', false);

            await expect.poll(() => testee.find('[data-test-id="menu-loading"]').exists()).toBe(false);
            expected.forEach((expected, selector) => {
                console.log(`Testing ${selector}`);
                expect(testee.find(selector).exists()).toBe(expected);
            });
        });

        it('should render loading state initially', () => {
            expect(testee.find('[data-test-id="menu-loading"]').exists()).toBe(true);
        });

        it('should stop rendering loading state on authentication', async () => {
            authService.setSignedInUser(mockSignedInUser());
            await expect.poll(() => testee.find('[data-test-id="menu-loading"]').exists()).toBe(false);
        });
    });

    describe('users with role ADMIN', () => {
        const signedInUser = mockSignedInUser({
            permissions: Object.values(Permission),
            roles: [Role.ADMIN, Role.TEAM_MEMBER],
        });

        beforeEach(() => {
            server.use(http.get('/api/v1/account', () => HttpResponse.json(signedInUser, { status: 200 })));
            authService.setSignedInUser(undefined);
            testee = mount(AppMenu, {});
        });

        it('should show correct menu items', async () => {
            const expected = new Map<string, boolean>();
            expected.set('[data-test-id="menu-item-home"]', true);
            expected.set('[data-test-id="menu-item-onboarding"]', false);
            expected.set('[data-test-id="menu-item-calendar"]', true);
            expected.set('[data-test-id="menu-item-event-list"]', true);
            expected.set('[data-test-id="menu-item-event-admin"]', true);
            expected.set('[data-test-id="menu-item-user-list"]', true);
            expected.set('[data-test-id="menu-item-admin-settings"]', true);
            expected.set('[data-test-id="menu-item-account"]', true);
            expected.set('[data-test-id="menu-item-logout"]', true);
            expected.set('[data-test-id="menu-item-login"]', false);
            expected.set('[data-test-id="menu-item-register"]', false);

            await expect.poll(() => testee.find('[data-test-id="menu-loading"]').exists()).toBe(false);
            expected.forEach((expected, selector) => {
                console.log(`Testing ${selector}`);
                expect(testee.find(selector).exists()).toBe(expected);
            });
        });
    });
});
