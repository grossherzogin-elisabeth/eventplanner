import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { EventState } from '@/domain';
import EventStateBanner from '@/ui/components/events/EventStateBanner.vue';
import { usePositions } from '@/ui/composables/Positions.ts';
import { mockEvent, mockRegistrationCaptain, mockSlotCaptain } from '~/mocks';

describe('EventStateBanner.vue', () => {
    let testee: VueWrapper;
    let event = mockEvent();

    beforeEach(async () => {
        event = mockEvent();
        testee = mount(EventStateBanner, {
            props: { event },
        });
        await usePositions().loading;
    });

    afterEach(() => testee.unmount());

    it('should show draft banner as info', async () => {
        const event = mockEvent({ state: EventState.Draft });
        await testee.setProps({ event });
        expect(testee.text()).toContain(testee.vm.$t('components.event-state-banner.draft'));
    });

    it('should show planning banner as info', async () => {
        const event = mockEvent({ state: EventState.OpenForSignup });
        await testee.setProps({ event });
        expect(testee.text()).toContain(testee.vm.$t('components.event-state-banner.planning'));
    });

    it('should show canceled banner as warning', async () => {
        const event = mockEvent({ state: EventState.Canceled });
        await testee.setProps({ event });
        expect(testee.text()).toContain(testee.vm.$t('components.event-state-banner.canceled'));
    });

    it('should show waiting list banner as info', async () => {
        const event = mockEvent({ state: EventState.Planned, signedInUserRegistration: mockRegistrationCaptain() });
        await testee.setProps({ event });
        expect(testee.text()).toContain(testee.vm.$t('components.event-state-banner.waiting-list', { signedInUserPosition: 'Captain' }));
    });

    it('should show assigned banner as success', async () => {
        const event = mockEvent({
            state: EventState.Planned,
            signedInUserRegistration: mockRegistrationCaptain(),
            signedInUserAssignedSlot: mockSlotCaptain(),
            isSignedInUserAssigned: true,
        });
        await testee.setProps({ event });
        expect(testee.text()).toContain(testee.vm.$t('components.event-state-banner.assigned', { signedInUserPosition: 'Captain' }));
    });

    it('should show missing crew banner as warning', async () => {
        const event = mockEvent({ state: EventState.Planned });
        await testee.setProps({ event });
        expect(testee.text()).toContain(
            testee.vm.$t('components.event-state-banner.missing-crew', { openPositions: 'Deckhand, Mate, Engineer, Captain' })
        );
    });
});
