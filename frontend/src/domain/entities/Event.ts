import type { EventState, EventType, Location, PositionKey, Registration, Slot } from '@/domain';

export type EventKey = string;

export interface Event {
    key: EventKey;
    type: EventType;
    state: EventState;
    name: string;
    description: string;
    start: Date;
    end: Date;
    locations: Location[];
    slots: Slot[];
    registrations: Registration[];
    assignedUserCount: number;
    signedInUserAssignedPosition?: PositionKey;
    signedInUserWaitingListPosition?: PositionKey;
}
