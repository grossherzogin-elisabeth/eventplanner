import type { Event, EventKey, Registration } from '@/domain';

export interface EventRegistrationsRepository {
    createRegistration(eventKey: EventKey, registration: Registration): Promise<Event>;

    updateRegistration(eventKey: EventKey, registration: Registration): Promise<Event>;

    deleteRegistration(eventKey: EventKey, registration: Registration): Promise<Event>;
}
