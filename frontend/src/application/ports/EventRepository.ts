import type { Event, EventKey, ImportError } from '@/domain';

export interface EventRepository {
    findAll(year: number): Promise<Event[]>;

    updateEvent(eventKey: EventKey, event: Partial<Event>): Promise<Event>;

    deleteEvent(eventKey: EventKey): Promise<void>;

    createEvent(event: Event): Promise<Event>;

    importEvents(year: number, file: Blob): Promise<ImportError[]>;
}
