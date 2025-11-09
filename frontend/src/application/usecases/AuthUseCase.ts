import type { AccountRepository, AuthService, Config, UserRepository } from '@/application';
import type { SignedInUser, UserKey } from '@/domain';
import { Permission } from '@/domain';

export class AuthUseCase {
    private readonly config: Config;
    private readonly authService: AuthService;
    private readonly accountRepository: AccountRepository;
    private readonly userRepository: UserRepository;
    private authentication?: Promise<string | undefined>;

    constructor(params: {
        config: Config;
        authService: AuthService;
        accountRepository: AccountRepository;
        userRepository: UserRepository;
    }) {
        this.config = params.config;
        this.authService = params.authService;
        this.accountRepository = params.accountRepository;
        this.userRepository = params.userRepository;
    }

    public async authenticate(redirectPath?: string): Promise<string | undefined> {
        const user = await this.accountRepository.getAccount();
        if (user && user.permissions.includes(Permission.READ_USER_DETAILS) && this.config.overrideSignedInUserKey) {
            const impersonatedUser = await this.userRepository.findByKey(this.config.overrideSignedInUserKey);
            if (impersonatedUser) {
                this.authService.impersonate(impersonatedUser);
            }
        }
        this.authService.setSignedInUser(user);
        if (!user) {
            if (redirectPath) {
                localStorage.setItem('auth.redirect', redirectPath);
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

    public async login(): Promise<void> {
        await this.accountRepository.login();
    }

    /**
     * Starts the logout flow
     */
    public async logout(): Promise<void> {
        await this.accountRepository.logout();
    }

    public impersonateUser(userKey: UserKey | null): void {
        if (userKey) {
            localStorage.setItem('eventplanner.overrideSignedInUserKey', userKey);
        } else {
            localStorage.removeItem('eventplanner.overrideSignedInUserKey');
        }
        window.location.reload();
    }

    public isLoggedIn(): boolean | null {
        return this.authService.getSignedInUser() !== undefined;
    }

    public getSignedInUser(): SignedInUser {
        return (
            this.authService.getSignedInUser() || {
                key: '',
                gender: '',
                lastName: '',
                firstName: '',
                email: '',
                roles: [],
                permissions: [],
                impersonated: false,
                positions: [],
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
