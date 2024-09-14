import type { EventRepresentation } from '@/adapter/rest/EventRestRepository';
import { EventRestRepository } from '@/adapter/rest/EventRestRepository';
import { getCsrfToken } from '@/adapter/util/Csrf';
import type { EventRegistrationsRepository } from '@/application';
import type { Event, EventKey, Registration } from '@/domain';

interface RegistrationCreateRequest {
    positionKey: string;
    name?: string;
    userKey?: string;
    note?: string;
}

interface RegistrationUpdateRequest {
    positionKey: string;
    name?: string;
    userKey?: string;
    note?: string;
}

export class EventRegistrationRestRepository implements EventRegistrationsRepository {
    public async createRegistration(eventKey: EventKey, registration: Registration): Promise<Event> {
        const requestBody: RegistrationCreateRequest = {
            positionKey: registration.positionKey,
            userKey: registration.userKey,
            name: registration.name,
            note: registration.note,
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
            positionKey: registration.positionKey,
            userKey: registration.userKey,
            name: registration.name,
            note: registration.note,
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
}
