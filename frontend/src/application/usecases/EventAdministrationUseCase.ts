import type { EventRepository } from '@/application';
import type { EventCachingService } from '@/application/services/EventCachingService';
import type { Event, EventKey, ImportError } from '@/domain';

export class EventAdministrationUseCase {
    private readonly eventCachingService: EventCachingService;
    private readonly eventRepository: EventRepository;

    constructor(params: { eventCachingService: EventCachingService; eventRepository: EventRepository }) {
        this.eventCachingService = params.eventCachingService;
        this.eventRepository = params.eventRepository;
    }

    public async updateEvent(eventKey: EventKey, event: Partial<Event>): Promise<Event> {
        const savedEvent = await this.eventRepository.updateEvent(eventKey, event);
        return this.eventCachingService.updateCache(savedEvent);
    }

    public async createEvent(event: Event): Promise<Event> {
        const savedEvent = await this.eventRepository.createEvent(event);
        return this.eventCachingService.updateCache(savedEvent);
    }

    public async importEvents(year: number, file: Blob): Promise<ImportError[]> {
        return this.eventRepository.importEvents(year, file);
    }

    public async contactTeam(event: Event): Promise<void> {
        // contact all including crew.@grossherzogin-elisabeth.de
        throw new Error('Not implemented');
    }

    public async contactWaitingList(event: Event): Promise<void> {
        // contact all including crew.@grossherzogin-elisabeth.de
        throw new Error('Not implemented');
    }
}
