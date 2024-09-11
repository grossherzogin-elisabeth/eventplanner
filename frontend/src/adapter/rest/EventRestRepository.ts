import { getCsrfToken } from '@/adapter/util/Csrf';
import type { EventRepository } from '@/application';
import { DateUtils } from '@/common';
import type { Event, EventKey, EventState, ImportError, PositionKey, UserKey } from '@/domain';
import { EventType, SlotCriticality } from '@/domain';

interface SlotRepresentation {
    key: string;
    order: number;
    required: boolean;
    criticality?: number; // TODO finish refactoring
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
    state?: string;
    name?: string;
    description?: string;
    start?: string;
    end?: string;
    locations?: LocationRepresentation[];
    slots?: SlotRepresentation[];
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
                order: slotRepresentation.order,
                criticality: slotRepresentation.required ? SlotCriticality.Security : SlotCriticality.Optional,
                positionKeys: slotRepresentation.positionKeys as PositionKey[],
                positionName: slotRepresentation.name,
            })),
            assignedUserCount: eventRepresentation.registrations.filter((it) => it.slotKey).length,
        };
        event.type = EventRestRepository.mapEventType(event);
        return event;
    }

    private static mapEventType(event: Event): EventType {
        const start = DateUtils.cropToPrecision(event.start, 'days');
        const end = DateUtils.cropToPrecision(event.end, 'days');
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
            slots: event.slots?.map((it) => ({
                key: it.key,
                order: it.order,
                required: it.criticality >= 1,
                criticality: it.criticality,
                positionKeys: it.positionKeys,
                name: it.positionName,
            })),
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
            slots: updateRequest.slots?.map((it) => ({
                key: it.key,
                order: it.order,
                required: it.criticality >= 1,
                criticality: it.criticality,
                positionKeys: it.positionKeys,
                name: it.positionName,
            })),
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
}
