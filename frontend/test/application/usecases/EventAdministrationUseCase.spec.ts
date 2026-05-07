import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { EventRegistrationsRepository, EventRepository } from '@/application/ports';
import type { AuthService, ErrorHandlingService, EventCachingService, NotificationService } from '@/application/services';
import { EventAdministrationUseCase } from '@/application/usecases/EventAdministrationUseCase';
import type { Event, EventService, ResolvedRegistrationSlot, User } from '@/domain';
import { EventState } from '@/domain';
import {
    mockEvent,
    mockPositionCaptain,
    mockPositionDeckhand,
    mockRegistrationCaptain,
    mockRegistrationDeckhand,
    mockRegistrationEngineer,
    mockRegistrationGuest,
    mockSlotCaptain,
    mockSlotDeckhand,
    mockUserDeckhand,
} from '~/mocks';

describe('EventAdministrationUseCase', () => {
    let testee: EventAdministrationUseCase;
    let eventCachingService: EventCachingService;
    let eventService: EventService;
    let authService: AuthService;
    let eventRepository: EventRepository;
    let eventRegistrationsRepository: EventRegistrationsRepository;
    let notificationService: NotificationService;
    let errorHandlingService: ErrorHandlingService;

    beforeEach(() => {
        eventCachingService = {
            removeFromCache: vi.fn(async () => undefined),
            updateCache: vi.fn(async (event: Event) => event),
            getEventByKey: vi.fn(async () => undefined),
        } as unknown as EventCachingService;
        eventService = {
            updateComputedValues: vi.fn((event: Event) => event),
        } as unknown as EventService;
        authService = {
            getSignedInUser: vi.fn(() => undefined),
        } as unknown as AuthService;
        eventRepository = {
            deleteEvent: vi.fn(async () => undefined),
            updateEvent: vi.fn(async (_key: string, patch: Partial<Event>) => mockEvent({ ...patch })),
            findByKey: vi.fn(async () => mockEvent()),
            createEvent: vi.fn(async (event: Event) => event),
            optimizeSlots: vi.fn(async (event: Event) => event.slots),
        } as unknown as EventRepository;
        eventRegistrationsRepository = {
            createRegistration: vi.fn(async () => mockEvent()),
        } as unknown as EventRegistrationsRepository;
        notificationService = {
            success: vi.fn(),
            warning: vi.fn(),
        } as unknown as NotificationService;
        errorHandlingService = {
            handleRawError: vi.fn(),
            handleError: vi.fn(),
        } as unknown as ErrorHandlingService;

        testee = new EventAdministrationUseCase({
            eventCachingService,
            eventService,
            authService,
            eventRepository,
            eventRegistrationsRepository,
            notificationService,
            errorHandlingService,
        });
    });

    it('should delete event and remove it from cache', async () => {
        const event = mockEvent({ key: 'event-1' });

        await testee.deleteEvent(event);

        expect(eventRepository.deleteEvent).toHaveBeenCalledWith('event-1');
        expect(eventCachingService.removeFromCache).toHaveBeenCalledWith('event-1');
        expect(notificationService.success).toHaveBeenCalled();
    });

    it('should handle delete event errors and rethrow', async () => {
        const error = new Error('delete failed');
        eventRepository.deleteEvent = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.deleteEvent(mockEvent());
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBe(error);
            expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
        }
    });

    it('should cancel event and persist canceled state with message', async () => {
        const event = mockEvent({ key: 'event-1', state: EventState.OpenForSignup });
        const updated = mockEvent({ key: 'event-1', state: EventState.Canceled, description: 'crew canceled' });
        const cached = mockEvent({ key: 'event-1', state: EventState.Canceled, description: 'crew canceled' });
        eventRepository.updateEvent = vi.fn(async () => updated);
        eventCachingService.updateCache = vi.fn(async () => cached);

        const result = await testee.cancelEvent(event, 'crew canceled');

        expect(eventRepository.updateEvent).toHaveBeenCalledWith('event-1', {
            state: EventState.Canceled,
            description: 'crew canceled',
        });
        expect(eventService.updateComputedValues).toHaveBeenCalledWith(updated, undefined);
        expect(eventCachingService.updateCache).toHaveBeenCalledWith(updated);
        expect(notificationService.success).toHaveBeenCalled();
        expect(result).toEqual(cached);
    });

    it('should calculate registration deltas when updating an event', async () => {
        const regA = mockRegistrationCaptain({ key: 'a', note: 'old note' });
        const regB = mockRegistrationEngineer({ key: 'b' });
        const regAChanged = mockRegistrationCaptain({ key: 'a', note: 'new note' });
        const regC = mockRegistrationDeckhand({ key: 'c' });
        const original = mockEvent({ key: 'event-1', registrations: [regA, regB] });
        const patch: Partial<Event> = { registrations: [regAChanged, regC] };

        eventCachingService.getEventByKey = vi.fn(async () => original);

        await testee.updateEvent('event-1', patch);

        expect(eventRepository.updateEvent).toHaveBeenCalledWith('event-1', patch, [regB], [regC], [regAChanged]);
        expect(notificationService.success).toHaveBeenCalled();
    });

    it('should skip batch update when patch has no batch-updatable values', async () => {
        await testee.updateEvents(['a', 'b'], { key: 'ignore-me' } as Partial<Event>);

        expect(eventRepository.updateEvent).not.toHaveBeenCalled();
        expect(notificationService.success).not.toHaveBeenCalled();
    });

    it('should handle update event errors with retry metadata and rethrow', async () => {
        const original = mockEvent({ key: 'event-1' });
        const patch: Partial<Event> = { description: 'new text' };
        const error = new Error('update failed');

        eventCachingService.getEventByKey = vi.fn(async () => original);
        eventRepository.updateEvent = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.updateEvent('event-1', patch);
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBe(error);
            expect(errorHandlingService.handleError).toHaveBeenCalledWith(
                expect.objectContaining({
                    error,
                    retry: expect.any(Function),
                })
            );
        }
    });

    it('should warn when addRegistrations does not add any registration', async () => {
        const registration = mockRegistrationDeckhand({ userKey: 'user-1' });
        const event = mockEvent({ registrations: [mockRegistrationDeckhand({ userKey: 'user-1' })] });

        await testee.addRegistrations([event], registration);

        expect(eventRegistrationsRepository.createRegistration).not.toHaveBeenCalled();
        expect(notificationService.warning).toHaveBeenCalled();
    });

    it('should add registrations for all matching events', async () => {
        const registration = mockRegistrationDeckhand({ userKey: 'new-user' });
        const eventA = mockEvent({ key: 'a', registrations: [] });
        const eventB = mockEvent({ key: 'b', registrations: [] });
        eventRegistrationsRepository.createRegistration = vi.fn(async (eventKey: string) => mockEvent({ key: eventKey }));

        await testee.addRegistrations([eventA, eventB], registration);

        expect(eventRegistrationsRepository.createRegistration).toHaveBeenCalledWith('a', registration);
        expect(eventRegistrationsRepository.createRegistration).toHaveBeenCalledWith('b', registration);
        expect(eventCachingService.updateCache).toHaveBeenCalledTimes(2);
        expect(notificationService.success).toHaveBeenCalled();
    });

    it('should add one registration and update cache', async () => {
        const saved = mockEvent({ key: 'event-1' });
        const registration = mockRegistrationGuest({ key: 'guest-1' });
        eventRegistrationsRepository.createRegistration = vi.fn(async () => saved);

        await testee.addRegistration('event-1', registration);

        expect(eventRegistrationsRepository.createRegistration).toHaveBeenCalledWith('event-1', registration);
        expect(eventCachingService.updateCache).toHaveBeenCalledWith(saved);
        expect(notificationService.success).toHaveBeenCalled();
    });

    it('should assign user to slot and optimize slots', async () => {
        const registration = mockRegistrationDeckhand({ key: 'reg-1', userKey: 'user-1' });
        const slot = mockSlotDeckhand({ key: 'slot-1', assignedRegistrationKey: undefined });
        const event = mockEvent({ key: 'event-1', registrations: [registration], slots: [slot], assignedUserCount: 0 });
        const user: User = mockUserDeckhand({ key: 'user-1' });
        const optimizedSlots = [mockSlotDeckhand({ key: 'slot-1', assignedRegistrationKey: 'reg-1' })];
        eventRepository.optimizeSlots = vi.fn(async () => optimizedSlots);

        const result = await testee.assignUserToSlot(event, user, 'slot-1');

        expect(eventRepository.optimizeSlots).toHaveBeenCalledWith(event);
        expect(result.slots).toEqual(optimizedSlots);
        expect(result.assignedUserCount).toBe(1);
    });

    it('should fail assigning user when slot cannot be resolved', async () => {
        const event = mockEvent({ slots: [] });
        const user = mockUserDeckhand();

        try {
            await testee.assignUserToSlot(event, user, 'missing-slot');
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBeDefined();
        }
    });

    it('should fail assigning guest when registration cannot be resolved', async () => {
        const slot = mockSlotDeckhand({ key: 'slot-1' });
        const event = mockEvent({ slots: [slot], registrations: [] });

        try {
            await testee.assignGuestToSlot(event, 'Unknown Guest', 'slot-1');
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBeDefined();
        }
    });

    it('should unassign slot and optimize slots', async () => {
        const slot = mockSlotCaptain({ key: 'slot-1', assignedRegistrationKey: 'reg-1' });
        const event = mockEvent({ slots: [slot] });
        const optimizedSlots = [mockSlotCaptain({ key: 'slot-1', assignedRegistrationKey: undefined })];
        eventRepository.optimizeSlots = vi.fn(async () => optimizedSlots);

        const result = await testee.unassignSlot(event, 'slot-1');

        expect(eventRepository.optimizeSlots).toHaveBeenCalledWith(event);
        expect(result.slots).toEqual(optimizedSlots);
    });

    it('should filter waiting list registrations and sort by priority and qualifications', () => {
        const waitingLowPrio: ResolvedRegistrationSlot = {
            name: 'Anna',
            state: 'waiting-list',
            position: mockPositionDeckhand({ prio: 1 }),
            registration: mockRegistrationDeckhand({ key: 'r-low' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        } as unknown as ResolvedRegistrationSlot;
        const waitingHighPrio: ResolvedRegistrationSlot = {
            name: 'Ben',
            state: 'waiting-list',
            position: mockPositionCaptain({ prio: 5 }),
            registration: mockRegistrationCaptain({ key: 'r-high' }),
            expiredQualifications: ['q-1'],
            hasOverwrittenPosition: false,
        } as unknown as ResolvedRegistrationSlot;
        const assigned: ResolvedRegistrationSlot = {
            name: 'Crew',
            state: 'assigned',
            position: mockPositionCaptain({ prio: 10 }),
            registration: mockRegistrationCaptain({ key: 'r-assigned' }),
            slot: mockSlotCaptain(),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        } as unknown as ResolvedRegistrationSlot;

        const result = testee.filterForWaitingList([waitingLowPrio, assigned, waitingHighPrio]);

        expect(result).toEqual([waitingHighPrio, waitingLowPrio]);
    });

    it('should filter crew registrations by assigned slot', () => {
        const waiting: ResolvedRegistrationSlot = {
            name: 'Waiter',
            state: 'waiting-list',
            position: mockPositionDeckhand(),
            registration: mockRegistrationDeckhand(),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        } as unknown as ResolvedRegistrationSlot;
        const assigned: ResolvedRegistrationSlot = {
            name: 'Crew',
            state: 'assigned',
            position: mockPositionCaptain(),
            registration: mockRegistrationCaptain(),
            slot: mockSlotCaptain(),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        } as unknown as ResolvedRegistrationSlot;

        const result = testee.filterForCrew([waiting, assigned]);

        expect(result).toEqual([assigned]);
    });
});
