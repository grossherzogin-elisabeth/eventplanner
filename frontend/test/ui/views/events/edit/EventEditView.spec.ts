import type { Router } from 'vue-router';
import { useAuthUseCase } from '@/application';
import { EventSignupType } from '@/domain';
import { setupRouter } from '@/ui/plugins/router.ts';
import { Routes } from '@/ui/views/Routes.ts';
import EventEditView from '@/ui/views/events/edit/EventEditView.vue';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { mockEventRepresentation, server } from '~/mocks';

describe('EventEditView', () => {
    let router: Router;
    let testee: VueWrapper;

    beforeAll(() => {
        router = setupRouter(useAuthUseCase());
    });

    beforeEach(async () => {
        await router.push({ name: Routes.EventEdit, params: { year: 2025, key: 'example-event' } });
        testee = mount(EventEditView, { global: { plugins: [router] } });
    });

    it('should render event name', async () => {
        await awaitEventLoaded();
    });

    describe('Events with manual Assignment', () => {
        beforeEach(async () => {
            const event = mockEventRepresentation({ signupType: EventSignupType.Assignment });
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(event)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should render all tabs', async () => {
            await awaitEventLoaded();
            const tabs = findTabs();
            expect(tabs.find('[data-test-id="tab-data"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-locations"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-registrations"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(true);
        });
    });

    describe('Events with open signup', () => {
        beforeEach(async () => {
            const event = mockEventRepresentation({ signupType: EventSignupType.Open });
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(event)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should not render slots and crew manager tabs', async () => {
            await awaitEventLoaded();
            const tabs = findTabs();
            expect(tabs.find('[data-test-id="tab-data"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-locations"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-registrations"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(false);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(false);
        });
    });

    function findTabs(): DOMWrapper<Element> {
        return testee.find('[data-test-id="tabbar"]');
    }

    async function awaitEventLoaded(): Promise<void> {
        const title = testee.find('[data-test-id="title"]');
        await expect.poll(() => title.text()).includes('Example Event');
    }
});
