import type { Router } from 'vue-router';
import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { EventRepresentation } from '@/adapter/rest/EventRestRepository.ts';
import { useAuthUseCase } from '@/application';
import { setupRouter } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes.ts';
import EventDetailsView from '@/ui/views/events/details/EventDetailsView.vue';

describe('EventDetailsView', () => {
    let router: Router;
    let testee: VueWrapper;

    beforeAll(() => {
        router = setupRouter(useAuthUseCase());
    });

    beforeEach(async () => {
        await router.push({ name: Routes.EventDetails, params: { year: 2025, key: 'example-event' } });
        testee = mount(EventDetailsView, { global: { plugins: [router] } });
    });

    it('should render event name', async () => {
        await awaitEventLoaded();
    });

    async function awaitEventLoaded(event?: EventRepresentation): Promise<void> {
        const title = testee.find('[data-test-id="title"]');
        await expect.poll(() => title.text()).includes(event?.name ?? 'Example Event');
    }
});
