import type { RegistrationCreateRequest, RegistrationUpdateRequest } from '@/adapter';
import { getCsrfToken } from '@/adapter/util/Csrf';
import type { EventRepository } from '@/application';
import { cropToPrecision, deserializeDate, isSameDate } from '@/common';
import type { Event, EventKey, EventState, Registration, Slot } from '@/domain';
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
    note?: string | null;
    confirmed?: boolean | null;
    overnightStay?: boolean | null;
    arrival?: Date | null;
}

interface LocationRepresentation {
    name: string;
    icon: string;
    address?: string;
    country?: string;
    addressLink?: string;
    information?: string;
    informationLink?: string;
    eta?: string;
    etd?: string;
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
    registrationsToRemove?: string[];
    registrationsToAdd?: RegistrationCreateRequest[];
    registrationsToUpdate?: RegistrationUpdateRequest[];
}

interface OptimizeEventSlotsRequest {
    slots: SlotRepresentation[];
    registrations: RegistrationRepresentation[];
}

export class EventRestRepository implements EventRepository {
    public static mapEventToDomain(eventRepresentation: EventRepresentation): Event {
        const event: Event = {
            key: eventRepresentation.key,
            type: EventType.MultiDayEvent,
            name: eventRepresentation.name,
            description: eventRepresentation.description,
            state: eventRepresentation.state as EventState,
            start: deserializeDate(eventRepresentation.start),
            end: deserializeDate(eventRepresentation.end),
            days: 0,
            registrations: eventRepresentation.registrations.map((it) => ({
                key: it.key,
                positionKey: it.positionKey,
                userKey: it.userKey ?? undefined,
                name: it.name ?? undefined,
                note: it.note ?? undefined,
                confirmed: it.confirmed ?? undefined,
                overnightStay: it.overnightStay ?? undefined,
                arrival: it.arrival ?? undefined,
            })),
            locations: eventRepresentation.locations.map((locationRepresentation, index) => ({
                name: locationRepresentation.name,
                icon: locationRepresentation.icon,
                address: locationRepresentation.address,
                country: locationRepresentation.country,
                order: index + 1,
                addressLink: locationRepresentation.addressLink,
                information: locationRepresentation.information,
                informationLink: locationRepresentation.informationLink,
                eta: locationRepresentation.eta ? deserializeDate(locationRepresentation.eta) : undefined,
                etd: locationRepresentation.etd ? deserializeDate(locationRepresentation.etd) : undefined,
            })),
            slots: EventRestRepository.mapSlotsToDomain(eventRepresentation.slots),
            assignedUserCount: eventRepresentation.slots.filter((it) => it.assignedRegistrationKey).length,
            canSignedInUserJoin: false,
            canSignedInUserLeave: false,
            canSignedInUserUpdateRegistration: false,
        };
        event.type = EventRestRepository.mapEventType(event);
        return event;
    }

    private static mapSlotsToDomain(slots: SlotRepresentation[]): Slot[] {
        return slots.map((slotRepresentation) => ({
            key: slotRepresentation.key,
            order: slotRepresentation.order,
            criticality: slotRepresentation.criticality,
            positionKeys: slotRepresentation.positionKeys,
            positionName: slotRepresentation.name ?? undefined,
            assignedRegistrationKey: slotRepresentation.assignedRegistrationKey ?? undefined,
        }));
    }

    private static mapEventType(event: Event): EventType {
        const start = cropToPrecision(event.start, 'days');
        const end = cropToPrecision(event.end, 'days');
        const durationDays = new Date(event.end.getTime() - event.start.getTime()).getDate();

        if (event.name.includes('Arbeitsdienst')) {
            return EventType.WorkEvent;
        }
        if (isSameDate(start, end)) {
            return EventType.SingleDayEvent;
        }
        if (durationDays <= 3) {
            return EventType.WeekendEvent;
        }
        return EventType.MultiDayEvent;
    }

    public async findByKey(key: EventKey, accessKey?: string): Promise<Event> {
        let url = `/api/v1/events/${key}`;
        if (accessKey) {
            url = `${url}?accessKey=${accessKey}`;
        }
        const response = await fetch(url, {
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation = await response.clone().json();
        return EventRestRepository.mapEventToDomain(responseData);
    }

    public async findAll(year: number): Promise<Event[]> {
        const response = await fetch(`/api/v1/events?year=${year}`, {
            credentials: 'include',
            headers: {
                Accept: 'application/json',
            },
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: EventRepresentation[] = await response.clone().json();
        return responseData.map(EventRestRepository.mapEventToDomain);
    }

    public async export(year: number): Promise<Blob> {
        const response = await fetch(`/api/v1/events?year=${year}`, {
            credentials: 'include',
            headers: {
                Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            },
        });
        if (!response.ok) {
            throw response;
        }
        return await response.clone().blob();
    }

    public async createEvent(event: Event): Promise<Event> {
        const requestBody: EventCreateRequest = {
            state: event.state,
            name: event.name,
            description: event.description,
            start: event.start.toISOString(),
            end: event.end.toISOString(),
            locations: event.locations.map((location) => ({
                icon: location.icon,
                name: location.name,
                country: location.country,
                address: location.address,
                addressLink: location.addressLink,
                information: location.information,
                informationLink: location.informationLink,
                eta: location.eta?.toISOString(),
                etd: location.etd?.toISOString(),
            })),
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
                'Accept': 'application/json',
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

    public async updateEvent(
        eventKey: EventKey,
        updateRequest: Partial<Event>,
        registrationsToRemove?: Registration[],
        registrationsToAdd?: Registration[],
        registrationsToUpdate?: Registration[]
    ): Promise<Event> {
        const requestBody: EventUpdateRequest = {
            state: updateRequest.state,
            name: updateRequest.name,
            description: updateRequest.description,
            start: updateRequest.start?.toISOString(),
            end: updateRequest.end?.toISOString(),
            locations: updateRequest.locations?.map((location) => ({
                icon: location.icon,
                name: location.name,
                country: location.country,
                address: location.address,
                addressLink: location.addressLink,
                information: location.information,
                informationLink: location.informationLink,
                eta: location.eta?.toISOString(),
                etd: location.etd?.toISOString(),
            })),
            slots: updateRequest.slots?.map((it) => ({
                key: it.key,
                order: it.order,
                criticality: it.criticality,
                positionKeys: it.positionKeys,
                name: it.positionName,
                assignedRegistrationKey: it.assignedRegistrationKey,
            })),
            registrationsToRemove: registrationsToRemove?.map((it) => it.key),
            registrationsToAdd: registrationsToAdd?.map((it) => ({
                registrationKey: it.key,
                positionKey: it.positionKey,
                name: it.name,
                userKey: it.userKey,
                note: it.note,
            })),
            registrationsToUpdate: registrationsToUpdate?.map((it) => ({
                registrationKey: it.key,
                positionKey: it.positionKey,
                name: it.name,
                userKey: it.userKey,
                note: it.note,
            })),
        };
        const response = await fetch(`/api/v1/events/${eventKey}`, {
            method: 'PATCH',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Accept': 'application/json',
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

    public async deleteEvent(eventKey: EventKey): Promise<void> {
        const response = await fetch(`/api/v1/events/${eventKey}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
    }

    public async downloadImoList(event: Event): Promise<Blob> {
        const response = await fetch(`/api/v1/events/${event.key}/imo-list`, {
            method: 'GET',
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        return response.clone().blob();
    }

    public async downloadConsumptionList(event: Event): Promise<Blob> {
        const response = await fetch(`/api/v1/events/${event.key}/consumption-list`, {
            method: 'GET',
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        return response.clone().blob();
    }

    public async downloadCaptainList(event: Event): Promise<Blob> {
        const response = await fetch(`/api/v1/events/${event.key}/captain-list`, {
            method: 'GET',
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        return response.clone().blob();
    }

    public async optimizeSlots(event: Event): Promise<Slot[]> {
        const requestBody: OptimizeEventSlotsRequest = {
            slots: event.slots?.map((it) => ({
                key: it.key,
                order: it.order,
                criticality: it.criticality,
                positionKeys: it.positionKeys,
                name: it.positionName,
                assignedRegistrationKey: it.assignedRegistrationKey,
            })),
            registrations: event.registrations.map((it) => ({
                key: it.key,
                positionKey: it.positionKey,
                name: it.name,
                userKey: it.userKey,
                note: it.note,
                confirmed: it.confirmed,
            })),
        };
        const response = await fetch(`/api/v1/events/${event.key}/optimized-slots`, {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const responseData: SlotRepresentation[] = await response.clone().json();
        return EventRestRepository.mapSlotsToDomain(responseData);
    }
}
