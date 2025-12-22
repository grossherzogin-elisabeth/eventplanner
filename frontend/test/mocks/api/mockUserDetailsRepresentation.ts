import type { UserDetailsRepresentation } from '@/adapter/rest/UserRestRepository';
import { Role } from '@/domain';
import {
    CAPTAIN,
    DECKHAND,
    ENGINEER,
    MATE,
    QUALIFICATION_CAPTAIN,
    QUALIFICATION_DECKHAND,
    QUALIFICATION_ENGINEER,
    QUALIFICATION_EXPIRES,
    QUALIFICATION_GENERIC,
    QUALIFICATION_MATE,
    USER_CAPTAIN,
    USER_DECKHAND,
    USER_ENGINEER,
    USER_MATE,
} from '~/mocks/keys';

export function mockUserDetailsRepresentationSignedInUser(overwrite?: Partial<UserDetailsRepresentation>): UserDetailsRepresentation {
    const user: UserDetailsRepresentation = {
        key: 'mocked',
        authKey: 'mocked',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@email.com',
        gender: 'd',
        positions: [DECKHAND],
        roles: [Role.ADMIN],
        qualifications: [
            { qualificationKey: QUALIFICATION_CAPTAIN, expires: true, expiresAt: '2024-07-10T09:00:00Z' },
            { qualificationKey: QUALIFICATION_MATE, expires: false },
        ],
        createdAt: '2025-03-15T10:53:43.170969Z',
        updatedAt: '2025-09-10T08:14:45.257150Z',
        verifiedAt: '2025-06-19T07:48:00Z',
        lastLoginAt: '2025-03-16T10:05:42.768603Z',
        address: {
            addressLine1: 'Teststreet 12',
            addressLine2: '',
            town: 'Testtown',
            zipCode: '12345',
            country: 'DE',
        },
        emergencyContact: {
            name: '',
            phone: '',
        },
        dateOfBirth: '1999-08-12',
        placeOfBirth: 'Testtown',
        passNr: 'L01X00T47',
        nationality: 'DE',
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserDetailsRepresentationCaptain(overwrite?: Partial<UserDetailsRepresentation>): UserDetailsRepresentation {
    const user: UserDetailsRepresentation = {
        key: USER_CAPTAIN,
        authKey: USER_CAPTAIN,
        firstName: 'Charlie',
        lastName: 'Captain',
        email: 'charlie.captain@example.com',
        positions: [CAPTAIN, MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_CAPTAIN, expires: true, expiresAt: '2024-07-10T09:00:00Z' },
            { qualificationKey: QUALIFICATION_MATE, expires: false },
        ],
        createdAt: '2025-03-15T10:53:43.170969Z',
        updatedAt: '2025-09-10T08:14:45.257150Z',
        verifiedAt: '2025-06-19T07:48:00Z',
        lastLoginAt: '2025-03-16T10:05:42.768603Z',
        address: {
            addressLine1: 'Teststreet 12',
            addressLine2: '',
            town: 'Testtown',
            zipCode: '12345',
            country: 'DE',
        },
        emergencyContact: {
            name: '',
            phone: '',
        },
        dateOfBirth: '1999-08-12',
        placeOfBirth: 'Testtown',
        passNr: 'L01X00T47',
        nationality: 'DE',
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserDetailsRepresentationEngineer(overwrite?: Partial<UserDetailsRepresentation>): UserDetailsRepresentation {
    const user: UserDetailsRepresentation = {
        key: USER_ENGINEER,
        authKey: USER_ENGINEER,
        firstName: 'Alice',
        lastName: 'Engine',
        email: 'alice.engine@example.com',
        positions: [ENGINEER],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_ENGINEER, expires: false },
            { qualificationKey: QUALIFICATION_GENERIC, expires: false },
        ],
        createdAt: '2025-03-15T10:53:43.170969Z',
        updatedAt: '2025-09-10T08:14:45.257150Z',
        verifiedAt: '2025-06-19T07:48:00Z',
        lastLoginAt: '2025-03-16T10:05:42.768603Z',
        address: {
            addressLine1: 'Teststreet 12',
            addressLine2: '',
            town: 'Testtown',
            zipCode: '12345',
            country: 'DE',
        },
        emergencyContact: {
            name: '',
            phone: '',
        },
        dateOfBirth: '1999-08-12',
        placeOfBirth: 'Testtown',
        passNr: 'L01X00T47',
        nationality: 'DE',
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserDetailsRepresentationMate(overwrite?: Partial<UserDetailsRepresentation>): UserDetailsRepresentation {
    const user: UserDetailsRepresentation = {
        key: USER_MATE,
        authKey: USER_MATE,
        firstName: 'Max',
        lastName: 'Mate',
        email: 'max.mate@example.com',
        positions: [MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_MATE, expires: false },
            { qualificationKey: QUALIFICATION_EXPIRES, expires: true, expiresAt: '1990-07-10T09:00:00Z' },
        ],
        createdAt: '2025-03-15T10:53:43.170969Z',
        updatedAt: '2025-09-10T08:14:45.257150Z',
        verifiedAt: '2025-06-19T07:48:00Z',
        lastLoginAt: '2025-03-16T10:05:42.768603Z',
        address: {
            addressLine1: 'Teststreet 12',
            addressLine2: '',
            town: 'Testtown',
            zipCode: '12345',
            country: 'DE',
        },
        emergencyContact: {
            name: '',
            phone: '',
        },
        dateOfBirth: '1999-08-12',
        placeOfBirth: 'Testtown',
        passNr: 'L01X00T47',
        nationality: 'DE',
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserDetailsRepresentationDeckhand(overwrite?: Partial<UserDetailsRepresentation>): UserDetailsRepresentation {
    const user: UserDetailsRepresentation = {
        key: USER_DECKHAND,
        authKey: USER_DECKHAND,
        firstName: 'Dean',
        lastName: 'Deck',
        email: 'dean.deck@example.com',
        positions: [DECKHAND],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_DECKHAND, expires: false },
            { qualificationKey: QUALIFICATION_EXPIRES, expires: true },
        ],
        createdAt: '2025-03-15T10:53:43.170969Z',
        updatedAt: '2025-09-10T08:14:45.257150Z',
        verifiedAt: '2025-06-19T07:48:00Z',
        lastLoginAt: '2025-03-16T10:05:42.768603Z',
        address: {
            addressLine1: 'Teststreet 12',
            addressLine2: '',
            town: 'Testtown',
            zipCode: '12345',
            country: 'DE',
        },
        emergencyContact: {
            name: '',
            phone: '',
        },
        dateOfBirth: '1999-08-12',
        placeOfBirth: 'Testtown',
        passNr: 'L01X00T47',
        nationality: 'DE',
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserDetailsRepresentations(): UserDetailsRepresentation[] {
    return [
        mockUserDetailsRepresentationSignedInUser(),
        mockUserDetailsRepresentationCaptain(),
        mockUserDetailsRepresentationEngineer(),
        mockUserDetailsRepresentationMate(),
        mockUserDetailsRepresentationDeckhand(),
    ];
}
