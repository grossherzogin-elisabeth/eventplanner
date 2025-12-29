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
        roles: [Role.ADMIN],
        permissions: Object.values(Permission),
        positions: [DECKHAND],
    };
    return overwrite ? Object.assign(account, overwrite) : account;
}
