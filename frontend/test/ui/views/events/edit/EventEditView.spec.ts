import { nextTick } from 'vue';
import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { EventRepresentation } from '@/adapter/rest/EventRestRepository.ts';
import { useAuthUseCase } from '@/application';
import { EventSignupType, EventState } from '@/domain';
import { Routes } from '@/ui/views/Routes';
import EventEditView from '@/ui/views/events/edit/EventEditView.vue';
import { mockEventRepresentation, server } from '~/mocks';
import { mockRouter } from '~/mocks/router/mockRouter';
import { find } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

const eventInStateDraft = {
    name: 'Events in state draft',
    representation: mockEventRepresentation({
        state: EventState.Draft,
        signupType: EventSignupType.Assignment,
    }),
};
const eventInStateCrewSignup = {
    name: 'Events in state crew signup',
    representation: mockEventRepresentation({
        state: EventState.OpenForSignup,
        signupType: EventSignupType.Assignment,
    }),
};
const eventInStatePlanned = {
    name: 'Events in state planned',
    representation: mockEventRepresentation({
        state: EventState.Planned,
        signupType: EventSignupType.Assignment,
    }),
};
const eventInStateCanceled = {
    name: 'Events in state canceled',
    representation: mockEventRepresentation({
        state: EventState.Canceled,
        signupType: EventSignupType.Assignment,
    }),
};
const eventWithOpenSignup = {
    name: 'Events with open signup',
    representation: mockEventRepresentation({
        state: EventState.OpenForSignup,
        signupType: EventSignupType.Open,
    }),
};
const eventsWithCrewAssignment = [eventInStateDraft, eventInStateCrewSignup, eventInStatePlanned];
const allEvents = [eventInStateDraft, eventInStateCrewSignup, eventInStatePlanned, eventInStateCanceled, eventWithOpenSignup];

describe('EventEditView', () => {
    let testee: VueWrapper;

    beforeEach(async () => {
        await useAuthUseCase().authenticate();
        await router.push({ name: Routes.EventEdit, params: { year: 2025, key: 'example-event' } });
        testee = mount(EventEditView, {
            global: {
                plugins: [router],
            },
        });
    });

    describe.each(allEvents)('$name', ({ representation }) => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should render event name', async () => {
            await awaitEventLoaded();
        });

        it('should render context menu', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            expect(menu.exists()).toBe(true);
        });

        it('should render all export actions', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            const exports = menu.findAll('[data-test-id="action-export"]');
            expect(exports).toHaveLength(2);
            expect(exports[0].text()).toContain('some template');
            expect(exports[1].text()).toContain('some other template');
        });

        it('should render basic tabs', async () => {
            await awaitEventLoaded();
            const tabs = findTabs();
            expect(tabs.find('[data-test-id="tab-data"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-locations"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-registrations"]').exists()).toBe(true);
        });

        it('should render basic context menu actions', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            expect(menu.find('[data-test-id="action-add-registration"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-add-location"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-contact-crew"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-cancel"]').exists()).toBe(true);
        });

        it('should show confirmation dialog on cancel action', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            await menu.find('[data-test-id="action-cancel"]').trigger('click');

            const dialog = find('[data-test-id="cancel-event-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should open dialog to add registration', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            await menu.find('[data-test-id="action-add-registration"]').trigger('click');

            const dialog = find('[data-test-id="add-registration-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should open dialog to add event location', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            await menu.find('[data-test-id="action-add-location"]').trigger('click');

            const dialog = find('[data-test-id="edit-location-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });
    });

    describe.each(eventsWithCrewAssignment)('$name', ({ representation }) => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should render all tabs', async () => {
            await awaitEventLoaded();
            const tabs = findTabs();
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(true);
        });

        it('should open dialog to add event slot', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            await menu.find('[data-test-id="action-add-slot"]').trigger('click');

            const dialog = find('[data-test-id="edit-slot-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });
    });

    describe('Events in state planned', () => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(eventInStatePlanned.representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should have correct context menu actions', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            expect(menu.find('[data-test-id="action-add-slot"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-open-for-crew-signup"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-publish-crew-planning"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-reset-crew-planning"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-cancel"]').exists()).toBe(true);
        });
    });

    describe('Events in state draft', () => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(eventInStateDraft.representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should show draft info', async () => {
            await awaitEventLoaded();
            expect(testee.find('[data-test-id="info-draft-state"]').exists()).toBe(true);
        });

        it('should have open for signup action', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            expect(menu.find('[data-test-id="action-open-for-crew-signup"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-publish-crew-planning"]').exists()).toBe(false);
        });
    });

    describe('Events in state canceled', () => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(eventInStateCanceled.representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should show canceled info', async () => {
            await awaitEventLoaded();
            expect(testee.find('[data-test-id="info-canceled-state"]').exists()).toBe(true);
        });
    });

    describe('Events in state crew signup', () => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(eventInStateCrewSignup.representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should show crew signup info', async () => {
            await awaitEventLoaded();
            expect(testee.find('[data-test-id="info-crew-signup-state"]').exists()).toBe(true);
        });

        it('should have publish crew planning action', async () => {
            await awaitEventLoaded();
            const menu = await findContextMenu();
            expect(menu.find('[data-test-id="action-open-for-crew-signup"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-publish-crew-planning"]').exists()).toBe(true);
        });
    });

    describe('Events with open signup', () => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(eventWithOpenSignup.representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should not render slots and crew manager tabs', async () => {
            await awaitEventLoaded();
            const tabs = findTabs();
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(false);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(false);
        });

        it('should have correct context menu actions', async () => {
            await awaitEventLoaded();
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
        return find('[data-test-id="context-menu"]');
    }

    function findTabs(): DOMWrapper<Element> {
        return testee.find('[data-test-id="tabbar"]');
    }

    async function awaitEventLoaded(event?: EventRepresentation): Promise<void> {
        const title = testee.find('[data-test-id="title"]');
        await expect.poll(() => title.text()).includes(event?.name ?? 'Example Event');
    }
});
