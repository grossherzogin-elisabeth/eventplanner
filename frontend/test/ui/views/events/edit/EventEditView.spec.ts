import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { DOMWrapper, mount } from '@vue/test-utils';
import type { VueWrapper } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { EventRepresentation } from '@/adapter/rest/EventRestRepository.ts';
import { useAuthUseCase } from '@/application';
import { wait } from '@/common';
import { EventSignupType, EventState } from '@/domain';
import { setupRouter } from '@/ui/plugins/router';
import { Routes } from '@/ui/views/Routes';
import EventEditView from '@/ui/views/events/edit/EventEditView.vue';
import { mockEventRepresentation, server } from '~/mocks';

const eventInStateDraft = mockEventRepresentation({
    name: 'eventInStateDraft',
    state: EventState.Draft,
    signupType: EventSignupType.Assignment,
});
const eventInStateCrewSignup = mockEventRepresentation({
    name: 'eventInStateCrewSignup',
    state: EventState.OpenForSignup,
    signupType: EventSignupType.Assignment,
});
const eventInStatePlanned = mockEventRepresentation({
    name: 'eventInStatePlanned',
    state: EventState.Planned,
    signupType: EventSignupType.Assignment,
});
const eventInStateCanceled = mockEventRepresentation({
    name: 'eventInStateCanceled',
    state: EventState.Canceled,
    signupType: EventSignupType.Assignment,
});
const eventWithOpenSignup = mockEventRepresentation({
    name: 'eventWithOpenSignup',
    state: EventState.OpenForSignup,
    signupType: EventSignupType.Open,
});

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

    describe.each([eventInStateDraft, eventInStateCrewSignup, eventInStatePlanned, eventInStateCanceled, eventWithOpenSignup])(
        '$0',
        (event) => {
            beforeEach(async () => {
                server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(event)));
                testee = mount(EventEditView, { global: { plugins: [router] } });
            });

            it('should render event name', async () => {
                await awaitEventLoaded(event);
            });

            it('should render context menu', async () => {
                await awaitEventLoaded(event);
                const menu = await findContextMenu();
                expect(menu.exists()).toBe(true);
            });

            it('should render all export actions', async () => {
                await awaitEventLoaded(event);
                const menu = await findContextMenu();
                expect(menu.findAll('[data-test-id="action-export"]')).toHaveLength(2);
            });

            it('should render basic tabs', async () => {
                await awaitEventLoaded(event);
                const tabs = findTabs();
                expect(tabs.find('[data-test-id="tab-data"]').exists()).toBe(true);
                expect(tabs.find('[data-test-id="tab-locations"]').exists()).toBe(true);
                expect(tabs.find('[data-test-id="tab-registrations"]').exists()).toBe(true);
            });

            it('should render basic context menu actions', async () => {
                await awaitEventLoaded(event);
                const menu = await findContextMenu();
                expect(menu.find('[data-test-id="action-add-registration"]').exists()).toBe(true);
                expect(menu.find('[data-test-id="action-add-location"]').exists()).toBe(true);
                expect(menu.find('[data-test-id="action-contact-crew"]').exists()).toBe(true);
                expect(menu.find('[data-test-id="action-cancel"]').exists()).toBe(true);
            });
        }
    );

    describe('Planned events', () => {
        const event = eventInStatePlanned;

        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(event)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should render all tabs', async () => {
            await awaitEventLoaded(event);
            const tabs = findTabs();
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(true);
        });

        it('should show correct context menu actions', async () => {
            await awaitEventLoaded(event);
            const menu = await findContextMenu();
            expect(menu.find('[data-test-id="action-add-slot"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-open-for-crew-signup"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-publish-crew-planning"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-reset-crew-planning"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-cancel"]').exists()).toBe(true);
        });
    });

    describe('Events with open signup', () => {
        const event = eventWithOpenSignup;

        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(event)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should not render slots and crew manager tabs', async () => {
            await awaitEventLoaded(event);
            const tabs = findTabs();
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(false);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(false);
        });

        it('should show correct context menu actions', async () => {
            await awaitEventLoaded(event);
            const menu = await findContextMenu();
            expect(menu.find('[data-test-id="action-add-slot"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-open-for-crew-signup"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-publish-crew-planning"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-reset-crew-planning"]').exists()).toBe(false);
        });
    });

    async function findContextMenu(): Promise<DOMWrapper<Element>> {
        const triggers = testee.findAll('[data-test-id="menu-trigger"]').filter((trigger) => trigger.isVisible());
        expect(triggers).toHaveLength(1);
        await triggers[0].find('button').trigger('click');
        await nextTick();
        await wait(100);
        const menus = document.querySelectorAll('[data-test-id="context-menu"]');
        expect(menus).toHaveLength(1);
        return new DOMWrapper(menus[0]);
    }

    function findTabs(): DOMWrapper<Element> {
        return testee.find('[data-test-id="tabbar"]');
    }

    async function awaitEventLoaded(event: EventRepresentation): Promise<void> {
        const title = testee.find('[data-test-id="title"]');
        await expect.poll(() => title.text()).includes(event.name);
    }
});
