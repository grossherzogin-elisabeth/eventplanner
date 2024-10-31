import type {
    AuthService,
    EventRegistrationsRepository,
    NotificationService,
    PositionCachingService,
    UserCachingService,
} from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { EventCachingService } from '@/application/services/EventCachingService';
import { formatIcsDate } from '@/common/date';
import { saveBlobToFile, saveStringToFile } from '@/common/utils/DownloadUtils.ts';
import type { Event, EventKey, EventService, PositionKey, RegistrationService, UserKey } from '@/domain';
import { EventState, EventType, SlotCriticality } from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot';

export class EventUseCase {
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;
    private readonly authService: AuthService;
    private readonly eventService: EventService;
    private readonly eventCachingService: EventCachingService;
    private readonly userCachingService: UserCachingService;
    private readonly positionCachingService: PositionCachingService;
    private readonly registrationService: RegistrationService;
    private readonly eventRegistrationsRepository: EventRegistrationsRepository;

    constructor(params: {
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
        authService: AuthService;
        eventService: EventService;
        eventCachingService: EventCachingService;
        userCachingService: UserCachingService;
        positionCachingService: PositionCachingService;
        registrationService: RegistrationService;
        eventRegistrationsRepository: EventRegistrationsRepository;
    }) {
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
        this.authService = params.authService;
        this.eventService = params.eventService;
        this.eventCachingService = params.eventCachingService;
        this.userCachingService = params.userCachingService;
        this.positionCachingService = params.positionCachingService;
        this.registrationService = params.registrationService;
        this.eventRegistrationsRepository = params.eventRegistrationsRepository;
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

    public async getEventByKey(year: number, eventKey: EventKey): Promise<Event> {
        try {
            const signedInUser = this.authService.getSignedInUser();
            let event = await this.eventCachingService.getEventByKey(eventKey);
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
                message: 'Die angefragte Reise gibt es nicht, oder sie ist nicht sichtbar.',
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

    public async joinEvent(event: Event, positionKey: PositionKey): Promise<Event> {
        try {
            const savedEvent = await this.joinEventInternal(event, positionKey);
            if (savedEvent.type === EventType.WorkEvent) {
                this.notificationService.success('Deine Anmeldung wurde gespeichert');
            } else {
                this.notificationService.success('Du stehst jetzt auf der Warteliste');
            }
            return savedEvent;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async joinEvents(events: Event[], positionKey: PositionKey): Promise<void> {
        try {
            await Promise.all(events.map((event) => this.joinEventInternal(event, positionKey)));
            this.notificationService.success('Deine Anmeldungen wurde gespeichert');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    private async joinEventInternal(event: Event, positionKey: PositionKey): Promise<Event> {
        const signedInUser = this.authService.getSignedInUser();
        if (event.registrations.find((it) => it.userKey === signedInUser?.key)) {
            // There already is a registration for this user. Nothing to do here to get required state.
            return this.eventService.updateComputedValues(event, signedInUser);
        }
        let savedEvent = await this.eventRegistrationsRepository.createRegistration(event.key, {
            key: '',
            positionKey: positionKey,
            userKey: signedInUser?.key,
        });
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
            // crew is not public yet, so all registrations are on the waiting list-admin
            return registrations.filter((it) => it.registration !== undefined);
        }
        return registrations.filter((it) => it.registration !== undefined && it.slot === undefined);
    }

    public filterForCrew(event: Event, registrations: ResolvedRegistrationSlot[]): ResolvedRegistrationSlot[] {
        if ([EventState.Draft, EventState.OpenForSignup].includes(event.state)) {
            // crew is not public yet, so all registrations are on the waiting list-admin
            return [];
        }
        return registrations.filter(
            (it) => it.registration || (it.slot && it.slot.criticality >= SlotCriticality.Important)
        );
    }

    public async leaveEvents(events: Event[]): Promise<void> {
        try {
            const canLeaveAllEvents = !events.find((it) => !it.canSignedInUserLeave);
            if (!canLeaveAllEvents) {
                this.errorHandlingService.handleError({
                    title: 'Absage über die App nicht möglich',
                    message: `
                            Mindestens eine Reise kann nicht mehr über die App abgesagt werden, da sie in weniger als 7
                            Tagen starten wird. Bitte melde dich im Büro ab und versuche kurzfristige Absagen soweit möglich
                            zu vermeiden, da es dann schwierig ist noch einen Ersatz für dich zu finden.
                        `,
                });
                return;
            }
            await Promise.all(events.map((event) => this.leaveEventInternal(event)));
            this.notificationService.success('Deine Teilnahme an den ausgewählten Reisen wurde storniert');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async leaveEventsWaitingListOnly(events: Event[]): Promise<void> {
        try {
            await Promise.all(
                events
                    .filter((event) => event.signedInUserWaitingListPosition)
                    .map((event) => this.leaveEventInternal(event))
            );
            this.notificationService.success('Du stehst für die ausgewählten Reisen nicht mehr auf der Warteliste');
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
                        Du kannst diese Reise nicht mehr über die App absagen, da sie in weniger als 7 Tagen starten
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

    public async downloadImoList(event: Event): Promise<void> {
        const file = await this.eventRepository.downloadImoList(event);
        saveBlobToFile(`${event.name.replace('s', '_')}_imolist.txt`, file);
    }

    public downloadCalendarEntry(event: Event): void {
        // create ics file
        const lines = [
            'BEGIN:VCALENDAR',
            'VERSION:2.0',
            'METHOD:PUBLISH',
            'BEGIN:VEVENT',
            `UID:${event.key}@großherzogin-elisabeth.de`,
            `LOCATION:${event.locations[0]?.address || event.locations[0]?.name}`,
            `DTSTAMP:${formatIcsDate(new Date())}`,
            'ORGANIZER;CN=Grossherzogin Elisabeth e.V.:MAILTO:office@grossherzogin-elisabeth.de',
            `DTSTART:${formatIcsDate(event.start)}`,
            `DTEND:${formatIcsDate(event.end)}`,
            `SUMMARY:${event.name}`,
            `DESCRIPTION:${event.description}`,
            'END:VEVENT',
            'BEGIN:VALARM',
            'DESCRIPTION:REMINDER',
            'TRIGGER;RELATED=START:-P1W', // reminder 1 week before event
            'ACTION:DISPLAY',
            'END:VALARM',
            'END:VCALENDAR',
        ];
        // download ics file
        saveStringToFile('Event.ics', lines.join('\n'));
    }
}
