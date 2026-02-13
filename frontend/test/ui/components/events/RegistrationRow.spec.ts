import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useAuthService } from '@/application';
import type { Event, Position, ResolvedRegistrationSlot } from '@/domain';
import { Permission, RegistrationSlotState } from '@/domain';
import RegistrationRow from '@/ui/components/events/RegistrationRow.vue';
import { usePositions } from '@/ui/composables/Positions.ts';
import { mockEvent, mockPositionCaptain, mockRegistrationCaptain, mockSignedInUser, mockSlotCaptain, mockUserCaptain } from '~/mocks';

describe('RegistrationRow.vue', () => {
    const authService = useAuthService();
    let signedInUser = mockSignedInUser();
    let event: Event;
    let registration: ResolvedRegistrationSlot;
    let position: Position;
    let testee: VueWrapper;

    beforeEach(async () => {
        authService.setSignedInUser(signedInUser);
        event = mockEvent();
        position = mockPositionCaptain();
        registration = {
            state: RegistrationSlotState.ASSIGNED,
            registration: mockRegistrationCaptain(),
            slot: mockSlotCaptain(),
            user: mockUserCaptain(),
            position: position,
            name: 'Name',
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        testee = mount(RegistrationRow, {
            props: { event, registration },
            global: { stubs: { teleport: true } },
        });
        await usePositions().loading;
    });

    it('should render user name and position', async () => {
        expect(testee.text()).toContain(registration.name);
        expect(testee.text()).toContain(position.name);
    });

    it('should render placeholder', async () => {
        registration = {
            state: RegistrationSlotState.OPEN,
            registration: undefined,
            slot: mockSlotCaptain(),
            position: position,
            user: undefined,
            name: '',
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        await testee.setProps({ event, registration });
        expect(testee.text()).toContain(testee.vm.$t('components.event-participants-card.empty'));
        expect(testee.text()).toContain(position.name);
    });

    describe('Users without permission users:read-details', () => {
        beforeAll(() => (signedInUser = mockSignedInUser({ permissions: [] })));

        it('should not have link to user details', async () => {
            const link = testee.find('a');
            expect(link.exists()).toBe(false);
        });
    });

    describe('Users with permission users:read-details', () => {
        beforeAll(() => (signedInUser = mockSignedInUser({ permissions: [Permission.READ_USER_DETAILS] })));

        it('should have link to user details', async () => {
            const link = testee.find('a');
            expect(link.exists()).toBe(true);
            expect(link.text()).toContain(registration.name);
        });
    });
});
