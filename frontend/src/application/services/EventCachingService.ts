import type { AuthService, EventRepository } from '@/application';
import { AsyncDebouncer } from '@/application/utils/AsyncDebouncer';
import { type Cache, DateUtils } from '@/common';
import type { Event, EventKey, Registration } from '@/domain';
import { EventState } from '@/domain';

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

    public async removeFromCache(eventKey: EventKey): Promise<void> {
        return await this.cache.deleteByKey(eventKey);
    }

    public async updateCache(event: Event): Promise<Event> {
        const updated = this.updateComputedValues(event);
        if ((await this.cache.count()) > 0) {
            return await this.cache.save(updated);
        }
        return updated;
    }

    private updateComputedValues(event: Event): Event {
        const signedInUser = this.authService.getSignedInUser();
        const registration = event.registrations.find((it: Registration) => it.userKey === signedInUser?.key);
        if (registration !== undefined) {
            event.canSignedInUserJoin = false;
            const slot = event.slots.find((it) => it.assignedRegistrationKey === registration.key);
            if (slot) {
                event.signedInUserAssignedPosition = registration.positionKey;
                event.canSignedInUserLeave = event.start.getTime() > DateUtils.add(new Date(), { days: 7 }).getTime();
            } else {
                event.signedInUserWaitingListPosition = registration.positionKey;
                event.canSignedInUserLeave = event.start.getTime() > new Date().getTime();
            }
        } else {
            event.canSignedInUserLeave = false;
            event.canSignedInUserJoin =
                event.start.getTime() > new Date().getTime() &&
                ![EventState.Draft, EventState.Canceled].includes(event.state);
        }
        if (event.state === EventState.Canceled) {
            event.canSignedInUserJoin = false;
            event.canSignedInUserLeave = false;
        }
        return event;
    }

    private async fetchEvents(year: number): Promise<Event[]> {
        return AsyncDebouncer.debounce('fetchEvents' + year, async () => {
            const events = await this.eventRepository.findAll(year);
            await this.cache.saveAll(events.map((evt) => this.updateComputedValues(evt)));
            return events;
        });
    }
}
