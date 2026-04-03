import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import type { EventRepresentation, RegistrationRepresentation, SlotRepresentation } from '@/adapter/rest/EventRestRepository.ts';
import { SlotCriticality } from '@/domain';
import { Routes } from '@/ui/views/Routes';
import ConfirmParticipationView from '@/ui/views/events/confirm/ConfirmParticipationView.vue';
import { DECKHAND, USER_DECKHAND, mockEventRepresentation, mockRouter, server } from '~/mocks';
import { setupSignedInUser } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

const registration: RegistrationRepresentation = {
    key: 'registration-key',
    positionKey: DECKHAND,
    userKey: USER_DECKHAND,
    note: 'note',
    confirmed: false,
    overnightStay: false,
};
const slot: SlotRepresentation = {
    key: 'slot-key',
    order: 1,
    positionKeys: [DECKHAND],
    criticality: SlotCriticality.Required,
    assignedRegistrationKey: 'registration-key',
};

describe('ConfirmParticipationView.vue', () => {
    let testee: VueWrapper;
    let event: EventRepresentation;

    beforeEach(async () => {
        server.use(http.get('/api/v1/events/example-event', () => HttpResponse.json(event)));
        const currentYear = new Date().getFullYear();
        vi.setSystemTime(new Date(currentYear, 6, 1).getTime());
        await router.push({
            name: Routes.EventConfirmParticipation,
            params: {
                eventKey: 'example-event',
                registrationKey: 'registration-key',
            },
            query: { accessKey: 'access-key' },
        });
    });

    describe('with unconfirmed registration', () => {
        beforeEach(async () => {
            server.use(http.get('/api/v1/events/example-event/registrations/registration-key/confirm', () => HttpResponse.json(event)));
            server.use(http.get('/api/v1/events/example-event/registrations/registration-key/decline', () => HttpResponse.json(event)));
            event = mockEventRepresentation({
                registrations: [{ ...registration, confirmed: false }],
                slots: [{ ...slot }],
            });
            testee = mount(ConfirmParticipationView, { global: { plugins: [router] } });
        });

        it('should render confirm and decline buttons', async () => {
            await loading();
            expect(testee.find('[data-test-id="button-confirm"]').exists()).toBe(true);
            expect(testee.find('[data-test-id="button-decline"]').exists()).toBe(true);
        });

        it('should render info after confirmation', async () => {
            await loading();
            await testee.find('[data-test-id="button-confirm"]').trigger('click');
            await expect.poll(() => testee.find('[data-test-id="registration-confirmed"]').exists()).toBe(true);
        });

        it('should render info after decline', async () => {
            await loading();
            await testee.find('[data-test-id="button-decline"]').trigger('click');
            await expect.poll(() => testee.find('[data-test-id="registration-just-canceled"]').exists()).toBe(true);
        });
    });

    describe('with confirmed registration', () => {
        beforeEach(async () => {
            event = mockEventRepresentation({
                registrations: [{ ...registration, confirmed: true }],
                slots: [{ ...slot }],
            });
            testee = mount(ConfirmParticipationView, { global: { plugins: [router] } });
        });

        it('should render confirmation info', async () => {
            await loading();
            expect(testee.find('[data-test-id="registration-confirmed"]').exists()).toBe(true);
        });
    });

    describe('with canceled registration', () => {
        beforeEach(async () => {
            setupSignedInUser({ key: USER_DECKHAND });
            event = mockEventRepresentation();
            testee = mount(ConfirmParticipationView, { global: { plugins: [router] } });
        });

        it('should render canceled info', async () => {
            await loading();
            expect(testee.find('[data-test-id="registration-canceled"]').exists()).toBe(true);
        });
    });

    describe('with unknown registration', () => {
        beforeEach(async () => {
            event = mockEventRepresentation();
            testee = mount(ConfirmParticipationView, { global: { plugins: [router] } });
        });

        it('should render not found info', async () => {
            await loading();
            expect(testee.find('[data-test-id="registration-not-found"]').exists()).toBe(true);
        });
    });

    describe('with registration belonging to different user', () => {
        beforeEach(async () => {
            setupSignedInUser({ key: 'some-other-user' });
            event = mockEventRepresentation({
                registrations: [{ ...registration, confirmed: false }],
                slots: [{ ...slot }],
            });
            testee = mount(ConfirmParticipationView, { global: { plugins: [router] } });
        });

        it('should render different user info', async () => {
            await loading();
            expect(testee.find('[data-test-id="registration-belongs-to-other-user"]').exists()).toBe(true);
        });
    });

    async function loading(): Promise<void> {
        await expect.poll(() => testee.find('[data-test-id="loading"]').exists()).toBe(false);
    }
});
