import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAppSettingsUseCase } from '@/application';
import { Permission } from '@/domain';
import AppSettingsView from '@/ui/views/settings/AppSettingsView.vue';
import TabNotifications from '@/ui/views/settings/tabs/TabNotifications.vue';
import TabUi from '@/ui/views/settings/tabs/TabUi.vue';
import { mockRouter, mockSettings } from '~/mocks';
import { getTabs, setupUserPermissions } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('AppSettingsView.vue', async () => {
    let testee: VueWrapper;

    beforeEach(() => {
        testee = mount(AppSettingsView);
        setupUserPermissions([Permission.WRITE_SETTINGS]);
    });

    it('should render all tabs', async () => {
        const tabs = getTabs(testee);
        expect(tabs.find('[data-test-id="tab-general"]').exists()).toBe(true);
        expect(tabs.find('[data-test-id="tab-notifications"]').exists()).toBe(true);
        expect(tabs.find('[data-test-id="tab-positions"]').exists()).toBe(true);
        expect(tabs.find('[data-test-id="tab-qualifications"]').exists()).toBe(true);
    });

    it('should save settings on notifications updated', async () => {
        const updateSettingsFunc = vi.spyOn(useAppSettingsUseCase(), 'updateSettings');
        await expect.poll(() => testee.findComponent(TabNotifications).exists()).toBe(true); // await initial load
        testee.findComponent(TabNotifications).vm.$emit('update:model-value', mockSettings());
        expect(updateSettingsFunc).toHaveBeenCalled();
    });

    it('should save settings on ui updated', async () => {
        const updateSettingsFunc = vi.spyOn(useAppSettingsUseCase(), 'updateSettings');
        await expect.poll(() => testee.findComponent(TabUi).exists()).toBe(true); // await initial load
        testee.findComponent(TabUi).vm.$emit('update:model-value', mockSettings());
        expect(updateSettingsFunc).toHaveBeenCalled();
    });
});
