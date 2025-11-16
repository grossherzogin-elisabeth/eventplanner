import type { UserRepresentation } from '@/adapter/rest/UserRestRepository';
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

export function mockUserRepresentationCaptain(overwrite?: Partial<UserRepresentation>): UserRepresentation {
    const user: UserRepresentation = {
        key: USER_CAPTAIN,
        firstName: 'Charlie',
        lastName: 'Captain',
        email: 'charlie.captain@example.com',
        positions: [CAPTAIN, MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_CAPTAIN, expires: true, expiresAt: '2024-07-10T09:00:00Z' },
            { qualificationKey: QUALIFICATION_MATE, expires: false },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserRepresentationEngineer(overwrite?: Partial<UserRepresentation>): UserRepresentation {
    const user: UserRepresentation = {
        key: USER_ENGINEER,
        firstName: 'Alice',
        lastName: 'Engine',
        email: 'alice.engine@example.com',
        positions: [ENGINEER],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_ENGINEER, expires: false },
            { qualificationKey: QUALIFICATION_GENERIC, expires: false },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserRepresentationMate(overwrite?: Partial<UserRepresentation>): UserRepresentation {
    const user: UserRepresentation = {
        key: USER_MATE,
        firstName: 'Max',
        lastName: 'Mate',
        email: 'max.mate@example.com',
        positions: [MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_MATE, expires: false },
            { qualificationKey: QUALIFICATION_EXPIRES, expires: true, expiresAt: '1990-07-10T09:00:00Z' },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserRepresentationDeckhand(overwrite?: Partial<UserRepresentation>): UserRepresentation {
    const user: UserRepresentation = {
        key: USER_DECKHAND,
        firstName: 'Dean',
        lastName: 'Deck',
        email: 'dean.deck@example.com',
        positions: [DECKHAND],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_DECKHAND, expires: false },
            { qualificationKey: QUALIFICATION_EXPIRES, expires: true },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserRepresentations(): UserRepresentation[] {
    return [
        mockUserRepresentationCaptain(),
        mockUserRepresentationEngineer(),
        mockUserRepresentationMate(),
        mockUserRepresentationDeckhand(),
    ];
}
