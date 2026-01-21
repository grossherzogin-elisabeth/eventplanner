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
import { isSameDate, subtractFromDate } from '@/common';
import { saveBlobToFile, saveStringToFile } from '@/common/utils/DownloadUtils';
import type {
    Event,
    EventKey,
    EventService,
    Registration,
    RegistrationKey,
    RegistrationService,
    ResolvedRegistrationSlot,
    UserKey,
} from '@/domain';
import { EventSignupType, EventState } from '@/domain';

export class EventUseCase {
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;
    private readonly authService: AuthService;
    private readonly eventService: EventService;
    private readonly eventRepository: EventRepository;
    private readonly eventCachingService: EventCachingService;
    private readonly userCachingService: UserCachingService;
    private readonly positionCachingService: PositionCachingService;
    private readonly registrationService: RegistrationService;
    private readonly eventRegistrationsRepository: EventRegistrationsRepository;
    private readonly calendarService: CalendarService;

    constructor(params: {
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
        authService: AuthService;
        eventService: EventService;
        eventRepository: EventRepository;
        eventCachingService: EventCachingService;
        userCachingService: UserCachingService;
        positionCachingService: PositionCachingService;
        registrationService: RegistrationService;
        eventRegistrationsRepository: EventRegistrationsRepository;
        calendarService: CalendarService;
    }) {
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
        this.authService = params.authService;
        this.eventService = params.eventService;
        this.eventCachingService = params.eventCachingService;
        this.eventRepository = params.eventRepository;
        this.userCachingService = params.userCachingService;
        this.positionCachingService = params.positionCachingService;
        this.registrationService = params.registrationService;
        this.eventRegistrationsRepository = params.eventRegistrationsRepository;
        this.calendarService = params.calendarService;
    }

    public async getEvents(year: number): Promise<Event[]> {
        try {
            const events = await this.eventCachingService.getEvents(year);
            return events
                .map((event) => this.eventService.updateComputedValues(event, this.authService.getSignedInUser()))
                .sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async exportEvents(year: number): Promise<void> {
        try {
            const blob = await this.eventRepository.export(year);
            saveBlobToFile(`Einsatzmatrix ${year}.xlsx`, blob);
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getEventByKey(year: number, eventKey: EventKey, ignoreCache: boolean = false): Promise<Event> {
        try {
            const signedInUser = this.authService.getSignedInUser();
            let event = await this.eventCachingService.getEventByKey(eventKey);
            if (ignoreCache) {
                event = await this.eventRepository.findByKey(eventKey);
                await this.eventCachingService.updateCache(event);
            }
            if (event) {
                return this.eventService.updateComputedValues(event, signedInUser);
            }
            const events = await this.getEvents(year);
            event = events.find((it) => it.key === eventKey);
            if (event) {
                return this.eventService.updateComputedValues(event, signedInUser);
            }
            this.errorHandlingService.handleError({
                title: '404 - nicht gefunden',
                message: 'Die angefragte Veranstaltung gibt es nicht, oder sie ist nicht sichtbar.',
            });
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
        throw new Error('not found');
    }

    public async getEventsByUser(year: number, userKey: UserKey): Promise<Event[]> {
        try {
            const events = await this.eventCachingService.getEvents(year);
            return events
                .map((event) => this.eventService.updateComputedValues(event, this.authService.getSignedInUser()))
                .filter((evt) => evt.registrations.find((reg) => reg.userKey === userKey))
                .sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getFutureEvents(): Promise<Event[]> {
        try {
            const signedInUser = this.authService.getSignedInUser();
            const now = new Date();
            const events = await this.eventCachingService.getEvents(now.getFullYear());
            const eventsNextYear = await this.eventCachingService.getEvents(now.getFullYear() + 1);
            const eventsNextNextYear = await this.eventCachingService.getEvents(now.getFullYear() + 2);
            return events
                .map((event) => this.eventService.updateComputedValues(event, signedInUser))
                .concat(eventsNextYear)
                .concat(eventsNextNextYear)
                .filter((evt) => evt.end > now)
                .sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getFutureEventsByUser(userKey: UserKey): Promise<Event[]> {
        try {
            const signedInUser = this.authService.getSignedInUser();
            const events = await this.getFutureEvents();
            return events
                .map((event) => this.eventService.updateComputedValues(event, signedInUser))
                .filter((evt) => evt.registrations.find((reg) => reg.userKey === userKey))
                .sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async joinEvent(event: Event, registration: Registration): Promise<Event> {
        try {
            const savedEvent = await this.joinEventInternal(event, registration);
            if (savedEvent.signupType === EventSignupType.Assignment) {
                this.notificationService.success('Du stehst jetzt auf der Warteliste');
            } else {
                this.notificationService.success('Deine Anmeldung wurde gespeichert');
            }
            return savedEvent;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async joinEvents(events: Event[], registration: Registration): Promise<void> {
        try {
            await Promise.all(
                events.map((event) => {
                    const eventRegistration = { ...registration };
                    if (!isSameDate(event.start, event.end)) {
                        eventRegistration.overnightStay = true;
                    }
                    // we only support setting arrival on the day before the event start for now
                    eventRegistration.arrival ??= subtractFromDate(event.start, { days: 1 });

                    return this.joinEventInternal(event, eventRegistration);
                })
            );
            this.notificationService.success('Deine Anmeldungen wurde gespeichert');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    private async joinEventInternal(event: Event, registration: Registration): Promise<Event> {
        const signedInUser = this.authService.getSignedInUser();
        if (event.registrations.find((it) => it.userKey === signedInUser?.key)) {
            // There already is a registration for this user. Nothing to do here to get required state.
            return this.eventService.updateComputedValues(event, signedInUser);
        }
        let savedEvent = await this.eventRegistrationsRepository.createRegistration(event.key, registration);
        savedEvent = this.eventService.updateComputedValues(savedEvent, signedInUser);
        savedEvent = await this.eventCachingService.updateCache(savedEvent);
        return savedEvent;
    }

    public async updateRegistration(event: Event, registration: Registration): Promise<Event> {
        const signedInUser = this.authService.getSignedInUser();
        let savedEvent = await this.eventRegistrationsRepository.updateRegistration(event.key, registration);
        savedEvent = this.eventService.updateComputedValues(savedEvent, signedInUser);
        savedEvent = await this.eventCachingService.updateCache(savedEvent);
        return savedEvent;
    }

    public async resolveRegistrations(event: Event): Promise<ResolvedRegistrationSlot[]> {
        const users = await this.userCachingService.getUsers();
        const positions = await this.positionCachingService.getPositions();
        return this.registrationService.resolveRegistrations(event, users, positions);
    }

    public filterForWaitingList(event: Event, registrations: ResolvedRegistrationSlot[]): ResolvedRegistrationSlot[] {
        if ([EventState.Draft, EventState.OpenForSignup].includes(event.state)) {
            // crew is not public yet, so all registrations are on the waiting list
            return registrations.filter((it) => it.registration !== undefined);
        }
        return registrations.filter((it) => it.registration !== undefined && it.slot === undefined);
    }

    public filterForCrew(event: Event, registrations: ResolvedRegistrationSlot[]): ResolvedRegistrationSlot[] {
        if ([EventState.Draft, EventState.OpenForSignup].includes(event.state)) {
            // crew is not public yet, so all registrations are on the waiting list
            return [];
        }
        return registrations.filter((it) => it.slot !== undefined);
    }

    public async leaveEvents(events: Event[]): Promise<void> {
        try {
            const canLeaveAllEvents = !events.find((it) => !it.canSignedInUserLeave);
            if (!canLeaveAllEvents) {
                this.errorHandlingService.handleError({
                    title: 'Absage über die App nicht möglich',
                    message: `
                            Mindestens eine Veranstaltung kann nicht mehr über die App abgesagt werden, da sie in weniger als 7
                            Tagen starten wird. Bitte melde dich im Büro ab und versuche kurzfristige Absagen soweit möglich
                            zu vermeiden, da es dann schwierig ist noch einen Ersatz für dich zu finden.
                        `,
                });
                return;
            }
            await Promise.all(events.map((event) => this.leaveEventInternal(event)));
            this.notificationService.success('Deine Teilnahme an den ausgewählten Veranstaltungen wurde storniert');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async leaveEventsWaitingListOnly(events: Event[]): Promise<void> {
        try {
            await Promise.all(
                events
                    .filter((event) => event.signedInUserRegistration)
                    .filter((event) => !event.isSignedInUserAssigned)
                    .map((event) => this.leaveEventInternal(event))
            );
            this.notificationService.success('Du stehst für die ausgewählten Veranstaltungen nicht mehr auf der Warteliste');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async leaveEvent(event: Event): Promise<Event> {
        try {
            const signedInUser = this.authService.getSignedInUser();
            const registration = event.registrations.find((it) => it.userKey === signedInUser?.key);
            if (!registration) {
                return this.eventService.updateComputedValues(event, signedInUser);
            }
            if (!event.canSignedInUserLeave) {
                this.errorHandlingService.handleError({
                    title: 'Absage über die App nicht möglich',
                    message: `
                        Du kannst diese Veranstaltung nicht mehr über die App absagen, da sie in weniger als 7 Tagen starten
                        wird. Bitte melde dich im Büro ab und versuche kurzfristige Absagen soweit möglich zu vermeiden,
                        da es dann schwierig ist noch einen Ersatz für dich zu finden.
                    `,
                });
                return event;
            }

            const hasAssignedSlot = event.slots.find((it) => it.assignedRegistrationKey === registration.key);

            const savedEvent = await this.leaveEventInternal(event);

            if (hasAssignedSlot) {
                this.notificationService.success('Deine Teilnahme wurde storniert');
            } else {
                this.notificationService.success('Du hast die Warteliste verlassen');
            }
            return savedEvent;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    private async leaveEventInternal(event: Event): Promise<Event> {
        const signedInUser = this.authService.getSignedInUser();
        const registration = event.registrations.find((it) => it.userKey === signedInUser?.key);
        if (!registration) {
            return this.eventService.updateComputedValues(event, signedInUser);
        }
        let savedEvent = await this.eventRegistrationsRepository.deleteRegistration(event.key, registration);
        savedEvent = this.eventService.updateComputedValues(savedEvent, signedInUser);
        savedEvent = await this.eventCachingService.updateCache(savedEvent);
        return savedEvent;
    }

    public downloadCalendarEntries(events: Event[]): void {
        const ics = this.calendarService.createCalendarEntries(events);
        saveStringToFile(`events.ics`, ics);
    }

    public downloadCalendarEntry(event: Event): void {
        const ics = this.calendarService.createCalendarEntries([event]);
        saveStringToFile(`${event.name}.ics`, ics);
    }

    public async getEventByAccessKey(eventKey: EventKey, accessKey: string): Promise<Event> {
        const event = await this.eventRepository.findByKey(eventKey, accessKey);
        return this.eventService.updateComputedValues(event, this.authService.getSignedInUser());
    }

    public async confirmParticipation(eventKey: EventKey, registrationKey: RegistrationKey, accessKey: string): Promise<void> {
        try {
            await this.eventRegistrationsRepository.confirmParticipation(eventKey, registrationKey, accessKey);
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async declineParticipation(eventKey: EventKey, registrationKey: RegistrationKey, accessKey: string): Promise<void> {
        try {
            await this.eventRegistrationsRepository.declineParticipation(eventKey, registrationKey, accessKey);
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }
}
