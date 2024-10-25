import type { AuthService, EventRegistrationsRepository, NotificationService } from '@/application';
import type { EventRepository } from '@/application/ports/EventRepository';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { EventCachingService } from '@/application/services/EventCachingService';
import { DateFormatter } from '@/common/date';
import type { Event, EventKey, PositionKey, UserKey } from '@/domain';
import { EventType } from '@/domain';

export class EventUseCase {
    private readonly eventCachingService: EventCachingService;
    private readonly eventRepository: EventRepository;
    private readonly authService: AuthService;
    private readonly eventRegistrationsRepository: EventRegistrationsRepository;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        eventCachingService: EventCachingService;
        eventRepository: EventRepository;
        authService: AuthService;
        eventRegistrationsRepository: EventRegistrationsRepository;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.eventCachingService = params.eventCachingService;
        this.eventRepository = params.eventRepository;
        this.authService = params.authService;
        this.eventRegistrationsRepository = params.eventRegistrationsRepository;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async getEvents(year: number): Promise<Event[]> {
        try {
            const events = await this.eventCachingService.getEvents(year);
            return events.sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getEventByKey(year: number, eventKey: EventKey): Promise<Event> {
        try {
            let event = await this.eventCachingService.getEventByKey(eventKey);
            if (event) {
                return event;
            }
            const events = await this.getEvents(year);
            event = events.find((it) => it.key === eventKey);
            if (event) {
                return event;
            }
            this.errorHandlingService.handleError({
                title: '404 - nicht gefunden',
                message: 'Hoppla, die Reise oder das Event gibt es anscheinend nicht',
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
                .filter((evt) => evt.registrations.find((reg) => reg.userKey === userKey))
                .sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getFutureEvents(): Promise<Event[]> {
        try {
            const now = new Date();
            const events = await this.eventCachingService.getEvents(now.getFullYear());
            return events.filter((evt) => evt.end > now).sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getFutureEventsByUser(userKey: UserKey): Promise<Event[]> {
        try {
            const now = new Date();
            const events = await this.eventCachingService.getEvents(now.getFullYear());
            return events
                .filter((evt) => evt.end > now)
                .filter((evt) => evt.registrations.find((reg) => reg.userKey === userKey))
                .sort((a, b) => a.start.getTime() - b.start.getTime());
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async joinEvent(event: Event, positionKey: PositionKey): Promise<Event> {
        try {
            const user = this.authService.getSignedInUser();
            if (!user) {
                throw new Error('Authentifizierung erforderlich');
            }
            let savedEvent = await this.eventRegistrationsRepository.createRegistration(event.key, {
                key: '',
                positionKey: positionKey,
                userKey: user.key,
            });
            savedEvent = await this.eventCachingService.updateCache(savedEvent);
            if (event.type === EventType.WorkEvent) {
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

    public async leaveEvent(event: Event): Promise<Event> {
        try {
            const user = this.authService.getSignedInUser();
            if (!user) {
                throw new Error('Authentifizierung erforderlich');
            }
            const registration = event.registrations.find((it) => it.userKey === user.key);
            if (!registration) {
                throw new Error('Anmeldung nicht gefunden');
            }
            const hasSlot = event.slots.find((it) => it.assignedRegistrationKey === registration.key);
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
            let savedEvent = await this.eventRegistrationsRepository.deleteRegistration(event.key, registration);
            savedEvent = await this.eventCachingService.updateCache(savedEvent);
            if (hasSlot) {
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

    public downloadCalendarEntry(event: Event): void {
        // create ics file
        const lines = [
            'BEGIN:VCALENDAR',
            'VERSION:2.0',
            'METHOD:PUBLISH',
            'BEGIN:VEVENT',
            `UID:${event.key}@großherzogin-elisabeth.de`,
            `LOCATION:${event.locations[0]?.address || event.locations[0]?.name}`,
            `DTSTAMP:${DateFormatter.formatIcsDate(new Date())}`,
            'ORGANIZER;CN=Grossherzogin Elisabeth e.V.:MAILTO:office@grossherzogin-elisabeth.de',
            `DTSTART:${DateFormatter.formatIcsDate(event.start)}`,
            `DTEND:${DateFormatter.formatIcsDate(event.end)}`,
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
        try {
            const downloadElement = document.createElement('a');
            downloadElement.setAttribute(
                'href',
                'data:text/plain;charset=utf-8,' + encodeURIComponent(lines.join('\n'))
            );
            downloadElement.setAttribute('download', 'Event.ics');
            downloadElement.style.display = 'none';
            document.body.appendChild(downloadElement);
            downloadElement.click();
            document.body.removeChild(document);
        } catch (e) {
            // ignore
        }
    }

    private openEmail(subject: string, body: string): void {
        const email = `mailto:office@grossherzogin-elisabeth.de?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
        window.open(email, '_blank');
    }
}
