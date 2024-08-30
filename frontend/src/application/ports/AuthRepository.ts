import type { SignedInUser } from '@/domain';

export interface AuthRepository {
    onLogin(): Promise<SignedInUser>;

    onLogout(): Promise<void>;

    /**
     * Returns the current users logged in state. This function does not validate the token, but only returns the
     * current saved state. If you want to test if the user has a valid token, use the async 'validate' function.
     * @return true if logged in
     */
    isLoggedIn(): boolean;

    /**
     * Tests if the user is logged in, and if not redirects to login page.
     * @param redirectPath the page to go back to after login, if empty the current 'document.URL' will be used
     */
    login(redirectPath?: string): Promise<string | undefined>;

    /**
     * Logs the user out
     */
    logout(): void;

    getSignedInUser(): SignedInUser | undefined;
}
