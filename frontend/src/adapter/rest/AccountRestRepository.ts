import { getAccessKeyHeader } from '@/adapter/rest/util/getAccessKeyHeader';
import type { AccountRepository } from '@/application';
import { wait } from '@/common';
import type { Permission, Role, SignedInUser } from '@/domain';

export interface AccountRepresentation {
    key: string;
    email: string;
    emailHash?: string;
    roles: string[];
    permissions: string[];
    positions: string[];
    gender: string;
    firstName: string;
    lastName: string;
    hasLimitedAccess: boolean;
}

export class AccountRestRepository implements AccountRepository {
    public async getAccount(): Promise<SignedInUser> {
        const response = await fetch('/api/v1/account', {
            credentials: 'include',
            headers: getAccessKeyHeader(),
        });
        if (!response.ok) {
            throw response;
        }
        const account = (await response.clone().json()) as AccountRepresentation;
        return this.mapAccountToSignedInUser(account);
    }

    public async login(redirectTo?: string): Promise<void> {
        if (redirectTo) {
            localStorage.setItem('auth.redirect', redirectTo);
        }
        globalThis.location.href = `/auth/login/default`;
        await wait(500);
    }

    public async logout(): Promise<void> {
        globalThis.location.href = `/auth/logout`;
        await wait(500);
    }

    private mapAccountToSignedInUser(user: AccountRepresentation): SignedInUser {
        return {
            key: user.key,
            gender: user.gender,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            emailHash: user.emailHash,
            roles: user.roles as Role[],
            permissions: user.permissions as Permission[],
            positions: user.positions,
            hasLimitedAccess: user.hasLimitedAccess,
            impersonated: false,
        };
    }
}
