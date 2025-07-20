import type { EventAccessType, EventState, EventType, Location, Registration, Slot } from '@/domain';

export type EventKey = string;

export interface Event {
    key: EventKey;
    type: EventType;
    accessType: EventAccessType;
    state: EventState;
    name: string;
    description: string;
    start: Date;
    end: Date;
    days: number;
    locations: Location[];
    slots: Slot[];
    registrations: Registration[];

    isInPast?: boolean;
    assignedUserCount: number;

    canSignedInUserJoin: boolean;
    canSignedInUserLeave: boolean;
    canSignedInUserUpdateRegistration: boolean;
    isSignedInUserAssigned?: boolean;
    signedInUserRegistration?: Registration;
    signedInUserAssignedSlot?: Slot;
}
