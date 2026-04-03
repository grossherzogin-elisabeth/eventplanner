import type { Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { AppSettings } from '@/domain';
import { Routes } from '@/ui/views/Routes.ts';
import TabUi from '@/ui/views/settings/tabs/TabUi.vue';
import { mockRouter, mockSettings } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabUi.vue', () => {
    let settings: AppSettings;
    let testee: VueWrapper;
    let emitted: AppSettings | undefined;

    beforeEach(async () => {
        emitted = undefined;
        settings = mockSettings();
        await router.push({ name: Routes.AppSettings, query: { tab: 'general' } });
        testee = mount(TabUi, {
            props: {
                'modelValue': settings,
                'onUpdate:modelValue': (e) => (emitted = e),
            },
            global: { plugins: [router] },
        });
    });

    it('should show all ui settings', async () => {
        expect(testee.text()).toContain(settings.ui.tabTitle);
        expect(testee.text()).toContain(settings.ui.menuTitle);
        expect(testee.text()).toContain(settings.ui.supportEmail);
        expect(testee.text()).toContain(settings.ui.technicalSupportEmail);
    });

    it('should emit update event for tab title changes', async () => {
        await testee.find('[data-test-id="tab-title"]').trigger('click');
        const dialog = testee.find('[data-test-id="dialog"]');
        await dialog.find('[data-test-id="input-tab-title"] input').setValue('changed');
        await dialog.find('[data-test-id="button-submit"]').trigger('click');
        expect(emitted?.ui.tabTitle).toEqual('changed');
    });

    it('should emit update event for menu title changes', async () => {
        await testee.find('[data-test-id="menu-title"]').trigger('click');
        const dialog = testee.find('[data-test-id="dialog"]');
        await dialog.find('[data-test-id="input-menu-title"] input').setValue('changed');
        await dialog.find('[data-test-id="button-submit"]').trigger('click');
        expect(emitted?.ui.menuTitle).toEqual('changed');
    });

    it('should emit update event for support email changes', async () => {
        await testee.find('[data-test-id="support-email"]').trigger('click');
        const dialog = testee.find('[data-test-id="dialog"]');
        await dialog.find('[data-test-id="input-support-email"] input').setValue('changed');
        await dialog.find('[data-test-id="button-submit"]').trigger('click');
        expect(emitted?.ui.supportEmail).toEqual('changed');
    });

    it('should emit update event for tech support email changes', async () => {
        await testee.find('[data-test-id="tech-support-email"]').trigger('click');
        const dialog = testee.find('[data-test-id="dialog"]');
        await dialog.find('[data-test-id="input-tech-support-email"] input').setValue('changed');
        await dialog.find('[data-test-id="button-submit"]').trigger('click');
        expect(emitted?.ui.technicalSupportEmail).toEqual('changed');
    });
});
