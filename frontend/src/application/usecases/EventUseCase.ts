import type { AuthService, EventRegistrationsRepository } from '@/application';
import type { EventRepository } from '@/application/ports/EventRepository';
import type { EventCachingService } from '@/application/services/EventCachingService';
import { DateFormatter } from '@/common/date';
import type { Event, EventKey, PositionKey, UserKey } from '@/domain';

export class EventUseCase {
    private readonly eventCachingService: EventCachingService;
    private readonly eventRepository: EventRepository;
    private readonly authService: AuthService;
    private readonly eventRegistrationsRepository: EventRegistrationsRepository;

    constructor(params: {
        eventCachingService: EventCachingService;
        eventRepository: EventRepository;
        authService: AuthService;
        eventRegistrationsRepository: EventRegistrationsRepository;
    }) {
        this.eventCachingService = params.eventCachingService;
        this.eventRepository = params.eventRepository;
        this.authService = params.authService;
        this.eventRegistrationsRepository = params.eventRegistrationsRepository;
    }

    public async getEvents(year: number): Promise<Event[]> {
        const events = await this.eventCachingService.getEvents(year);
        return events.sort((a, b) => a.start.getTime() - b.start.getTime());
    }

    public async getEventByKey(year: number, eventKey: EventKey): Promise<Event> {
        let event = await this.eventCachingService.getEventByKey(eventKey);
        if (event) {
            return event;
        }
        const events = await this.getEvents(year);
        event = events.find((it) => it.key === eventKey);
        if (event) {
            return event;
        }
        throw new Error('Event nicht gefunden');
    }

    public async getEventsByUser(year: number, userKey: UserKey): Promise<Event[]> {
        const events = await this.eventCachingService.getEvents(year);
        return events
            .filter((evt) => evt.registrations.find((reg) => reg.userKey === userKey))
            .sort((a, b) => a.start.getTime() - b.start.getTime());
    }

    public async getFutureEvents(): Promise<Event[]> {
        const now = new Date();
        const events = await this.eventCachingService.getEvents(now.getFullYear());
        return events.filter((evt) => evt.end > now).sort((a, b) => a.start.getTime() - b.start.getTime());
    }

    public async getFutureEventsByUser(userKey: UserKey): Promise<Event[]> {
        const now = new Date();
        const events = await this.eventCachingService.getEvents(now.getFullYear());
        return events
            .filter((evt) => evt.end > now)
            .filter((evt) => evt.registrations.find((reg) => reg.userKey === userKey))
            .sort((a, b) => a.start.getTime() - b.start.getTime());
    }

    public async joinEvent(event: Event, positionKey: PositionKey): Promise<Event> {
        const user = this.authService.getSignedInUser();
        if (!user) {
            throw new Error('Authentifizierung erforderlich');
        }
        try {
            const savedEvent = await this.eventRegistrationsRepository.createRegistration(event.key, {
                key: '',
                positionKey: positionKey,
                userKey: user.key,
            });
            return this.eventCachingService.updateCache(savedEvent);
        } catch (e) {
            const title = `Anmeldung: ${event.name} am ${DateFormatter.formatDate(event.start)}`;
            const message = `Moin liebes Büro Team,

                Ich möchte mich gerne für die Reise "${event.name}" am ${DateFormatter.formatDate(event.start)} auf die Warteliste setzen lassen. Kontaktiert mich gerne, wenn hier noch ein Platz frei ist oder wird.

                Viele Grüße,`;
            this.openEmail(title, message);
            return event;
        }
    }

    public async leaveEvent(event: Event): Promise<Event> {
        const user = this.authService.getSignedInUser();
        if (!user) {
            throw new Error('Authentifizierung erforderlich');
        }
        try {
            const registration = event.registrations.find((it) => it.userKey === user.key);
            if (!registration) {
                throw new Error('Anmeldung nicht gefunden');
            }
            const savedEvent = await this.eventRegistrationsRepository.deleteRegistration(event.key, registration);
            return this.eventCachingService.updateCache(savedEvent);
        } catch (e) {
            const title = `Absage: ${event.name} am ${DateFormatter.formatDate(event.start)}`;
            const message = `Moin liebes Büro Team,

                Leider kann ich an der Reise "${event.name}" am ${DateFormatter.formatDate(event.start)} nicht teilnehmen. Bitte streicht mich von der Crew Liste.

                Viele Grüße,`;
            this.openEmail(title, message);
            return event;
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
