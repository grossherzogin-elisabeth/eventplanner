import type { EventRegistrationsRepository, EventRepository } from '@/application';
import type { EventCachingService } from '@/application/services/EventCachingService';
import type { Event, EventKey, ImportError, Registration } from '@/domain';

export class EventAdministrationUseCase {
    private readonly eventCachingService: EventCachingService;
    private readonly eventRepository: EventRepository;
    private readonly eventRegistrationsRepository: EventRegistrationsRepository;

    constructor(params: {
        eventCachingService: EventCachingService;
        eventRepository: EventRepository;
        eventRegistrationsRepository: EventRegistrationsRepository;
    }) {
        this.eventCachingService = params.eventCachingService;
        this.eventRepository = params.eventRepository;
        this.eventRegistrationsRepository = params.eventRegistrationsRepository;
    }

    public async updateEvent(eventKey: EventKey, event: Partial<Event>): Promise<Event> {
        const original = await this.eventCachingService.getEventByKey(eventKey);
        // TODO create diff of registrations and save those that have changed
        console.log(original?.registrations);
        console.log(event?.registrations);
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

    public async addRegistration(eventKey: EventKey, registration: Registration): Promise<void> {
        const savedEvent = await this.eventRegistrationsRepository.createRegistration(eventKey, registration);
        await this.eventCachingService.updateCache(savedEvent);
    }

    public async deleteRegistration(eventKey: EventKey, registration: Registration): Promise<void> {
        const savedEvent = await this.eventRegistrationsRepository.deleteRegistration(eventKey, registration);
        await this.eventCachingService.updateCache(savedEvent);
    }

    public async updateRegistrations(eventKey: EventKey, registrations: Registration[]): Promise<void> {
        for (let i = 0; i < registrations.length; i++) {
            const registration = registrations[i];
            const savedEvent = await this.eventRegistrationsRepository.updateRegistration(eventKey, registration);
            await this.eventCachingService.updateCache(savedEvent);
        }
    }
}
