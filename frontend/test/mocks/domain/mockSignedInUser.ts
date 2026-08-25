import type { SignedInUser } from '@/domain';
import { Permission, Role } from '@/domain';
import { DECKHAND } from '~/mocks/keys';

export function mockSignedInUser(overwrite?: Partial<SignedInUser>): SignedInUser {
    const user: SignedInUser = {
        key: 'mocked',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@email.com',
        roles: [Role.TEAM_MEMBER],
        permissions: [
            Permission.READ_EVENTS,
            Permission.LIST_USERS,
            Permission.UPDATE_OWN_REGISTRATIONS,
            Permission.READ_OWN_USER,
            Permission.LIST_QUALIFICATIONS,
            Permission.LIST_POSITIONS,
        ],
        positions: [DECKHAND],
        hasLimitedAccess: false,
        impersonated: false,
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}
