import type { EventRegistrationsRepository, EventRepository, NotificationService } from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { EventCachingService } from '@/application/services/EventCachingService';
import type { Event, EventKey, ImportError, Registration } from '@/domain';

export class EventAdministrationUseCase {
    private readonly eventCachingService: EventCachingService;
    private readonly eventRepository: EventRepository;
    private readonly eventRegistrationsRepository: EventRegistrationsRepository;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        eventCachingService: EventCachingService;
        eventRepository: EventRepository;
        eventRegistrationsRepository: EventRegistrationsRepository;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.eventCachingService = params.eventCachingService;
        this.eventRepository = params.eventRepository;
        this.eventRegistrationsRepository = params.eventRegistrationsRepository;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async updateEvent(eventKey: EventKey, event: Partial<Event>): Promise<Event> {
        const original = await this.eventCachingService.getEventByKey(eventKey);
        // TODO create diff of registrations and save those that have changed
        console.log(original?.registrations);
        console.log(event?.registrations);
        let savedEvent = await this.eventRepository.updateEvent(eventKey, event);
        savedEvent = await this.eventCachingService.updateCache(savedEvent);
        this.notificationService.success('Deine Änderungen wurden gespeichert');
        return savedEvent;
    }

    public async createEvent(event: Event): Promise<Event> {
        let savedEvent = await this.eventRepository.createEvent(event);
        savedEvent = await this.eventCachingService.updateCache(savedEvent);
        this.notificationService.success('Das neue Event wurde gespeichert');
        return savedEvent;
    }

    public async importEvents(year: number, file: Blob): Promise<ImportError[]> {
        const errors = await this.eventRepository.importEvents(year, file);
        if (errors.length === 0) {
            this.notificationService.success('Events wurden erfolgreich importiert');
        } else {
            this.notificationService.warning('Beim Import traten einige Warnungen auf!');
        }
        return errors;
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
        this.notificationService.success('Anmeldung wurde hinzugefügt');
    }

    public async deleteRegistration(eventKey: EventKey, registration: Registration): Promise<void> {
        const savedEvent = await this.eventRegistrationsRepository.deleteRegistration(eventKey, registration);
        await this.eventCachingService.updateCache(savedEvent);
        this.notificationService.success('Anmeldung wurde gelöscht');
    }

    public async updateRegistrations(eventKey: EventKey, registrations: Registration[]): Promise<void> {
        for (let i = 0; i < registrations.length; i++) {
            const registration = registrations[i];
            const savedEvent = await this.eventRegistrationsRepository.updateRegistration(eventKey, registration);
            await this.eventCachingService.updateCache(savedEvent);
        }
    }
}
