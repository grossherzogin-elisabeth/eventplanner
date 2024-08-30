import type { AuthRepository } from '@/application/ports/AuthRepository';
import type { SignedInUser } from '@/domain';

export class AuthUseCase {
    private readonly authRepository: AuthRepository;

    constructor(params: { authRepository: AuthRepository }) {
        this.authRepository = params.authRepository;
    }

    /**
     * Triggers the login flow
     */
    public async login(redirectPath?: string): Promise<string | undefined> {
        return this.authRepository.login(redirectPath);
    }

    /**
     * Logs the user out
     */
    public logout(): void {
        this.authRepository.logout();
    }

    /**
     * Resolves a promise when the user gets logged in. The promise resolves instantly, if the user is
     * currently logged in.
     */
    public onLogin(): Promise<SignedInUser> {
        return this.authRepository.onLogin();
    }

    /**
     * Resolves a promise when the user gets logged out. The promise resolves instantly, if the user is
     * currently logged out.
     */
    public onLogout(): Promise<void> {
        return this.authRepository.onLogout();
    }

    public getSignedInUser(): SignedInUser {
        return (
            this.authRepository.getSignedInUser() || {
                key: '',
                gender: 'd',
                username: '',
                lastname: '',
                firstname: '',
                email: '',
                phone: '',
                roles: [],
                permissions: [],
            }
        );
    }
}
