import type { AccountRepository, UserRepository } from '@/application/ports';
import type { AuthService, ConfigService } from '@/application/services';
import type { SignedInUser, UserKey } from '@/domain';
import { Permission } from '@/domain';

export class AuthUseCase {
    private readonly configService: ConfigService;
    private readonly authService: AuthService;
    private readonly accountRepository: AccountRepository;
    private readonly userRepository: UserRepository;

    constructor(params: {
        configService: ConfigService;
        authService: AuthService;
        accountRepository: AccountRepository;
        userRepository: UserRepository;
    }) {
        this.configService = params.configService;
        this.authService = params.authService;
        this.accountRepository = params.accountRepository;
        this.userRepository = params.userRepository;
    }

    public getPendingRedirect(): string | undefined {
        return localStorage.getItem('auth.redirect') || undefined;
    }

    public clearPendingRedirect(): void {
        localStorage.removeItem('auth.redirect');
    }

    public async authenticate(redirectPath?: string): Promise<SignedInUser | undefined> {
        const user = this.authService.getSignedInUser() || (await this.accountRepository.getAccount());
        this.authService.setSignedInUser(user);
        if (!user && redirectPath) {
            localStorage.setItem('auth.redirect', redirectPath);
        }

        const overrideSignedInUserKey = this.configService.getConfig().overrideSignedInUserKey;
        if (user && overrideSignedInUserKey && user.permissions.includes(Permission.READ_USER_DETAILS)) {
            const impersonatedUser = await this.userRepository.findByKey(overrideSignedInUserKey);
            if (impersonatedUser) {
                this.authService.impersonate(impersonatedUser);
            }
        }

        return user;
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
        const signedInUser = this.authService.getSignedInUser();
        if (signedInUser) {
            return signedInUser;
        }
        return new Promise((resolve) => {
            // only use this callback once to resolve the promise
            const removeListener = this.authService.onLogin((signedInUser) => {
                resolve(signedInUser);
                removeListener();
            });
        });
    }

    public async onLogout(): Promise<void> {
        const signedInUser = this.authService.getSignedInUser();
        if (!signedInUser) {
            return;
        }
        return new Promise((resolve) => {
            // only use this callback once to resolve the promise
            const removeListener = this.authService.onLogout(() => {
                resolve();
                removeListener();
            });
        });
    }
}
