import type { EventRegistrationsRepository, EventRepository, NotificationService } from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { EventCachingService } from '@/application/services/EventCachingService';
import type { Event, EventKey, ImportError, Registration } from '@/domain';
import { EventState } from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot';

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

    public async deleteEvent(event: Event): Promise<void> {
        try {
            await this.eventRepository.deleteEvent(event.key);
            await this.eventCachingService.removeFromCache(event.key);
            this.notificationService.success('Reise wurde gelöscht');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async cancelEvent(event: Event, message: string): Promise<Event> {
        try {
            let savedEvent = await this.eventRepository.updateEvent(event.key, {
                state: EventState.Canceled,
                description: message, // TODO is this the right place to put the message?
            });
            savedEvent = await this.eventCachingService.updateCache(savedEvent);
            this.notificationService.success('Reise wurde abgesagt');
            return savedEvent;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async updateEvents(eventKeys: EventKey[], patch: Partial<Event>): Promise<void> {
        try {
            // make sure all non batch updatable fields are unset
            delete patch.key;
            delete patch.start;
            delete patch.end;
            delete patch.registrations;
            delete patch.slots;
            delete patch.signedInUserAssignedPosition;
            delete patch.signedInUserWaitingListPosition;
            delete patch.canSignedInUserJoin;
            delete patch.canSignedInUserLeave;
            delete patch.assignedUserCount;

            for (const eventKey of eventKeys) {
                await this.updateEventInternal(eventKey, patch);
            }
            this.notificationService.success('Deine Änderungen wurden gespeichert');
        } catch (e) {
            this.errorHandlingService.handleError({
                title: 'Speichern fehlgeschlagen',
                message: `Deine Änderungen konnten nicht gespeichert werden. Bitte versuche es erneut. Sollte der Fehler
                wiederholt auftreten, melde ihn gerne.`,
                error: e,
                retry: () => this.updateEvents(eventKeys, patch),
            });
            throw e;
        }
    }

    public async updateEvent(eventKey: EventKey, event: Partial<Event>): Promise<Event> {
        try {
            const updatedEvent = await this.updateEventInternal(eventKey, event);
            this.notificationService.success('Deine Änderungen wurden gespeichert');
            return updatedEvent;
        } catch (e) {
            this.errorHandlingService.handleError({
                title: 'Speichern fehlgeschlagen',
                message: `Deine Änderungen konnten nicht gespeichert werden. Bitte versuche es erneut. Sollte der Fehler
                wiederholt auftreten, melde ihn gerne.`,
                error: e,
                retry: () => this.updateEvent(eventKey, event),
            });
            throw e;
        }
    }

    private async updateEventInternal(eventKey: EventKey, event: Partial<Event>): Promise<Event> {
        const original = await this.eventCachingService.getEventByKey(eventKey);
        if (original && event.registrations) {
            const originalRegistrationKeys = original.registrations.map((r) => r.key);
            const newRegistrationKeys = event.registrations.map((r) => r.key);

            const newRegistrations = event.registrations.filter((r) => !originalRegistrationKeys.includes(r.key));
            const deletedRegistrations = original.registrations.filter((r) => !newRegistrationKeys.includes(r.key));
            const changedRegistrations = event.registrations.filter((a) => {
                const b = original.registrations.find((it) => it.key === a.key);
                return b !== undefined && (a.name !== b.name || a.positionKey !== b.positionKey || a.note !== b.note);
            });

            for (const r of newRegistrations) {
                await this.eventRegistrationsRepository.createRegistration(eventKey, r);
            }
            for (const r of changedRegistrations) {
                await this.eventRegistrationsRepository.updateRegistration(eventKey, r);
            }
            for (const r of deletedRegistrations) {
                await this.eventRegistrationsRepository.deleteRegistration(eventKey, r);
            }
        }

        let savedEvent = await this.eventRepository.updateEvent(eventKey, event);
        savedEvent = await this.eventCachingService.updateCache(savedEvent);
        return savedEvent;
    }

    public async createEvent(event: Event): Promise<Event> {
        try {
            let savedEvent = await this.eventRepository.createEvent(event);
            savedEvent = await this.eventCachingService.updateCache(savedEvent);
            this.notificationService.success('Das neue Event wurde gespeichert');
            return savedEvent;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
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
        console.log('Contact team of event ' + event.key);
        this.errorHandlingService.handleError({
            title: 'TODO',
            message: 'Diese Funktion ist derzeit noch nicht implementiert',
        });
    }

    public filterForWaitingList(registrations: ResolvedRegistrationSlot[]): ResolvedRegistrationSlot[] {
        return registrations.filter((it) => it.registration !== undefined && it.slot === undefined);
    }

    public filterForCrew(registrations: ResolvedRegistrationSlot[]): ResolvedRegistrationSlot[] {
        return registrations.filter((it) => it.slot !== undefined);
    }

    public async addRegistration(eventKey: EventKey, registration: Registration): Promise<void> {
        try {
            const savedEvent = await this.eventRegistrationsRepository.createRegistration(eventKey, registration);
            await this.eventCachingService.updateCache(savedEvent);
            this.notificationService.success('Anmeldung wurde hinzugefügt');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }
}
