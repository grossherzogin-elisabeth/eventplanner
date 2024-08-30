import type { AuthRepository } from '@/application';
import type { Config } from '@/application/values/Config';
import { Timer } from '@/common';
import type { SignedInUser } from '@/domain';
import { Permission, Role } from '@/domain';

interface AccountRepresentation {
    key: string;
    email: string;
    roles: string[];
    permissions: string[];
}

export class AuthRestRepository implements AuthRepository {
    private readonly config: Config;
    private user: AccountRepresentation | null = null;
    private loginListeners: (() => void)[] = [];
    private logoutListeners: (() => void)[] = [];

    constructor(config: Config) {
        this.config = config;
    }

    public async onLogin(): Promise<SignedInUser> {
        if (this.user !== null) {
            return this.mapUserToSignedInUser(this.user);
        }
        return new Promise((resolve, reject) => {
            this.loginListeners.push(() => {
                if (this.user != null) {
                    resolve(this.mapUserToSignedInUser(this.user));
                } else {
                    reject('User is still undefined');
                }
            });
        });
    }

    public async onLogout(): Promise<void> {
        if (!this.isLoggedIn()) {
            return Promise.resolve();
        }
        return new Promise((resolve) => {
            this.logoutListeners.push(() => resolve());
        });
    }

    public isLoggedIn(): boolean {
        return this.user !== null;
    }

    public async login(redirectPath?: string): Promise<string | undefined> {
        if (!this.isLoggedIn()) {
            try {
                const response = await fetch('/api/v1/account', { credentials: 'include' });
                if (!response.ok) {
                    await this.navigateToLogin(redirectPath);
                    return Promise.reject();
                }
                this.user = (await response.clone().json()) as AccountRepresentation;
                this.loginListeners.forEach((cb) => cb());
                const redirect = localStorage.getItem('auth.redirect') || undefined;
                localStorage.removeItem('auth.redirect');
                return redirect;
            } catch (e) {
                await this.navigateToLogin(redirectPath);
            }
        }
        return undefined;
    }

    public logout(): void {
        this.user = null;
        this.logoutListeners.forEach((cb) => cb());
        window.location.href = this.config.authLogoutEndpoint;
    }

    public getSignedInUser(): SignedInUser | undefined {
        if (this.user) {
            const user = this.mapUserToSignedInUser(this.user);
            if (user.roles.includes(Role.ADMIN)) {
                user.permissions.push(Permission.BETA_FEATURES);
            }
            return user;
        }
        return undefined;
    }

    private async navigateToLogin(redirectPath?: string): Promise<void> {
        if (!this.config.askBeforeLogin || confirm('Navigate to login?')) {
            localStorage.setItem('auth.redirect', redirectPath || window.location.pathname);
            window.location.href = this.config.authLoginEndpoint;
            await Timer.wait(500);
        }
    }

    private mapUserToSignedInUser(user: AccountRepresentation): SignedInUser {
        return {
            key: this.config.overrideSignedInUserKey || user.key,
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
