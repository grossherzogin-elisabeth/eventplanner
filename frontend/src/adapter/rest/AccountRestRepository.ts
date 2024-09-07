import type { AccountRepository } from '@/application';
import type { SignedInUser } from '@/domain';
import type { Permission, Role } from '@/domain';

interface AccountRepresentation {
    key: string;
    email: string;
    roles: string[];
    permissions: string[];
}

export class AccountRestRepository implements AccountRepository {
    public async getAccount(): Promise<SignedInUser | undefined> {
        try {
            const response = await fetch('/api/v1/account', { credentials: 'include' });
            if (!response.ok) {
                return undefined;
            }
            const account = (await response.clone().json()) as AccountRepresentation;
            return this.mapAccountToSignedInUser(account);
        } catch (e) {
            return undefined;
        }
    }

    private mapAccountToSignedInUser(user: AccountRepresentation): SignedInUser {
        return {
            key: user.key,
            username: '?',
            firstname: '?',
            lastname: '?',
            email: user.email,
            phone: '?',
            gender: 'd',
            roles: user.roles as Role[],
            permissions: user.permissions as Permission[],
        };
    }
}
