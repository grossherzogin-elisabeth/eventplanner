import { getCsrfToken } from '@/adapter/util/Csrf';
import type { EventRepository } from '@/application';
import { cropToPrecision } from '@/common';
import type { Event, EventKey, EventState, ImportError, PositionKey } from '@/domain';
import { EventType } from '@/domain';

interface SlotRepresentation {
    key: string;
    order: number;
    criticality: number;
    positionKeys: string[];
    name?: string | null;
    assignedRegistrationKey?: string | null;
}

interface RegistrationRepresentation {
    key: string;
    positionKey: string;
    name?: string | null;
    userKey?: string | null;
}

interface LocationRepresentation {
    name: string;
    icon: string;
}

export interface EventRepresentation {
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
}

interface EventUpdateRequest {
    state?: string | null;
    name?: string | null;
    description?: string | null;
    start?: string | null;
    end?: string | null;
    locations?: LocationRepresentation[];
    slots?: SlotRepresentation[];
}

interface ImportErrorRepresentation {
    eventKey: string;
    eventName: string;
    start: string;
    end: string;
    message: string;
}

export class EventRestRepository implements EventRepository {
    public static mapEventToDomain(eventRepresentation: EventRepresentation): Event {
        const event: Event = {
            key: eventRepresentation.key,
            type: EventType.MultiDayEvent,
            name: eventRepresentation.name,
            description: eventRepresentation.description,
            state: eventRepresentation.state as EventState,
            start: EventRestRepository.parseDate(eventRepresentation.start),
            end: EventRestRepository.parseDate(eventRepresentation.end),
            registrations: eventRepresentation.registrations.map((it) => ({
                key: it.key,
                positionKey: it.positionKey,
                userKey: it.userKey || undefined,
                name: it.name || undefined,
            })),
            locations: eventRepresentation.locations.map((locationRepresentation) => ({
                name: locationRepresentation.name,
                icon: locationRepresentation.icon,
            })),
            slots: eventRepresentation.slots.map((slotRepresentation) => ({
                key: slotRepresentation.key,
                order: slotRepresentation.order,
                criticality: slotRepresentation.criticality,
                positionKeys: slotRepresentation.positionKeys as PositionKey[],
                positionName: slotRepresentation.name || undefined,
                assignedRegistrationKey: slotRepresentation.assignedRegistrationKey || undefined,
            })),
            assignedUserCount: eventRepresentation.slots.filter((it) => it.assignedRegistrationKey).length,
            canSignedInUserJoin: false,
            canSignedInUserLeave: false,
        };
        event.type = EventRestRepository.mapEventType(event);
        return event;
    }

    private static mapEventType(event: Event): EventType {
        const start = cropToPrecision(event.start, 'days');
        const end = cropToPrecision(event.end, 'days');
        const durationDays = new Date(event.end.getTime() - event.start.getTime()).getDate();

        if (start.getTime() === end.getTime()) {
            return EventType.SingleDayEvent;
        }
        if (durationDays <= 3) {
            return EventType.WeekendEvent;
        }
        return EventType.MultiDayEvent;
    }

    public async findAll(year: number): Promise<Event[]> {
        const response = await fetch(`/api/v1/events?year=${year}`, {
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation[] = await response.clone().json();
        return responseData.map(EventRestRepository.mapEventToDomain);
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
            slots: event.slots?.map((it) => ({
                key: it.key,
                order: it.order,
                criticality: it.criticality,
                positionKeys: it.positionKeys,
                name: it.positionName,
                assignedRegistrationKey: it.assignedRegistrationKey,
            })),
        };
        const response = await fetch(`/api/v1/events`, {
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

    public async updateEvent(eventKey: EventKey, updateRequest: Partial<Event>): Promise<Event> {
        const requestBody: EventUpdateRequest = {
            state: updateRequest.state,
            name: updateRequest.name,
            description: updateRequest.description,
            start: updateRequest.start?.toISOString(),
            end: updateRequest.end?.toISOString(),
            locations: updateRequest.locations,
            slots: updateRequest.slots?.map((it) => ({
                key: it.key,
                order: it.order,
                criticality: it.criticality,
                positionKeys: it.positionKeys,
                name: it.positionName,
                assignedRegistrationKey: it.assignedRegistrationKey,
            })),
        };
        const response = await fetch(`/api/v1/events/${eventKey}`, {
            method: 'PATCH',
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

    private static parseDate(date: string): Date {
        // js cannot parse an ISO date time like 2024-06-25T00:00+02:00[Europe/Berlin]
        if (date.includes('[')) {
            return new Date(date.substring(0, date.indexOf('[')));
        }
        return new Date(date);
    }
}
