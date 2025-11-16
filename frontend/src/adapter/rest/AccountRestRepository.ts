import type { AccountRepository } from '@/application';
import { wait } from '@/common';
import type { Permission, PositionKey, Role, SignedInUser } from '@/domain';

export interface AccountRepresentation {
    key: string;
    email: string;
    roles: string[];
    permissions: string[];
    positions: string[];
    gender: string;
    firstName: string;
    lastName: string;
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
            console.error(e);
            return undefined;
        }
    }

    public async login(redirectTo?: string): Promise<void> {
        if (redirectTo) {
            localStorage.setItem('auth.redirect', redirectTo);
        }
        window.location.href = `/auth/login/default`;
        await wait(500);
    }

    public async logout(): Promise<void> {
        window.location.href = `/auth/logout`;
        await wait(500);
    }

    private mapAccountToSignedInUser(user: AccountRepresentation): SignedInUser {
        return {
            key: user.key,
            gender: user.gender,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            roles: user.roles as Role[],
            permissions: user.permissions as Permission[],
            positions: user.positions as PositionKey[],
            impersonated: false,
        };
    }
}
