import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import type { VueWrapper } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { EventRepresentation, RegistrationRepresentation } from '@/adapter/rest/EventRestRepository';
import { useAuthUseCase } from '@/application';
import type { SignedInUser } from '@/domain';
import { SlotCriticality } from '@/domain';
import EventDetailsCard from '@/ui/components/events/EventDetailsCard.vue';
import EventLocationsCard from '@/ui/components/events/EventLocationsCard.vue';
import EventParticipantsCard from '@/ui/components/events/EventParticipantsCard.vue';
import EventRegistrationDetailsCard from '@/ui/components/events/EventRegistrationDetailsCard.vue';
import { Routes } from '@/ui/views/Routes';
import EventDetailsView from '@/ui/views/events/details/EventDetailsView.vue';
import { DECKHAND, mockEventRepresentation, mockRouter, server } from '~/mocks';
import { awaitPageContentLoaded, openPageContextMenu } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

const year = new Date().getFullYear();

describe('EventDetailsView.vue', () => {
    let testee: VueWrapper;
    let event: EventRepresentation;
    let signedInUser: SignedInUser;

    beforeEach(async () => {
        event = mockEventRepresentation();
        server.use(http.get(`/api/v1/events?year=${year}`, () => HttpResponse.json([event])));
        signedInUser = await useAuthUseCase().authenticate(); // uses the default mocked http requests for authentication
        await router.push({ name: Routes.EventDetails, params: { year, key: 'example-event' } });
    });

    describe('Signed in user is not registered', () => {
        beforeEach(async () => {
            testee = mount(EventDetailsView, { global: { plugins: [router] } });
        });

        it('should render signup button', async () => {
            await awaitPageContentLoaded(testee);
            const primaryBtn = testee.find('.btn-primary');
            expect(primaryBtn.exists()).toBe(true);
            expect(primaryBtn.text()).toContain(testee.vm.$t('views.event-details.sign-up'));
        });

        it('should render event details card', async () => {
            await awaitPageContentLoaded(testee);
            const card = testee.findComponent(EventDetailsCard);
            expect(card.exists()).toBe(true);
        });

        it('should render event locations card', async () => {
            await awaitPageContentLoaded(testee);
            const card = testee.findComponent(EventLocationsCard);
            expect(card.exists()).toBe(true);
        });

        it('should render event participants card', async () => {
            await awaitPageContentLoaded(testee);
            const card = testee.findComponent(EventParticipantsCard);
            expect(card.exists()).toBe(true);
        });
    });

    describe('Signed in user is on waiting list', () => {
        beforeEach(async () => {
            const registration: RegistrationRepresentation = {
                key: 'signed-in-user-regitration-key',
                userKey: signedInUser.key,
                positionKey: DECKHAND,
            };
            event.registrations.push(registration);

            testee = mount(EventDetailsView, { global: { plugins: [router] } });
        });

        it('should render leave waiting list button', async () => {
            await awaitPageContentLoaded(testee);
            const primaryBtn = testee.find('.btn-danger');
            expect(primaryBtn.exists()).toBe(true);
            expect(primaryBtn.text()).toContain(testee.vm.$t('views.event-details.leave-waitinglist'));
        });

        it('should have calendar entry action', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            const action = menu.find('[data-test-id="action-create-calendar-entry"]');
            expect(action.exists()).toBe(true);
            expect(action.classes()).not.toContain('disabled');
        });

        it('should have edit registration action', async () => {
            await awaitPageContentLoaded(testee);
            const menu = await openPageContextMenu(testee);
            const action = menu.find('[data-test-id="action-edit-registration"]');
            expect(action.exists()).toBe(true);
            expect(action.classes()).not.toContain('disabled');
        });

        it('should render registration card', async () => {
            await awaitPageContentLoaded(testee);
            const card = testee.findComponent(EventRegistrationDetailsCard);
            expect(card.exists()).toBe(true);
        });
    });

    describe('Signed in user is assigned to crew', () => {
        beforeEach(async () => {
            const registration: RegistrationRepresentation = {
                key: 'signed-in-user-regitration-key',
                userKey: signedInUser.key,
                positionKey: DECKHAND,
            };
            event.registrations.push(registration);
            event.slots.push({
                key: 'signed-in-user-slot-key',
                criticality: SlotCriticality.Important,
                order: 0,
                positionKeys: [DECKHAND],
                assignedRegistrationKey: registration.key,
            });

            server.use(http.get(`/api/v1/events?year=${year}`, () => HttpResponse.json([event])));
            testee = mount(EventDetailsView, { global: { plugins: [router] } });
        });

        it('should render leave crew button', async () => {
            await awaitPageContentLoaded(testee);
            const primaryBtn = testee.find('.btn-danger');
            expect(primaryBtn.exists()).toBe(true);
            expect(primaryBtn.text()).toContain(testee.vm.$t('views.event-details.leave-crew'));
        });
    });
});
