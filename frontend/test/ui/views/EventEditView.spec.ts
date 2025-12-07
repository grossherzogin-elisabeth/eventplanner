import type { Router } from 'vue-router';
import { useAuthUseCase } from '@/application';
import { setupRouter } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes';
import EventEditView from '@/ui/views/events/edit/EventEditView.vue';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

describe('EventEditView', () => {
    let router: Router;
    let testee: VueWrapper;

    beforeAll(() => {
        router = setupRouter(useAuthUseCase());
    });

    beforeEach(async () => {
        await router.push({ name: Routes.EventEdit, params: { year: 2025, key: 'example-event' } });
        testee = mount(EventEditView, {
            global: { plugins: [router] },
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('should render event name', async () => {
        await awaitEventLoaded();
    });

    it('should render all tabs', async () => {
        await awaitEventLoaded();
        const tabbar = testee.find('[data-test-id="tabbar"]');
        expect(tabbar.find('[data-test-id="tab-data"]').exists()).toBe(true);
        expect(tabbar.find('[data-test-id="tab-locations"]').exists()).toBe(true);
        expect(tabbar.find('[data-test-id="tab-registrations"]').exists()).toBe(true);
        expect(tabbar.find('[data-test-id="tab-slots"]').exists()).toBe(true);
        expect(tabbar.find('[data-test-id="tab-crew"]').exists()).toBe(true);
    });

    async function awaitEventLoaded(): Promise<void> {
        const title = testee.find('[data-test-id="title"]');
        await expect.poll(() => title.text()).includes('Example Event');
    }
});
