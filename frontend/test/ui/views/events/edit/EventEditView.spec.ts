import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { AuthService } from '@/application';
import { useAuthService } from '@/application';
import { EventSignupType, EventState, Permission } from '@/domain';
import { Routes } from '@/ui/views/Routes';
import EventEditView from '@/ui/views/events/edit/EventEditView.vue';
import { mockEventRepresentation, mockRouter, mockSignedInUser, server } from '~/mocks';
import { awaitPageContentLoaded, getTabs, openPageContextMenu } from '~/utils';

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

describe('EventEditView.vue', () => {
    let testee: VueWrapper;
    let authService: AuthService;

    beforeEach(async () => {
        authService = useAuthService();
        const signedInUser = mockSignedInUser();
        signedInUser.permissions.push(Permission.WRITE_EVENTS, Permission.WRITE_EVENT_DETAILS, Permission.WRITE_EVENT_SLOTS);
        authService.setSignedInUser(signedInUser);

        await router.push({ name: Routes.EventEdit, params: { year: 2025, key: 'example-event' } });
    });

    describe.each(allEvents)('$name', ({ representation }) => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should render event name', async () => {
            await awaitPageContentLoaded(testee);
            expect(testee.text()).toContain(representation.name);
        });

        it('should render context menu', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            expect(menu.exists()).toBe(true);
        });

        it('should render all export actions', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            const exports = menu.findAll('[data-test-id="action-export"]');
            expect(exports).toHaveLength(2);
            expect(exports[0].text()).toContain('some template');
            expect(exports[1].text()).toContain('some other template');
        });

        it('should render basic tabs', async () => {
            await awaitPageContentLoaded(testee);
            const tabs = getTabs(testee);
            expect(tabs.find('[data-test-id="tab-data"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-locations"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-registrations"]').exists()).toBe(true);
        });

        it('should render basic context menu actions', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            expect(menu.find('[data-test-id="action-add-registration"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-add-location"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-contact-crew"]').exists()).toBe(true);
            expect(menu.find('[data-test-id="action-cancel"]').exists()).toBe(true);
        });

        it('should show confirmation dialog on cancel action', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            await menu.find('[data-test-id="action-cancel"]').trigger('click');

            const dialog = testee.find('[data-test-id="cancel-event-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should open dialog to add registration', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            await menu.find('[data-test-id="action-add-registration"]').trigger('click');

            const dialog = testee.find('[data-test-id="add-registration-dialog"]');
            expect(dialog.exists()).toBe(true);
            expect(dialog.isVisible()).toBe(true);
        });

        it('should open dialog to add event location', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            await menu.find('[data-test-id="action-add-location"]').trigger('click');

            const dialog = testee.find('[data-test-id="edit-location-dialog"]');
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
            await awaitPageContentLoaded(testee);
            const tabs = getTabs(testee);
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(true);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(true);
        });

        it('should open dialog to add event slot', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            await menu.find('[data-test-id="action-add-slot"]').trigger('click');

            const dialog = testee.find('[data-test-id="edit-slot-dialog"]');
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
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
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
            await awaitPageContentLoaded(testee);
            expect(testee.find('[data-test-id="info-draft-state"]').exists()).toBe(true);
        });

        it('should have open for signup action', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
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
            await awaitPageContentLoaded(testee);
            expect(testee.find('[data-test-id="info-canceled-state"]').exists()).toBe(true);
        });
    });

    describe('Events in state crew signup', () => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(eventInStateCrewSignup.representation)));
            testee = mount(EventEditView, { global: { plugins: [router] } });
        });

        it('should show crew signup info', async () => {
            await awaitPageContentLoaded(testee);
            expect(testee.find('[data-test-id="info-crew-signup-state"]').exists()).toBe(true);
        });

        it('should have publish crew planning action', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
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
            await awaitPageContentLoaded(testee);
            const tabs = getTabs(testee);
            expect(tabs.find('[data-test-id="tab-slots"]').exists()).toBe(false);
            expect(tabs.find('[data-test-id="tab-crew"]').exists()).toBe(false);
        });

        it('should have correct context menu actions', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            expect(menu.find('[data-test-id="action-add-slot"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-open-for-crew-signup"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-publish-crew-planning"]').exists()).toBe(false);
            expect(menu.find('[data-test-id="action-reset-crew-planning"]').exists()).toBe(false);
        });
    });
});
