import type { Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { AppSettings } from '@/domain';
import { Routes } from '@/ui/views/Routes.ts';
import TabNotifications from '@/ui/views/settings/TabNotifications.vue';
import { mockRouter, mockSettings } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('TabNotifications.vue', () => {
    let settings: AppSettings;
    let testee: VueWrapper;
    let emitted: AppSettings | undefined;

    beforeEach(async () => {
        settings = mockSettings();
        emitted = undefined;
        await router.push({ name: Routes.AppSettings, query: { tab: 'general' } });
        testee = mount(TabNotifications, {
            props: {
                'modelValue': settings,
                'onUpdate:modelValue': (e) => (emitted = e),
            },
            global: { plugins: [router], stubs: { teleport: true } },
        });
    });

    it('should show all notifications settings', async () => {
        expect(testee.text()).toContain(settings.email.from);
        expect(testee.text()).toContain(testee.vm.$t('views.settings.notifications.teams-webhook-set-up'));
    });

    it('should emit update event for teams webhook changes', async () => {
        await testee.find('[data-test-id="teams-webhook"]').trigger('click');
        const dialog = testee.find('[data-test-id="dialog"]');
        await dialog.find('[data-test-id="input-teams-webhook"] textarea').setValue('changed');
        await dialog.find('[data-test-id="button-submit"]').trigger('click');
        expect(emitted?.notifications.teamsWebhookUrl).toEqual('changed');
    });

    it('should emit update event for email changes', async () => {
        await testee.find('[data-test-id="email"]').trigger('click');
        const dialog = testee.find('[data-test-id="dialog"]');
        await dialog.find('[data-test-id="input-from-display-name"] input').setValue('changed');
        await dialog.find('[data-test-id="button-submit"]').trigger('click');
        expect(emitted?.email.fromDisplayName).toEqual('changed');
    });
});
