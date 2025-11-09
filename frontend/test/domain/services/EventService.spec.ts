import type { Event } from '@/domain';
import { EventState, SlotCriticality } from '@/domain';
import { EventService } from '@/domain/services/EventService';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mockEvent } from '~/mocks/mockEvent';
import { mockLocation1, mockLocation2, mockLocation3 } from '~/mocks/mockLocation';
import { CAPTAIN, ENGINEER } from '~/mocks/mockPosition.ts';
import {
    REGISTRATION_CAPTAIN,
    REGISTRATION_DECKHAND,
    REGISTRATION_ENGINEER,
    REGISTRATION_GUEST,
    REGISTRATION_MATE,
    mockRegistrationCaptain,
    mockRegistrationDeckhand,
    mockRegistrationGuest,
    mockRegistrationMate,
} from '~/mocks/mockRegistration';
import { mockSignedInUser } from '~/mocks/mockSignedInUser.ts';
import {
    SLOT_CAPTAIN,
    SLOT_DECKHAND,
    SLOT_MATE,
    mockSlotCaptain,
    mockSlotDeckhand,
    mockSlotEngineer,
    mockSlotMate,
} from '~/mocks/mockSlot.ts';
import { USER_CAPTAIN, USER_MATE, mockUserCaptain, mockUserDeckhand } from '~/mocks/mockUsers';

vi.mock('uuid', () => ({
    v4: vi.fn(() => 'mock-uuid-123'),
}));

describe('EventService', () => {
    let testee: EventService;

    beforeEach(() => {
        testee = new EventService();
        // Set a fixed system time for predictable date calculations
        vi.setSystemTime(new Date('2024-07-01T12:00:00Z'));
    });

    afterEach(() => {
        // Restore real system time after each test
        vi.useRealTimers();
    });

    describe('doEventsHaveOverlappingDays', () => {
        it('should return false if any event is undefined', () => {
            expect(testee.doEventsHaveOverlappingDays(mockEvent(), undefined)).toBe(false);
            expect(testee.doEventsHaveOverlappingDays(undefined, mockEvent())).toBe(false);
            expect(testee.doEventsHaveOverlappingDays(undefined, undefined)).toBe(false);
        });

        it('should return true if events overlap', () => {
            const a: Event = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-12') });
            const b: Event = mockEvent({ start: new Date('2024-07-11'), end: new Date('2024-07-13') });
            expect(testee.doEventsHaveOverlappingDays(a, b)).toBe(true);
        });

        it('should return true if events overlap on the same day', () => {
            const a: Event = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-10') });
            const b: Event = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-10') });
            expect(testee.doEventsHaveOverlappingDays(a, b)).toBe(true);
        });

        it('should return  true if an event encloses another', () => {
            const a: Event = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-15') });
            const b: Event = mockEvent({ start: new Date('2024-07-11'), end: new Date('2024-07-12') });
            expect(testee.doEventsHaveOverlappingDays(a, b)).toBe(true);
        });

        it('should return false if events dont overlap', () => {
            const a: Event = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-11') });
            const b: Event = mockEvent({ start: new Date('2024-07-12'), end: new Date('2024-07-13') });
            expect(testee.doEventsHaveOverlappingDays(a, b)).toBe(false);
        });

        it('should return false for events that follow directly on each other, but no overlap', () => {
            const a: Event = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-10') });
            const b: Event = mockEvent({ start: new Date('2024-07-11'), end: new Date('2024-07-11') });
            expect(testee.doEventsHaveOverlappingDays(a, b)).toBe(false);
        });
    });

    describe('doesEventMatchFilter', () => {
        it('should return true if name matches (case-insensitive)', () => {
            expect(testee.doesEventMatchFilter(mockEvent(), 'Example')).toBe(true);
            expect(testee.doesEventMatchFilter(mockEvent(), 'EXAMPLE')).toBe(true);
        });

        it('should return true if description matches (case-insensitive)', () => {
            expect(testee.doesEventMatchFilter(mockEvent(), 'mocked event')).toBe(true);
            expect(testee.doesEventMatchFilter(mockEvent(), 'mocked EVENT')).toBe(true);
        });

        it('should return true if a location name matches (case-insensitive)', () => {
            expect(testee.doesEventMatchFilter(mockEvent(), 'Elsfleth')).toBe(true);
            expect(testee.doesEventMatchFilter(mockEvent(), 'ELSFLETH')).toBe(true);
        });

        it('should return false if no attibute matches', () => {
            expect(testee.doesEventMatchFilter(mockEvent(), 'nonexistent')).toBe(false);
        });

        it('should return true if filter is empty', () => {
            expect(testee.doesEventMatchFilter(mockEvent(), '')).toBe(true);
        });
    });

    describe('cancelUserRegistration', () => {
        it('should remove the registration of the user', () => {
            const event = mockEvent();
            const updatedEvent = testee.cancelUserRegistration(event, USER_CAPTAIN);
            expect(updatedEvent.registrations).not.toContain(mockRegistrationCaptain());
            expect(updatedEvent.registrations).toHaveLength(mockEvent().registrations.length - 1);
        });

        it('should do nothing if user has no registration', () => {
            const event = mockEvent();
            const updatedEvent = testee.cancelUserRegistration(event, 'some-other-user');
            expect(updatedEvent).toEqual(mockEvent());
        });

        it('should do nothing if userkey is undefined', () => {
            const event = mockEvent();
            const updatedEvent = testee.cancelUserRegistration(event, undefined);
            expect(updatedEvent).toEqual(mockEvent());
        });
    });

    describe('cancelGuestRegistration', () => {
        it('should remove a registration by name', () => {
            const event = mockEvent();
            const updatedEvent = testee.cancelGuestRegistration(event, mockRegistrationGuest().name);
            expect(updatedEvent.registrations).not.toContain(mockRegistrationGuest());
            expect(updatedEvent.registrations).toHaveLength(mockEvent().registrations.length - 1);
        });

        it('should do nothing if no registration exists with given name', () => {
            const event = mockEvent();
            const updatedEvent = testee.cancelGuestRegistration(event, 'some other name');
            expect(updatedEvent).toEqual(mockEvent());
        });

        it('should do nothing if name is undefined', () => {
            const event = mockEvent();
            const updatedEvent = testee.cancelGuestRegistration(event, undefined);
            expect(updatedEvent).toEqual(mockEvent());
        });
    });

    describe('canUserBeAssignedToSlot', () => {
        it('should return true if user has correct position', () => {
            expect(testee.canUserBeAssignedToSlot(mockEvent(), mockUserCaptain(), SLOT_CAPTAIN)).toBe(true);
        });

        it('should return false if user has no matching position', () => {
            expect(testee.canUserBeAssignedToSlot(mockEvent(), mockUserDeckhand(), SLOT_CAPTAIN)).toBe(false);
        });

        it('should return false if slot does not exist', () => {
            expect(testee.canUserBeAssignedToSlot(mockEvent(), mockUserDeckhand(), 'non-existent-slot')).toBe(false);
        });

        it('should return false if user has no registration', () => {
            expect(testee.canUserBeAssignedToSlot(mockEvent({ registrations: [] }), mockUserDeckhand(), SLOT_DECKHAND)).toBe(false);
        });

        it('should return true if user has multiple position and one matches', () => {
            expect(testee.canUserBeAssignedToSlot(mockEvent(), mockUserCaptain(), SLOT_CAPTAIN)).toBe(true);
            expect(testee.canUserBeAssignedToSlot(mockEvent(), mockUserCaptain(), SLOT_MATE)).toBe(true);
        });
    });

    describe('getOpenSlots', () => {
        it('should return only unassigned slots', () => {
            const event = mockEvent({
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN }),
                    mockSlotMate({ assignedRegistrationKey: REGISTRATION_MATE }),
                    mockSlotDeckhand(),
                    mockSlotEngineer(),
                ],
            });
            const openSlots = testee.getOpenSlots(event);
            expect(openSlots).toEqual([mockSlotDeckhand(), mockSlotEngineer()]);
        });

        it('should return empty array if all slots are assigned', () => {
            const event = mockEvent({
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN }),
                    mockSlotMate({ assignedRegistrationKey: REGISTRATION_MATE }),
                    mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_DECKHAND }),
                    mockSlotEngineer({ assignedRegistrationKey: REGISTRATION_ENGINEER }),
                ],
            });
            expect(testee.getOpenSlots(event)).toEqual([]);
        });

        it('should return all slots when no slot is assigned', () => {
            const event = mockEvent();
            expect(testee.getOpenSlots(event)).toEqual(event.slots);
        });
    });

    describe('updateSlot', () => {
        it('should update a slot', () => {
            const event = mockEvent();
            const updatedSlot = mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN });
            const updatedEvent = testee.updateSlot(event, updatedSlot);
            expect(updatedEvent.slots).toContainEqual(updatedSlot);
        });

        it('should do nothing if slot cannot be found on event', () => {
            const event = mockEvent();
            const nonExistentSlot = mockSlotDeckhand({ key: 'non-existent-key' });
            const updatedEvent = testee.updateSlot(event, nonExistentSlot);
            expect(updatedEvent).toEqual(mockEvent());
        });
    });

    describe('removeSlot', () => {
        it('should remove a slot and update the order of the remaining ones', () => {
            const event = mockEvent();
            const slotToRemove = mockSlotMate();
            const updatedEvent = testee.removeSlot(event, slotToRemove);
            expect(updatedEvent.slots).not.toContain(slotToRemove);
        });

        it('should do nothing if slot does not exist', () => {
            const event = mockEvent();
            const nonExistentSlot = mockSlotDeckhand({ key: 'non-existent-key' });
            const updatedEvent = testee.removeSlot(event, nonExistentSlot);
            expect(updatedEvent).toEqual(mockEvent());
        });

        it('should update order of remaining slots on remove', () => {
            const event = mockEvent();
            const updatedEvent = testee.removeSlot(event, event.slots[0]);
            for (let i = 0; i < updatedEvent.slots.length; i++) {
                expect(updatedEvent.slots[i].order).toBe(i + 1);
            }
        });
    });

    describe('duplicateSlot', () => {
        it('should create slot with new key', () => {
            const event = mockEvent();
            const slotToDuplicate = mockSlotDeckhand();
            const updatedEvent = testee.duplicateSlot(event, slotToDuplicate);
            expect(updatedEvent.slots).toHaveLength(mockEvent().slots.length + 1);
            const duplicatedSlot = updatedEvent.slots.find((s) => s.key === 'mock-uuid-123');
            expect(duplicatedSlot).toBeDefined();
            expect(duplicatedSlot?.positionKeys).toBe(slotToDuplicate.positionKeys);
            expect(duplicatedSlot?.criticality).toBe(slotToDuplicate.criticality);
        });

        it('should create slot without assigned registration', () => {
            const slotToDuplicate = mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_DECKHAND });
            const event = mockEvent({ slots: [slotToDuplicate] });
            const updatedEvent = testee.duplicateSlot(event, slotToDuplicate);

            const duplicatedSlot = updatedEvent.slots.find((s) => s.key === 'mock-uuid-123');
            expect(duplicatedSlot).toBeDefined();
            expect(duplicatedSlot?.assignedRegistrationKey).toBeUndefined();
        });

        it('should update order of all slots on duplicate', () => {
            const event = mockEvent();
            const updatedEvent = testee.duplicateSlot(event, event.slots[0]);
            for (let i = 0; i < updatedEvent.slots.length; i++) {
                expect(updatedEvent.slots[i].order).toBe(i + 1);
            }
        });
    });

    describe('moveSlot', () => {
        it('should move slot up by one', () => {
            const event = mockEvent();
            const slotToMove = event.slots[2];
            const updatedEvent = testee.moveSlot(event, slotToMove, -1);
            expect(updatedEvent.slots[1]).toBe(slotToMove);
        });

        it('should move slot down by one', () => {
            const event = mockEvent();
            const slotToMove = event.slots[2];
            const updatedEvent = testee.moveSlot(event, slotToMove, 1);
            expect(updatedEvent.slots[3]).toBe(slotToMove);
        });

        it('should do nothing if offset is 0', () => {
            const event = mockEvent();
            const slotToMove = event.slots[2];
            const updatedEvent = testee.moveSlot(event, slotToMove, 0);
            expect(updatedEvent).toEqual(mockEvent());
        });

        it('should not move slot below index 0', () => {
            const event = mockEvent();
            const slotToMove = event.slots[2];
            const updatedEvent = testee.moveSlot(event, slotToMove, -10);
            expect(updatedEvent.slots[0]).toBe(slotToMove);
        });

        it('should not move slot above last index', () => {
            const event = mockEvent();
            const slotToMove = event.slots[2];
            const updatedEvent = testee.moveSlot(event, slotToMove, 10);
            expect(updatedEvent.slots[updatedEvent.slots.length - 1]).toBe(slotToMove);
        });

        it('should update order of all slots on move', () => {
            const event = mockEvent();
            const updatedEvent = testee.moveSlot(event, event.slots[2], -2);
            for (let i = 0; i < updatedEvent.slots.length; i++) {
                expect(updatedEvent.slots[i].order).toBe(i + 1);
            }
        });
    });

    describe('updateLocation', () => {
        it('should update an existing location', () => {
            const event = mockEvent();
            const updatedLocation = mockLocation2({ name: 'updated location' });
            const updatedEvent = testee.updateLocation(event, updatedLocation);
            expect(updatedEvent.locations).toContainEqual(updatedLocation);
            expect(updatedEvent.locations[1].name).toBe('updated location');
        });

        it('should do nothing if location does not exist', () => {
            const event = mockEvent();
            const nonExistentLocation = mockLocation1({ order: 99 });
            const updatedEvent = testee.updateLocation(event, nonExistentLocation);
            expect(updatedEvent).toEqual(mockEvent());
        });
    });

    describe('removeLocation', () => {
        it('should remove a location', () => {
            const event = mockEvent();
            const updatedEvent = testee.removeLocation(event, event.locations[2]);
            expect(updatedEvent.locations).not.toContain(mockLocation3());
            expect(updatedEvent.locations).toHaveLength(2);
        });

        it('should do nothing if location does not exist', () => {
            const event = mockEvent();
            const nonExistentLocation = mockLocation1({ order: 99 });
            const updatedEvent = testee.removeLocation(event, nonExistentLocation);
            expect(updatedEvent).toEqual(mockEvent());
        });

        it('should update order of all locations on remove', () => {
            const event = mockEvent();
            const updatedEvent = testee.removeLocation(event, event.locations[0]);
            for (let i = 0; i < updatedEvent.locations.length; i++) {
                expect(updatedEvent.locations[i].order).toBe(i + 1);
            }
        });
    });

    describe('moveLocation', () => {
        it('should move location up by one', () => {
            const event = mockEvent();
            const locationToMove = event.locations[2];
            const updatedEvent = testee.moveLocation(event, locationToMove, -1);
            expect(updatedEvent.locations[1]).toBe(locationToMove);
        });

        it('should move location down by one', () => {
            const event = mockEvent();
            const locationToMove = event.locations[0];
            const updatedEvent = testee.moveLocation(event, locationToMove, 1);
            expect(updatedEvent.locations[1]).toBe(locationToMove);
        });

        it('should do nothing if offset is 0', () => {
            const event = mockEvent();
            const locationToMove = event.locations[2];
            const updatedEvent = testee.moveLocation(event, locationToMove, 0);
            expect(updatedEvent).toEqual(mockEvent());
        });

        it('should not move location below index 0', () => {
            const event = mockEvent();
            const locationToMove = event.locations[2];
            const updatedEvent = testee.moveLocation(event, locationToMove, -10);
            expect(updatedEvent.locations[0]).toBe(locationToMove);
        });

        it('should not move location above last index', () => {
            const event = mockEvent();
            const locationToMove = event.locations[2];
            const updatedEvent = testee.moveLocation(event, locationToMove, 10);
            expect(updatedEvent.locations[updatedEvent.locations.length - 1]).toBe(locationToMove);
        });

        it('should update order of all locations on move', () => {
            const event = mockEvent();
            const updatedEvent = testee.moveLocation(event, event.locations[2], -2);
            for (let i = 0; i < updatedEvent.locations.length; i++) {
                expect(updatedEvent.locations[i].order).toBe(i + 1);
            }
        });
    });

    describe('hasOpenSlots', () => {
        it('should return true for any slot criticality', () => {
            const event = mockEvent();
            event.slots
                .filter((it) => it.criticality !== SlotCriticality.Required)
                .forEach((it) => (it.assignedRegistrationKey = 'some-key'));
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Required)).toBe(true);
            expect(testee.hasOpenRequiredSlots(event)).toBe(true);
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Important)).toBe(true);
            expect(testee.hasOpenImportantSlots(event)).toBe(true);
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Optional)).toBe(true);
        });

        it('should return true for important', () => {
            const event = mockEvent();
            event.slots
                .filter((it) => it.criticality === SlotCriticality.Required)
                .forEach((it) => (it.assignedRegistrationKey = 'some-key'));
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Required)).toBe(false);
            expect(testee.hasOpenRequiredSlots(event)).toBe(false);
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Important)).toBe(true);
            expect(testee.hasOpenImportantSlots(event)).toBe(true);
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Optional)).toBe(true);
        });

        it('should return true for optional', () => {
            const event = mockEvent();
            event.slots
                .filter((it) => it.criticality !== SlotCriticality.Optional)
                .forEach((it) => (it.assignedRegistrationKey = 'some-key'));
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Required)).toBe(false);
            expect(testee.hasOpenRequiredSlots(event)).toBe(false);
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Important)).toBe(false);
            expect(testee.hasOpenImportantSlots(event)).toBe(false);
            expect(testee.hasOpenSlots(event, undefined, SlotCriticality.Optional)).toBe(true);
        });

        it('should return false when all slots are assigned', () => {
            const event = mockEvent();
            event.slots.forEach((it) => (it.assignedRegistrationKey = 'some-key'));
            expect(testee.hasOpenRequiredSlots(event)).toBe(false);
            expect(testee.hasOpenImportantSlots(event)).toBe(false);
            expect(testee.hasOpenSlots(event)).toBe(false);
        });

        it('should return true for filtered positions', () => {
            const event = mockEvent({
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN }),
                    mockSlotEngineer(),
                    mockSlotMate(),
                    mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_DECKHAND }),
                ],
            });
            expect(testee.hasOpenSlots(event, [ENGINEER])).toBe(true);
            expect(testee.hasOpenSlots(event, [CAPTAIN])).toBe(false);
            expect(testee.hasOpenSlots(event, [ENGINEER], SlotCriticality.Required)).toBe(true);
            expect(testee.hasOpenSlots(event, [CAPTAIN], SlotCriticality.Required)).toBe(false);
        });
    });

    describe('findRegistration', () => {
        it('should find registration by user key', () => {
            expect(testee.findRegistration(mockEvent(), USER_MATE, undefined)).toEqual(mockRegistrationMate());
        });

        it('should find registration by name', () => {
            expect(testee.findRegistration(mockEvent(), undefined, mockRegistrationGuest().name)).toEqual(mockRegistrationGuest());
        });

        it('should return undefined if no registration found', () => {
            expect(testee.findRegistration(mockEvent(), 'non-existent-user', undefined)).toBeUndefined();
            expect(testee.findRegistration(mockEvent(), undefined, 'Non Existent Guest')).toBeUndefined();
        });
    });

    describe('getAssignedRegistrations', () => {
        it('should return all assigned registrations', () => {
            const event = mockEvent({
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN }),
                    mockSlotEngineer(),
                    mockSlotMate(),
                    mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_DECKHAND }),
                ],
            });
            const assigned = testee.getAssignedRegistrations(event);
            expect(assigned).toContainEqual(mockRegistrationCaptain());
            expect(assigned).toContainEqual(mockRegistrationDeckhand());
            expect(assigned).toHaveLength(2);
        });

        it('should return empty array when no registrations assigned', () => {
            expect(testee.getAssignedRegistrations(mockEvent())).toEqual([]);
        });

        it('should return registrations only once when user is incorrectly assigned twice', () => {
            const event = mockEvent({
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN }),
                    mockSlotEngineer(),
                    mockSlotMate({ assignedRegistrationKey: REGISTRATION_CAPTAIN }),
                    mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_DECKHAND }),
                ],
            });
            const assigned = testee.getAssignedRegistrations(event);
            expect(assigned).toContainEqual(mockRegistrationCaptain());
            expect(assigned).toContainEqual(mockRegistrationDeckhand());
            expect(assigned).toHaveLength(2);
        });
    });

    describe('getRegistrationsOnWaitinglist', () => {
        it('should return all registrations if non are assigned', () => {
            const event = mockEvent();
            const waitinglist = testee.getRegistrationsOnWaitinglist(event);
            expect(waitinglist).toHaveLength(event.registrations.length);
        });

        it('should return unassigned registrations', () => {
            const event = mockEvent({
                slots: [
                    mockSlotCaptain(),
                    mockSlotEngineer({ assignedRegistrationKey: REGISTRATION_ENGINEER }),
                    mockSlotMate({ assignedRegistrationKey: REGISTRATION_MATE }),
                    mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_DECKHAND }),
                ],
            });
            const unassigned = testee.getRegistrationsOnWaitinglist(event);
            expect(unassigned).toContainEqual(mockRegistrationCaptain());
            expect(unassigned).toContainEqual(mockRegistrationGuest());
            expect(unassigned).toHaveLength(2);
        });

        it('should return empty array if all registrations are assigned', () => {
            const event = mockEvent({
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN }),
                    mockSlotEngineer({ assignedRegistrationKey: REGISTRATION_ENGINEER }),
                    mockSlotMate({ assignedRegistrationKey: REGISTRATION_MATE }),
                    mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_DECKHAND }),
                    mockSlotDeckhand({ assignedRegistrationKey: REGISTRATION_GUEST }),
                ],
            });
            expect(testee.getRegistrationsOnWaitinglist(event)).toEqual([]);
        });
    });

    describe('validate', () => {
        it('should return empty object for valid event', () => {
            expect(testee.validate(mockEvent())).toEqual({});
        });

        it('should return error for missing required attributes', () => {
            expect(testee.validate(mockEvent({ name: '' }))).toEqual({ name: ['generic.validation.required'] });
            expect(testee.validate(mockEvent({ start: undefined }))).toEqual({ start: ['generic.validation.required'] });
            expect(testee.validate(mockEvent({ end: undefined }))).toEqual({ end: ['generic.validation.required'] });
        });

        it('should return error for name with more than 35 characters', () => {
            expect(testee.validate(mockEvent({ name: 'a'.repeat(36) }))).toEqual({ name: ['generic.validation.max-length:35'] });
        });

        it('should return error for end date before start date', () => {
            const invalidEvent = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-09') });
            expect(testee.validate(invalidEvent)).toEqual({ end: ['Das Enddatum muss nach dem Startdatum liegen'] });
        });

        it('should return multiple errors', () => {
            const invalidEvent = mockEvent({
                name: '',
                start: undefined,
                end: undefined,
            });
            expect(testee.validate(invalidEvent)).toEqual({
                name: ['generic.validation.required'],
                start: ['generic.validation.required'],
                end: ['generic.validation.required'],
            });
        });
    });

    describe('validatePartial', () => {
        it('should return empty object for valid event', () => {
            expect(testee.validatePartial(mockEvent())).toEqual({});
        });

        it('should return error for name with more than 35 characters', () => {
            expect(testee.validatePartial(mockEvent({ name: 'a'.repeat(36) }))).toEqual({ name: ['generic.validation.max-length:35'] });
        });

        it('should return error for end date before start date', () => {
            const invalidEvent = mockEvent({ start: new Date('2024-07-10'), end: new Date('2024-07-09') });
            expect(testee.validatePartial(invalidEvent)).toEqual({ end: ['Das Enddatum muss nach dem Startdatum liegen'] });
        });

        it('should return errors for missing fields', () => {
            expect(testee.validatePartial({})).toEqual({});
        });
    });

    describe('updateComputedValues', () => {
        const futureEvent = mockEvent({ start: new Date('2024-07-10T09:00:00Z'), end: new Date('2024-07-12T17:00:00Z') });
        const pastEvent = mockEvent({ start: new Date('2024-06-01T09:00:00Z'), end: new Date('2024-06-03T17:00:00Z') });
        const soonEvent = mockEvent({ start: new Date('2024-07-05T09:00:00Z'), end: new Date('2024-07-06T17:00:00Z') });
        const userWithAssignment = mockSignedInUser({ key: USER_CAPTAIN });
        const userWithRegistration = mockSignedInUser({ key: USER_MATE });
        const userWithoutRegistration = mockSignedInUser();

        pastEvent.slots[0].assignedRegistrationKey = REGISTRATION_CAPTAIN;
        soonEvent.slots[0].assignedRegistrationKey = REGISTRATION_CAPTAIN;
        futureEvent.slots[0].assignedRegistrationKey = REGISTRATION_CAPTAIN;

        vi.setSystemTime(new Date('2024-07-01T12:00:00Z'));

        it('should set isInPast correctly', () => {
            let updatedEvent = testee.updateComputedValues({ ...futureEvent });
            expect(updatedEvent.isInPast).toBe(false);

            updatedEvent = testee.updateComputedValues({ ...pastEvent });
            expect(updatedEvent.isInPast).toBe(true);
        });

        it('should set signed in user registration correctly', () => {
            let updatedEvent = testee.updateComputedValues({ ...pastEvent }, userWithAssignment);
            expect(updatedEvent.signedInUserRegistration).toEqual(mockRegistrationCaptain());
            expect(updatedEvent.isSignedInUserAssigned).toBe(true);

            updatedEvent = testee.updateComputedValues({ ...pastEvent }, userWithRegistration);
            expect(updatedEvent.signedInUserRegistration).toEqual(mockRegistrationMate());
            expect(updatedEvent.isSignedInUserAssigned).toBe(false);

            updatedEvent = testee.updateComputedValues({ ...pastEvent }, userWithoutRegistration);
            expect(updatedEvent.signedInUserRegistration).toBeUndefined();
            expect(updatedEvent.isSignedInUserAssigned).toBe(false);
        });

        it('should set signed in user assigned slot correctly', () => {
            let updatedEvent = testee.updateComputedValues({ ...pastEvent }, userWithAssignment);
            expect(updatedEvent.signedInUserAssignedSlot).toEqual(pastEvent.slots[0]);

            updatedEvent = testee.updateComputedValues({ ...pastEvent }, userWithRegistration);
            expect(updatedEvent.signedInUserAssignedSlot).toBeUndefined();
        });

        it('should update flags of past event for signed in user with registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...pastEvent }, userWithRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(false);
            expect(updatedEvent.canSignedInUserLeave).toBe(false);
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(false);
        });

        it('should update flags of past event for signed in user without registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...pastEvent }, userWithoutRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(false);
            expect(updatedEvent.canSignedInUserLeave).toBe(false);
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(false);
        });

        it('should update flags of soon event for signed in user with assigned registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...soonEvent }, userWithRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(false);
            expect(updatedEvent.canSignedInUserLeave).toBe(true); // FIXME should be false?
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(true);
        });

        it('should update flags of soon event for signed in user with registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...soonEvent }, userWithRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(false);
            expect(updatedEvent.canSignedInUserLeave).toBe(true);
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(true);
        });

        it('should update flags of soon event for signed in user without registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...soonEvent }, userWithoutRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(true);
            expect(updatedEvent.canSignedInUserLeave).toBe(false);
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(false);
        });

        it('should update flags of future event for signed in user with assigned registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...futureEvent }, userWithAssignment);
            expect(updatedEvent.canSignedInUserJoin).toBe(false);
            expect(updatedEvent.canSignedInUserLeave).toBe(true);
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(true);
        });

        it('should update flags of future event for signed in user with registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...futureEvent }, userWithRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(false);
            expect(updatedEvent.canSignedInUserLeave).toBe(true);
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(true);
        });

        it('should update flags of future event for signed in user without registration correctly', () => {
            const updatedEvent = testee.updateComputedValues({ ...futureEvent }, userWithoutRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(true);
            expect(updatedEvent.canSignedInUserLeave).toBe(false);
            expect(updatedEvent.canSignedInUserUpdateRegistration).toBe(false);
        });

        it('should set flags for canceled events correctly', () => {
            const canceledEvent = mockEvent({ ...futureEvent, state: EventState.Canceled });
            const updatedEvent = testee.updateComputedValues(canceledEvent, userWithoutRegistration);
            expect(updatedEvent.canSignedInUserJoin).toBe(false);
        });

        it('should calculate days corerctly', () => {
            const oneDayEvent = mockEvent({ start: new Date('2024-07-10T09:00:00Z'), end: new Date('2024-07-10T17:00:00Z') });
            let updatedEvent = testee.updateComputedValues(oneDayEvent);
            expect(updatedEvent.days).toBe(1);

            const multiDayEvent = mockEvent({ start: new Date('2024-07-10T09:00:00Z'), end: new Date('2024-07-17T17:00:00Z') });
            updatedEvent = testee.updateComputedValues(multiDayEvent);
            expect(updatedEvent.days).toBe(8);
        });
    });
});
