import type { AuthService, Config } from '@/application';
import type { AccountRepository } from '@/application/ports/AccountRepository';
import { Timer } from '@/common';
import type { SignedInUser, UserKey } from '@/domain';

export class AuthUseCase {
    private readonly config: Config;
    private readonly authService: AuthService;
    private readonly accountRepository: AccountRepository;
    private authentication?: Promise<string | undefined>;

    constructor(params: { config: Config; authService: AuthService; accountRepository: AccountRepository }) {
        this.config = params.config;
        this.authService = params.authService;
        this.accountRepository = params.accountRepository;
    }

    public async authenticate(redirectPath?: string): Promise<string | undefined> {
        const user = await this.accountRepository.getAccount();
        this.authService.setSignedInUser(user);
        if (!user) {
            if (redirectPath) {
                localStorage.setItem('auth.redirect', redirectPath || window.location.pathname);
            }
            return undefined;
        }
        const redirect = localStorage.getItem('auth.redirect') || undefined;
        localStorage.removeItem('auth.redirect');
        return redirect;
    }

    public async firstAuthentication(redirectPath?: string): Promise<string | undefined> {
        if (!this.authentication) {
            this.authentication = this.authenticate(redirectPath);
            return this.authentication;
        }
        return undefined;
    }

    /**
     * Starts the login flow
     */
    public async loginWithCredentials(userName: string, password: string): Promise<void> {
        window.location.href = `${this.config.authLoginEndpoint}/default`;
        await Timer.wait(500);
    }

    /**
     * Starts the login flow
     */
    public async loginWithIdentityProvider(provider: string): Promise<void> {
        if (provider != 'google') {
            alert(`Der Login mit ${provider} ist noch nicht implementiert.`);
            return;
        }
        window.location.href = `${this.config.authLoginEndpoint}/${provider}`;
        await Timer.wait(500);
    }

    /**
     * Starts the logout flow
     */
    public logout(): void {
        window.location.href = this.config.authLogoutEndpoint;
    }

    public impersonateUser(userKey: UserKey): void {
        // TODO
    }

    public isLoggedIn(): boolean | null {
        return this.authService.getSignedInUser() !== undefined;
    }

    public getSignedInUser(): SignedInUser {
        return (
            this.authService.getSignedInUser() || {
                key: '',
                gender: 'd',
                username: 'anonymous',
                lastname: '',
                firstname: '',
                email: '',
                phone: '',
                roles: [],
                permissions: [],
            }
        );
    }

    public async onLogin(): Promise<SignedInUser> {
        return this.authService.onLogin();
    }

    public async onLogout(): Promise<void> {
        return this.authService.onLogout();
    }
}
