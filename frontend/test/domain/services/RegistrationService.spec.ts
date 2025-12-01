import { RegistrationService, RegistrationSlotState, useRegistrationService } from '@/domain';
import { beforeEach, describe, expect, it } from 'vitest';
import {
    CAPTAIN,
    QUALIFICATION_CAPTAIN,
    QUALIFICATION_EXPIRES,
    REGISTRATION_CAPTAIN,
    REGISTRATION_MATE,
    mockEvent,
    mockPositionCaptain,
    mockPositionDeckhand,
    mockPositions,
    mockRegistrationCaptain,
    mockRegistrationGuest,
    mockRegistrationMate,
    mockSlotCaptain,
    mockUserCaptain,
    mockUsers,
} from '~/mocks';

describe('RegistrationService', () => {
    let testee: RegistrationService;

    const users = mockUsers();
    const positions = mockPositions();

    beforeEach(() => {
        testee = new RegistrationService();
    });

    it('should construct instance with useRegistrationService', () => {
        const testee = useRegistrationService();
        expect(testee).toBeDefined();
    });

    describe('resolveRegistrations', () => {
        it('should resolve empty slot', () => {
            const event = mockEvent({ slots: [mockSlotCaptain()], registrations: [] });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].state).toEqual(RegistrationSlotState.OPEN);
            expect(result[0].slot).toEqual(mockSlotCaptain());
            expect(result[0].position).toEqual(mockPositionCaptain());
            expect(result[0].user).toBeUndefined();
            expect(result[0].name).toEqual('');
            expect(result[0].registration).toBeUndefined();
            expect(result[0].hasOverwrittenPosition).toBe(false);
            expect(result[0].expiredQualifications).toHaveLength(0);
        });

        it('should resolve waiting list registration', () => {
            const event = mockEvent({ slots: [], registrations: [mockRegistrationCaptain()] });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].state).toEqual(RegistrationSlotState.WAITING_LIST);
            expect(result[0].slot).toBeUndefined();
            expect(result[0].position).toEqual(mockPositionCaptain());
            expect(result[0].user).toEqual(mockUserCaptain());
            expect(result[0].name).toEqual('Charlie Captain');
            expect(result[0].registration).toEqual(mockRegistrationCaptain());
            expect(result[0].hasOverwrittenPosition).toBe(false);
        });

        it('should resolve waiting list registration and empty slot', () => {
            const event = mockEvent({ slots: [mockSlotCaptain()], registrations: [mockRegistrationCaptain()] });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(2);
        });

        it('should resolve guest registration', () => {
            const event = mockEvent({ slots: [], registrations: [mockRegistrationGuest()] });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].state).toEqual(RegistrationSlotState.WAITING_LIST);
            expect(result[0].slot).toBeUndefined();
            expect(result[0].position).toEqual(mockPositionDeckhand());
            expect(result[0].user).toBeUndefined();
            expect(result[0].name).toEqual('Gustav Guest');
            expect(result[0].registration).toEqual(mockRegistrationGuest());
            expect(result[0].hasOverwrittenPosition).toBe(false);
        });

        it('should handle missing user', () => {
            const event = mockEvent({ slots: [], registrations: [mockRegistrationCaptain()] });
            const result = testee.resolveRegistrations(event, [], positions);
            expect(result).toHaveLength(1);
            expect(result[0].state).toEqual(RegistrationSlotState.WAITING_LIST);
            expect(result[0].position).toEqual(mockPositionCaptain());
            expect(result[0].user).toBeUndefined();
            expect(result[0].name).toEqual('');
            expect(result[0].registration).toEqual(mockRegistrationCaptain());
            expect(result[0].hasOverwrittenPosition).toBe(false);
        });

        it('should resolve assigned registration', () => {
            const slot = mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN });
            const registration = mockRegistrationCaptain();
            const event = mockEvent({
                slots: [slot],
                registrations: [registration],
            });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].state).toEqual(RegistrationSlotState.ASSIGNED);
            expect(result[0].slot).toEqual(slot);
            expect(result[0].position).toEqual(mockPositionCaptain());
            expect(result[0].user).toEqual(mockUserCaptain());
            expect(result[0].name).toEqual('Charlie Captain');
            expect(result[0].registration).toEqual(registration);
            expect(result[0].hasOverwrittenPosition).toBe(false);
        });

        it('should resolve confirmed registration', () => {
            const slot = mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_CAPTAIN });
            const registration = mockRegistrationCaptain({ confirmed: true });
            const event = mockEvent({
                slots: [slot],
                registrations: [registration],
            });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].state).toEqual(RegistrationSlotState.CONFIRMED);
        });

        it('should detect overwritten position', () => {
            const slot = mockSlotCaptain({ assignedRegistrationKey: REGISTRATION_MATE });
            const registration = mockRegistrationMate({ positionKey: CAPTAIN });
            const event = mockEvent({
                slots: [slot],
                registrations: [registration],
            });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].hasOverwrittenPosition).toBe(true);
        });

        it('should detect qualification expired based on event end date', () => {
            const event = mockEvent({
                end: new Date('2024-07-11T09:00:00Z'), // captain qualification expires one day before
                slots: [],
                registrations: [mockRegistrationCaptain()],
            });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].expiredQualifications).toEqual([QUALIFICATION_CAPTAIN]);
        });

        it('should detect qualification not expired based on event end date', () => {
            const event = mockEvent({
                end: new Date('2024-07-09T09:00:00Z'), // captain qualification expires one day later
                slots: [],
                registrations: [mockRegistrationCaptain()],
            });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].expiredQualifications).toEqual([]);
        });

        it('should resolve correct expired qualification count for always expired qualification', () => {
            const event = mockEvent({
                slots: [],
                registrations: [mockRegistrationMate()],
            });
            const result = testee.resolveRegistrations(event, users, positions);
            expect(result).toHaveLength(1);
            expect(result[0].expiredQualifications).toEqual([QUALIFICATION_EXPIRES]);
        });
    });
});
