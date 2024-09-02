import { getCsrfToken } from '@/adapter/util/Csrf';
import type { EventRepository } from '@/application';
import type { Event, EventKey, ImportError, PositionKey, UserKey } from '@/domain';
import { EventState, EventType } from '@/domain';

interface SlotRepresentation {
    key: string;
    order: number;
    required: boolean;
    positionKeys: string[];
    name?: string;
}

interface RegistrationRepresentation {
    positionKey: string;
    name?: string;
    userKey?: string;
    slotKey?: string;
}

interface LocationRepresentation {
    name: string;
    icon: string;
}

interface EventRepresentation {
    key: string;
    state: string;
    templateKey: string;
    name: string;
    description: string;
    start: string;
    end: string;
    locations: LocationRepresentation[];
    slots: SlotRepresentation[];
    registrations: RegistrationRepresentation[];
}

interface EventCreateRequest {
    state: string;
    name: string;
    description: string;
    start: string;
    end: string;
    locations: LocationRepresentation[];
    slots: SlotRepresentation[];
    registrations: RegistrationRepresentation[];
}

interface EventUpdateRequest {
    state?: string;
    name?: string;
    description?: string;
    start?: string;
    end?: string;
    locations?: LocationRepresentation[];
    slots?: SlotRepresentation[];
    registrations?: RegistrationRepresentation[];
}

interface JoinEventRequest {
    teamMemberKey: string;
    positionKey: string;
}

interface ImportErrorRepresentation {
    eventKey: string;
    eventName: string;
    start: string;
    end: string;
    message: string;
}

export class EventRestRepository implements EventRepository {
    private static mapEventToDomain(eventRepresentation: EventRepresentation): Event {
        return {
            key: eventRepresentation.key,
            type: EventType.VOYAGE,
            name: eventRepresentation.name,
            description: eventRepresentation.description,
            state: eventRepresentation.state as EventState,
            start: EventRestRepository.parseDate(eventRepresentation.start),
            end: EventRestRepository.parseDate(eventRepresentation.end),
            registrations: eventRepresentation.registrations.map((it) => ({
                positionKey: it.positionKey,
                userKey: it.userKey,
                name: it.name,
                slotKey: it.slotKey,
            })),
            locations: eventRepresentation.locations.map((locationRepresentation) => ({
                name: locationRepresentation.name,
                icon: locationRepresentation.icon,
            })),
            slots: eventRepresentation.slots.map((slotRepresentation) => ({
                key: slotRepresentation.key,
                required: slotRepresentation.required,
                order: slotRepresentation.order,
                positionKeys: slotRepresentation.positionKeys as PositionKey[],
                positionName: slotRepresentation.name,
            })),
            assignedUserCount: eventRepresentation.registrations.filter((it) => it.slotKey).length,
        };
    }

    public async findAll(year: number): Promise<Event[]> {
        const response = await fetch(`/api/v1/events/by-year/${year}`, {
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation[] = await response.clone().json();
        const events: Event[] = responseData.map(EventRestRepository.mapEventToDomain);
        return events;
    }

    public async importEvents(year: number, file: Blob): Promise<ImportError[]> {
        const formParams = new FormData();
        formParams.append('file', file);
        // don't add 'Content-Type': 'multipart/form-data' header, as this will break the upload!
        const response = await fetch(`/api/v1/import/events/${year}`, {
            method: 'POST',
            credentials: 'include',
            body: formParams,
            headers: {
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const errors: ImportErrorRepresentation[] = await response.clone().json();

        const map = new Map<string, ImportError>();
        errors.forEach((err) => {
            const eventErrs = map.get(err.eventKey) || {
                eventName: err.eventName,
                messages: [],
                start: EventRestRepository.parseDate(err.start),
                end: EventRestRepository.parseDate(err.end),
            };
            map.set(err.eventKey, eventErrs);
            eventErrs.messages.push(err.message);
        });
        return [...map.values()].sort((a, b) => b.start.getTime() - a.start.getTime());
    }

    public async createEvent(event: Event): Promise<Event> {
        const requestBody: EventCreateRequest = {
            state: event.state,
            name: event.name,
            description: event.description,
            start: event.start?.toISOString(),
            end: event.end?.toISOString(),
            locations: event.locations,
            slots: event.slots,
            registrations: event.registrations,
        };
        const response = await fetch(`/api/v1/events`, {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
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

    public async joinEvent(eventKey: EventKey, teamMemberKey: UserKey, positionKey: PositionKey): Promise<Event> {
        const requestBody: JoinEventRequest = {
            teamMemberKey: teamMemberKey,
            positionKey: positionKey,
        };
        const response = await fetch(`/api/v1/events/${eventKey}/registrations`, {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
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

    public async leaveEvent(eventKey: EventKey, teamMemberKey: UserKey): Promise<Event> {
        const response = await fetch(`/api/v1/events/${eventKey}/registrations/${teamMemberKey}`, {
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

    public async updateEvent(eventKey: EventKey, updateRequest: Partial<Event>): Promise<Event> {
        const requestBody: EventUpdateRequest = {
            state: updateRequest.state,
            name: updateRequest.name,
            description: updateRequest.description,
            start: updateRequest.start?.toISOString(),
            end: updateRequest.end?.toISOString(),
            locations: updateRequest.locations,
            slots: updateRequest.slots,
            registrations: updateRequest.registrations,
        };
        const response = await fetch(`/api/v1/events/${eventKey}`, {
            method: 'PUT',
            credentials: 'include',
            body: JSON.stringify(requestBody),
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

    private static parseDate(date: string): Date {
        // js cannot parse an ISO date time like 2024-06-25T00:00+02:00[Europe/Berlin]
        if (date.includes('[')) {
            return new Date(date.substring(0, date.indexOf('[')));
        }
        return new Date(date);
    }

    private generateWorkEvent(date: Date): Event {
        const start = new Date(date.getTime());
        start.setHours(9, 0, 0, 0);
        const end = new Date(date.getTime());
        end.setHours(17, 0, 0, 0);
        return {
            key: 'arbeitsdienst_' + date.toDateString(),
            state: EventState.OpenForSignup,
            type: EventType.WorkEvent,
            name: 'Arbeitsdienst',
            description: '',
            start: start,
            end: end,
            locations: [
                {
                    name: 'Elsfleth',
                    icon: 'fa-hammer',
                },
            ],
            slots: [],
            registrations: [],
            assignedUserCount: 0,
        };
    }
}
