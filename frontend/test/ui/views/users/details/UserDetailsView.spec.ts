import { nextTick } from 'vue';
import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { UserDetailsRepresentation } from '@/adapter/rest/UserRestRepository.ts';
import { Permission } from '@/domain';
import { Routes } from '@/ui/views/Routes.ts';
import UserDetailsView from '@/ui/views/users/details/UserDetailsView.vue';
import { mockRouter, mockUserDetailsRepresentation, server } from '~/mocks';
import { awaitPageContentLoaded, getTabs, openPageContextMenu, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('UserDetailsView.vue', () => {
    let testee: VueWrapper;
    let user: UserDetailsRepresentation;

    beforeEach(async () => {
        user = mockUserDetailsRepresentation();
        await router.push({ name: Routes.UserDetails, params: { key: user.key } });
        server.use(http.get(`/api/v1/users/${user.key}`, () => HttpResponse.json(user)));
        testee = mount(UserDetailsView, { global: { plugins: [router] } });
        await nextTick();
    });

    it('should render user name', async () => {
        await awaitPageContentLoaded(testee);
        expect(testee.text()).toContain(user.firstName);
        expect(testee.text()).toContain(user.lastName);
    });

    describe('users with permission users:read-details', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_USER_DETAILS]);
        });

        it('should not render user roles tab', async () => {
            await awaitPageContentLoaded(testee);
            const tabs = getTabs(testee);
            expect(tabs.find('[data-test-id="tab-roles"]').exists()).toBe(false);
        });

        it('should not allow any input', async () => {
            await awaitPageContentLoaded(testee);
            const inputs = testee.findAll('input');
            // Make sure this test does actually test something. 10 is an arbitrary expected count though. We just
            // expect there to be multiple input fields. As of writing this test there are 21 inputs in total. The exact
            // number may change though, and we don't want this test to break every time.
            expect(inputs.length).toBeGreaterThan(10);
            inputs.forEach((input) => expect(input.element.disabled).toBe(true));
        });

        it('should not render save button', async () => {
            await awaitPageContentLoaded(testee);
            expect(testee.find('[name="save"]').exists()).toBe(false);
        });

        it('should not render context menu', async () => {
            const triggers = testee.findAll('[data-test-id="menu-trigger"]').filter((trigger) => trigger.isVisible());
            expect(triggers).toHaveLength(0);
        });
    });

    describe('users with permission users:write', () => {
        beforeEach(() => {
            setupUserPermissions([Permission.READ_USER_DETAILS, Permission.WRITE_USERS]);
        });

        it('should render user roles tab', async () => {
            await awaitPageContentLoaded(testee);
            const tabs = getTabs(testee);
            expect(tabs.find('[data-test-id="tab-roles"]').exists()).toBe(true);
        });

        it('should render save button', async () => {
            await awaitPageContentLoaded(testee);
            expect(testee.find('[name="save"]').exists()).toBe(true);
        });

        it('should render context menu', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            expect(menu.find('[data-test-id="action-impersonate"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-add-registration"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-add-qualification"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-contact"]').exists()).toBe(true);
        });
    });
});
