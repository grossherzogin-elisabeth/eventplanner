import type { EventRegistrationsRepository, EventRepository } from '@/application/ports';
import type { AuthService, ErrorHandlingService, EventCachingService, NotificationService } from '@/application/services';
import { filterUndefined } from '@/common';
import { saveBlobToFile } from '@/common/utils/DownloadUtils';
import type { Event, EventKey, EventService, Registration, ResolvedRegistrationSlot, SlotKey, User } from '@/domain';
import { EventState } from '@/domain';

export class EventAdministrationUseCase {
    private readonly eventCachingService: EventCachingService;
    private readonly eventService: EventService;
    private readonly authService: AuthService;
    private readonly eventRepository: EventRepository;
    private readonly eventRegistrationsRepository: EventRegistrationsRepository;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;
    private exportTemplates: string[] | undefined = undefined;

    constructor(params: {
        eventCachingService: EventCachingService;
        eventService: EventService;
        authService: AuthService;
        eventRepository: EventRepository;
        eventRegistrationsRepository: EventRegistrationsRepository;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.eventCachingService = params.eventCachingService;
        this.eventService = params.eventService;
        this.authService = params.authService;
        this.eventRepository = params.eventRepository;
        this.eventRegistrationsRepository = params.eventRegistrationsRepository;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async deleteEvent(event: Event): Promise<void> {
        try {
            await this.eventRepository.deleteEvent(event.key);
            await this.eventCachingService.removeFromCache(event.key);
            this.notificationService.success('Veranstaltung wurde gelöscht');
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
            savedEvent = this.eventService.updateComputedValues(savedEvent, this.authService.getSignedInUser());
            savedEvent = await this.eventCachingService.updateCache(savedEvent);
            this.notificationService.success('Veranstaltung wurde abgesagt');
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
            delete patch.signedInUserRegistration;
            delete patch.signedInUserAssignedSlot;
            delete patch.isSignedInUserAssigned;
            delete patch.canSignedInUserJoin;
            delete patch.canSignedInUserLeave;
            delete patch.assignedUserCount;

            if (Object.values(patch).filter(filterUndefined).length === 0) {
                return;
            }

            console.log(`Applying patch for ${eventKeys.length} events`, patch);
            for (let i = 0; i < eventKeys.length; i++) {
                const eventKey = eventKeys[i];
                console.log(`Updating event ${eventKey} (${i + 1}/${eventKeys.length})`);
                await this.updateEventInternal(eventKey, patch);
            }

            for (const eventKey of eventKeys) {
                await this.updateEventInternal(eventKey, patch);
            }
            this.notificationService.success('Deine Änderungen wurden gespeichert');
        } catch (e) {
            this.errorHandlingService.handleError({
                title: 'Speichern fehlgeschlagen',
                message: `Deine Änderungen konnten nicht gespeichert werden. Bitte versuche es erneut. Sollte der Fehler
                wiederholt auftreten, melde ihn gerne.`,
                error: e instanceof Error || e instanceof Response ? e : undefined,
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
                error: e instanceof Error || e instanceof Response ? e : undefined,
                retry: () => this.updateEvent(eventKey, event),
            });
            throw e;
        }
    }

    private async updateEventInternal(eventKey: EventKey, event: Partial<Event>): Promise<Event> {
        let original = await this.eventCachingService.getEventByKey(eventKey);
        if (!original) {
            console.log(`📡 Fetching event ${eventKey}`);
            // workaround: we don't have the original cached yet, because for example a reload on a details page happened
            original = await this.eventRepository.findByKey(eventKey);
        }
        let newRegistrations: Registration[] = [];
        let deletedRegistrations: Registration[] = [];
        let changedRegistrations: Registration[] = [];
        if (original && event.registrations) {
            const originalRegistrationKeys = original.registrations.map((r) => r.key);
            const newRegistrationKeys = event.registrations.map((r) => r.key);

            newRegistrations = event.registrations.filter((r) => !originalRegistrationKeys.includes(r.key));
            deletedRegistrations = original.registrations.filter((r) => !newRegistrationKeys.includes(r.key));
            changedRegistrations = event.registrations.filter((a) => {
                const b = original.registrations.find((it) => it.key === a.key);
                return b !== undefined && (a.name !== b.name || a.positionKey !== b.positionKey || a.note !== b.note);
            });
        }

        console.log(`📡 Updating event ${eventKey}`);
        let savedEvent = await this.eventRepository.updateEvent(
            eventKey,
            event,
            deletedRegistrations,
            newRegistrations,
            changedRegistrations
        );
        savedEvent = this.eventService.updateComputedValues(savedEvent, this.authService.getSignedInUser());
        savedEvent = await this.eventCachingService.updateCache(savedEvent);
        return savedEvent;
    }

    public async createEvent(event: Event): Promise<Event> {
        console.log('Creating event');
        try {
            let savedEvent = await this.eventRepository.createEvent(event);
            savedEvent = this.eventService.updateComputedValues(savedEvent, this.authService.getSignedInUser());
            savedEvent = await this.eventCachingService.updateCache(savedEvent);
            this.notificationService.success('Das neue Event wurde gespeichert');
            return savedEvent;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public filterForWaitingList(registrations: ResolvedRegistrationSlot[]): ResolvedRegistrationSlot[] {
        return registrations
            .filter((it) => it.registration !== undefined && it.slot === undefined)
            .sort((a, b) => {
                return (
                    b.position.prio - a.position.prio || // sort by slot prio
                    a.expiredQualifications.length - b.expiredQualifications.length || // sort by expired qualifications count
                    a.name.localeCompare(b.name) // sort by name alphabetically
                );
            });
    }

    public filterForCrew(registrations: ResolvedRegistrationSlot[]): ResolvedRegistrationSlot[] {
        return registrations.filter((it) => it.slot !== undefined);
    }

    public async addRegistrations(events: Event[], registration: Registration): Promise<void> {
        try {
            let eventsToUpdate = events;
            if (registration.userKey) {
                eventsToUpdate = eventsToUpdate.filter((it) => !it.registrations.some((it) => it.userKey === registration.userKey));
            } else if (registration.name) {
                eventsToUpdate = eventsToUpdate.filter((it) => it.registrations.some((it) => it.name === registration.name));
            }
            if (eventsToUpdate.length === 0) {
                this.notificationService.warning('Keine neue Anmeldung hinzugefügt');
                return;
            }

            const updates = eventsToUpdate.map(async (event) => {
                const savedEvent = await this.eventRegistrationsRepository.createRegistration(event.key, registration);
                await this.eventCachingService.updateCache(savedEvent);
            });
            await Promise.all(updates);
            this.notificationService.success('Anmeldungen wurden hinzugefügt');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
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

    public async assignUserToSlot(event: Event, user: User, slotKey: SlotKey): Promise<Event> {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        if (!slot.positionKeys.some((positionkey) => user.positionKeys?.includes(positionkey))) {
            console.warn(`Assigning ${user.firstName} ${user.lastName} to slot with mismatching positions!`);
            // throw new Error('User does not have the required position');
        }
        const registration = event.registrations.find((it) => it.userKey === user.key);
        if (!registration) {
            throw new Error('Failed to resolve user registration');
        }
        slot.assignedRegistrationKey = registration.key;
        event.assignedUserCount = event.slots.filter((it) => it.assignedRegistrationKey).length;
        event.slots = await this.eventRepository.optimizeSlots(event);
        return event;
    }

    public async assignGuestToSlot(event: Event, name: string, slotKey: SlotKey): Promise<Event> {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        const registration = event.registrations.find((it) => it.name === name);
        if (!registration) {
            throw new Error('Failed to resolve guest registration');
        }
        slot.assignedRegistrationKey = registration.key;
        event.assignedUserCount = event.slots.filter((it) => it.assignedRegistrationKey).length;
        event.slots = await this.eventRepository.optimizeSlots(event);
        return event;
    }

    public async unassignSlot(event: Event, slotKey: SlotKey): Promise<Event> {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        slot.assignedRegistrationKey = undefined;
        event.slots = await this.eventRepository.optimizeSlots(event);
        return event;
    }

    public async getExportTemplates(): Promise<string[]> {
        if (!this.exportTemplates) {
            this.exportTemplates = await this.eventRepository.getExportTemplates();
        }
        return this.exportTemplates;
    }

    public async exportEvent(event: Event, template: string): Promise<void> {
        const file = await this.eventRepository.exportEvent(event, template);
        saveBlobToFile(`${template}_${this.formatDate(event.start)}.xlsx`, file);
    }

    private formatDate(date: Date | string | number): string {
        const d = new Date(date);
        const day = d.getDate() < 10 ? `0${d.getDate()}` : d.getDate().toString();
        const month = d.getMonth() < 9 ? `0${d.getMonth() + 1}` : (d.getMonth() + 1).toString();
        return `${d.getFullYear()}-${month}-${day}`;
    }
}
