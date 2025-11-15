import type { Registration } from '@/domain';
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
    USER_CAPTAIN,
    USER_DECKHAND,
    USER_ENGINEER,
    USER_MATE,
} from '~/mocks/keys';

export function mockRegistrationCaptain(overwrite?: Partial<Registration>): Registration {
    const registration: Registration = {
        key: REGISTRATION_CAPTAIN,
        positionKey: CAPTAIN,
        userKey: USER_CAPTAIN,
        name: undefined,
    };
    return overwrite ? Object.assign(registration, overwrite) : registration;
}

export function mockRegistrationEngineer(overwrite?: Partial<Registration>): Registration {
    const registration: Registration = {
        key: REGISTRATION_ENGINEER,
        positionKey: ENGINEER,
        userKey: USER_ENGINEER,
        name: undefined,
    };
    return overwrite ? Object.assign(registration, overwrite) : registration;
}

export function mockRegistrationMate(overwrite?: Partial<Registration>): Registration {
    const registration: Registration = {
        key: REGISTRATION_MATE,
        positionKey: MATE,
        userKey: USER_MATE,
        name: undefined,
    };
    return overwrite ? Object.assign(registration, overwrite) : registration;
}

export function mockRegistrationDeckhand(overwrite?: Partial<Registration>): Registration {
    const registration: Registration = {
        key: REGISTRATION_DECKHAND,
        positionKey: DECKHAND,
        userKey: USER_DECKHAND,
        name: undefined,
    };
    return overwrite ? Object.assign(registration, overwrite) : registration;
}

export function mockRegistrationGuest(overwrite?: Partial<Registration>): Registration {
    const registration: Registration = {
        key: REGISTRATION_GUEST,
        positionKey: DECKHAND,
        userKey: undefined,
        name: 'Gustav Guest',
    };
    return overwrite ? Object.assign(registration, overwrite) : registration;
}

export function mockRegistrations(): Registration[] {
    return [
        mockRegistrationCaptain(),
        mockRegistrationEngineer(),
        mockRegistrationMate(),
        mockRegistrationDeckhand(),
        mockRegistrationGuest(),
    ];
}
