import type { Event } from '@/domain';
import { EventSignupType, EventState, EventType } from '@/domain';
import { mockLocations } from '~/mocks/mockLocation';
import { mockRegistrations } from '~/mocks/mockRegistration';
import { mockSlots } from '~/mocks/mockSlot';

export function mockEvent(overwrite?: Partial<Event>): Event {
    const event: Event = {
        key: 'example-event',
        type: EventType.WeekendEvent,
        name: 'Example Event',
        description: 'This is a mocked event',
        start: new Date('2024-07-10T09:00:00Z'),
        end: new Date('2024-07-12T17:00:00Z'),
        state: EventState.Planned,
        signupType: EventSignupType.Assignment,
        registrations: mockRegistrations(),
        slots: mockSlots(),
        locations: mockLocations(),
        isInPast: false,
        signedInUserRegistration: undefined,
        canSignedInUserJoin: false,
        isSignedInUserAssigned: false,
        canSignedInUserUpdateRegistration: false,
        canSignedInUserLeave: false,
        signedInUserAssignedSlot: undefined,
        days: 0,
        assignedUserCount: 0,
    };
    return overwrite ? Object.assign(event, overwrite) : event;
}
