import type { EventRepository, Storage } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Event, EventKey } from '@/domain';

export class EventCachingService {
    private readonly eventRepository: EventRepository;
    private readonly cache: Storage<EventKey, Event>;
    private readonly initialized: Promise<void>;
    private fetchedYears: number[] = [];

    constructor(params: { eventRepository: EventRepository; cache: Storage<EventKey, Event> }) {
        this.eventRepository = params.eventRepository;
        this.cache = params.cache;
        this.initialized = this.initialize();
    }

    private async initialize(): Promise<void> {
        const currentYear = new Date().getFullYear();
        try {
            const events = await Promise.all([
                this.fetchEvents(currentYear - 1),
                this.fetchEvents(currentYear),
                this.fetchEvents(currentYear + 1),
                this.fetchEvents(currentYear + 2),
            ]);
            await this.cache.deleteAll();
            await this.cache.saveAll(events.flatMap((it) => it));
        } catch (e) {
            console.log('Failed to load events, continuing with cached data');
        }
    }

    public async getEvents(year: number): Promise<Event[]> {
        await this.initialized;
        let cached = await this.cache.findAll();
        cached = cached.filter((it) => it.start.getFullYear() === year);
        if (cached.length > 0) {
            return cached;
        }
        if (this.fetchedYears.includes(year)) {
            return [];
        }
        return this.fetchEvents(year);
    }

    public async getEventByKey(eventKey: EventKey): Promise<Event | undefined> {
        await this.initialized;
        return await this.cache.findByKey(eventKey);
    }

    public async removeFromCache(eventKey: EventKey): Promise<void> {
        await this.initialized;
        return await this.cache.deleteByKey(eventKey);
    }

    public async updateCache(event: Event): Promise<Event> {
        await this.initialized;
        const all = await this.cache.findAll();
        if (all.find((it) => it.start.getFullYear() === event.start.getFullYear())) {
            return await this.cache.save(event);
        }
        return event;
    }

    public async clear(): Promise<void> {
        await this.initialized;
        await this.cache.deleteAll();
        this.fetchedYears = [];
    }

    private async fetchEvents(year: number): Promise<Event[]> {
        this.fetchedYears.push(year);
        console.log(`📡 Fetching events of ${year}`);
        return debounce('fetchEvents' + year, async () => {
            const events = await this.eventRepository.findAll(year);
            await this.cache.saveAll(events);
            return events;
        });
    }
}
