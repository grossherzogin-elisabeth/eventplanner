import type { SignedInUser } from '@/domain';

export interface AuthRepository {
    loginWithRedirect(clientName: string): Promise<string | undefined>;
    loginWithCredentials(userName: string, password: string): Promise<string | undefined>;
    logout(): void;
    getSignedInUser(): Promise<SignedInUser | undefined>;
}
