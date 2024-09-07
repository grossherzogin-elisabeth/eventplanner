import type { Event, EventKey, ImportError } from '@/domain';

export interface EventRepository {
    findAll(year: number): Promise<Event[]>;

    updateEvent(eventKey: EventKey, event: Partial<Event>): Promise<Event>;

    createEvent(event: Event): Promise<Event>;

    importEvents(year: number, file: Blob): Promise<ImportError[]>;
}
