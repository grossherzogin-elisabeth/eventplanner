import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import type { VueWrapper } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { EventRepresentation } from '@/adapter/rest/EventRestRepository';
import { useAuthUseCase } from '@/application';
import type { SignedInUser } from '@/domain';
import { Routes } from '@/ui/views/Routes';
import EventCard from '@/ui/views/home/EventCard.vue';
import HomeView from '@/ui/views/home/HomeView.vue';
import { v4 as randomUUID } from 'uuid';
import { DECKHAND, mockEventRepresentation, mockRouter, server } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('HomeView.vue', () => {
    let testee: VueWrapper;
    let events: EventRepresentation[];
    let signedInUser: SignedInUser;

    beforeEach(async () => {
        vi.setSystemTime(new Date(2024, 3, 1).getTime());
        signedInUser = await useAuthUseCase().authenticate(); // uses the default mocked http requests for authentication
        events = [
            mockEventRepresentation({
                key: 'a',
                name: 'past event should not be visible',
                start: '2024-01-05T16:00',
                end: '2024-01-08T16:00',
                registrations: [{ key: randomUUID(), positionKey: DECKHAND, userKey: signedInUser.key }],
            }),
            mockEventRepresentation({
                key: 'b',
                name: 'event without registration for signed in user should not be visible',
                start: '2024-03-05T16:00',
                end: '2024-03-08T16:00',
                registrations: [],
            }),
            mockEventRepresentation({
                key: 'c',
                name: 'future event A should be visible',
                start: '2024-04-05T16:00',
                end: '2024-04-08T16:00',
                registrations: [{ key: randomUUID(), positionKey: DECKHAND, userKey: signedInUser.key }],
            }),
            mockEventRepresentation({
                key: 'd',
                name: 'future event B should be visible',
                start: '2024-05-05T16:00',
                end: '2024-05-08T16:00',
                registrations: [{ key: randomUUID(), positionKey: DECKHAND, userKey: signedInUser.key }],
            }),
            mockEventRepresentation({
                key: 'e',
                name: 'event in next year should be visible',
                start: '2025-05-05T16:00',
                end: '2025-05-08T16:00',
                registrations: [{ key: randomUUID(), positionKey: DECKHAND, userKey: signedInUser.key }],
            }),
        ];
        server.use(http.get('/api/v1/events', () => HttpResponse.json(events)));
        await router.push({ name: Routes.Home });
        testee = mount(HomeView, { global: { plugins: [router] } });
    });

    afterEach(async () => {
        vi.setSystemTime(vi.getRealSystemTime());
    });

    it('should render loading skeleton initially', async () => {
        expect(testee.find('[data-test-id="loading"]').exists()).toBe(true);
        await expect.poll(() => testee.find('[data-test-id="loading"]').exists()).toBe(false);
    });

    it('should render only future events', async () => {
        await awaitEventsLoaded();
        const eventCards = testee.findAllComponents(EventCard);
        expect(eventCards).toHaveLength(3);
        expect(eventCards[0].text()).toContain('future event A should be visible');
        expect(eventCards[1].text()).toContain('future event B should be visible');
        expect(eventCards[2].text()).toContain('event in next year should be visible');
    });

    async function awaitEventsLoaded(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="loading"]').exists()).toBe(false);
    }
});
