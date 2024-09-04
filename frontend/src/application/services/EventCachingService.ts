import type { AuthService, EventRepository } from '@/application';
import type { Cache } from '@/common';
import type { Event, EventKey, Registration } from '@/domain';

export class EventCachingService {
    private readonly eventRepository: EventRepository;
    private readonly authService: AuthService;
    private readonly cache: Cache<EventKey, Event>;

    constructor(params: { eventRepository: EventRepository; authService: AuthService; cache: Cache<EventKey, Event> }) {
        this.eventRepository = params.eventRepository;
        this.authService = params.authService;
        this.cache = params.cache;
    }

    public async getEvents(year: number): Promise<Event[]> {
        let cached = await this.cache.findAll();
        cached = cached.filter((it) => it.start.getFullYear() === year);
        if (cached.length > 0) {
            return cached;
        }
        return this.fetchEvents(year);
    }

    public async getEventByKey(eventKey: EventKey): Promise<Event | undefined> {
        return await this.cache.findByKey(eventKey);
    }

    public async updateCache(event: Event): Promise<Event> {
        if ((await this.cache.count()) > 0) {
            return await this.cache.save(event);
        }
        return event;
    }

    private async fetchEvents(year: number): Promise<Event[]> {
        const events = await this.eventRepository.findAll(year);
        const signedInUser = this.authService.getSignedInUser();
        events.forEach((evt: Event) => {
            const registration = evt.registrations.find((it: Registration) => it.userKey === signedInUser?.key);
            if (registration?.slotKey) {
                evt.signedInUserAssignedPosition = registration.positionKey;
            } else if (registration) {
                evt.signedInUserWaitingListPosition = registration.positionKey;
            }
        });
        await this.cache.saveAll(events);
        return events;
    }
}
