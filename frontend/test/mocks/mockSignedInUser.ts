import type { SignedInUser } from '@/domain';
import { Permission, Role } from '@/domain';
import { DECKHAND } from './mockPosition';

export function mockSignedInUser(overwrite?: Partial<SignedInUser>): SignedInUser {
    const user: SignedInUser = {
        key: 'mocked',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@email.com',
        roles: [Role.TEAM_MEMBER],
        permissions: [
            Permission.READ_EVENTS,
            Permission.READ_USERS,
            Permission.WRITE_OWN_REGISTRATIONS,
            Permission.READ_OWN_USER,
            Permission.READ_QUALIFICATIONS,
            Permission.READ_POSITIONS,
        ],
        positions: [DECKHAND],
        impersonated: false,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}
