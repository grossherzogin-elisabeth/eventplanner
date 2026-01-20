import type { AccountRepository, UserRepository } from '@/application/ports';
import type { AuthService, ConfigService, NotificationService } from '@/application/services';
import type { SignedInUser, UserKey } from '@/domain';
import { Permission } from '@/domain';

export class AuthUseCase {
    private readonly configService: ConfigService;
    private readonly authService: AuthService;
    private readonly accountRepository: AccountRepository;
    private readonly userRepository: UserRepository;
    private readonly notificationService: NotificationService;
    private authorizedAt: Date | undefined;

    constructor(params: {
        configService: ConfigService;
        authService: AuthService;
        accountRepository: AccountRepository;
        userRepository: UserRepository;
        notificationService: NotificationService;
    }) {
        this.configService = params.configService;
        this.authService = params.authService;
        this.accountRepository = params.accountRepository;
        this.userRepository = params.userRepository;
        this.notificationService = params.notificationService;
    }

    public async authenticate(): Promise<SignedInUser> {
        let signedInUser = this.authService.getSignedInUser();
        if (signedInUser) {
            // do this only once, or depending on time since last auth?
            if (!this.authorizedAt) {
                this.authorizedAt = new Date();
                // lazy refresh asynchronously in background
                this.fetchSignedInUser();
            }
            return this.impersonate(signedInUser);
        }

        signedInUser = await this.fetchSignedInUser();
        if (!signedInUser) {
            throw new Error('Unable to authenticate user');
        }
        return this.impersonate(signedInUser);
    }

    private async impersonate(signedInUser: SignedInUser): Promise<SignedInUser> {
        const overrideSignedInUserKey = this.configService.getConfig().overrideSignedInUserKey;
        if (signedInUser && overrideSignedInUserKey && signedInUser.permissions.includes(Permission.READ_USER_DETAILS)) {
            const impersonatedUser = await this.userRepository.findByKey(overrideSignedInUserKey);
            if (impersonatedUser) {
                this.authService.impersonate(impersonatedUser);
            }
        }
        return signedInUser;
    }

    private async fetchSignedInUser(): Promise<SignedInUser | undefined> {
        try {
            const signedInUser: SignedInUser = await this.accountRepository.getAccount();
            this.authService.setSignedInUser(signedInUser);
            console.log(`👋 Hello ${signedInUser.firstName}`);
            return signedInUser;
        } catch (e: unknown) {
            const status = (e as { status?: number }).status;
            if (status === undefined || status === 502 || status === 503 || status === 500) {
                this.authService.setOffline();
                console.warn('App is running in offline mode');
                this.notificationService.warning('Du bis offline. Manche Funktionen sind nicht verfügbar.');
                // user is probably offline, or server is not available
                // continue with stored session when possible
                return undefined;
            } else {
                this.authService.setSignedInUser(undefined);
            }
            throw e;
        }
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
        globalThis.location.reload();
    }

    public getSignedInUser(): SignedInUser {
        const signedInUser = this.authService.getSignedInUser();
        if (!signedInUser) {
            throw new Error('authentication required');
        }
        return signedInUser;
    }

    public async onAuthenticationDone(): Promise<boolean> {
        const signedInUser = this.authService.getSignedInUser();
        if (signedInUser) {
            return true;
        }
        return new Promise((resolve) => {
            // only use this callback once to resolve the promise
            const removeListener = this.authService.onChange(() => {
                resolve(false);
                removeListener();
            });
        });
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
