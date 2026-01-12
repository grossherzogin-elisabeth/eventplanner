import type { EventRepresentation } from '@/adapter/rest/EventRestRepository';
import { EventSignupType, EventState, EventType, SlotCriticality } from '@/domain';
import {
    CAPTAIN,
    DECKHAND,
    ENGINEER,
    MATE,
    REGISTRATION_CAPTAIN,
    REGISTRATION_DECKHAND,
    REGISTRATION_ENGINEER,
    REGISTRATION_GUEST,
    REGISTRATION_MATE,
    SLOT_CAPTAIN,
    SLOT_DECKHAND,
    SLOT_ENGINEER,
    SLOT_MATE,
    USER_CAPTAIN,
    USER_DECKHAND,
    USER_ENGINEER,
    USER_MATE,
} from '~/mocks';

export function mockEventRepresentation(overwrite?: Partial<EventRepresentation>): EventRepresentation {
    const year = new Date().getFullYear();
    const event: EventRepresentation = {
        key: 'example-event',
        type: EventType.WeekendEvent,
        name: 'Example Event',
        description: 'This is a mocked event',
        start: `${year}-07-10T09:00:00Z`,
        end: `${year}-07-12T17:00:00Z`,
        state: EventState.Planned,
        signupType: EventSignupType.Assignment,
        registrations: [
            {
                key: REGISTRATION_CAPTAIN,
                positionKey: CAPTAIN,
                userKey: USER_CAPTAIN,
                name: undefined,
            },
            {
                key: REGISTRATION_ENGINEER,
                positionKey: ENGINEER,
                userKey: USER_ENGINEER,
                name: undefined,
            },
            {
                key: REGISTRATION_MATE,
                positionKey: MATE,
                userKey: USER_MATE,
                name: undefined,
            },
            {
                key: REGISTRATION_DECKHAND,
                positionKey: DECKHAND,
                userKey: USER_DECKHAND,
                name: undefined,
            },
            {
                key: REGISTRATION_GUEST,
                positionKey: DECKHAND,
                userKey: undefined,
                name: 'Gustav Guest',
            },
        ],
        slots: [
            {
                key: SLOT_CAPTAIN,
                order: 1,
                positionKeys: [CAPTAIN],
                criticality: SlotCriticality.Required,
            },
            {
                key: SLOT_ENGINEER,
                order: 2,
                positionKeys: [ENGINEER],
                criticality: SlotCriticality.Required,
            },
            {
                key: SLOT_MATE,
                order: 3,
                positionKeys: [MATE],
                criticality: SlotCriticality.Important,
            },
            {
                key: SLOT_DECKHAND,
                order: 4,
                positionKeys: [DECKHAND],
                criticality: SlotCriticality.Optional,
            },
        ],
        locations: [
            { name: 'Elsfleth', country: 'DE', icon: '' },
            { name: 'North Sea', country: 'DE', icon: '' },
            { name: 'Elsfleth', country: 'DE', icon: '' },
        ],
    };
    return overwrite ? Object.assign(event, overwrite) : event;
}
