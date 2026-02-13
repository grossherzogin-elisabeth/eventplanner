import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { EventType } from '@/domain';
import EventDetailsCard from '@/ui/components/events/EventDetailsCard.vue';
import { mockEvent } from '~/mocks';

describe('EventDetailsCard.vue', () => {
    let testee: VueWrapper;
    let event = mockEvent();

    beforeEach(async () => {
        event = mockEvent({
            type: EventType.SingleDayEvent,
            start: new Date(`2024-07-10T09:00:00Z`),
            end: new Date(`2024-07-12T16:00:00Z`),
            assignedUserCount: 15,
        });
        testee = mount(EventDetailsCard, {
            props: { event },
        });
    });

    it('should render event category', async () => {
        expect(testee.text()).toContain(testee.vm.$t(`generic.event-type.${event.type}`));
    });

    it('should render crew count', async () => {
        expect(testee.text()).toContain(testee.vm.$t('components.event-details-card.assigned', { count: event.assignedUserCount }));
    });

    it('should render start date', async () => {
        expect(testee.text()).toContain('10.07.');
    });

    it('should render start time', async () => {
        expect(testee.text()).toContain('11:00'); // CET
    });

    it('should render end date', async () => {
        expect(testee.text()).toContain('12.07.');
    });

    it('should render end time', async () => {
        expect(testee.text()).toContain('18:00'); // CET
    });

    it('should render description', async () => {
        expect(testee.text()).toContain(event.description);
    });
});
