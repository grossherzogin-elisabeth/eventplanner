import type { Event, EventKey, Registration, RegistrationKey } from '@/domain';

export interface EventRegistrationsRepository {
    createRegistration(eventKey: EventKey, registration: Registration): Promise<Event>;

    updateRegistration(eventKey: EventKey, registration: Registration): Promise<Event>;

    deleteRegistration(eventKey: EventKey, registration: Registration): Promise<Event>;

    confirmParticipation(eventKey: EventKey, registrationKey: RegistrationKey, accessKey: string): Promise<void>;

    declineParticipation(eventKey: EventKey, registrationKey: RegistrationKey, accessKey: string): Promise<void>;
}
