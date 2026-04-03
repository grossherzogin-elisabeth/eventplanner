import type { EventRepresentation } from '@/adapter/rest/EventRestRepository';
import { EventRestRepository } from '@/adapter/rest/EventRestRepository';
import { getCsrfToken } from '@/adapter/util/Csrf';
import type { EventRegistrationsRepository } from '@/application';
import { toIsoDateString } from '@/common';
import type { Event, EventKey, Registration, RegistrationKey } from '@/domain';

export interface RegistrationCreateRequest {
    registrationKey: string;
    positionKey: string;
    name?: string | null;
    userKey?: string | null;
    note?: string | null;
    overnightStay?: boolean | null;
    arrival?: string | null;
}

export interface RegistrationUpdateRequest {
    registrationKey: string;
    positionKey: string;
    name?: string | null;
    userKey?: string | null;
    note?: string | null;
    overnightStay?: boolean | null;
    arrival?: string | null;
}

export class EventRegistrationRestRepository implements EventRegistrationsRepository {
    public async createRegistration(eventKey: EventKey, registration: Registration): Promise<Event> {
        const requestBody: RegistrationCreateRequest = {
            registrationKey: registration.key,
            positionKey: registration.positionKey,
            userKey: registration.userKey,
            name: registration.name,
            note: registration.note,
            overnightStay: registration.overnightStay,
            arrival: toIsoDateString(registration.arrival),
        };
        const response = await fetch(`/api/v1/events/${eventKey}/registrations`, {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation = await response.clone().json();
        return EventRestRepository.mapEventToDomain(responseData);
    }

    public async updateRegistration(eventKey: EventKey, registration: Registration): Promise<Event> {
        const requestBody: RegistrationUpdateRequest = {
            registrationKey: registration.key,
            positionKey: registration.positionKey,
            userKey: registration.userKey,
            name: registration.name,
            note: registration.note,
            overnightStay: registration.overnightStay,
            arrival: toIsoDateString(registration.arrival),
        };
        const response = await fetch(`/api/v1/events/${eventKey}/registrations/${registration.key}`, {
            method: 'PUT',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation = await response.clone().json();
        return EventRestRepository.mapEventToDomain(responseData);
    }

    public async deleteRegistration(eventKey: EventKey, registration: Registration): Promise<Event> {
        const response = await fetch(`/api/v1/events/${eventKey}/registrations/${registration.key}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation = await response.clone().json();
        return EventRestRepository.mapEventToDomain(responseData);
    }

    public async confirmParticipation(eventKey: EventKey, registrationKey: RegistrationKey, accessKey: string): Promise<void> {
        const response = await fetch(`/api/v1/events/${eventKey}/registrations/${registrationKey}/confirm?accessKey=${accessKey}`, {
            method: 'GET',
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
    }

    public async declineParticipation(eventKey: EventKey, registrationKey: RegistrationKey, accessKey: string): Promise<void> {
        const response = await fetch(`/api/v1/events/${eventKey}/registrations/${registrationKey}/decline?accessKey=${accessKey}`, {
            method: 'GET',
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
    }
}
