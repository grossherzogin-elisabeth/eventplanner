import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { PositionUseCase } from '@/application';
import { usePositionUseCase } from '@/application';
import type { Event, User } from '@/domain';
import { EventSignupType, EventType } from '@/domain';
import UserListRow from '@/ui/views/users/list/UserListRow.vue';
import {
    CAPTAIN,
    DECKHAND,
    ENGINEER,
    MATE,
    REGISTRATION_ENGINEER,
    mockEvent,
    mockPositionCaptain,
    mockPositionDeckhand,
    mockPositionEngineer,
    mockPositionMate,
    mockRegistrationEngineer,
    mockSlotEngineer,
    mockUserEngineer,
} from '~/mocks';

describe('UserListRow.vue', () => {
    let testee: VueWrapper;
    let positionUseCase: PositionUseCase;

    beforeEach(() => {
        positionUseCase = usePositionUseCase();
        vi.spyOn(positionUseCase, 'getPositions').mockResolvedValue([
            mockPositionCaptain(),
            mockPositionEngineer(),
            mockPositionMate(),
            mockPositionDeckhand(),
        ]);
    });

    afterEach(() => testee?.unmount());

    it('should render nick name and last name', async () => {
        const user = mockUserEngineer({ nickName: 'Chief' });
        testee = mountTestee({ user });

        await expect.poll(() => testee.text()).toContain('Chief Engine');
        expect(testee.text()).not.toContain('Alice Engine');
    });

    it('should render no-position hint when user has no positions', async () => {
        const user = mockUserEngineer({ positionKeys: [] });
        testee = mountTestee({ user });

        await expect.poll(() => testee.find('[data-test-id="user-no-position"]').exists()).toBe(true);
    });

    it('should render position overflow count for more than two positions', async () => {
        const user = mockUserEngineer({ positionKeys: [CAPTAIN, ENGINEER, MATE, DECKHAND] });
        testee = mountTestee({ user });

        await expect.poll(() => testee.findAll('[data-test-id="user-position"]').length).toBe(2);
        expect(testee.findAll('[data-test-id="user-position-additional"]')).toHaveLength(2);
        expect(testee.find('[data-test-id="user-position-overflow"]').text()).toContain('+ 2');
    });

    it('should render event counters for assigned and waiting-list registrations', async () => {
        const user = mockUserEngineer();
        const events: Event[] = [
            mockEvent({
                type: EventType.SingleDayEvent,
                signupType: EventSignupType.Assignment,
                registrations: [mockRegistrationEngineer()],
                slots: [mockSlotEngineer({ assignedRegistrationKey: REGISTRATION_ENGINEER })],
            }),
            mockEvent({
                type: EventType.WeekendEvent,
                signupType: EventSignupType.Assignment,
                registrations: [mockRegistrationEngineer()],
                slots: [mockSlotEngineer({ assignedRegistrationKey: undefined })],
            }),
            mockEvent({
                type: EventType.MultiDayEvent,
                signupType: EventSignupType.Open,
                registrations: [mockRegistrationEngineer()],
                slots: [],
            }),
        ];

        testee = mountTestee({ user, events });

        await expect.poll(() => counterValues()[0]).toBe('1');
        expect(counterValues()).toEqual(['1', '-', '1', '1']);
    });

    function counterValues(): string[] {
        return [
            testee.get('[data-test-id="user-single-day-events-count"]').text().trim(),
            testee.get('[data-test-id="user-weekend-events-count"]').text().trim(),
            testee.get('[data-test-id="user-multi-day-events-count"]').text().trim(),
            testee.get('[data-test-id="user-waiting-list-count"]').text().trim(),
        ];
    }

    function mountTestee(props: { user?: User; events?: Event[] }): VueWrapper {
        return mount(UserListRow, { props });
    }
});
