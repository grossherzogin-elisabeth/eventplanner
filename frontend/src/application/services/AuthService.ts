import type { Config } from '@/application';
import type { SignedInUser, UserDetails } from '@/domain';
import { Permission, Role } from '@/domain';

export class AuthService {
    private readonly config: Config;
    private loginListeners: (() => void)[] = [];
    private logoutListeners: (() => void)[] = [];
    private signedInUser: SignedInUser | undefined = undefined;
    private impersonating: UserDetails | null = null;

    constructor(params: { config: Config }) {
        this.config = params.config;
    }

    public getSignedInUser(): SignedInUser | undefined {
        if (this.signedInUser && this.impersonating) {
            return {
                ...this.signedInUser,
                key: this.impersonating.key,
                email: this.impersonating.email,
                firstname: this.impersonating.firstName,
                lastname: this.impersonating.lastName,
                impersonated: true,
            };
        }
        return this.signedInUser;
    }

    public setSignedInUser(signedInUser?: SignedInUser): void {
        if (signedInUser) {
            if (signedInUser.roles.includes(Role.ADMIN)) {
                signedInUser.permissions.push(Permission.BETA_FEATURES);
            }
            this.signedInUser = signedInUser;
            this.loginListeners.forEach((cb) => cb());
        } else {
            this.signedInUser = undefined;
            this.logoutListeners.forEach((cb) => cb());
        }
    }

    public async onLogin(): Promise<SignedInUser> {
        if (this.signedInUser !== undefined) {
            return this.signedInUser;
        }
        return new Promise((resolve) => {
            this.loginListeners.push(() => {
                if (this.signedInUser != undefined) {
                    resolve(this.signedInUser);
                }
            });
        });
    }

    public async onLogout(): Promise<void> {
        if (!this.signedInUser) {
            return Promise.resolve();
        }
        return new Promise((resolve) => {
            this.logoutListeners.push(() => resolve());
        });
    }

    public impersonate(user: UserDetails | null): void {
        this.impersonating = user;
    }
}
