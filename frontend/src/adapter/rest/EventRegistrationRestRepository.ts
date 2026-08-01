import type { EventRepresentation } from '@/adapter/rest/EventRestRepository';
import { EventRestRepository } from '@/adapter/rest/EventRestRepository';
import { getAccessKeyHeader } from '@/adapter/rest/util/getAccessKeyHeader';
import { getCsrfTokenHeader } from '@/adapter/rest/util/getCsrfTokenHeader';
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
                ...getCsrfTokenHeader(),
                ...getAccessKeyHeader(),
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
                ...getCsrfTokenHeader(),
                ...getAccessKeyHeader(),
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
                ...getCsrfTokenHeader(),
                ...getAccessKeyHeader(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation = await response.clone().json();
        return EventRestRepository.mapEventToDomain(responseData);
    }

    public async confirmParticipation(eventKey: EventKey, registrationKey: RegistrationKey): Promise<void> {
        const response = await fetch(`/api/v1/events/${eventKey}/registrations/${registrationKey}/confirm`, {
            method: 'GET',
            credentials: 'include',
            headers: getAccessKeyHeader(),
        });
        if (!response.ok) {
            throw response;
        }
    }

    public async declineParticipation(eventKey: EventKey, registrationKey: RegistrationKey): Promise<void> {
        const response = await fetch(`/api/v1/events/${eventKey}/registrations/${registrationKey}/decline`, {
            method: 'GET',
            credentials: 'include',
            headers: getAccessKeyHeader(),
        });
        if (!response.ok) {
            throw response;
        }
    }
}
