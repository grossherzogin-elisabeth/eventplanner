import type { Event, EventKey } from '@/domain';

export interface EventRepository {
    findByKey(key: EventKey, accessKey?: string): Promise<Event>;

    findAll(year: number): Promise<Event[]>;

    export(year: number): Promise<Blob>;

    updateEvent(eventKey: EventKey, event: Partial<Event>): Promise<Event>;

    deleteEvent(eventKey: EventKey): Promise<void>;

    createEvent(event: Event): Promise<Event>;

    downloadImoList(event: Event): Promise<Blob>;

    downloadConsumptionList(event: Event): Promise<Blob>;

    downloadCaptainList(event: Event): Promise<Blob>;
}
