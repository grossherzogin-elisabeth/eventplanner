import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import EventRegistrationDetailsCard from '@/ui/components/events/EventRegistrationDetailsCard.vue';
import RegistrationArrivalDateCard from '@/ui/components/events/RegistrationArrivalDateCard.vue';
import RegistrationNoteCard from '@/ui/components/events/RegistrationNoteCard.vue';
import RegistrationOvernightStayCard from '@/ui/components/events/RegistrationOvernightStayCard.vue';
import RegistrationPositionCard from '@/ui/components/events/RegistrationPositionCard.vue';
import { usePositions } from '@/ui/composables/Positions.ts';
import { mockEvent, mockRegistrationCaptain } from '~/mocks';

describe('EventRegistrationDetailsCard.vue', () => {
    let testee: VueWrapper;
    let event = mockEvent();
    let registration = mockRegistrationCaptain();

    beforeEach(async () => {
        event = mockEvent();
        registration = mockRegistrationCaptain({
            arrival: event.start,
            overnightStay: true,
            note: 'note',
        });
        testee = mount(EventRegistrationDetailsCard, {
            props: { event, registration },
        });
        await usePositions().loading;
    });

    it('should render registration position', async () => {
        const position = testee.findComponent(RegistrationPositionCard);
        expect(position.text()).toContain('Captain');
    });

    it('should render overnight stay', async () => {
        const overnightStay = testee.findComponent(RegistrationOvernightStayCard);
        expect(overnightStay.text()).toContain(testee.vm.$t('generic.yes'));
    });

    it('should render arrival on day before', async () => {
        const arrival = testee.findComponent(RegistrationArrivalDateCard);
        expect(arrival.text()).toContain(testee.vm.$t('generic.yes'));
    });

    it('should render note', async () => {
        const note = testee.findComponent(RegistrationNoteCard);
        expect(note.text()).toContain(registration.note);
    });
});
