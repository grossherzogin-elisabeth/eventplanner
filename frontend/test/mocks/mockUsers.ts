import type { User } from '@/domain';
import { Role } from '@/domain';
import { CAPTAIN, DECKHAND, ENGINEER, MATE } from '~/mocks/mockPosition';

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
        qualifications: [],
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
        qualifications: [],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}

export function mockUserMate(overwrite?: Partial<User>): User {
    const user: User = {
        key: USER_ENGINEER,
        firstName: 'Max',
        lastName: 'Mate',
        email: 'max.mate@example.com',
        positionKeys: [MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [],
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
        qualifications: [],
        verified: true,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}
