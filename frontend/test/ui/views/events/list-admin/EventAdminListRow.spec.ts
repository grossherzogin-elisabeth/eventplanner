import { afterEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { DateTimeFormat } from '@/common/date';
import { EventState } from '@/domain';
import EventAdminListRow from '@/ui/views/events/list-admin/EventAdminListRow.vue';
import { mockEvent, mockRegistrationDeckhand, mockRegistrationEngineer } from '~/mocks';

describe('EventAdminListRow.vue', () => {
    let testee: VueWrapper;

    afterEach(() => testee?.unmount());

    it('should render the event name', () => {
        const event = mockEvent({ name: 'Harbor Weekend 2027' });
        testee = mountTestee(event);

        expect(testee.text()).toContain(event.name);
    });

    it('should render date information', () => {
        const event = mockEvent({
            start: new Date('2027-07-10T09:00:00Z'),
            end: new Date('2027-07-12T17:00:00Z'),
            days: 2,
        });
        testee = mountTestee(event);

        expect(testee.text()).toContain(testee.vm.$d(event.start, DateTimeFormat.DDD_DD_MM));
        expect(testee.text()).toContain(event.days);
    });

    it('should render the event description', () => {
        const event = mockEvent({ description: 'Special crew briefing before departure' });
        testee = mountTestee(event);

        expect(testee.text()).toContain(String(event.description));
    });

    it('should render registration count without waiting list addition', () => {
        const event = mockEvent({
            state: EventState.Planned,
            registrations: [mockRegistrationDeckhand(), mockRegistrationEngineer()],
            assignedUserCount: 0, // should be ignored
            waitingListCount: 2,
        });
        testee = mountTestee(event);

        expect(testee.text()).toContain(String(event.registrations.length));
    });

    it('should render crew counter with waiting list addition', () => {
        const event = mockEvent({
            state: EventState.Planned,
            assignedUserCount: 3,
            waitingListCount: 2,
        });
        testee = mountTestee(event);

        expect(testee.text()).toContain(String(event.assignedUserCount));
        expect(testee.text()).toContain(`+${event.waitingListCount}`);
    });

    it('should render the event status', () => {
        const event = mockEvent({ state: EventState.Draft });
        testee = mountTestee(event);

        expect(testee.text()).toContain(testee.vm.$t('domain.event-state.draft'));
    });

    function mountTestee(event: ReturnType<typeof mockEvent>): VueWrapper {
        return mount(EventAdminListRow, { props: { event } });
    }
});
