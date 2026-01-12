import type { EventRepository, Storage } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Event, EventKey } from '@/domain';

export class EventCachingService {
    private readonly eventRepository: EventRepository;
    private readonly cache: Storage<EventKey, Event>;

    constructor(params: { eventRepository: EventRepository; cache: Storage<EventKey, Event> }) {
        this.eventRepository = params.eventRepository;
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
        const all = await this.cache.findAll();
        if (all.find((it) => it.start.getFullYear() === event.start.getFullYear())) {
            return await this.cache.save(event);
        }
        return event;
    }

    public async clear(): Promise<void> {
        await this.cache.deleteAll();
    }

    private async fetchEvents(year: number): Promise<Event[]> {
        return debounce('fetchEvents' + year, async () => {
            const events = await this.eventRepository.findAll(year);
            await this.cache.saveAll(events);
            return events;
        });
    }
}
