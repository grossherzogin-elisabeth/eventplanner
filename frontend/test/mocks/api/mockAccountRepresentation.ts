import type { AccountRepresentation } from '@/adapter/rest/AccountRestRepository';
import { Permission, Role } from '@/domain';
import { DECKHAND } from '~/mocks/keys';

export function mockAccountRepresentation(overwrite?: Partial<AccountRepresentation>): AccountRepresentation {
    const account: AccountRepresentation = {
        key: 'mocked',
        firstName: 'John',
        lastName: 'Doe',
        gender: 'd',
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
    };
    return overwrite ? Object.assign(account, overwrite) : account;
}
