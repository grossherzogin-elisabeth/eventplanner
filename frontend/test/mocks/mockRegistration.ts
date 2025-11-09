import type { Registration } from '@/domain';
import { CAPTAIN, DECKHAND, ENGINEER, MATE } from '~/mocks/mockPosition';
import { USER_CAPTAIN, USER_DECKHAND, USER_ENGINEER, USER_MATE } from '~/mocks/mockUsers';

export const REGISTRATION_CAPTAIN = 'reg-captain-key';
export const REGISTRATION_ENGINEER = 'reg-engineer-key';
export const REGISTRATION_MATE = 'reg-mate-key';
export const REGISTRATION_DECKHAND = 'reg-deckhand-key';
export const REGISTRATION_GUEST = 'reg-guest-key';

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
