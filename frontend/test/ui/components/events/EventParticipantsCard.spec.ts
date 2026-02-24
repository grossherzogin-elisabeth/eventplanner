import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { usePositionCachingService, useUserCachingService } from '@/application';
import { EventSignupType, EventState } from '@/domain';
import EventParticipantsCard from '@/ui/components/events/EventParticipantsCard.vue';
import { mockEvent } from '~/mocks';

describe('EventParticipantsCard.vue', () => {
    let testee: VueWrapper;
    let event = mockEvent();

    beforeAll(async () => {
        await useUserCachingService().getUsers();
        await usePositionCachingService().getPositions();
    });

    beforeEach(async () => {
        event = mockEvent();
    });

    it('should render placeholder', async () => {
        testee = mount(EventParticipantsCard, { props: { event } });
        expect(placeholder().exists()).toBe(true);
        expect(registrationList().exists()).toBe(false);
        expect(crewList().exists()).toBe(false);
        expect(waitingList().exists()).toBe(false);
    });

    it('should render crew and waiting list', async () => {
        event.assignedUserCount = event.registrations.length;
        testee = mount(EventParticipantsCard, { props: { event } });
        expect(placeholder().exists()).toBe(false);
        expect(registrationList().exists()).toBe(false);
        expect(crewList().exists()).toBe(true);
        expect(waitingList().exists()).toBe(true);
    });

    it('should render registrations for open signup', async () => {
        event.assignedUserCount = event.registrations.length;
        event.signupType = EventSignupType.Open;
        testee = mount(EventParticipantsCard, { props: { event } });
        expect(placeholder().exists()).toBe(false);
        expect(registrationList().exists()).toBe(true);
        expect(crewList().exists()).toBe(false);
        expect(waitingList().exists()).toBe(false);
    });

    it('should render registrations events in signup state', async () => {
        event.assignedUserCount = event.registrations.length;
        event.state = EventState.OpenForSignup;
        testee = mount(EventParticipantsCard, { props: { event } });
        expect(placeholder().exists()).toBe(false);
        expect(registrationList().exists()).toBe(true);
        expect(crewList().exists()).toBe(false);
        expect(waitingList().exists()).toBe(false);
    });

    function crewList(): DOMWrapper<HTMLUListElement> {
        return testee.find('[data-test-id="crew-list"]');
    }

    function waitingList(): DOMWrapper<HTMLUListElement> {
        return testee.find('[data-test-id="waiting-list"]');
    }

    function registrationList(): DOMWrapper<HTMLUListElement> {
        return testee.find('[data-test-id="registration-list"]');
    }

    function placeholder(): DOMWrapper<HTMLUListElement> {
        return testee.find('[data-test-id="registrations-placeholder"]');
    }
});
