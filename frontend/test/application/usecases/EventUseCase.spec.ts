import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { EventRegistrationsRepository, EventRepository } from '@/application/ports';
import type {
    AuthService,
    CalendarService,
    ErrorHandlingService,
    EventCachingService,
    NotificationService,
    PositionCachingService,
    UserCachingService,
} from '@/application/services';
import { EventUseCase } from '@/application/usecases/EventUseCase';
import * as DownloadUtils from '@/common/utils/DownloadUtils';
import type { Event, EventService, RegistrationService } from '@/domain';
import { EventSignupType, EventState } from '@/domain';
import {
    mockEvent,
    mockPositionCaptain,
    mockRegistrationCaptain,
    mockRegistrationDeckhand,
    mockSignedInUser,
    mockSlotCaptain,
} from '~/mocks';

describe('EventUseCase', () => {
    let testee: EventUseCase;
    let notificationService: NotificationService;
    let errorHandlingService: ErrorHandlingService;
    let authService: AuthService;
    let eventService: EventService;
    let eventRepository: EventRepository;
    let eventCachingService: EventCachingService;
    let userCachingService: UserCachingService;
    let positionCachingService: PositionCachingService;
    let registrationService: RegistrationService;
    let eventRegistrationsRepository: EventRegistrationsRepository;
    let calendarService: CalendarService;

    beforeEach(() => {
        notificationService = { success: vi.fn() } as unknown as NotificationService;
        errorHandlingService = { handleRawError: vi.fn(), handleError: vi.fn() } as unknown as ErrorHandlingService;
        authService = { getSignedInUser: vi.fn(() => mockSignedInUser()) } as unknown as AuthService;
        eventService = { updateComputedValues: vi.fn((event: Event) => event) } as unknown as EventService;
        eventRepository = {
            findByKey: vi.fn(async (key: string) => mockEvent({ key })),
            findAll: vi.fn(async () => []),
            export: vi.fn(async () => new Blob(['xlsx'])),
        } as unknown as EventRepository;
        eventCachingService = {
            getEvents: vi.fn(async () => []),
            getEventByKey: vi.fn(async () => undefined),
            updateCache: vi.fn(async (event: Event) => event),
        } as unknown as EventCachingService;
        userCachingService = { getUsers: vi.fn(async () => []) } as unknown as UserCachingService;
        positionCachingService = { getPositions: vi.fn(async () => []) } as unknown as PositionCachingService;
        registrationService = { resolveRegistrations: vi.fn(() => []) } as unknown as RegistrationService;
        eventRegistrationsRepository = {
            createRegistration: vi.fn(async () => mockEvent()),
            updateRegistration: vi.fn(async () => mockEvent()),
            deleteRegistration: vi.fn(async () => mockEvent()),
            confirmParticipation: vi.fn(async () => undefined),
            declineParticipation: vi.fn(async () => undefined),
        } as unknown as EventRegistrationsRepository;
        calendarService = { createCalendarEntries: vi.fn(() => 'BEGIN:VCALENDAR') } as unknown as CalendarService;

        testee = new EventUseCase({
            notificationService,
            errorHandlingService,
            authService,
            eventService,
            eventRepository,
            eventCachingService,
            userCachingService,
            positionCachingService,
            registrationService,
            eventRegistrationsRepository,
            calendarService,
        });
    });

    it('should get events sorted by start date', async () => {
        const later = mockEvent({ key: 'later', start: new Date('2026-05-01T08:00:00Z') });
        const earlier = mockEvent({ key: 'earlier', start: new Date('2026-04-01T08:00:00Z') });
        eventCachingService.getEvents = vi.fn(async () => [later, earlier]);

        const result = await testee.getEvents(2026);

        expect(result.map((it) => it.key)).toEqual(['earlier', 'later']);
        expect(eventService.updateComputedValues).toHaveBeenCalledTimes(2);
    });

    it('should get event by key from cache when available', async () => {
        const event = mockEvent({ key: 'event-1' });
        eventCachingService.getEventByKey = vi.fn(async () => event);

        const result = await testee.getEventByKey(2026, 'event-1');

        expect(eventRepository.findByKey).not.toHaveBeenCalled();
        expect(result).toEqual(event);
    });

    it('should fetch event by key from repository when ignoreCache is true', async () => {
        const event = mockEvent({ key: 'event-1' });
        eventRepository.findByKey = vi.fn(async () => event);

        const result = await testee.getEventByKey(2026, 'event-1', true);

        expect(eventRepository.findByKey).toHaveBeenCalledWith('event-1');
        expect(eventCachingService.updateCache).toHaveBeenCalledWith(event);
        expect(result).toEqual(event);
    });

    it('should handle missing event with not found error', async () => {
        eventCachingService.getEventByKey = vi.fn(async () => undefined);
        eventCachingService.getEvents = vi.fn(async () => []);

        try {
            await testee.getEventByKey(2026, 'missing');
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBeDefined();
            expect(errorHandlingService.handleError).toHaveBeenCalled();
        }
    });

    it('should join event and show waiting list notification for assignment events', async () => {
        const event = mockEvent({ key: 'event-1', signupType: EventSignupType.Assignment, registrations: [] });
        const registration = mockRegistrationDeckhand({ userKey: 'mocked' });
        const saved = mockEvent({ key: 'event-1', signupType: EventSignupType.Assignment });
        eventRegistrationsRepository.createRegistration = vi.fn(async () => saved);

        const result = await testee.joinEvent(event, registration);

        expect(eventRegistrationsRepository.createRegistration).toHaveBeenCalledWith('event-1', registration);
        expect(eventCachingService.updateCache).toHaveBeenCalledWith(saved);
        expect(notificationService.success).toHaveBeenCalled();
        expect(result).toEqual(saved);
    });

    it('should skip join registration creation when signed in user is already registered', async () => {
        const event = mockEvent({ key: 'event-1', registrations: [mockRegistrationDeckhand({ userKey: 'mocked' })] });
        const registration = mockRegistrationDeckhand({ userKey: 'mocked' });

        await testee.joinEvent(event, registration);

        expect(eventRegistrationsRepository.createRegistration).not.toHaveBeenCalled();
    });

    it('should join multiple events and set default arrival and overnight stay', async () => {
        const event = mockEvent({
            key: 'event-1',
            registrations: [],
            start: new Date('2026-07-10T09:00:00Z'),
            end: new Date('2026-07-12T17:00:00Z'),
        });
        const registration = mockRegistrationDeckhand({ arrival: undefined, overnightStay: undefined, userKey: 'mocked' });

        await testee.joinEvents([event], registration);

        expect(eventRegistrationsRepository.createRegistration).toHaveBeenCalledWith(
            'event-1',
            expect.objectContaining({ overnightStay: true, arrival: new Date('2026-07-09T09:00:00Z') })
        );
        expect(notificationService.success).toHaveBeenCalled();
    });

    it('should prevent leaving events when one event is not leavable', async () => {
        const cannotLeave = mockEvent({ canSignedInUserLeave: false });

        await testee.leaveEvents([cannotLeave]);

        expect(errorHandlingService.handleError).toHaveBeenCalled();
        expect(eventRegistrationsRepository.deleteRegistration).not.toHaveBeenCalled();
    });

    it('should leave assigned event and show cancellation notification', async () => {
        const registration = mockRegistrationCaptain({ key: 'reg-1', userKey: 'mocked' });
        const event = mockEvent({
            key: 'event-1',
            canSignedInUserLeave: true,
            registrations: [registration],
            slots: [mockSlotCaptain({ assignedRegistrationKey: 'reg-1' })],
        });
        const saved = mockEvent({ key: 'event-1', registrations: [] });
        eventRegistrationsRepository.deleteRegistration = vi.fn(async () => saved);

        const result = await testee.leaveEvent(event);

        expect(eventRegistrationsRepository.deleteRegistration).toHaveBeenCalledWith('event-1', registration);
        expect(notificationService.success).toHaveBeenCalled();
        expect(result).toEqual(saved);
    });

    it('should leave waiting list event and show waiting list notification', async () => {
        const registration = mockRegistrationCaptain({ key: 'reg-1', userKey: 'mocked' });
        const event = mockEvent({ key: 'event-1', canSignedInUserLeave: true, registrations: [registration], slots: [] });
        const saved = mockEvent({ key: 'event-1', registrations: [] });
        eventRegistrationsRepository.deleteRegistration = vi.fn(async () => saved);

        await testee.leaveEvent(event);

        expect(notificationService.success).toHaveBeenCalled();
    });

    it('should resolve registrations using cached users and positions', async () => {
        const event = mockEvent();
        const users = [mockSignedInUser()];
        const positions = [mockPositionCaptain()];
        userCachingService.getUsers = vi.fn(async () => users as never[]);
        positionCachingService.getPositions = vi.fn(async () => positions);

        await testee.resolveRegistrations(event);

        expect(registrationService.resolveRegistrations).toHaveBeenCalledWith(event, users, positions);
    });

    it('should filter waiting list for draft events with all registrations', () => {
        const event = mockEvent({ state: EventState.Draft });
        const registrations = [
            { registration: mockRegistrationCaptain(), slot: mockSlotCaptain() },
            { registration: mockRegistrationDeckhand(), slot: undefined },
        ] as never;

        const result = testee.filterForWaitingList(event, registrations);

        expect(result).toHaveLength(2);
    });

    it('should filter crew entries only for planned events', () => {
        const event = mockEvent({ state: EventState.Planned });
        const registrations = [
            { registration: mockRegistrationCaptain(), slot: mockSlotCaptain() },
            { registration: mockRegistrationDeckhand(), slot: undefined },
        ] as never;

        const result = testee.filterForCrew(event, registrations);

        expect(result).toHaveLength(1);
    });

    it('should download one calendar entry as ics file', () => {
        const saveStringToFileSpy = vi.spyOn(DownloadUtils, 'saveStringToFile').mockImplementation(() => undefined);
        const event = mockEvent({ name: 'Harbor Weekend' });

        testee.downloadCalendarEntry(event);

        expect(calendarService.createCalendarEntries).toHaveBeenCalledWith([event]);
        expect(saveStringToFileSpy).toHaveBeenCalledWith('Harbor Weekend.ics', 'BEGIN:VCALENDAR');
    });

    it('should confirm participation via repository', async () => {
        await testee.confirmParticipation('event-1', 'reg-1', 'access-key');

        expect(eventRegistrationsRepository.confirmParticipation).toHaveBeenCalledWith('event-1', 'reg-1', 'access-key');
    });
});
