import type { EventRepository, Storage } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Event, EventKey } from '@/domain';

export class EventCachingService {
    private readonly eventRepository: EventRepository;
    private readonly storage: Storage<EventKey, Event>;
    private readonly initialized: Promise<void>;
    private fetchedYears: number[] = [];

    constructor(params: { eventRepository: EventRepository; cache: Storage<EventKey, Event> }) {
        this.eventRepository = params.eventRepository;
        this.storage = params.cache;
        this.initialized = this.initialize();
    }

    private async initialize(): Promise<void> {
        const currentYear = new Date().getFullYear();
        try {
            const events = await Promise.all([
                this.fetchEvents(currentYear - 2),
                this.fetchEvents(currentYear - 1),
                this.fetchEvents(currentYear),
                this.fetchEvents(currentYear + 1),
                this.fetchEvents(currentYear + 2),
            ]);
            await this.storage.deleteAll();
            await this.storage.saveAll(events.flatMap((it) => it));
        } catch (e: unknown) {
            const response = e as { status?: number };
            if (response.status === 401 || response.status === 403) {
                // users session is no longer valid, clear all locally stored data
                await this.storage.deleteAll();
                console.error('Failed to fetch events, clearing local data');
            } else {
                console.warn('Failed to fetch events, continuing with local data');
            }
        }
    }

    public async getEvents(year: number): Promise<Event[]> {
        await this.initialized;
        let cached = await this.storage.findAll();
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
        return await this.storage.findByKey(eventKey);
    }

    public async removeFromCache(eventKey: EventKey): Promise<void> {
        await this.initialized;
        return await this.storage.deleteByKey(eventKey);
    }

    public async updateCache(event: Event): Promise<Event> {
        await this.initialized;
        const all = await this.storage.findAll();
        if (all.find((it) => it.start.getFullYear() === event.start.getFullYear())) {
            return await this.storage.save(event);
        }
        return event;
    }

    public async clear(): Promise<void> {
        await this.initialized;
        await this.storage.deleteAll();
        this.fetchedYears = [];
    }

    private async fetchEvents(year: number): Promise<Event[]> {
        this.fetchedYears.push(year);
        console.log(`📡 Fetching events of ${year}`);
        return debounce('fetchEvents' + year, async () => {
            const events = await this.eventRepository.findAll(year);
            await this.storage.saveAll(events);
            return events;
        });
    }
}
