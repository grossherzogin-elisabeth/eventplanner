import type { User } from '@/domain';
import { Role } from '@/domain';
import { CAPTAIN, DECKHAND, ENGINEER, MATE } from './mockPosition';
import {
    QUALIFICATION_CAPTAIN,
    QUALIFICATION_DECKHAND,
    QUALIFICATION_ENGINEER,
    QUALIFICATION_EXPIRES,
    QUALIFICATION_GENERIC,
    QUALIFICATION_MATE,
} from './mockQualification.ts';

export const USER_CAPTAIN = 'user-captain-key';
export const USER_ENGINEER = 'user-engineer-key';
export const USER_MATE = 'user-mate-key';
export const USER_DECKHAND = 'user-deckhand-key';

export function mockUserCaptain(overwrite?: Partial<User>): User {
    const user: User = {
        key: USER_CAPTAIN,
        firstName: 'Charlie',
        lastName: 'Captain',
        email: 'charlie.captain@example.com',
        positionKeys: [CAPTAIN, MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_CAPTAIN, expires: true, expiresAt: new Date('2024-07-10T09:00:00Z') },
            { qualificationKey: QUALIFICATION_MATE, expires: false },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserEngineer(overwrite?: Partial<User>): User {
    const user: User = {
        key: USER_ENGINEER,
        firstName: 'Alice',
        lastName: 'Engine',
        email: 'alice.engine@example.com',
        positionKeys: [ENGINEER],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_ENGINEER, expires: false },
            { qualificationKey: QUALIFICATION_GENERIC, expires: false },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserMate(overwrite?: Partial<User>): User {
    const user: User = {
        key: USER_MATE,
        firstName: 'Max',
        lastName: 'Mate',
        email: 'max.mate@example.com',
        positionKeys: [MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_MATE, expires: false },
            { qualificationKey: QUALIFICATION_EXPIRES, expires: true, expiresAt: new Date('1990-07-10T09:00:00Z') },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserDeckhand(overwrite?: Partial<User>): User {
    const user: User = {
        key: USER_ENGINEER,
        firstName: 'Dean',
        lastName: 'Deck',
        email: 'dean.deck@example.com',
        positionKeys: [DECKHAND],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_DECKHAND, expires: false },
            { qualificationKey: QUALIFICATION_EXPIRES, expires: true },
        ],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUsers(): User[] {
    return [mockUserCaptain(), mockUserEngineer(), mockUserMate(), mockUserDeckhand()];
}
